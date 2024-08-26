package com.calamus.easykorean;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.calamus.easykorean.app.Routing;
import com.calamus.easykorean.app.UserInformation;
import com.calamus.easykorean.app.MyHttp;
import org.json.JSONObject;
import java.util.concurrent.Executor;

public class SignUpActivity extends AppCompatActivity {

    //Data Type Variable
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    int infoCount=0;
    String name,phone,password,confirmPassword;
    Animation animOut;
    Animation animIn;

    //View Type Variable
    Button bt;
    EditText et_info;
    TextView tv_1,tv_2,tv_3,tv_4;
    ProgressBar loading;
    TextView tv_error,tv_login,tv_info,tv1;
    ImageView iv_back,iv_ready;

    Executor postExecutor;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        sharedPreferences=getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        editor=sharedPreferences.edit();
        animOut= AnimationUtils.loadAnimation(this, android.R.anim.fade_out);
        animIn= AnimationUtils.loadAnimation(this, android.R.anim.fade_in);

        postExecutor = ContextCompat.getMainExecutor(this);

        bt=findViewById(R.id.bt_SignUp);
        loading =findViewById(R.id.pb_loading);
        tv_error=findViewById(R.id.tv_error);
        tv_login=findViewById(R.id.tv_login);
        loading.setVisibility(View.INVISIBLE);
        tv_error.setVisibility(View.INVISIBLE);
        tv1=findViewById(R.id.tv1); //this is only 'Already a member'
        tv_1=findViewById(R.id.tv_1);
        tv_2=findViewById(R.id.tv_2);
        tv_3=findViewById(R.id.tv_3);
        tv_4=findViewById(R.id.tv_4);
        et_info=findViewById(R.id.et_sign_up);
        tv_info=findViewById(R.id.tv_info_header);
        iv_back=findViewById(R.id.iv_back);
        iv_ready=findViewById(R.id.iv_sign_up_ready);

        setUpForm();
        bt.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                tv_error.setVisibility(View.GONE);
                tv_error.setAnimation(animOut);

