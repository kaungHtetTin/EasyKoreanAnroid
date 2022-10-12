package com.calamus.easykorean;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.calamus.easykorean.app.AppHandler;
import com.calamus.easykorean.app.Routing;
import com.calamus.easykorean.app.MyHttp;

import java.util.concurrent.Executor;

public class UpdateBioActivity extends AppCompatActivity {

    ImageView iv_profile,iv_back;
    TextView tv_name,tv_save;
    ProgressBar pb;
    EditText et_bio;

    String bio,username,profileUrl,userId;
    Executor postExecutor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_bio);
        getSupportActionBar().hide();
        postExecutor= ContextCompat.getMainExecutor(this);
        bio=getIntent().getExtras().getString("bio");
        userId=getIntent().getExtras().getString("userId");
        username=getIntent().getExtras().getString("username");
        profileUrl=getIntent().getExtras().getString("profileUrl");

        setUpView();
    }

    private void setUpView(){
        iv_profile=findViewById(R.id.iv_profile);
        tv_name=findViewById(R.id.tv_name);
        tv_save=findViewById(R.id.tv_save);
        pb=findViewById(R.id.pb);
        et_bio=findViewById(R.id.et_bio);
        iv_back=findViewById(R.id.iv_back);

        if(!profileUrl.equals("")) AppHandler.setPhotoFromRealUrl(iv_profile,profileUrl);
        tv_name.setText(username);

        et_bio.setText(bio);

        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        tv_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateBio();
            }
        });
    }

    private void updateBio(){
        String bio;
        if(TextUtils.isEmpty(et_bio.getText().toString())){
            bio="";
        }else{
            bio=et_bio.getText().toString();
        }
        pb.setVisibility(View.VISIBLE);
        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.POST, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    postExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            pb.setVisibility(View.INVISIBLE);
                            Log.e("Response ",response);
                            finish();
                        }
                    });
                }
                @Override
                public void onError(String msg) {
                    postExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            pb.setVisibility(View.INVISIBLE);
                            Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }).url(Routing.CHANGE_BIO)
                    .field("userId",userId)
                    .field("bio", bio);

            myHttp.runTask();
        }).start();
    }
}