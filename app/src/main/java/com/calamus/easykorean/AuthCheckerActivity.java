package com.calamus.easykorean;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

public class AuthCheckerActivity extends AppCompatActivity {

    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth_checker);
        sharedPreferences= getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        boolean autoLogin=sharedPreferences.getBoolean("AlreadyLogin",false);

        Intent intent=new Intent();
        if(autoLogin){
            String phone=sharedPreferences.getString("phone",null);
            intent.putExtra("success",true);
            intent.putExtra("userid",phone);

        }else{
            intent.putExtra("success",false);
        }
        setResult(Activity.RESULT_OK,intent);
        finish();
    }


}