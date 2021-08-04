package com.calamus.easykorean;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.calamus.easykorean.app.AppHandler;
import com.calamus.easykorean.app.MyHttp;
import com.calamus.easykorean.app.Routing;
import com.calamus.easykorean.app.UserInformation;

import org.json.JSONObject;

import java.util.Objects;
import java.util.concurrent.Executor;

import me.myatminsoe.mdetect.MDetect;

public class SignUpActivity extends AppCompatActivity {

    //Data Type Variable
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    //View Type Variable
    Button bt;
    EditText et_name, et_phone,et_password;
    ProgressBar loading;

    Executor postExecutor;
    TextView tv_error,tv_login;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        sharedPreferences=getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        editor=sharedPreferences.edit();
        MDetect.INSTANCE.init(Objects.requireNonNull(this));
        postExecutor = ContextCompat.getMainExecutor(this);

        bt=findViewById(R.id.bt_SignUp);
        et_name=findViewById(R.id.et_name);
        et_phone=findViewById(R.id.et_phone);
        et_password=findViewById(R.id.et_password);
        loading =findViewById(R.id.pb_loading);
        tv_error=findViewById(R.id.tv_error);
        tv_login=findViewById(R.id.tv_login);
        loading.setVisibility(View.INVISIBLE);
        tv_error.setVisibility(View.INVISIBLE);
        bt.setOnClickListener(v -> {
            loading.setVisibility(View.VISIBLE);
            tv_error.setVisibility(View.INVISIBLE);
            tv_error.setText("");
            checkAndSignUp(AppHandler.changeUnicode(et_name.getText().toString()),et_phone.getText().toString(),et_password.getText().toString());
        });

        tv_login.setOnClickListener(view -> {
            Intent intent=new Intent(SignUpActivity.this,LoginActivity.class);
            startActivity(intent);
            finish();
        });

    }

    private void checkAndSignUp(String name , String phone,String password) {
        if(name.isEmpty()){
            loading.setVisibility(View.INVISIBLE);
            tv_error.setVisibility(View.VISIBLE);
            tv_error.append("Please enter your name\n");
        }

        if (phone.isEmpty()){
            loading.setVisibility(View.INVISIBLE);
            tv_error.setVisibility(View.VISIBLE);
            tv_error.append("Please enter your phone number\n");

        }

        if(password.isEmpty()){
            loading.setVisibility(View.INVISIBLE);
            tv_error.setVisibility(View.VISIBLE);
            tv_error.append("Please enter your password\n");
        }

        if(password.length()<5){
            tv_error.setVisibility(View.VISIBLE);
            tv_error.append("Your password must have at least 5 letters\n");

        }
        if(!name.isEmpty()&&!phone.isEmpty()&&!password.isEmpty()&&password.length()>=5){
            joinNow(name , phone,password);

        }else{
            loading.setVisibility(View.INVISIBLE);
            tv_error.setVisibility(View.VISIBLE);
            bt.setEnabled(true);
        }
    }


    private void joinNow(String name,String phone,String password){
        bt.setEnabled(false);
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
                        tv_error.setVisibility(View.VISIBLE);
                        tv_error.setText("An unexpected error! Connect to help center");
                        bt.setEnabled(true);
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
                editor.putBoolean("AlreadyLogin", true);
                editor.putString("phone",phone);
                editor.apply();

                UserInformation userInformation=new UserInformation(SignUpActivity.this);
                userInformation.getGeneralData(phone);

                Intent intent=new Intent(SignUpActivity.this,MainActivity.class);
                intent.putExtra("message","login");
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
}