package com.calamus.easykorean;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
    TextView signUpText,tv_error,tv_forgetPassword;
    ProgressBar loading;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    Executor postExecutor;

    ImageView iv_login_english,iv_login_japanese,iv_login_chinese;
    LinearLayout orContainer,loginContainer;

    TextView tv_terms,tv_privacy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sharedPreferences=getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        editor=sharedPreferences.edit();
        et_phone=findViewById(R.id.et_phone);
        et_password=findViewById(R.id.et_password);
        signUpText = findViewById(R.id.tv_login);
        loginButton = findViewById(R.id.bt_log_in);
        loading = findViewById(R.id.pb_loading);
        loading.setVisibility(View.INVISIBLE);

        tv_error=findViewById(R.id.tv_error);
        tv_error.setVisibility(View.INVISIBLE);
        tv_forgetPassword=findViewById(R.id.tv_forget_password);
        postExecutor = ContextCompat.getMainExecutor(this);


        orContainer=findViewById(R.id.or_container);
        loginContainer=findViewById(R.id.login_with_container);
        iv_login_english=findViewById(R.id.login_english);
        iv_login_chinese=findViewById(R.id.login_chinese);
        iv_login_japanese=findViewById(R.id.login_japanese);

        loginButton.setOnClickListener(v -> {

            logInValidate(et_phone.getText().toString(), et_password.getText().toString());
            tv_error.setVisibility(View.INVISIBLE);
        });

        signUpText.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
            finish();
        });



        tv_forgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(LoginActivity.this,ForgetPasswordActivity.class);
                startActivity(intent);
            }
        });

        checkOtherCalamusApp();

        iv_login_english.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Intent intent=new Intent("com.qanda.learnroom.AuthCheckerActivity");
                    intent.putExtra("request_app",Routing.MAJOR);
                    mStartForResult.launch(intent);
                    tv_error.setVisibility(View.INVISIBLE);
                }catch (Exception e){
                    tv_error.setVisibility(View.VISIBLE);
                    tv_error.setText("Can't Login. Easy English is not latest version");
                }
            }
        });

        iv_login_japanese.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Intent intent=new Intent("com.calamus.easyjapanese.AuthCheckerActivity");
                    intent.putExtra("request_app",Routing.MAJOR);
                    mStartForResult.launch(intent);
                    tv_error.setVisibility(View.INVISIBLE);
                }catch (Exception e){
                    tv_error.setVisibility(View.VISIBLE);
                    tv_error.setText("Can't Login");
                    Log.e("loginClickErr ",e.toString());
                }
            }
        });

        iv_login_chinese.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Intent intent=new Intent("com.calamus.easychinese.AuthCheckerActivity");
                    intent.putExtra("request_app",Routing.MAJOR);
                    mStartForResult.launch(intent);
                    tv_error.setVisibility(View.INVISIBLE);
                }catch (Exception e){
                    tv_error.setVisibility(View.VISIBLE);
                    tv_error.setText("Can't Login");
                }
            }
        });

        tv_terms=findViewById(R.id.tv_terms);
        tv_privacy=findViewById(R.id.tv_privacy);

        tv_terms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(LoginActivity.this, WebSiteActivity.class);
                i.putExtra("link", Routing.TERMS_OF_USE);
                startActivity(i);
            }
        });

        tv_privacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(LoginActivity.this, WebSiteActivity.class);
                i.putExtra("link", Routing.PRIVACY_POLICY);
                startActivity(i);
            }
        });
    }

    ActivityResultLauncher<Intent> mStartForResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent intent = result.getData();
                        boolean success=intent.getBooleanExtra("success",false);

                        Log.e("success ",success+"");
                        if(success){
                            String phone=intent.getStringExtra("userid");
                            String auth_token=intent.getStringExtra("auth_token");
                            if(phone!=null&&auth_token!=null)gotoApp(phone,auth_token);
                            else  {
                                tv_error.setVisibility(View.VISIBLE);
                                tv_error.setText("Can't login! Please try again");
                            }
                        }else{
                            tv_error.setVisibility(View.VISIBLE);
                            tv_error.setText("Can't login! Please try again with another way.");
                        }

                    }
                }
            });


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
                        editor.putString("password",password);
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
                }).url(Routing.LOGIN)
                        .field("phone",phone)
                        .field("password",password);

                myHttp.runTask();
            }).start();
        }
    }

    private void doAsResult(String result,String phone){
        try {
            JSONObject jo=new JSONObject(result);
            String response=jo.getString("result");
            if(response.equals("go")){
                String authToken=jo.getString("auth_token");
                editor.putString("auth_token",authToken);
                gotoApp(phone,authToken);

            }else{
                loading.setVisibility(View.INVISIBLE);
                loginButton.setEnabled(true);
                tv_error.setVisibility(View.VISIBLE);
                tv_error.setText(response);
            }

        }catch (Exception ignored){}
    }

    private void gotoApp(String phone,String auth_token){
        editor.putBoolean("AlreadyLogin", true);
        editor.putString("phone",phone);
        editor.putString("auth_token",auth_token);
        editor.apply();

        UserInformation userInformation=new UserInformation(LoginActivity.this);
        userInformation.getGeneralData(phone,auth_token);

        Intent intent=new Intent(LoginActivity.this,MainActivity.class);
        intent.putExtra("message","login");

        startActivity(intent);
        finish();
    }

    private void checkOtherCalamusApp(){
        boolean otherAppExist=false;

        if(isPackageInstalled("com.qanda.learnroom")){
            iv_login_english.setVisibility(View.VISIBLE);
            otherAppExist=true;
        }

        if(isPackageInstalled("com.calamus.easyjapanese")){
            iv_login_japanese.setVisibility(View.VISIBLE);
            otherAppExist=true;
        }

        if(isPackageInstalled("com.calamus.easychinese")){
            iv_login_chinese.setVisibility(View.VISIBLE);
            otherAppExist=true;
        }

        if(otherAppExist){
            orContainer.setVisibility(View.VISIBLE);
            loginContainer.setVisibility(View.VISIBLE);
        }

    }

    private boolean isPackageInstalled(String packageName) {
        PackageManager packageManager=getPackageManager();
        try {
            packageManager.getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("AppNameNot ",e.toString());
            return false;
        }
    }
}