package com.example.phakneath.jobber;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;

public class PolicyActivity extends AppCompatActivity implements View.OnClickListener {

    private WebView web;
    private ImageView backBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_policy);

        initView();
        web.loadUrl("file:///android_asset/privacypolicy.html");
        backBtn.setOnClickListener(this::onClick);
    }

    private void initView() {
        web = findViewById(R.id.web);
        backBtn = findViewById(R.id.back);
    }

    @Override
    public void onClick(View v) {
        if(v == backBtn)
        {
            finish();
        }
    }
}
