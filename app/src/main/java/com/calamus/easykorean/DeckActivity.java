package com.calamus.easykorean;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.calamus.easykorean.adapters.DeckAdapter;
import com.calamus.easykorean.app.Routing;
import com.calamus.easykorean.models.DeckModel;
import com.calamus.easykorean.app.MyHttp;
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
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.Executor;

public class DeckActivity extends AppCompatActivity {

    RecyclerView recycler;
    LinearLayout shimmerLayout;
    public SwipeRefreshLayout swipe;
    boolean isVip;
    SharedPreferences sharedPreferences;
    Executor postExecutor;
    String currentUserId;
    DeckAdapter adapter;
    ArrayList<DeckModel> decks = new ArrayList<>();
    String _id;
    private InterstitialAd mInterstitialAd=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deck);
        sharedPreferences=getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        isVip=sharedPreferences.getBoolean("isVIP",false);
        currentUserId=sharedPreferences.getString("phone",null);
        _id = sharedPreferences.getString("_id",null);
        postExecutor = ContextCompat.getMainExecutor(this);
        Objects.requireNonNull(getSupportActionBar()).hide();

        initView();

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
                    mInterstitialAd.show(DeckActivity.this);
                } else {
                    Log.d("TAG", "The interstitial ad wasn't ready yet.");
                    finish();
                }
            }
        });

    }

    private void initView(){
        recycler = findViewById(R.id.recycler);
        swipe=findViewById(R.id.swipe);

        shimmerLayout=findViewById(R.id.shimmer_layout);
        swipe.setRefreshing(false);

        recycler.setVisibility(View.GONE);
        shimmerLayout.setVisibility(View.VISIBLE);

        LinearLayoutManager lm = new LinearLayoutManager(this);
        recycler.setLayoutManager(lm);
        recycler.setItemAnimator(new DefaultItemAnimator());

        adapter = new DeckAdapter(this,decks);
        recycler.setAdapter(adapter);

        setUpCustomAppBar();
        fetchDecks();
    }

    private void setUpCustomAppBar(){

        TextView tv=findViewById(R.id.tv_appbar);

        tv.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        tv.setMarqueeRepeatLimit(-1);
        tv.setSingleLine(true);
        tv.setSelected(true);

        ImageView iv=findViewById(R.id.iv_back);
        tv.setText("Flashcard Learning");
        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mInterstitialAd != null) {
                    mInterstitialAd.show(DeckActivity.this);
                } else {
                    Log.d("TAG", "The interstitial ad wasn't ready yet.");
                    finish();
                }
            }
        });

    }

    private void fetchDecks(){
        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.GET, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    postExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                doAsResult(response);
                            }catch (Exception e){}

                        }
                    });
                }
                @Override
                public void onError(String msg) {
                    postExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }).url(Routing.FC_GET_PROGRESS+"?user_id="+_id);
            myHttp.runTask();
        }).start();
    }

    private void doAsResult(String response){
        Log.e("flashcard",response);
        try {
            recycler.setVisibility(View.VISIBLE);
            shimmerLayout.setVisibility(View.GONE);
            JSONObject jo = new JSONObject(response).getJSONObject("data")
                    .getJSONObject(Routing.FLASHCARD_LANGUAGE_ID);
            JSONArray ja = jo.getJSONArray("decks");

            for(int i = 0; i<ja.length(); i++){
                JSONObject joDeck = ja.getJSONObject(i);
                String id = joDeck.getString("deck_id");
                String title = joDeck.getString("deck_title");
                int new_words = joDeck.getInt("new_words");
                int total_cards = joDeck.getInt("total_cards");
                int mastered_cards = joDeck.getInt("mastered_cards");
                int learned_cards = joDeck.getInt("learned_cards");
                int recall_words = joDeck.getInt("recall_words");
                int progress_percent = joDeck.getInt("progress_percent");

                decks.add(new DeckModel(id,learned_cards,mastered_cards,total_cards,progress_percent,
                        recall_words,new_words,title));
            }

            adapter.notifyDataSetChanged();


        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
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
}