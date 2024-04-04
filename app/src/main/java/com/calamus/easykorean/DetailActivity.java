package com.calamus.easykorean;


import androidx.activity.OnBackPressedCallback;
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
import android.widget.Toast;

import com.calamus.easykorean.app.MyHttp;
import com.calamus.easykorean.app.Routing;
import com.calamus.easykorean.app.XUtils;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DetailActivity extends AppCompatActivity {
    WebView wv;
    SwipeRefreshLayout swipe;
    SharedPreferences share1;
    String title,content,link;
    SimpleDateFormat sdf= new SimpleDateFormat("MMMdd,yyyy HH:mm");
    boolean zg=true;
    boolean isVip;
    SharedPreferences sharedPreferences;
    Executor postExecutor;
    ExecutorService myExecutor;
    String lessonResult,userId,lessonId;

    ImageView iv_back,iv_spellCheck;
    private InterstitialAd mInterstitialAd=null;


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

        share1=getSharedPreferences("GeneralData",Context.MODE_PRIVATE);
        swipe.setOnRefreshListener(() -> setUpView());


        recordStudyingOnLesson();

        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mInterstitialAd != null) {
                    mInterstitialAd.show(DetailActivity.this);
                } else {
                    Log.d("TAG", "The interstitial ad wasn't ready yet.");
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

        if(!isVip){
            MobileAds.initialize(this, new OnInitializationCompleteListener() {
                @Override
                public void onInitializationComplete(InitializationStatus initializationStatus) {
                    loadAd();
                }
            });
        }

        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (mInterstitialAd != null) {
                    mInterstitialAd.show(DetailActivity.this);
                } else {
                    Log.d("TAG", "The interstitial ad wasn't ready yet.");
                    finish();
                }
            }
        });

    }

    private void loadAd(){
        AdRequest adRequest = new AdRequest.Builder().build();

        InterstitialAd.load(this,Routing.ADMOB_INTERSTITIAL, adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        mInterstitialAd = interstitialAd;
                        Toast.makeText(getApplicationContext(),"Ad loaded",Toast.LENGTH_SHORT).show();
                        mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback(){
                            @Override
                            public void onAdClicked() {
                                // Called when a click is recorded for an ad.

                            }

                            @Override
                            public void onAdDismissedFullScreenContent() {
                                // Called when ad is dismissed.
                                // Set the ad reference to null so you don't show the ad a second time.
                                mInterstitialAd = null;
                                finish();
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(AdError adError) {
                                // Called when ad fails to show.
                                mInterstitialAd = null;
                            }

                            @Override
                            public void onAdImpression() {
                                // Called when an impression is recorded for an ad.

                            }

                            @Override
                            public void onAdShowedFullScreenContent() {
                                // Called when ad is shown.

                            }
                        });

                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error

                        mInterstitialAd = null;
                        Toast.makeText(getApplicationContext(),"Ad fail",Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public String setMyanmar(String s) {
        return s;
    }

    @SuppressLint("SetJavaScriptEnabled")
    // @SuppressWarnings("deprecation")
    private void setUpView(){

        WebSettings settings = wv.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setAllowFileAccess(true);
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
    protected void onDestroy(){
          wv.destroy();
          wv=null;
        super.onDestroy();

    }
}
