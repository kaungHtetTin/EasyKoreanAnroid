package com.calamus.easykorean;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.calamus.easykorean.app.WebAppInterface;
import java.util.Objects;
import java.util.concurrent.Executor;

public class WebSiteActivity extends AppCompatActivity {

    WebView wv;
    SwipeRefreshLayout swipe;

    private boolean isRedirected;
    String Current_url,address,check;
    boolean isVip;
    SharedPreferences sharedPreferences;
    Executor postExecutor;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_site);
        setUpCustomAppBar();
        Objects.requireNonNull(getSupportActionBar()).hide();

        sharedPreferences=getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        isVip=sharedPreferences.getBoolean("isVIP",false);
        postExecutor= ContextCompat.getMainExecutor(this);
        wv=findViewById(R.id.go_web);
        swipe=findViewById(R.id.swipe_go_web);

        wv.setWebViewClient(new WebViewClient());
        WebSettings settings = wv.getSettings();
        settings.setJavaScriptEnabled(true);
        wv.addJavascriptInterface(new WebAppInterface(this), "Android");
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);

        Current_url= Objects.requireNonNull(getIntent().getExtras()).getString("link");
        check=Current_url;


        startWebView(wv,Current_url);

        swipe.setOnRefreshListener(() -> startWebView(wv,Current_url));

        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if(wv.canGoBack() && !check.equals(address)){
                    wv.goBack();

                }else {
                    finish();
                }
            }
        });

    }

    private void setUpCustomAppBar(){
        TextView tv=findViewById(R.id.tv_appbar);
        ImageView iv=findViewById(R.id.iv_back);
        tv.setText("Easy Korean");
        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (wv.canGoBack()&& !check.equals(address)) {
                    wv.goBack();
                } else {
                    finish();
                }
            }
        });
    }

    private void startWebView(WebView wv, String url){
        wv.setWebViewClient(new WebViewClient(){

            public boolean shouldOverrideUrlLoading(WebView view,String url){
                Current_url=url;
                view.loadUrl(url);
                isRedirected=true;
                return false;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                isRedirected=false;
            }
            public void onLoadResource(WebView view, String url){
                if(!isRedirected){
                    swipe.setRefreshing(true);
                }
            }

            public void onPageFinished(WebView view,String url){
                try{
                    isRedirected=true;
                    swipe.setRefreshing(false);

                }catch (Exception exception){
                    exception.printStackTrace();
                }
            }

        });

        wv.loadUrl(url);
        swipe.setRefreshing(false);
    }
}

