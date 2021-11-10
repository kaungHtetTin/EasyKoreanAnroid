package com.calamus.easykorean;

import java.io.IOException;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import com.calamus.easykorean.service.MusicService;

public class VideoPlayerActivity extends Activity implements SurfaceHolder.Callback, MediaPlayer.OnPreparedListener, VideoControllerView.MediaPlayerControl,AudioManager.OnAudioFocusChangeListener  {

    SurfaceView videoSurface;
    MediaPlayer player;
    VideoControllerView controller;
    Uri videoUri;

    AudioManager audioManager;
    AudioAttributes playbackAttributes;
    AudioFocusRequest focusRequest;
    final Object focusLock = new Object();
    boolean playbackDelayed = false;
    boolean playbackNowAuthorized = false;
    public static boolean userPause;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        playbackAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();
        videoUri= (Uri) getIntent().getExtras().get("videoData");

        videoSurface = findViewById(R.id.videoSurface);

        autoRotateOnScreen(videoUri);

        SurfaceHolder videoHolder = videoSurface.getHolder();
        videoHolder.addCallback(this);


        AudioManager mAudioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);

        if (mAudioManager.isMusicActive()) {
            Intent i = new Intent("com.android.music.musicservicecommand");
            i.putExtra("command", "pause");
            sendBroadcast(i);
        }

        Intent intent=new Intent(this, MusicService.class);
        stopService(intent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            focusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(playbackAttributes)
                    .setAcceptsDelayedFocusGain(true)
                    .setOnAudioFocusChangeListener(this)
                    .build();


            int res = audioManager.requestAudioFocus(focusRequest);
            synchronized (focusLock) {
                if (res == AudioManager.AUDIOFOCUS_REQUEST_FAILED) {
                    playbackNowAuthorized = false;
                } else if (res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                    playbackNowAuthorized = true;
                    startingPlayer();
                } else if (res == AudioManager.AUDIOFOCUS_REQUEST_DELAYED) {
                    playbackDelayed = true;
                    playbackNowAuthorized = false;
                }

            }
        }else{
            int result = audioManager.requestAudioFocus(this,
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN);
            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                startingPlayer();
            }
        }

    }

    private void startingPlayer(){
        player = new MediaPlayer();
        controller = new VideoControllerView(this);
        try {
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            player.setDataSource(this,videoUri);
            player.setOnPreparedListener(this);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        controller.show();
        return false;
    }

    // Implement SurfaceHolder.Callback
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        player.setDisplay(holder);
        player.prepareAsync();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
    // End SurfaceHolder.Callback

    // Implement MediaPlayer.OnPreparedListener
    @Override
    public void onPrepared(MediaPlayer mp) {
        controller.setMediaPlayer(this);
        controller.setAnchorView(findViewById(R.id.videoSurfaceContainer));
        player.start();
    }
    // End MediaPlayer.OnPreparedListener

    // Implement VideoMediaController.MediaPlayerControl
    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public int getCurrentPosition() {
        return player.getCurrentPosition();
    }

    @Override
    public int getDuration() {
        return player.getDuration();
    }

    @Override
    public boolean isPlaying() {
        return player.isPlaying();
    }

    @Override
    public void pause() {
        player.pause();
    }

    @Override
    public void seekTo(int i) {
        player.seekTo(i);
    }

    @Override
    public void start() {
        player.start();
    }

    @Override
    public boolean isFullScreen() {
        return false;
    }

    @Override
    public void toggleFullScreen() {

    }


    private void autoRotateOnScreen(Uri videoUri){
        try{
            MediaMetadataRetriever retriever=new MediaMetadataRetriever();
            Bitmap bmp;
            retriever.setDataSource(this,videoUri);
            bmp=retriever.getFrameAtTime();

            int videoWidth=bmp.getWidth();
            int videoHeight=bmp.getHeight();
            if(videoWidth>videoHeight)this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            else this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

        }catch (Exception ignored){}

    }

    @Override
    public void onBackPressed() {
        player.pause();
        player.stop();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        player.stop();
        player.pause();
    }

    @Override
    public void onAudioFocusChange(int focusChange) {

    }
}
