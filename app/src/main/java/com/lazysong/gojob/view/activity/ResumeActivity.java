package com.lazysong.gojob.view.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.lazysong.gojob.R;

public class ResumeActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView titleActionbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resume);

        toolbar = (Toolbar) findViewById(R.id.toolbar_base);
        toolbar.setNavigationIcon(R.mipmap.md_nav_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ResumeActivity.this.finish();
            }
        });
        titleActionbar = (TextView) findViewById(R.id.titleToolbar);
        titleActionbar.setText("个人简历");
    }
}
