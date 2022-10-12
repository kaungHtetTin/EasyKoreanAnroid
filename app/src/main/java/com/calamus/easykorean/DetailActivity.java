package com.calamus.easykorean;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import com.calamus.easykorean.app.MyHttp;
import com.calamus.easykorean.app.Routing;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import me.myatminsoe.mdetect.MDetect;
import me.myatminsoe.mdetect.Rabbit;

import static java.lang.Thread.sleep;

public class DetailActivity extends AppCompatActivity {
    WebView wv;
    SwipeRefreshLayout swipe;
    SharedPreferences share1;
    String title,content,link;
    SimpleDateFormat sdf= new SimpleDateFormat("MMMdd,yyyy HH:mm");
    boolean zg=true;
    boolean isVip,isTimeToShowAds=true;
    SharedPreferences sharedPreferences;
    private InterstitialAd interstitialAd;
    Executor postExecutor;
    ExecutorService myExecutor;
    String lessonResult,userId,lessonId;

    ImageView iv_back,iv_spellCheck;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Objects.requireNonNull(getSupportActionBar()).hide();
        sharedPreferences=getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        isVip=sharedPreferences.getBoolean("isVIP",false);
        userId=sharedPreferences.getString("phone","");
        postExecutor= ContextCompat.getMainExecutor(this);
        myExecutor= Executors.newFixedThreadPool(3);
        link=getIntent().getExtras().getString("link");
        title=getIntent().getExtras().getString("title");
        lessonId=getIntent().getExtras().getString("lessonId");
        wv = findViewById(R.id.detailWeb);

        iv_back=findViewById(R.id.iv_back);
        iv_spellCheck=findViewById(R.id.iv_spellCheck);


        wv.setWebViewClient(new WebViewClient());
        setUpView();
        swipe=findViewById(R.id.swipe_2);
        swipe.setRefreshing(true);
        MDetect.INSTANCE.init(this);

        share1=getSharedPreferences("GeneralData",Context.MODE_PRIVATE);
        swipe.setOnRefreshListener(() -> setUpView());



        MobileAds.initialize(this, initializationStatus -> {});
        AdView adView = findViewById(R.id.adview);
        if(!isVip){
            adView.setVisibility(View.VISIBLE);
            AdRequest request=new AdRequest.Builder().build();
            adView.loadAd(request);
            myExecutor.execute(()->{
                try {
                    sleep(10000);
                    postExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            if (isTimeToShowAds) loadAds();
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }

        recordStudyingOnLesson();

        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isTimeToShowAds=false;
                if (interstitialAd != null) {
                    interstitialAd.show(DetailActivity.this);
                } else {
                    // Proceed to the next level.
                    finish();
                }
            }
        });

        iv_spellCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(zg){

                    wv.loadDataWithBaseURL("file:///", setMyanmar(content),"text/html","UTF-8",null);
                    zg=false;
                }else {

                    wv.loadDataWithBaseURL("file:///", content,"text/html","UTF-8",null);
                    zg=true;
                }
            }
        });

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

        extractLesson();


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


    private void extractLesson(){
        myExecutor.execute(()->{
            lessonResult=XUtils.fetch(link);
            postExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    content=processJson(lessonResult);
                    wv.loadDataWithBaseURL("file:///",processJson(lessonResult),"text/html","UTF-8",null);
                    swipe.setRefreshing(false);
                }
            });
        });
    }

    private void recordStudyingOnLesson(){
        myExecutor.execute(()->{
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.POST, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    Log.e("StudyRecSucc: ",response);
                }
                @Override
                public void onError(String msg) {
                    Log.e("StudyRecErr: ",msg);
                }
            }).url(Routing.STUDY_RECORD_A_LESSON)
                    .field("userId",userId)
                    .field("lessonId",lessonId);
            myHttp.runTask();

        });

    }



    @Override
    public void onBackPressed() {
        myExecutor.shutdown();
        isTimeToShowAds=false;
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
          myExecutor.shutdown();
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
