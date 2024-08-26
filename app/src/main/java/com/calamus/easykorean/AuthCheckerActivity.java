package com.calamus.easykorean;

import androidx.appcompat.app.AppCompatActivity;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;


import com.calamus.easykorean.app.MyHttp;
import com.calamus.easykorean.app.Routing;

import org.json.JSONObject;

import java.util.Objects;

public class AuthCheckerActivity extends AppCompatActivity {

    SharedPreferences sharedPreferences;
    String request_app,phone,auth_token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth_checker);
        sharedPreferences= getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        boolean autoLogin=sharedPreferences.getBoolean("AlreadyLogin",false);
        phone=sharedPreferences.getString("phone","");
        auth_token=sharedPreferences.getString("auth_token","");
        getSupportActionBar().hide();
        try {
            request_app= Objects.requireNonNull(getIntent().getExtras()).getString("request_app");
        }catch (NullPointerException e){
            request_app=null;
        }

        Intent intent=new Intent();
        if(autoLogin){
            if(request_app==null){
                String phone=sharedPreferences.getString("phone",null);
                intent.putExtra("success",true);
                intent.putExtra("userid",phone);
                setResult(Activity.RESULT_OK,intent);
                finish();
            }else{
                fetchToken();
            }

        }else{
            intent.putExtra("success",false);
            setResult(Activity.RESULT_OK,intent);
            finish();
        }
    }

    private void fetchToken(){
        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.GET, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    Intent intent=new Intent();
                    Log.e("token_response",response);
                    try {
                        JSONObject jo=new JSONObject(response);
                        boolean auth=jo.getBoolean("auth");
                        if(auth){
                            String new_token_for_requested_app=jo.getString("new_token_for_requested_app");
                            String phone=sharedPreferences.getString("phone",null);
                            intent.putExtra("success",true);
                            intent.putExtra("userid",phone);
                            intent.putExtra("auth_token",new_token_for_requested_app);

                        }else{
                            intent.putExtra("success",false);
                        }
                        setResult(Activity.RESULT_OK,intent);
                        finish();
                    }catch (Exception e){
                        Log.e("Token Request err",e.toString());
                        intent.putExtra("success",false);
                        setResult(Activity.RESULT_OK,intent);
                        finish();
                    }
                }
                @Override
                public void onError(String msg) {
                    Log.e("token_response_err",msg);
                    Intent intent=new Intent();
                    intent.putExtra("success",false);
                    setResult(Activity.RESULT_OK,intent);
                    finish();
                }
            }).url(Routing.REQUEST_TOKEN+"?request_app="+request_app+"&phone="+phone+"&host_token="+auth_token);
            myHttp.runTask();
        }).start();
    }

}