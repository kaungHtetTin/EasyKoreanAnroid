package com.calamus.easykorean;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.calamus.easykorean.app.AppHandler;
import com.calamus.easykorean.app.Downloader;
import com.calamus.easykorean.app.MyHttp;
import com.calamus.easykorean.app.Routing;
import com.calamus.easykorean.controller.LikeController;
import com.calamus.easykorean.interfaces.DownloadComplete;
import com.calamus.easykorean.service.DownloaderService;
import com.calamus.easykorean.service.MusicService;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.Executor;

import static com.calamus.easykorean.app.AppHandler.reactFormat;
import static com.calamus.easykorean.app.AppHandler.setPhotoFromRealUrl;

public class SongDetailActivity extends AppCompatActivity implements DownloadComplete {

    String id,title,artist,likeCount,downloadCount,url,lyricUrl,audioUrl,imageUrl,currentUserId;

    ImageView iv_songImage,iv_react,iv_downloaded,iv_isPlaying;
    TextView tv_title,tv_artist,tv_reactCount,tv_downloadCount,tv_lyrics;
    CardView cardReact;
    Button bt_download,bt_play;
    ProgressBar pb;
    boolean isLike;
    int rectCount;

    Animation animOut;
    Animation animIn;
    private InterstitialAd interstitialAd;
    Executor postExecutor;
    boolean isVip,alreadyDownloaded;
    SharedPreferences sharedPreferences;

