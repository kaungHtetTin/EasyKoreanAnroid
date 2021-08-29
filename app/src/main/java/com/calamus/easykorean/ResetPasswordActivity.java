package com.calamus.easykorean;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.calamus.easykorean.app.MyHttp;
import com.calamus.easykorean.app.Routing;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Objects;
import java.util.concurrent.Executor;

public class ResetPasswordActivity extends AppCompatActivity {

    TextView tv_title,tv_info,tv_error;
    EditText et_currentPassword, et_inputPassword;
    Button bt_confirm, bt_resetPassword;
    ProgressBar loading;
    ImageView iv_check;

    Animation animOut;
    Animation animIn;

    Executor postExecutor;
    String phone;
    String currentPassword;

    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        sharedPreferences=getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        phone=sharedPreferences.getString("phone","00");
        setTitle("Reset Password");
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        setUpView();
        animOut= AnimationUtils.loadAnimation(this, android.R.anim.fade_out);
        animIn= AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        postExecutor= ContextCompat.getMainExecutor(this);

    }


    private void setUpView(){
        tv_title=findViewById(R.id.tv_title);
        tv_info=findViewById(R.id.tv_info);
        tv_error=findViewById(R.id.tv_error);
        loading=findViewById(R.id.pb_loading);
        et_currentPassword=findViewById(R.id.et_currentPassword);
        et_inputPassword=findViewById(R.id.et_inputPassword);
        iv_check=findViewById(R.id.iv_check);
        bt_confirm=findViewById(R.id.bt_confirm);
        bt_resetPassword=findViewById(R.id.bt_reset);


        bt_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_error.setVisibility(View.GONE);
                tv_error.setAnimation(animOut);

                if(!TextUtils.isEmpty(et_currentPassword.getText().toString())){
                    bt_confirm.setEnabled(false);
                    currentPassword=et_currentPassword.getText().toString();
                    checkCurrentPassword(currentPassword);

                }else {
                    tv_error.setVisibility(View.VISIBLE);
                    tv_error.setText("Please enter your current password");
                    tv_error.setAnimation(animIn);
                }
            }
        });

        bt_resetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_error.setVisibility(View.GONE);
                tv_error.setAnimation(animOut);
                if(TextUtils.isEmpty(et_inputPassword.getText().toString())){
                    tv_error.setVisibility(View.VISIBLE);
                    tv_error.setText("Please enter your new Password!");
                    tv_error.setAnimation(animIn);
                }else if(et_inputPassword.getText().toString().length()<5){
                    tv_error.setVisibility(View.VISIBLE);
                    tv_error.setText("Your password must have at least 5 letters");
                    tv_error.setAnimation(animIn);
                }else{
                    bt_resetPassword.setEnabled(false);
                    resetPassword(et_inputPassword.getText().toString());
                }
            }
        });
    }

    private void resetPassword(String newPassword){
        loading.setVisibility(View.VISIBLE);
        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.GET, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    postExecutor.execute(() -> {
                        loading.setVisibility(View.INVISIBLE);
                        try {
                            JSONObject jo=new JSONObject(response);
                            String code=jo.getString("code");
                            String msg=jo.getString("msg");

                            if(code.equals("50")){
                                loading.setVisibility(View.INVISIBLE);
                                et_inputPassword.setVisibility(View.GONE);
                                bt_resetPassword.setVisibility(View.GONE);
                                tv_info.setText(msg);
                                iv_check.setVisibility(View.VISIBLE);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    });
                }
                @Override
                public void onError(String msg) {
                    postExecutor.execute(() -> {
                        loading.setVisibility(View.INVISIBLE);
                        bt_resetPassword.setEnabled(true);
                        tv_error.setVisibility(View.VISIBLE);
                        tv_error.setText("An unexpected error! Connect to help center");
                    });
                }
            }).url(Routing.RESET_PASSWORD_BY_USER+phone+"/"+currentPassword+"/"+newPassword);
            myHttp.runTask();
        }).start();
    }

    private void checkCurrentPassword(String pw){
        loading.setVisibility(View.VISIBLE);

        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.GET, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    postExecutor.execute(() -> {
                        loading.setVisibility(View.INVISIBLE);
                        try {
                            JSONObject jo=new JSONObject(response);
                            String code=jo.getString("code");
                            String msg=jo.getString("msg");
                            if(code.equals("50")){
                                et_currentPassword.setVisibility(View.GONE);
                                et_inputPassword.setVisibility(View.VISIBLE);
                                et_inputPassword.setAnimation(animIn);
                                bt_confirm.setVisibility(View.GONE);
                                bt_resetPassword.setVisibility(View.VISIBLE);
                                tv_info.setText(msg);
                                tv_title.setText("New Password");
                                tv_title.setAnimation(animIn);

                            }else{
                                tv_error.setVisibility(View.VISIBLE);
                                et_currentPassword.setText("");
                                tv_error.setText(msg);
                                tv_error.setAnimation(animIn);
                                bt_confirm.setEnabled(true);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    });
                }
                @Override
                public void onError(String msg) {
                    postExecutor.execute(() -> {
                        loading.setVisibility(View.INVISIBLE);
                        bt_confirm.setEnabled(true);
                        tv_error.setVisibility(View.VISIBLE);
                        tv_error.setText("An unexpected error! Connect to help center");

                    });
                }
            }).url(Routing.CHECK_CURRENT_PASSWORD+phone+"/"+pw);
            myHttp.runTask();
        }).start();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}