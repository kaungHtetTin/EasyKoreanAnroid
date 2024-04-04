package com.calamus.easykorean;

import static com.calamus.easykorean.app.AppHandler.setPhotoFromRealUrl;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.calamus.easykorean.adapters.NewFeedAdapter;
import com.calamus.easykorean.app.MyDialog;
import com.calamus.easykorean.app.MyHttp;
import com.calamus.easykorean.app.Routing;
import com.calamus.easykorean.models.NewfeedModel;

import java.util.concurrent.Executor;

public class AccountDeleteActivity extends AppCompatActivity {
    ImageView iv_profile;
    TextView tv_name;
    Button bt_delete;
    RelativeLayout content_layout,loading_layout;
    SharedPreferences sharedPreferences;
    String imageUrl,username,phone;
    Executor postExecutor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_delete);
        sharedPreferences=getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        imageUrl=sharedPreferences.getString("imageUrl",null);
        username=sharedPreferences.getString("Username",null);
        phone=sharedPreferences.getString("phone",null);
        postExecutor= ContextCompat.getMainExecutor(this);

        getSupportActionBar().hide();

        setUpCustomAppBar();
        setUpView();

    }

    private void setUpView(){
        iv_profile=findViewById(R.id.iv_profile);
        tv_name=findViewById(R.id.tv_name);
        bt_delete=findViewById(R.id.bt_delete);
        content_layout=findViewById(R.id.content_layout);
        loading_layout=findViewById(R.id.loading_layout);

        setPhotoFromRealUrl(iv_profile,imageUrl);
        tv_name.setText(username);

        bt_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MyDialog(AccountDeleteActivity.this, "Delete Account!", "Do you really want to delete!", new MyDialog.ConfirmClick() {
                    @Override
                    public void onConfirmClick() {
                        deleteAccount();
                    }
                }).showMyDialog();
            }
        });
    }

    private void setUpCustomAppBar(){

        TextView tv=findViewById(R.id.tv_appbar);
        ImageView iv=findViewById(R.id.iv_back);
        tv.setText("Delete Account");
        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    private void deleteAccount(){
        bt_delete.setEnabled(false);
        loading_layout.setVisibility(View.VISIBLE);

        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.POST, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    signOut();
                }
                @Override
                public void onError(String msg) {
                    postExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            bt_delete.setEnabled(true);
                            loading_layout.setVisibility(View.GONE);
                            Toast.makeText(AccountDeleteActivity.this, "Error! Please try again.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }).url(Routing.DELETE_ACCOUNT+"/"+phone)
                    .field("mCode",Routing.MAJOR_CODE);
            myHttp.runTask();
        }).start();

    }

    private void signOut() {
        SharedPreferences sharedPreferences1 = getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor1 = sharedPreferences1.edit();
        editor1.clear();
        editor1.apply();
        Intent intent = new Intent(AccountDeleteActivity.this, SplashScreenActivity.class);
        startActivity(intent);
        finish();
    }
}