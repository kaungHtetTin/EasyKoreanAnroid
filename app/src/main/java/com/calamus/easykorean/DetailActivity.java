package com.calamus.easykorean;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.calamus.easykorean.app.XUtils;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Objects;
import java.util.concurrent.Executor;
import me.myatminsoe.mdetect.MDetect;
import me.myatminsoe.mdetect.Rabbit;

public class DetailActivity extends AppCompatActivity {
    WebView wv;
    SwipeRefreshLayout swipe;
    SharedPreferences share1;
    String title,content,link;
    SimpleDateFormat sdf= new SimpleDateFormat("MMMdd,yyyy HH:mm");
    boolean zg=true;
    boolean isVip;
    SharedPreferences sharedPreferences;
    private InterstitialAd interstitialAd;
    Executor postExecutor;
    String lessonResult;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        sharedPreferences=getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        isVip=sharedPreferences.getBoolean("isVIP",false);
        postExecutor= ContextCompat.getMainExecutor(this);
        link=getIntent().getExtras().getString("link");
        title=getIntent().getExtras().getString("title");
        wv = findViewById(R.id.detailWeb);

        wv.setWebViewClient(new WebViewClient());
        setUpView();
        swipe=findViewById(R.id.swipe_2);
        swipe.setRefreshing(true);
        MDetect.INSTANCE.init(this);

        share1=getSharedPreferences("GeneralData",Context.MODE_PRIVATE);
        swipe.setOnRefreshListener(() -> setUpView());

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);


        MobileAds.initialize(this, initializationStatus -> {});

        AdView adView = findViewById(R.id.web_adview);
        if(!isVip){
            adView.setVisibility(View.VISIBLE);
            AdRequest request=new AdRequest.Builder().build();
            adView.loadAd(request);
            Thread timer=new Thread(){
                @Override
                public void run() {
                    super.run();
                    try {
                        sleep(15000);
                        postExecutor.execute(() -> loadAds());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };

            timer.start();
        }


    }


    
    public String setMyanmar(String s) {
        return Rabbit.uni2zg(s);
    }

    @SuppressLint("SetJavaScriptEnabled")
    // @SuppressWarnings("deprecation")
    private void setUpView(){

        WebSettings settings = wv.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAppCacheEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setAppCachePath(getCacheDir().getAbsolutePath());
        settings.setAllowFileAccess(true);
        settings.setAppCacheEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        new ContentFetcher().start();

    }

    public String processJson(String jsonString)
    {
        try
        {
            JSONObject jo=new JSONObject(jsonString);
            jo = jo.getJSONObject("entry");

            return jo.getJSONObject("content").getString("$t");
        }
        catch (Exception e)
        {
            return e.toString();
        }
    }


    class ContentFetcher extends Thread
    {
        @Override
        public void run() {
            super.run();
            lessonResult=XUtils.fetch(link);
            postExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    wv.loadDataWithBaseURL("file:///",processJson(lessonResult),"text/html","UTF-8",null);
                    swipe.setRefreshing(false);
                }
            });
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        menu.add("FONT")
                .setIcon(R.drawable.ic_spellcheck_black_24dp)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (interstitialAd != null) {
                interstitialAd.show(DetailActivity.this);
            } else {
                // Proceed to the next level.
                    finish();

            }
        } else if (item.getTitle().toString().equals("FONT")){

            if(zg){

                wv.loadDataWithBaseURL("file:///", setMyanmar(content),"text/html","UTF-8",null);
                zg=false;
            }else {

                wv.loadDataWithBaseURL("file:///", content,"text/html","UTF-8",null);
                zg=true;
            }

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (interstitialAd != null) {
            interstitialAd.show(DetailActivity.this);
        } else {

            super.onBackPressed();

        }
    }

    @Override
    protected void onDestroy(){
          wv.destroy();
          wv=null;
        super.onDestroy();

    }


    private void loadAds(){

        FullScreenContentCallback fullScreenContentCallback = new FullScreenContentCallback() {
            @Override
            public void onAdDismissedFullScreenContent() {
                interstitialAd = null;
                // Proceed to the next level.
                finish();

            }
        };

        InterstitialAd.load(
                this,
                "ca-app-pub-2472405866346270/9132394579",
                new AdRequest.Builder().build(),
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd ad) {
                        interstitialAd = ad;
                        interstitialAd.setFullScreenContentCallback(fullScreenContentCallback);
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                        // Code to be executed when an ad request fails.
                    }
                });
    }

}
