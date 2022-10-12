package com.calamus.easykorean;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class StartCourseActivity extends AppCompatActivity {
    Button bt_start;
    TextView tv_name;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_course);
        String name=getIntent().getExtras().getString("name");

        bt_start=findViewById(R.id.bt_start);
        tv_name=findViewById(R.id.tv_course_title);
        tv_name.setText("\""+name+"\"");

        bt_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(StartCourseActivity.this,MainActivity.class);
                intent.putExtra("message","login");
                startActivity(intent);
                finish();
            }
        });
    }
}