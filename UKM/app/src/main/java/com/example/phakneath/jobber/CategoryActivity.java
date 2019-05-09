package com.example.phakneath.jobber;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class CategoryActivity extends AppCompatActivity {

    TextView tv_category;
    ImageView back;
    Intent intent;
    Bundle bundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        intent = getIntent();
        bundle = intent.getExtras();

        tv_category = findViewById(R.id.tv_category);
        tv_category.setText(bundle.getString("Category"));
        back = findViewById(R.id.cat_back_btn);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent back = new Intent(CategoryActivity.this, MainActivity.class);
                startActivity(back);
            }
        });

    }
}
