package com.lazysong.gojob;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.connect.UserInfo;
import com.tencent.connect.common.Constants;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import org.json.JSONException;
import org.json.JSONObject;

public class QQLoginActivity extends AppCompatActivity {
    private String appId;
    private Button btnQQLogin;
    private TextView mUserInfo;
    private ImageView mUserImg;
    private static Tencent mTencent;
    private static boolean isLogin = false;
    private UserInfo mInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qqlogin);

        Intent intent = getIntent();
        appId = intent.getStringExtra("appId");
//        Toast.makeText(this, "appId: " + appId, Toast.LENGTH_SHORT).show();
        if (mTencent == null)
            mTencent = Tencent.createInstance(appId, this);

        initViews();

        btnQQLogin = (Button) findViewById(R.id.btnQQLogin);
        btnQQLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QQLogin();
            }
        });

    }

    private void initViews() {

        mUserInfo = (TextView) findViewById(R.id.userInfo);
        mUserImg = (ImageView) findViewById(R.id.userImg);
    }

    private void QQLogin() {
        if (!mTencent.isSessionValid()) {
            mTencent.login(this, "all", loginListener);
            isLogin = true;
        } else {
            if (!isLogin) {
                mTencent.login(this, "all", loginListener);
                isLogin = true;
            }
            else {
                mTencent.logout(this);
                isLogin = false;
            }

            updateUserInfo();
            updateLoginButton();
        }
    }

    IUiListener loginListener = new BaseUiListener() {
        @Override
        protected void doComplete(JSONObject values) {
            initOpenidAndToken(values);
            updateUserInfo();
            updateLoginButton();
            Toast.makeText(QQLoginActivity.this, "accessId" + mTencent.getAccessToken(), Toast.LENGTH_SHORT).show();
        }
    };

    public static void initOpenidAndToken(JSONObject jsonObject) {
        try {
            String token = jsonObject.getString(Constants.PARAM_ACCESS_TOKEN);
            String expires = jsonObject.getString(Constants.PARAM_EXPIRES_IN);
            String openId = jsonObject.getString(Constants.PARAM_OPEN_ID);
            if (!TextUtils.isEmpty(token) && !TextUtils.isEmpty(expires)
                    && !TextUtils.isEmpty(openId)) {
                mTencent.setAccessToken(token, expires);
                mTencent.setOpenId(openId);
            }
        } catch(Exception e) {
        }
    }

    private void updateUserInfo() {
        if (mTencent != null && mTencent.isSessionValid()) {
            IUiListener listener = new IUiListener() {

                @Override
                public void onError(UiError e) {

                }

                @Override
                public void onComplete(final Object response) {
                    Message msg = new Message();
                    msg.obj = response;
                    msg.what = 0;
                    mHandler.sendMessage(msg);
                    new Thread(){

                        @Override
                        public void run() {
                            JSONObject json = (JSONObject)response;
                            if(json.has("figureurl")){
                                Bitmap bitmap = null;
                                try {
                                    bitmap = Util.getbitmap(json.getString("figureurl_qq_2"));
                                } catch (JSONException e) {

                                }
                                Message msg = new Message();
                                msg.obj = bitmap;
                                msg.what = 1;
                                mHandler.sendMessage(msg);
                            }
                        }

                    }.start();
                }

                @Override
                public void onCancel() {

                }
            };
            mInfo = new UserInfo(this, mTencent.getQQToken());
            mInfo.getUserInfo(listener);

        } else {
            mUserInfo.setText("");
            mUserInfo.setVisibility(android.view.View.GONE);
            mUserImg.setVisibility(android.view.View.GONE);
        }
    }

    Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                JSONObject response = (JSONObject) msg.obj;
                if (response.has("nickname")) {
                    try {
                        mUserInfo.setVisibility(android.view.View.VISIBLE);
                        mUserInfo.setText(response.getString("nickname"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }else if(msg.what == 1){
                Bitmap bitmap = (Bitmap)msg.obj;
                mUserImg.setImageBitmap(bitmap);
                mUserImg.setVisibility(android.view.View.VISIBLE);
            }
        }

    };

    private void updateLoginButton() {
        if (mTencent != null && mTencent.isSessionValid()) {
            if (!isLogin) {
                btnQQLogin.setTextColor(Color.BLUE);
                btnQQLogin.setText("登录");
            } else {
                btnQQLogin.setTextColor(Color.RED);
                btnQQLogin.setText("退出帐号");
            }
        }
        else {
            btnQQLogin.setTextColor(Color.BLUE);
            btnQQLogin.setText("登录");
        }
    }

    private class BaseUiListener implements IUiListener {

        @Override
        public void onComplete(Object response) {
            if (null == response) {
                Util.showResultDialog(QQLoginActivity.this, "返回为空", "登录失败");
                return;
            }
            JSONObject jsonResponse = (JSONObject) response;
            if (null != jsonResponse && jsonResponse.length() == 0) {
                Util.showResultDialog(QQLoginActivity.this, "返回为空", "登录失败");
                return;
            }
            Util.showResultDialog(QQLoginActivity.this, response.toString(), "登录成功");
            /*// 有奖分享处理
            handlePrizeShare();*/
            doComplete((JSONObject)response);
        }

        protected void doComplete(JSONObject values) {

        }

        @Override
        public void onError(UiError e) {
            Util.toastMessage(QQLoginActivity.this, "onError: " + e.errorDetail);
            Util.dismissDialog();
        }

        @Override
        public void onCancel() {
            Util.toastMessage(QQLoginActivity.this, "onCancel: ");
            Util.dismissDialog();
            if (isLogin) {
                isLogin = false;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_LOGIN ||
                requestCode == Constants.REQUEST_APPBAR) {
            Tencent.onActivityResultData(requestCode,resultCode,data,loginListener);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}