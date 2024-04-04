package com.calamus.easykorean;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.calamus.easykorean.app.AppHandler;
import com.calamus.easykorean.app.Routing;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class WordDetailActivity extends AppCompatActivity {

    ImageView iv;
    TextView tv_date,tv_main,tv_detail;
    String wordOfTheDay;
    boolean isVIP;
    SharedPreferences sharedPreferences;

    private InterstitialAd mInterstitialAd=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_detail);
        sharedPreferences=getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        isVIP=sharedPreferences.getBoolean("isVIP",false);

        wordOfTheDay=getIntent().getExtras().getString("word","");

        setUpView();
        Objects.requireNonNull(getSupportActionBar()).hide();
        setUpCustomAppBar();

        if(!isVIP){
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
                    mInterstitialAd.show(WordDetailActivity.this);
                } else {
                    Log.d("TAG", "The interstitial ad wasn't ready yet.");
                    finish();
                }
            }
        });

    }

    private void loadAd(){
        AdRequest adRequest = new AdRequest.Builder().build();

        InterstitialAd.load(this, Routing.ADMOB_INTERSTITIAL, adRequest,
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

    private void setUpCustomAppBar(){

        TextView tv=findViewById(R.id.tv_appbar);
        ImageView iv=findViewById(R.id.iv_back);
        tv.setText("Detail");
        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mInterstitialAd != null) {
                    mInterstitialAd.show(WordDetailActivity.this);
                } else {
                    Log.d("TAG", "The interstitial ad wasn't ready yet.");
                    finish();
                }
            }
        });
    }

    private void setUpView(){
        iv=findViewById(R.id.iv_word_of_the_day);
        tv_date=findViewById(R.id.tv_date);
        tv_main=findViewById(R.id.tv_main);
        tv_detail=findViewById(R.id.tv_detail);


        SimpleDateFormat sdf= new SimpleDateFormat("MMMdd,yyyy HH:mm");
        Date resultDate=new Date(System.currentTimeMillis());
        tv_date.setText(sdf.format(resultDate));

        try{
            JSONArray ja=new JSONArray(wordOfTheDay);
            JSONObject jo=ja.getJSONObject(0);
            String english=jo.getString(Routing.MAJOR);
            String myanmar=jo.getString("myanmar");
            String speech=jo.getString("speech");
            String example=jo.getString("example");
            String thumb=jo.getString("thumb");
            tv_main.setText(english+" ( "+speech+" )\n"+ AppHandler.setMyanmar(myanmar));
            AppHandler.setPhotoFromRealUrl(iv,thumb);
            tv_detail.setText(example);

        }catch (Exception e){
            tv_detail.setText("Word of the day is not available now for a while");
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {

        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}