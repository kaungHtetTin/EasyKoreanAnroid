package com.calamus.easykorean;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.calamus.easykorean.app.AppHandler;
import com.calamus.easykorean.app.Routing;
import com.calamus.easykorean.app.MyHttp;
import com.calamus.easykorean.app.RealPathUtil;

import java.util.concurrent.Executor;

public class UpdateCoverPhotoActivity extends AppCompatActivity {


    ImageView iv_cover_photo,iv_profile,iv_back;
    TextView tv_name,tv_bio,tv_save;
    ProgressBar pb;

    String username,imageUri,profileUrl,myBio,userId;
    Uri coverUri;

    Executor postExecutor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_cover_photo);
        getSupportActionBar().hide();
        username=getIntent().getExtras().getString("username");
        profileUrl=getIntent().getExtras().getString("profileUrl");
        myBio=getIntent().getExtras().getString("myBio");
        imageUri=getIntent().getExtras().getString("imageUri");
        userId=getIntent().getExtras().getString("userId");
        postExecutor= ContextCompat.getMainExecutor(this);
        coverUri=Uri.parse(imageUri);
        setUpView();
    }

    private void setUpView(){
        iv_cover_photo=findViewById(R.id.iv_cover_photo);
        iv_profile=findViewById(R.id.iv_profile);
        iv_back=findViewById(R.id.iv_back);
        tv_name=findViewById(R.id.tv_name);
        tv_bio=findViewById(R.id.et_bio);
        tv_save=findViewById(R.id.tv_save);
        pb=findViewById(R.id.pb);

        tv_name.setText(username);
        if(!profileUrl.equals("")) AppHandler.setPhotoFromRealUrl(iv_profile,profileUrl);
        iv_cover_photo.setImageURI(coverUri);

        tv_bio.setText(myBio);

        tv_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveCoverPhoto();
            }
        });

        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    private void saveCoverPhoto(){
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
            }).url(Routing.CHANGE_COVER_PHOTO)
                    .field("phone",userId)
                    .file("cover_photo", RealPathUtil.getRealPath(UpdateCoverPhotoActivity.this,coverUri));

            myHttp.runTask();
        }).start();
    }
}