                if(!TextUtils.isEmpty(et_info.getText().toString())){
                    infoCount++;
                    setUpForm();
                }else{
                    showTextViewError("Please filled the required field");
                }
            }
        });

        tv_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(SignUpActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(SignUpActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }



    private void setUpForm() {
        switch (infoCount) {
            case 0:
                infoSetter("Please Enter Your Name");

                tv_1.setBackgroundResource(R.drawable.bg_signup_info_onprogress);

                break;
            case 1:

                name = et_info.getText().toString();

                infoSetter("Please Enter Phone");
                et_info.setInputType(InputType.TYPE_CLASS_PHONE);
                tv_2.setBackgroundResource(R.drawable.bg_signup_info_onprogress);
                tv_1.setBackgroundResource(R.drawable.bg_signup_info_complete);

                break;
            case 2:

                phone = et_info.getText().toString();
                checkAccount();

                break;
            case 3:
                password = et_info.getText().toString();

                if (password.length() > 4) {
                    infoSetter("Please Confirm Your Password");

                    tv_4.setBackgroundResource(R.drawable.bg_signup_info_onprogress);
                    tv_3.setBackgroundResource(R.drawable.bg_signup_info_complete);

                } else {
                    infoCount--;
                    showTextViewError("Password must be at least 5 letters");
                }

                break;
            case 4:
                confirmPassword = et_info.getText().toString();

                if (confirmPassword.equals(password)) {
                    tv_4.setBackgroundResource(R.drawable.bg_signup_info_complete);
                    infoSetter("Ready To Sign Up!");
                    tv_info.setText("Ready To Sign up!");
                    bt.setText("Sign up");
                } else {
                    infoCount--;
                    showTextViewError("Passwords did not match!");
                }
                break;

            case 5:
                loading.setVisibility(View.VISIBLE);
                loading.setAnimation(animIn);
                joinNow();
                break;
        }
    }



    private void checkAccount(){

        if(phone!=null){
            makeAnimationOut();
            new Thread(() -> {
                MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.GET, new MyHttp.Response() {
                    @Override
                    public void onResponse(String response) {
                        Log.e("email REs ",response);
                        postExecutor.execute(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    JSONObject jo=new JSONObject(response);
                                    boolean result=jo.getBoolean("exist");
                                    if(!result){
                                        infoSetter("Please Enter Your Password");
                                        et_info.setTransformationMethod(PasswordTransformationMethod.getInstance());
                                        et_info.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                                        tv_3.setBackgroundResource(R.drawable.bg_signup_info_onprogress);
                                        tv_2.setBackgroundResource(R.drawable.bg_signup_info_complete);

                                    }else{
                                        makeAnimationIn();
                                        showTextViewError("Already registered. Use another phone.");
                                        infoCount--;
                                    }

                                }catch (Exception e){
                                    showTextViewError("Unexpected Error occurred!.Please connect to help center");
                                    makeAnimationIn();
                                    infoCount--;
                                }
                            }
                        });

                    }
                    @Override
                    public void onError(String msg) {
                        Log.e("EmailErr ",msg);
                        postExecutor.execute(new Runnable() {
                            @Override
                            public void run() {
                                showTextViewError("Unexpected Error occurred!.\nPlease connect to help center\n"+msg);
                                makeAnimationIn();
                                infoCount--;
                            }
                        });

                    }
                }).url(Routing.CHECK_ACCOUNT+"?phone="+phone);
                myHttp.runTask();
            }).start();
        }else{
            infoCount--;
        }

    }

    private void joinNow(){
        bt.setEnabled(false);
        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.POST, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    postExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            editor.putString("password",password);
                            doAsResult(response,phone);
                        }
                    });
                }
                @Override
                public void onError(String msg) {
                    postExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            loading.setVisibility(View.INVISIBLE);
                            tv_error.setVisibility(View.VISIBLE);
                            tv_error.setText("An unexpected error! Connect to help center");
                            bt.setEnabled(true);
                        }
                    });
                }
            }).url(Routing.SIGN_UP)
                    .field("name",name)
                    .field("phone",phone)
                    .field("password",password);


            myHttp.runTask();
        }).start();

    }

    private void doAsResult(String result,String phone){
        try {
            JSONObject jo=new JSONObject(result);
            String response=jo.getString("result");

            if(response.equals("go")){
                String auth_token=jo.getString("auth_token");
                editor.putBoolean("AlreadyLogin", true);
                editor.putString("phone",phone);
                editor.putString("auth_token",auth_token);

                editor.apply();

                UserInformation userInformation=new UserInformation(SignUpActivity.this);
                userInformation.getGeneralData(phone,auth_token);

                Intent intent=new Intent(SignUpActivity.this,StartCourseActivity.class);
                intent.putExtra("name",name);
                startActivity(intent);

                finish();
            }else{
                loading.setVisibility(View.INVISIBLE);
                bt.setEnabled(true);
                tv_error.setVisibility(View.VISIBLE);
                tv_error.setText(response);
            }

        }catch (Exception e){}
    }

    private void infoSetter(String s){
        if(infoCount>0)makeAnimationOut();
        animateTransition(s);
    }

    private void makeAnimationOut(){

        et_info.setVisibility(View.INVISIBLE);
        bt.setVisibility(View.INVISIBLE);
        et_info.setAnimation(animOut);
        bt.setAnimation(animOut);
        loading.setVisibility(View.VISIBLE);
        loading.setAnimation(animIn);
    }

    private void makeAnimationIn(){

        et_info.setVisibility(View.VISIBLE);
        et_info.setAnimation(animIn);
        bt.setVisibility(View.VISIBLE);
        bt.setAnimation(animIn);
        loading.setVisibility(View.GONE);
        loading.setAnimation(animOut);
    }


    private void showTextViewError(String s){
        tv_error.setText(s);
        tv_error.setVisibility(View.VISIBLE);
        tv_error.setAnimation(animIn);
    }

    private void animateTransition(String s){

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(700);
                    postExecutor.execute(new Runnable() {
                        @Override
                        public void run() {

                            if(infoCount<4){
                                et_info.setVisibility(View.VISIBLE);
                                et_info.setText("");
                                et_info.setHint(s+"");
                                et_info.setAnimation(animIn);
                            }else {
                                iv_ready.setVisibility(View.VISIBLE);
                                iv_ready.setAnimation(animIn);
                                tv_login.setVisibility(View.INVISIBLE);
                                tv1.setVisibility(View.INVISIBLE);
                                tv_login.setAnimation(animOut);
                                tv1.setAnimation(animOut);
                            }
                            bt.setVisibility(View.VISIBLE);
                            bt.setAnimation(animIn);
                            loading.setVisibility(View.GONE);
                            loading.setAnimation(animOut);


                        }
                    });
                }catch (Exception e){
                    Log.e("Err ",e.toString());
                }
            }
        }).start();

    }
}
