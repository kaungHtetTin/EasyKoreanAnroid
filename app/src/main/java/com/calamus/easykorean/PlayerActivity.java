package com.calamus.easykorean;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.palette.graphics.Palette;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.calamus.easykorean.interfaces.ActionPlaying;
import com.calamus.easykorean.service.MusicService;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import me.myatminsoe.mdetect.MDetect;
import static com.calamus.easykorean.app.AppHandler.getFileByte;
import static com.calamus.easykorean.app.AppHandler.setMyanmar;
import static com.calamus.easykorean.fragments.SongFragmentTwo.songLocalLists;


public class PlayerActivity extends AppCompatActivity implements
        ActionPlaying, ServiceConnection {

    TextView tv_songName,tv_artistName,tv_durationPlayed,tv_durationTotal,tv_lyrics;
    ImageView iv_coverArt,iv_nextBtn,iv_preBtn,iv_backBtn,iv_shuffleBtn,iv_repeatBtn,iv_playPause;
    SeekBar seekBar;
    CardView card_playPause;
    int position=-1;
    static Uri uri;
    private Handler handler=new Handler();
    private Thread playThread,preThread,nextThread;
    MusicService musicService;
    boolean isActivityDestroy;
    public static boolean shuffleBoolean;
    public static boolean repeatBoolean;
    boolean lyricClick=false;
    Animation animOut;
    Animation animIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        MDetect.INSTANCE.init(this);
        getSupportActionBar().hide();
        isActivityDestroy=false;
        animOut= AnimationUtils.loadAnimation(this, android.R.anim.fade_out);
        animIn= AnimationUtils.loadAnimation(this, android.R.anim.fade_in);

        initViews();
        getIntentMethod();

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(musicService!=null&& b){
                    musicService.seekTo(i*1000);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        PlayerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(musicService!=null){
                    int mCurrentPosition=musicService.getCurrentPosition()/1000;
                    seekBar.setProgress(mCurrentPosition);
                    tv_durationPlayed.setText(formattedTime(mCurrentPosition));

                }
                handler.postDelayed(this,1000);
            }
        });

        iv_shuffleBtn.setOnClickListener(view -> {
            if(shuffleBoolean){
                shuffleBoolean=false;
                iv_shuffleBtn.setImageResource(R.drawable.ic_baseline_shuffle_off);
            }else{
                shuffleBoolean=true;
                iv_shuffleBtn.setImageResource(R.drawable.ic_baseline_shuffle_on);
            }
        });

        iv_repeatBtn.setOnClickListener(view -> {
            if(repeatBoolean){
                repeatBoolean=false;
                iv_repeatBtn.setImageResource(R.drawable.ic_baseline_repeat_off);
            }else{
                repeatBoolean=true;
                iv_repeatBtn.setImageResource(R.drawable.ic_baseline_repeat_one);
            }
        });

        tv_lyrics.setOnClickListener(view -> {
            if(!lyricClick){
                iv_coverArt.setAnimation(animOut);
                iv_coverArt.setVisibility(View.GONE);
                lyricClick=true;
            }else{
                iv_coverArt.setVisibility(View.VISIBLE);
                iv_coverArt.setAnimation(animIn);
                lyricClick=false;
            }
        });

        iv_backBtn.setOnClickListener(view -> {
            finish();
        });

    }

    private void initViews(){
        tv_songName=findViewById(R.id.tv_songName);
        tv_artistName=findViewById(R.id.tv_songArtist);
        tv_durationPlayed=findViewById(R.id.tv_durationPlayed);
        tv_durationTotal=findViewById(R.id.tv_durationTotal);
        iv_coverArt=findViewById(R.id.cover_art);
        iv_nextBtn=findViewById(R.id.iv_next);
        iv_preBtn=findViewById(R.id.iv_previous);
        iv_backBtn=findViewById(R.id.back_btn);
        iv_shuffleBtn=findViewById(R.id.iv_shuffle_off);
        iv_repeatBtn=findViewById(R.id.iv_repeat_on_off);
        iv_playPause=findViewById(R.id.play_pause);
        seekBar=findViewById(R.id.seekbar);
        card_playPause=findViewById(R.id.card_play_pause);
        tv_lyrics=findViewById(R.id.tv_lyrics);
    }


    private String formattedTime(int mCurrentPosition) {
        String totalout;
        String totalNew;
        String seconds=String.valueOf(mCurrentPosition%60);
        String minutes=String.valueOf(mCurrentPosition/60);
        totalout=minutes+":"+seconds;
        totalNew=minutes+":"+"0"+seconds;
        if(seconds.length()==1)return totalNew;
        else return totalout;
    }

    @Override
    protected void onResume() {
        Intent intent=new Intent(this,MusicService.class);
        bindService(intent,this,BIND_AUTO_CREATE);
        prevThreadBtn();
        nextThreadBtn();
        playThreadBtn();
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(this);
    }

    private void nextThreadBtn() {
        nextThread=new Thread(){
            @Override
            public void run() {
                super.run();
                iv_nextBtn.setOnClickListener(view -> nextButtonClick());
            }
        };
        nextThread.start();
    }

    public void nextButtonClick() {
        musicService.stop();
        musicService.release();

        if(shuffleBoolean &&!repeatBoolean){
            position=getRandom(songLocalLists.size()-1);
        }else if(!shuffleBoolean&&!repeatBoolean){
            position=((position+1)%songLocalLists.size());
        }

        uri=songLocalLists.get(position).getUri();
        musicService.createMediaPlayer(position);

        if(!isActivityDestroy)metaData();
        seekBar.setMax(musicService.getDuration()/1000);
        PlayerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(musicService!=null){
                    int mCurrentPosition=musicService.getCurrentPosition()/1000;
                    seekBar.setProgress(mCurrentPosition);
                }
                handler.postDelayed(this,1000);
            }
        });
        musicService.onCompleted();
        iv_playPause.setImageResource(R.drawable.ic_baseline_pause_24);
        musicService.start();
        musicService.showNotification(R.drawable.ic_baseline_pause_24,"Pause");

    }

    @Override
    public void playClick() {

        iv_playPause.setImageResource(R.drawable.ic_baseline_pause_24);
        musicService.start();
        musicService.showNotification(R.drawable.ic_baseline_pause_24,"Pause");

        seekBar.setMax(musicService.getDuration()/1000);
        PlayerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(musicService!=null){
                    int mCurrentPosition=musicService.getCurrentPosition()/1000;
                    seekBar.setProgress(mCurrentPosition);
                }
                handler.postDelayed(this,1000);
            }
        });
        musicService.onCompleted();
    }

    @Override
    public void pauseClick() {

        iv_playPause.setImageResource(R.drawable.ic_baseline_play_arrow_24);
        musicService.pause();
        musicService.showNotification(R.drawable.ic_baseline_play_arrow_24,"Play");

        seekBar.setMax(musicService.getDuration()/1000);
        PlayerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(musicService!=null){
                    int mCurrentPosition=musicService.getCurrentPosition()/1000;
                    seekBar.setProgress(mCurrentPosition);
                }
                handler.postDelayed(this,1000);
            }
        });
        musicService.onCompleted();
    }

    private int getRandom(int i) {
        Random random=new Random();
        return  random.nextInt(i+1);
    }

    private void prevThreadBtn() {
        preThread=new Thread(){
            @Override
            public void run() {
                super.run();
                iv_preBtn.setOnClickListener(view -> prevButtonClick());
            }
        };
        preThread.start();
    }

    public void prevButtonClick() {
        musicService.stop();
        musicService.release();

        if(shuffleBoolean &&!repeatBoolean){
            position=getRandom(songLocalLists.size()-1);
        }else if(!shuffleBoolean&&!repeatBoolean){
            position=((position-1)<0?(songLocalLists.size()-1):position-1);
        }

        uri=songLocalLists.get(position).getUri();
        musicService.createMediaPlayer(position);
        if(!isActivityDestroy)metaData();
       // tv_artistName.setText(songLocalLists.get(position).getArtist());
        seekBar.setMax(musicService.getDuration()/1000);
        PlayerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(musicService!=null){
                    int mCurrentPosition=musicService.getCurrentPosition()/1000;
                    seekBar.setProgress(mCurrentPosition);
                }
                handler.postDelayed(this,1000);
            }
        });
        musicService.onCompleted();
        iv_playPause.setImageResource(R.drawable.ic_baseline_pause_24);
        musicService.start();

        musicService.showNotification(R.drawable.ic_baseline_pause_24,"Pause");

    }

    private void playThreadBtn() {
        playThread=new Thread(){
            @Override
            public void run() {
                super.run();
                card_playPause.setOnClickListener(view -> playPauseButtonClick());
            }
        };
        playThread.start();
    }

    public void playPauseButtonClick() {
        if(musicService.isPlaying()){
            iv_playPause.setImageResource(R.drawable.ic_baseline_play_arrow_24);
            musicService.pause();
            musicService.showNotification(R.drawable.ic_baseline_play_arrow_24,"Play");
            MusicService.userPause=true;
            musicService.stopForeground();
        }else{
            iv_playPause.setImageResource(R.drawable.ic_baseline_pause_24);
            musicService.start();
            musicService.showNotification(R.drawable.ic_baseline_pause_24,"Pause");
            MusicService.userPause=false;
            musicService.startForeground();
        }
        seekBar.setMax(musicService.getDuration()/1000);
        PlayerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(musicService!=null){
                    int mCurrentPosition=musicService.getCurrentPosition()/1000;
                    seekBar.setProgress(mCurrentPosition);
                }
                handler.postDelayed(this,1000);
            }
        });
        musicService.onCompleted();

    }


    private void getIntentMethod() {
        position=getIntent().getExtras().getInt("position",-1);
        iv_playPause.setImageResource(R.drawable.ic_baseline_pause_24);
        uri=songLocalLists.get(position).getUri();
        Intent intent=new Intent(this,MusicService.class);
        intent.putExtra("servicePosition",position);
        startService(intent);

    }


    private void metaData(){

        int durationTotal=songLocalLists.get(position).getDuration()/1000;
        tv_durationTotal.setText(formattedTime(durationTotal));

        String tempName=songLocalLists.get(position).getTitle();
        String songFileName=tempName.substring(0,tempName.length()-4);
        String songName=songFileName.substring(0,songFileName.indexOf("("));
        String artistName=songFileName.substring(songFileName.indexOf("(")+1,songFileName.length()-1);
        tv_songName.setText(songName);
        tv_artistName.setText(artistName);
        iv_coverArt.setVisibility(View.VISIBLE);
        lyricClick=false;

        byte[] lyricsArr=getFileByte(songFileName+".txt",getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath());

        if(lyricsArr!=null){
            String lyrics=new String(lyricsArr, StandardCharsets.UTF_8);
            tv_lyrics.setText(setMyanmar(lyrics));
        }else{
            tv_lyrics.setText("");
        }


        Bitmap bitmap;
        byte[] art=getFileByte(songFileName+".png",getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath());
        if(art!=null){

            bitmap = BitmapFactory.decodeByteArray(art,0,art.length);
            ImageAnimation(this,iv_coverArt,bitmap);
            Palette.from(bitmap).generate(palette -> {
                Palette.Swatch swatch=palette.getDominantSwatch();

                if(swatch!=null){
                    ImageView gradient=findViewById(R.id.ImageViewGradient);
                    RelativeLayout mContainer=findViewById(R.id.mContainer);
                    gradient.setBackgroundResource(R.drawable.bg_player_gradient);
                    mContainer.setBackgroundResource(R.drawable.bg_player);
                    GradientDrawable gradientDrawable=new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                            new int[]{swatch.getRgb(),0x00000000});
                    gradient.setBackground(gradientDrawable);

                    GradientDrawable gradientDrawableBg=new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                            new int[]{swatch.getRgb(),swatch.getRgb()});
                    mContainer.setBackground(gradientDrawableBg);
                    tv_songName.setTextColor(swatch.getTitleTextColor());
                    tv_artistName.setTextColor(swatch.getBodyTextColor());
                    tv_lyrics.setTextColor(swatch.getBodyTextColor());
                }
            });
        }else{
            Glide.with(this)
                    .asBitmap()
                    .load(R.drawable.bg_music)
                    .into(iv_coverArt);

            ImageView gradient=findViewById(R.id.ImageViewGradient);
            RelativeLayout mContainer=findViewById(R.id.mContainer);
            gradient.setBackgroundResource(R.drawable.bg_player_gradient);
            mContainer.setBackgroundResource(R.drawable.bg_player);

            tv_songName.setTextColor(Color.WHITE);
            tv_artistName.setTextColor(Color.GRAY);

        }
    }

    public void ImageAnimation(Context context, ImageView imageView, Bitmap bitmap){


        animOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Glide.with(context).load(bitmap).into(imageView);
                animIn.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {}

                    @Override
                    public void onAnimationEnd(Animation animation) {}

                    @Override
                    public void onAnimationRepeat(Animation animation) {}
                });

                imageView.setAnimation(animIn);
                tv_lyrics.setAnimation(animIn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        imageView.startAnimation(animOut);
        tv_lyrics.startAnimation(animOut);
    }




    @Override
    public void onServiceConnected(ComponentName componentName, IBinder service) {
        MusicService.MyBinder myBinder=(MusicService.MyBinder)service;
        musicService=myBinder.getService(this);
        musicService.onCompleted();
        seekBar.setMax(musicService.getDuration()/1000);
        if(!isActivityDestroy)metaData();
        musicService.showNotification(R.drawable.ic_baseline_pause_24,"Pause");
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        musicService=null;
    }


    @Override
    protected void onDestroy() {
        isActivityDestroy=true;
        super.onDestroy();
    }

}