package com.calamus.easykorean;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.material.snackbar.Snackbar;
import com.calamus.easykorean.app.AppHandler;
import com.calamus.easykorean.app.Downloader;
import com.calamus.easykorean.app.Routing;
import com.calamus.easykorean.controller.LikeController;
import com.calamus.easykorean.interfaces.DownloadComplete;
import com.calamus.easykorean.service.DownloaderService;
import com.calamus.easykorean.service.MusicService;
import com.calamus.easykorean.app.MyHttp;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executor;
import static com.calamus.easykorean.app.AppHandler.reactFormat;
import static com.calamus.easykorean.app.AppHandler.setPhotoFromRealUrl;


public class SongDetailActivity extends AppCompatActivity implements DownloadComplete {

    String id,title,artist,likeCount,downloadCount,url,lyricUrl,audioUrl,imageUrl,currentUserId;

    ImageView iv_songImage,iv_react,iv_downloaded,iv_isPlaying,iv_back;
    TextView tv_title,tv_artist,tv_reactCount,tv_downloadCount,tv_lyrics;
    CardView card_play,card_download;
    ProgressBar pb;
    boolean isLike;
    int rectCount;
    ViewGroup main;

    Animation animOut;
    Animation animIn;
    Executor postExecutor;
    boolean isVip,alreadyDownloaded;
    SharedPreferences sharedPreferences;
    MediaPlayer mediaPlayer;
    private InterstitialAd mInterstitialAd=null;

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

        getSupportActionBar().hide();


        setUpView();

        getSongLyrics();
        setMediaPlayer();

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
                    mInterstitialAd.show(SongDetailActivity.this);
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

    private void setUpView(){
        iv_songImage=findViewById(R.id.songImage);
        iv_react=findViewById(R.id.iv_react);
        iv_back=findViewById(R.id.iv_back);
        tv_title=findViewById(R.id.tv_info_header);
        tv_artist=findViewById(R.id.tv_description);
        tv_reactCount=findViewById(R.id.tv_react);
        tv_downloadCount=findViewById(R.id.tv_downloadCount);
        iv_downloaded=findViewById(R.id.iv_downloadCheck);
        iv_isPlaying=findViewById(R.id.iv_isPlaying);
        tv_lyrics=findViewById(R.id.tv_lyrics);
        main=findViewById(R.id.main);
        pb=findViewById(R.id.pb_download);
        card_play=findViewById(R.id.card_play);
        card_download=findViewById(R.id.card_download);

        tv_artist.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        tv_artist.setMarqueeRepeatLimit(-1);
        tv_artist.setSingleLine(true);
        tv_artist.setSelected(true);

        tv_title.setText(title);
        tv_artist.setText(artist);
        iv_songImage.setClipToOutline(true);
        setPhotoFromRealUrl(iv_songImage,imageUrl);


        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer=null;
                finish();
            }
        });

        if(rectCount==0)tv_reactCount.setText("");
        else tv_reactCount.setText(reactFormat(rectCount));
        iv_downloaded.setVisibility(View.GONE);



        int downloads=Integer.parseInt(downloadCount);
        tv_downloadCount.setText(AppHandler.downloadFormat(downloads));

        iv_react.setBackgroundResource(R.drawable.ic_song_normal_react);
        if(isLike){
            iv_react.setBackgroundResource(R.drawable.ic_react_love);
        }


        iv_react.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(isLike){
                    iv_react.setBackgroundResource(R.drawable.ic_song_normal_react);
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
            }
        });


        File image= new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES).getPath(),url+".png");

        File lyric=new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).getPath(),url+".txt");

        card_download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                increaseDownloadCount(id);
                new Downloader(imageUrl,image).start();
                new Downloader(lyricUrl,lyric).start();

                Intent intent=new Intent(SongDetailActivity.this, DownloaderService.class);
                intent.putExtra("dir",getExternalFilesDir(Environment.DIRECTORY_MUSIC).getPath());
                intent.putExtra("filename",url+".mp3");
                intent.putExtra("downloadUrl",audioUrl);
                intent.putExtra("intentMessage","downloadSong");
                startService(intent);
                setSnackBar("Start Downloading");
            }
        });
    }

    private void setSnackBar(String s){
        final Snackbar sb=Snackbar.make(main,s,Snackbar.LENGTH_INDEFINITE);
        sb.setAction("View", v -> startActivity(new Intent(SongDetailActivity.this,
                        DownloadingListActivity.class)))
                .setActionTextColor(Color.WHITE)
                .show();
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

        card_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(SongDetailActivity.this, MusicService.class);
                stopService(intent);
                mediaPlayer.start();
                iv_downloaded.setAnimation(animOut);
                iv_downloaded.setVisibility(View.GONE);
                iv_isPlaying.setVisibility(View.VISIBLE);
                iv_isPlaying.setAnimation(animIn);
            }
        });
    }

    @Override
    public void actionOnDownloadComplete() {
        postExecutor.execute(new Runnable() {
            @Override
            public void run() {
                pb.setAnimation(animOut);
                pb.setVisibility(View.GONE);
                iv_isPlaying.setAnimation(animOut);
                iv_isPlaying.setVisibility(View.GONE);
                iv_downloaded.setVisibility(View.VISIBLE);
                iv_downloaded.setAnimation(animIn);

            }
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
    public void onBackPressed() {

        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer=null;
        super.onBackPressed();
    }

    private void getSongLyrics(){
        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.GET, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    postExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            tv_lyrics.setText(response);
                        }
                    });
                }
                @Override
                public void onError(String msg) {
                    postExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            tv_lyrics.setText(msg);
                        }
                    });
                }
            }).url(Routing.GET_SONG_LYRICS+url);
            myHttp.runTask();
        }).start();
    }

}