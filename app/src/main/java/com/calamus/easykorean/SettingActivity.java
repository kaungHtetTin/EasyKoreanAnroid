package com.calamus.easykorean;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Objects;

public class SettingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        setUpView();
        Objects.requireNonNull(getSupportActionBar()).hide();
        setUpCustomAppBar();

    }

    private void setUpCustomAppBar(){
        TextView tv=findViewById(R.id.tv_appbar);
        ImageView iv=findViewById(R.id.iv_back);
        tv.setText("Setting");
        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }


    private void setUpView(){


        //Edit Profile
        findViewById(R.id.setting_edit_profile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(SettingActivity.this,EditProfileActivity.class);
                startActivity(intent);
            }
        });

        //Reset Password
        findViewById(R.id.setting_reset_password).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(SettingActivity.this,ResetPasswordActivity.class);
                startActivity(intent);
            }
        });

        //Like Us On Facebook
        findViewById(R.id.setting_like_facebook).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFacebookPage("easyenglishcalamus");
            }
        });

        //Check Update
        findViewById(R.id.setting_check_update).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingActivity.this, UpdateActivity.class));
            }
        });

        //Share App
        findViewById(R.id.setting_share_app).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent shareingIntent = new Intent(Intent.ACTION_SEND);
                shareingIntent.setType("text/plain");
                String shareBody = "https://play.google.com/store/apps/details?id=" +getPackageName();
                shareingIntent.putExtra(Intent.EXTRA_SUBJECT, "Try out this best Language App.");
                shareingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(shareingIntent, "Share via"));
            }
        });

        //Rate Us
        findViewById(R.id.setting_rate_us).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goPlayStore();
            }
        });

        //blocked users
        findViewById(R.id.setting_blocked_user).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(SettingActivity.this,BlockUserActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.setting_delete_account).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(SettingActivity.this,AccountDeleteActivity.class);
                startActivity(intent);
            }
        });

        //Sign Out
        findViewById(R.id.setting_sign_out).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });
    }

    public void goPlayStore(){
        startActivity(new Intent(Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id="+ getPackageName())));
    }

    private void openFacebookPage(String pageId) {
        String pageUrl = "https://wwww.facebook.com/easykoreancalamus/";

        try {
            ApplicationInfo applicationInfo = getPackageManager().getApplicationInfo("com.facebook.katana", 0);

            if (applicationInfo.enabled) {
                int versionCode = getPackageManager().getPackageInfo("com.facebook.katana", 0).versionCode;
                String url;

                if (versionCode >= 3002850) {
                    url = "fb://facewebmodal/f?href=" + pageUrl;
                } else {
                    url = "fb://page/" + pageId;
                }

                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            } else {
                throw new Exception("Facebook is disabled");
            }
        } catch (Exception e) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(pageUrl)));
        }
    }

    private void signOut() {
        SharedPreferences sharedPreferences1 = getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor1 = sharedPreferences1.edit();
        editor1.clear();
        editor1.apply();
        Intent intent = new Intent(SettingActivity.this, SplashScreenActivity.class);
        startActivity(intent);
        finish();
    }
}