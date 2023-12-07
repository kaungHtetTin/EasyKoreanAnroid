package com.calamus.easykorean;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class BlockedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blocked);
        getSupportActionBar().hide();
        setUpCustomAppBar();

        String goSomeWhere= getIntent().getStringExtra("message");
        String payLoadStr=getIntent().getStringExtra("json");
        Toast.makeText(getApplicationContext(),payLoadStr+" "+goSomeWhere,Toast.LENGTH_SHORT).show();
    }

    private void setUpCustomAppBar(){

        TextView tv=findViewById(R.id.tv_appbar);
        ImageView iv=findViewById(R.id.iv_back);
        tv.setText("Error");
        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }
}