    MediaPlayer mediaPlayer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_detail);
        sharedPreferences=getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        isVip=sharedPreferences.getBoolean("isVIP",false);
        alreadyDownloaded=false;
        id=getIntent().getStringExtra("id");
        title=getIntent().getStringExtra("title");
        artist=getIntent().getStringExtra("artist");
        likeCount=getIntent().getStringExtra("likeCount");
        downloadCount=getIntent().getStringExtra("downloadCount");
        url=getIntent().getStringExtra("url");
        isLike=getIntent().getBooleanExtra("isLike",false);
        currentUserId=getIntent().getStringExtra("userId");
        rectCount=Integer.parseInt(likeCount);
        lyricUrl="https://www.calamuseducation.com/uploads/songs/lyrics/"+url+".txt";
        audioUrl="https://www.calamuseducation.com/uploads/songs/audio/"+url+".mp3";
        imageUrl="https://www.calamuseducation.com/uploads/songs/image/"+url+".png";

        animOut= AnimationUtils.loadAnimation(this, android.R.anim.fade_out);
        animIn= AnimationUtils.loadAnimation(this, android.R.anim.fade_in);

        postExecutor= ContextCompat.getMainExecutor(this);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        MobileAds.initialize(this, initializationStatus -> {});


        setUpView();

        AdView adView = findViewById(R.id.web_adview);
        getSongLyrics();
        setMediaPlayer();

        if(!isVip){
            adView.setVisibility(View.VISIBLE);
            AdRequest request=new AdRequest.Builder().build();
            adView.loadAd(request);
            loadAds();
        }
    }

    private void setUpView(){
        iv_songImage=findViewById(R.id.songImage);
        iv_react=findViewById(R.id.iv_react);
        tv_title=findViewById(R.id.tv_title);
        tv_artist=findViewById(R.id.tv_artist);
        tv_reactCount=findViewById(R.id.tv_reactCount);
        tv_downloadCount=findViewById(R.id.tv_downloadCount);
        cardReact=findViewById(R.id.card_react);
        bt_download=findViewById(R.id.bt_download);
        bt_play=findViewById(R.id.bt_play);
        iv_downloaded=findViewById(R.id.iv_downloadCheck);
        iv_isPlaying=findViewById(R.id.iv_isPlaying);
        tv_lyrics=findViewById(R.id.tv_lyrics);
        pb=findViewById(R.id.pb_download);

        tv_title.setText(title);
        tv_artist.setText(artist);
        setPhotoFromRealUrl(iv_songImage,imageUrl);

        if(rectCount==0)tv_reactCount.setText("");
        else tv_reactCount.setText(reactFormat(rectCount));


        int downloads=Integer.parseInt(downloadCount);
        tv_downloadCount.setText(AppHandler.downloadFormat(downloads));

        iv_react.setBackgroundResource(R.drawable.ic_normal_react);
        if(isLike){
            iv_react.setBackgroundResource(R.drawable.ic_react_love);
        }


        cardReact.setOnClickListener(view -> {

            if(isLike){
                iv_react.setBackgroundResource(R.drawable.ic_normal_react);
                rectCount--;
                if(rectCount>0)tv_reactCount.setText(reactFormat(rectCount));
                else tv_reactCount.setText("");
                isLike=false;

            }else{
                iv_react.setBackgroundResource(R.drawable.ic_react_love);
                rectCount++;
                tv_reactCount.setText(reactFormat(rectCount));
                isLike=true;
            }

            LikeController.likeTheSong(currentUserId,id);
        });


        File image= new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES).getPath(),url+".png");
        File lyric=new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).getPath(),url+".txt");

        bt_download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                increaseDownloadCount(id);

                new Downloader(imageUrl,image).start();
                new Downloader(lyricUrl,lyric).start();

                Toast.makeText(getApplicationContext(),"Start downloading",Toast.LENGTH_SHORT).show();
                Intent intent=new Intent(SongDetailActivity.this, DownloaderService.class);
                intent.putExtra("dir",getExternalFilesDir(Environment.DIRECTORY_MUSIC).getPath());
                intent.putExtra("filename",url+".mp3");
                intent.putExtra("downloadUrl",audioUrl);
                startService(intent);
            }
        });
    }


    private void setMediaPlayer(){
        mediaPlayer=new MediaPlayer();
        mediaPlayer.setAudioAttributes(
                new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build()
        );
        try {
            mediaPlayer.setDataSource(audioUrl);
            mediaPlayer.prepareAsync();

        } catch (IOException e) {
            e.printStackTrace();
        }

        bt_play.setOnClickListener(view -> {
            Intent intent=new Intent(SongDetailActivity.this, MusicService.class);
            stopService(intent);
            mediaPlayer.start();
            iv_downloaded.setAnimation(animOut);
            iv_downloaded.setVisibility(View.GONE);
            iv_isPlaying.setVisibility(View.VISIBLE);
            iv_isPlaying.setAnimation(animIn);
        });
    }

    @Override
    public void actionOnDownloadComplete() {
        postExecutor.execute(() -> {
            pb.setAnimation(animOut);
            pb.setVisibility(View.GONE);
            iv_isPlaying.setAnimation(animOut);
            iv_isPlaying.setVisibility(View.GONE);
            iv_downloaded.setVisibility(View.VISIBLE);
            iv_downloaded.setAnimation(animIn);

        });
    }

    private void increaseDownloadCount(String songid){
        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.POST, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {}
                @Override
                public void onError(String msg) {}
            }).url(Routing.DOWNLOAD_A_SONG)
                    .field("song_id",songid);
            myHttp.runTask();
        }).start();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer=null;
            if (interstitialAd != null) {
                interstitialAd.show(SongDetailActivity.this);
            } else {
                // Proceed to the next level.
                finish();

            }
        }
        return super.onOptionsItemSelected(item);
    }


    private void loadAds(){

        FullScreenContentCallback fullScreenContentCallback = new FullScreenContentCallback() {
            @Override
            public void onAdDismissedFullScreenContent() {
                interstitialAd = null;
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

    @Override
    public void onBackPressed() {

        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer=null;
        if (interstitialAd != null) {
            interstitialAd.show(SongDetailActivity.this);
        } else {
            super.onBackPressed();
        }
    }

    private void getSongLyrics(){
        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.GET, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    postExecutor.execute(() -> tv_lyrics.setText(response));
                }
                @Override
                public void onError(String msg) {
                    postExecutor.execute(() -> tv_lyrics.setText("Please check your internet connection!"));
                }
            }).url(Routing.GET_SONG_LYRICS+url);
            myHttp.runTask();
        }).start();
    }

}