package com.calamus.easykorean;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.calamus.easykorean.app.MyHttp;
import com.calamus.easykorean.app.Routing;
import com.calamus.easykorean.app.UserInformation;
import org.json.JSONObject;
import java.util.concurrent.Executor;

public class LoginActivity extends AppCompatActivity {

    //data type variable
    final int callRequestCode = 123;

    //view type variable
    EditText et_phone, et_password;
    Button loginButton;
    TextView signUpText, callCenter,tv_error,tv_forgetPassword;
    ProgressBar loading;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    Executor postExecutor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sharedPreferences=getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        editor=sharedPreferences.edit();
        et_phone=findViewById(R.id.et_phone);
        et_password=findViewById(R.id.et_password);
        loginButton = findViewById(R.id.bt_log_in);
        loading = findViewById(R.id.pb_loading);
        loading.setVisibility(View.INVISIBLE);
        signUpText = findViewById(R.id.signup_tv);
        callCenter = findViewById(R.id.tv_call_center);
        tv_error=findViewById(R.id.tv_error);
        tv_error.setVisibility(View.INVISIBLE);
        tv_forgetPassword=findViewById(R.id.tv_forget_password);
        postExecutor = ContextCompat.getMainExecutor(this);

        loginButton.setOnClickListener(v -> {

            logInValidate(et_phone.getText().toString(), et_password.getText().toString());
            tv_error.setVisibility(View.INVISIBLE);
        });

        signUpText.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
            finish();
        });

        callCenter.setOnClickListener(v -> {
            Intent intent= new Intent(Intent.ACTION_VIEW, Uri.parse("https://m.me/easykoreancalamus"));
            startActivity(intent);
        });

        tv_forgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(LoginActivity.this,ForgetPasswordActivity.class);
                startActivity(intent);
            }
        });

    }


    private void logInValidate(final String phone, String password){
        if(phone.isEmpty()){
            loginButton.setEnabled(true);
            tv_error.setVisibility(View.VISIBLE);
            tv_error.append("Please enter your phone number.\n");
            loading.setVisibility(View.INVISIBLE);
        }
        if(password.isEmpty()){
            loginButton.setEnabled(true);
            tv_error.setVisibility(View.VISIBLE);
            tv_error.setText("Please enter your password.");
            loading.setVisibility(View.INVISIBLE);
        }

        if(!phone.isEmpty()&&!password.isEmpty()){
            logIn(phone,password);
        }else{
            loginButton.setEnabled(true);
            loading.setVisibility(View.INVISIBLE);
        }
    }


    private void logIn(final String phone, String password) {

        loginButton.setEnabled(false);
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
                            loginButton.setEnabled(true);
                            tv_error.setVisibility(View.VISIBLE);
                            tv_error.setText("An unexpected error! Connect to help center");
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

                UserInformation userInformation=new UserInformation(LoginActivity.this);
                userInformation.getGeneralData(phone);

                Intent intent=new Intent(LoginActivity.this,MainActivity.class);
                intent.putExtra("message","login");

                startActivity(intent);
                finish();
            }else{
                loading.setVisibility(View.INVISIBLE);
                loginButton.setEnabled(true);
                tv_error.setVisibility(View.VISIBLE);
                tv_error.setText(response);
            }

        }catch (Exception ignored){}
    }
}