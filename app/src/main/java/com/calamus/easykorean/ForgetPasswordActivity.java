package com.calamus.easykorean;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.calamus.easykorean.app.MyHttp;
import com.calamus.easykorean.app.Routing;
import com.calamus.easykorean.app.UserInformation;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Objects;
import java.util.concurrent.Executor;

public class ForgetPasswordActivity extends AppCompatActivity {

    TextView tv_title,tv_info,tv_error;
    EditText et_phone,et_code,et_password;
    Button bt_search,bt_confirm,bt_reset,bt_login,bt_facebook,bt_sendCode;
    ProgressBar loading;
    String phone;
    String password;
    String code;
    Executor postExecutor;

    Animation animOut;
    Animation animIn;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);

        sharedPreferences=getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        editor=sharedPreferences.edit();

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        animOut= AnimationUtils.loadAnimation(this, android.R.anim.fade_out);
        animIn= AnimationUtils.loadAnimation(this, android.R.anim.fade_in);

        postExecutor= ContextCompat.getMainExecutor(this);
        setTitle("Forget Password");
        setUpView();

    }

    private void setUpView(){
        tv_title=findViewById(R.id.tv_title);
        tv_info=findViewById(R.id.tv_info);
        tv_error=findViewById(R.id.tv_error);
        et_phone=findViewById(R.id.et_inputPhone);
        et_code=findViewById(R.id.et_inputCode);
        et_password=findViewById(R.id.et_inputPassword);
        bt_search=findViewById(R.id.bt_search);
        bt_confirm=findViewById(R.id.bt_comfirm);
        bt_reset=findViewById(R.id.bt_reset);
        bt_login=findViewById(R.id.bt_logIn);
        loading=findViewById(R.id.pb_loading);
        bt_sendCode=findViewById(R.id.bt_sendCode);
        bt_facebook=findViewById(R.id.bt_facebook);

        tv_info.setText("Please enter your phone of the account that you want to reset password");

        bt_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_error.setVisibility(View.GONE);
                bt_facebook.setVisibility(View.GONE);
                tv_error.setAnimation(animOut);
                bt_facebook.setAnimation(animOut);

                if(!TextUtils.isEmpty(et_phone.getText().toString())){
                    bt_search.setEnabled(false);
                    phone=et_phone.getText().toString();
                    searchMyAccount(phone);

                }else {
                    tv_error.setVisibility(View.VISIBLE);
                    tv_error.setText("Please enter your phone number");
                    tv_error.setAnimation(animIn);
                }
            }
        });


        bt_sendCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_error.setVisibility(View.GONE);
                tv_error.setAnimation(animOut);
                bt_sendCode.setEnabled(false);
                tv_info.setText("Sending Code Again. Please Wait!");
                searchMyAccount(phone);

            }
        });

        bt_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_error.setVisibility(View.GONE);
                tv_error.setAnimation(animOut);
                loading.setVisibility(View.VISIBLE);

                if(!TextUtils.isEmpty(et_code.getText().toString())){
                    bt_confirm.setEnabled(false);
                    code=et_code.getText().toString();
                    confirmCode(code);
                }else{
                    tv_error.setVisibility(View.VISIBLE);
                    tv_error.setText("Please enter your code");
                    tv_error.setAnimation(animIn);
                    loading.setVisibility(View.INVISIBLE);
                    bt_confirm.setEnabled(true);
                }
            }
        });

        bt_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_error.setVisibility(View.GONE);
                tv_error.setAnimation(animOut);
                if(TextUtils.isEmpty(et_password.getText().toString())){
                    tv_error.setVisibility(View.VISIBLE);
                    tv_error.setText("Please enter your new Password!");
                    tv_error.setAnimation(animIn);
                }else if(et_password.getText().toString().length()<5){
                    tv_error.setVisibility(View.VISIBLE);
                    tv_error.setText("Your password must have at least 5 letters");
                    tv_error.setAnimation(animIn);
                }else{
                    bt_reset.setEnabled(false);
                    resetPassword(et_password.getText().toString());
                }
            }
        });

        bt_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logIn(phone,password);
                Log.e("Info: ","phone - "+phone +" pw - "+ password);
            }
        });

        bt_facebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(Intent.ACTION_VIEW, Uri.parse("https://m.me/easykoreancalamus"));
                startActivity(intent);
            }
        });
    }


    private void logIn(final String phone, String password) {

       bt_login.setEnabled(true);
        loading.setVisibility(View.VISIBLE);
        if (!TextUtils.isEmpty(phone) && !TextUtils.isEmpty(password)) {
            new Thread(() -> {
                MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.POST, new MyHttp.Response() {
                    @Override
                    public void onResponse(String response) {
                        postExecutor.execute(() -> doAsResult(response,phone));
                    }
                    @Override
                    public void onError(String msg) {
                        postExecutor.execute(() -> {
                            loading.setVisibility(View.INVISIBLE);
                            bt_login.setEnabled(true);
                            tv_error.setVisibility(View.VISIBLE);
                            tv_error.setText("An unexpected error! Please try again.");
                        });
                    }
                }).url(Routing.LOGIN).field("phone",phone).field("password",password);

                myHttp.runTask();
            }).start();
        }
    }

    private void doAsResult(String result,String phone){
        try {
            JSONObject jo=new JSONObject(result);
            String response=jo.getString("result");

            if(response.equals("go")){
                editor.putBoolean("AlreadyLogin", true);
                editor.putString("phone",phone);
                editor.apply();

                UserInformation userInformation=new UserInformation(ForgetPasswordActivity.this);
                userInformation.getGeneralData(phone);

                Intent intent=new Intent(ForgetPasswordActivity.this,MainActivity.class);
                intent.putExtra("message","login");

                startActivity(intent);
                finish();
            }else{
                loading.setVisibility(View.INVISIBLE);
                bt_login.setEnabled(true);
                tv_error.setVisibility(View.VISIBLE);
                tv_error.setText(response);
            }

        }catch (Exception ignored){}
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
                            password=jo.getString("password");
                            if(code.equals("50")){
                                loading.setVisibility(View.INVISIBLE);
                                et_password.setVisibility(View.GONE);
                                bt_reset.setVisibility(View.GONE);
                                tv_info.setText(msg);
                                tv_title.setText("Login");
                                tv_title.setAnimation(animIn);
                                bt_login.setVisibility(View.VISIBLE);
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
                        bt_reset.setEnabled(true);
                        tv_error.setVisibility(View.VISIBLE);
                        tv_error.setText("An unexpected error! Connect to help center");
                    });
                }
            }).url(Routing.RESET_PASSWORD_BY_CODE+phone+"/"+code+"/"+newPassword);
            myHttp.runTask();
        }).start();
    }

    private void confirmCode(String code){
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

                                et_code.setVisibility(View.GONE);
                                et_password.setVisibility(View.VISIBLE);
                                et_password.setAnimation(animIn);
                                bt_confirm.setVisibility(View.GONE);
                                bt_sendCode.setVisibility(View.GONE);
                                tv_info.setText(msg);
                                tv_title.setText("New Password");
                                tv_title.setAnimation(animIn);
                                bt_reset.setVisibility(View.VISIBLE);

                            }else {
                                tv_error.setVisibility(View.VISIBLE);
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
            }).url(Routing.VERIFY_CODE+phone+"/"+code);
            myHttp.runTask();
        }).start();
    }


    private void searchMyAccount(String phone){
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
                                et_phone.setVisibility(View.GONE);
                                et_code.setVisibility(View.VISIBLE);
                                et_code.setAnimation(animIn);
                                bt_search.setVisibility(View.GONE);
                                bt_confirm.setVisibility(View.VISIBLE);
                                tv_info.setText(msg);
                                tv_title.setText("Email Verification");
                                tv_title.setAnimation(animIn);
                                bt_sendCode.setVisibility(View.VISIBLE);
                                bt_sendCode.setEnabled(true);
                            }else if(code.equals("51")){
                                tv_error.setVisibility(View.VISIBLE);
                                tv_error.setText(msg);
                                tv_error.setAnimation(animIn);
                                bt_search.setEnabled(true);
                            }else {
                                tv_error.setVisibility(View.VISIBLE);
                                tv_error.setText(msg);
                                tv_error.setAnimation(animIn);
                                bt_search.setEnabled(true);
                                bt_facebook.setVisibility(View.VISIBLE);
                                bt_facebook.setAnimation(animIn);
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
                        bt_sendCode.setEnabled(true);
                        tv_error.setVisibility(View.VISIBLE);
                        tv_error.setText("An unexpected error! Connect to help center");
                    });
                }
            }).url(Routing.SEARCH_MY_ACCOUNT+phone+"/"+Routing.APP_NAME);
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