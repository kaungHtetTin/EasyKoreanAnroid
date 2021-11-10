package com.calamus.easykorean;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.calamus.easykorean.adapters.GameWordAdapter;
import com.calamus.easykorean.adapters.TopGamePlayerAdapter;
import com.calamus.easykorean.app.AppHandler;
import com.calamus.easykorean.app.MyDialog;
import com.calamus.easykorean.app.MyHttp;
import com.calamus.easykorean.app.Routing;
import com.calamus.easykorean.models.GameWordModel;
import com.calamus.easykorean.models.TopGamePlayerModel;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import me.myatminsoe.mdetect.MDetect;

public class GammingActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    Button bt_a,bt_b,bt_c;
    ProgressBar pb;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    Executor postExecutor;
    String ans,currentUserPhone;
    TextView tvHighestScore,tvCurrentScore;

    final ArrayList<Object> recyclerViewItems=new ArrayList<>();
    GameWordAdapter adapter;

    int currentScore=0;
    int highestScore;

    boolean isVip;

    ArrayList<TopGamePlayerModel> playerLists=new ArrayList<>();
    TopGamePlayerAdapter playerAdapter;
    RecyclerView playerRecycler;

    GameWordModel model;
    MediaPlayer mPlayer;

    private InterstitialAd interstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gamming);


        sharedPreferences=getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        editor=sharedPreferences.edit();
        currentUserPhone=sharedPreferences.getString("phone",null);
        highestScore=sharedPreferences.getInt("GameScore",0);
        isVip=sharedPreferences.getBoolean("isVIP",false);
        MDetect.INSTANCE.init(this);

        setUpView();

        setTitle("Game");

        postExecutor = ContextCompat.getMainExecutor(this);


        MobileAds.initialize(this, initializationStatus -> {});

        AdView adView = findViewById(R.id.adview);
        if(!isVip){
            adView.setVisibility(View.VISIBLE);
            AdRequest request=new AdRequest.Builder().build();
            adView.loadAd(request);
            Thread timer=new Thread(){
                @Override
                public void run() {
                    super.run();
                    try {
                        sleep(30000);
                        postExecutor.execute(() -> loadAds());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };

            timer.start();

        }

    }

    private void setUpView(){
        recyclerView=findViewById(R.id.recycler);
        playerRecycler=findViewById(R.id.recyclerPlayer);
        bt_a=findViewById(R.id.bt_a);
        bt_b=findViewById(R.id.bt_b);
        bt_c=findViewById(R.id.bt_c);
        pb=findViewById(R.id.pb_loading);
        tvCurrentScore=findViewById(R.id.tv_current_score);
        tvHighestScore=findViewById(R.id.tv_highest_score);


        adapter=new GameWordAdapter(this,recyclerViewItems);
        LinearLayoutManager lm=new LinearLayoutManager(this){};
        recyclerView.setLayoutManager(lm);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

        playerAdapter=new TopGamePlayerAdapter(this,playerLists);
        LinearLayoutManager lm2=new LinearLayoutManager(this){};
        playerRecycler.setLayoutManager(lm2);
        playerRecycler.setAdapter(playerAdapter);

        bt_a.setOnClickListener(v -> checkAndLoad("a"));

        bt_b.setOnClickListener(v -> checkAndLoad("b"));

        bt_c.setOnClickListener(v -> checkAndLoad("c"));
        fetchWord();

        tvCurrentScore.setText(currentScore+"");
        tvHighestScore.setText(highestScore+"");
        fetchTopGamePlayer();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        menu.add("QUIT")
                .setIcon(R.drawable.ic_quit)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getTitle().toString().equals("QUIT")){
           confirmExit();
        }

        return super.onOptionsItemSelected(item);
    }


    private void fetchWord(){

        pb.setVisibility(View.VISIBLE);
        bt_a.setEnabled(false);
        bt_b.setEnabled(false);
        bt_c.setEnabled(false);
        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.GET, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    Log.e("GameWord : ", response);
                    postExecutor.execute(() -> {

                        doAsResult(response);
                        bt_a.setEnabled(true);
                        bt_b.setEnabled(true);
                        bt_c.setEnabled(true);
                        pb.setVisibility(View.GONE);
                    });
                }
                @Override
                public void onError(String msg) {
                    Log.e("GameWord : Error ",msg);

                }
            }).url(Routing.GET_GAME_WORD);
            myHttp.runTask();
        }).start();

    }

    @Override
    public void onBackPressed() {


    }

    private void checkAndLoad(String ansWord){
        if(ansWord.equals(ans)){
            currentScore++;
            tvCurrentScore.setText(""+currentScore);
            fetchWord();
        }else {
            if(currentScore>highestScore){
                setMarkGameScore();
                highestScore=currentScore;
                editor.putInt("GameScore",highestScore);
                editor.apply();
            }

            recyclerViewItems.clear();
            playerLists.clear();
            fetchWord();
            fetchTopGamePlayer();
            tvHighestScore.setText(""+highestScore);
            showGameOverDialog(currentScore);

        }
    }


    private void doAsResult(String response){
        try {

            JSONArray ja=new JSONArray(response);
            JSONObject jo=ja.getJSONObject(0);
            String displayWord=jo.getString("display_word");
            String displayImageUrl=jo.getString("display_image");
            String displayAudio=jo.getString("display_audio");
            String category=jo.getString("category");
            String a=jo.getString("a");
            String b=jo.getString("b");
            String c=jo.getString("c");
            ans=jo.getString("ans");

            model=new GameWordModel(displayWord,displayAudio,displayImageUrl,category,a,b,c);
            recyclerViewItems.add(model);
            adapter.notifyDataSetChanged();
            recyclerView.smoothScrollToPosition(recyclerViewItems.size()-1);

        }catch (Exception ignored){}

    }


    @SuppressLint("SetTextI18n")
    private void showGameOverDialog(int score){
        TextView tv_highest,tv_current,tvResultAns;
        Button bt_quit, bt_restart;
        CardView cvImage,cvAudio,cvText;
        ImageView ivMain;
        TextView tvA,tvB,tvC,tvDisplayWord;

        View v=getLayoutInflater().inflate(R.layout.game_result_dialog,null);
        tv_highest=v.findViewById(R.id.tv_highest);
        tv_current=v.findViewById(R.id.tv_current);
        bt_quit=v.findViewById(R.id.bt_quit);
        bt_restart=v.findViewById(R.id.bt_restart);
        cvAudio=v.findViewById(R.id.card_audio);
        cvImage=v.findViewById(R.id.card_image);
        cvText=v.findViewById(R.id.card_text);
        tvResultAns=v.findViewById(R.id.tv_resultAns);
        tvA=v.findViewById(R.id.tv_ansA);
        tvB=v.findViewById(R.id.tv_ansB);
        tvC=v.findViewById(R.id.tv_ansC);
        ivMain=v.findViewById(R.id.ivDisplayImage);
        tvDisplayWord=v.findViewById(R.id.tv_displayWord);

        tvA.setText("A - "+model.getAnsA());
        tvB.setText("B - "+model.getAnsB());
        tvC.setText("C - "+model.getAnsC());

        tvResultAns.setText("This answer is ( "+ans+" )");

        if(model.getCategory().equals("1")){
            tvDisplayWord.setText(model.getDisplayWord());
            cvText.setVisibility(View.VISIBLE);
            cvAudio.setVisibility(View.GONE);
            cvImage.setVisibility(View.GONE);

        }else if(model.getCategory().equals("2")){
            AppHandler.setPhotoFromRealUrl(ivMain,model.getDisplayImage());
            cvImage.setVisibility(View.VISIBLE);
            cvAudio.setVisibility(View.GONE);
            cvText.setVisibility(View.GONE);

        }else{
            cvAudio.setVisibility(View.VISIBLE);
            cvImage.setVisibility(View.GONE);
            cvText.setVisibility(View.GONE);

            playAudio(model.getDisplayAudio());

            cvAudio.setOnClickListener(v13 -> mPlayer.start());
        }


        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setView(v);
        final AlertDialog ad=builder.create();
        ad.setCancelable(false);

        bt_restart.setOnClickListener(v12 -> {

            tvCurrentScore.setText("0");
            currentScore=0;
            ad.dismiss();


        });

        bt_quit.setOnClickListener(v1 -> {
            if (interstitialAd != null) {
                interstitialAd.show(GammingActivity.this);
            } else {
                // Proceed to the next level.
                finish();
            }
        });

        tv_highest.setText(highestScore+"");
        tv_current.setText(score+"");

        ad.show();
    }


    private void fetchTopGamePlayer(){
        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.GET, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    postExecutor.execute(() -> {
                      try {
                          JSONArray ja=new JSONArray(response);
                          for(int i=0;i<ja.length();i++){
                              JSONObject jo=ja.getJSONObject(i);
                              String name=jo.getString("learner_name");
                              String imageUrl=jo.getString("learner_image");
                              String gameScore=jo.getString("game_score");
                              playerLists.add(new TopGamePlayerModel(name,imageUrl,gameScore));
                          }
                      }catch (Exception ignored){}

                      playerAdapter.notifyDataSetChanged();
                    });
                }
                @Override
                public void onError(String msg) {
                    postExecutor.execute(() -> Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show());

                }
            }).url(Routing.GET_GAME_SCORE);
            myHttp.runTask();
        }).start();
    }

    private void setMarkGameScore(){
        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.POST, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    postExecutor.execute(() -> {

                    });
                }
                @Override
                public void onError(String msg) {
                    postExecutor.execute(() -> {
                       // Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show();
                    });

                }
            }).field("phone",currentUserPhone)
                    .field("score",currentScore+"")
                    .url(Routing.UPDATE_GAME_SCORE);
            myHttp.runTask();
        }).start();
    }


    public void  confirmExit(){

        String title="Exit Game!";
        String msg="Your latest highest score have been saved";
        MyDialog myDialog=new MyDialog(this, title, msg, () -> {
            if(currentScore>highestScore){
                setMarkGameScore();
                highestScore=currentScore;
                editor.putInt("GameScore",highestScore);
                editor.apply();
            }
            if (interstitialAd != null) {
                interstitialAd.show(GammingActivity.this);
            } else {
                // Proceed to the next level.
                finish();

            }
        });
        myDialog.showMyDialog();
    }


    private void playAudio(String url){

        mPlayer = new MediaPlayer();
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try{

            mPlayer.setDataSource(url);
            mPlayer.prepareAsync();


        }catch (IOException e){

            e.printStackTrace();
        }catch (IllegalArgumentException e){
            e.printStackTrace();

        }catch (SecurityException e){
            e.printStackTrace();

        }catch (IllegalStateException e){
            e.printStackTrace();

        }

        mPlayer.setOnCompletionListener(mediaPlayer -> {

        });

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