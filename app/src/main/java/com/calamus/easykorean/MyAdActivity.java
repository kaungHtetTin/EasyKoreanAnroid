package com.calamus.easykorean;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebViewClient;
import android.widget.TextView;
import com.calamus.easykorean.app.WebAppInterface;
import com.vk.sdk.WebView;

import java.util.concurrent.Executor;

public class MyAdActivity extends AppCompatActivity {

    WebView wv;
    TextView tv_skip;
    Executor postExecutor;
    boolean enableSkip=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_ad);
        getSupportActionBar().hide();
        postExecutor= ContextCompat.getMainExecutor(this);
        setUpView();
    }

    private void setUpView(){
        wv=findViewById(R.id.wv);
        tv_skip=findViewById(R.id.tv_skip);


        wv.setWebViewClient(new WebViewClient());
        WebSettings settings = wv.getSettings();
        settings.setJavaScriptEnabled(true);
        wv.addJavascriptInterface(new WebAppInterface(this), "Android");
        settings.setDomStorageEnabled(true);
        settings.setAppCacheEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setAppCachePath(getCacheDir().getAbsolutePath());
        settings.setAllowFileAccess(true);
        settings.setAppCacheEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);

        wv.loadUrl("https://www.calamuseducation.com/calamus-v2/ads/korea");

        setTimer();



        tv_skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(enableSkip)finish();
            }
        });


    }

    private void setTimer(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                int count=5;
                while (count>0){
                    int finalCount = count;
                    postExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            tv_skip.setText("Skip "+ finalCount);
                        }
                    });

                    count--;
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                enableSkip=true;
                postExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        tv_skip.setText("Skip Now");
                    }
                });

            }
        }).start();
    }

    @Override
    public void onBackPressed() { }
}