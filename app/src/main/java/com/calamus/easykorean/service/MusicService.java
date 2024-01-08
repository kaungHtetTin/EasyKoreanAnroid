package com.calamus.easykorean.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.calamus.easykorean.R;
import com.calamus.easykorean.SongListActivity;
import com.calamus.easykorean.recivers.NotificationReceiver;
import com.calamus.easykorean.interfaces.ActionPlaying;
import com.calamus.easykorean.models.SongModel;
import java.util.ArrayList;
import static com.calamus.easykorean.ApplicationClass.ACTION_NEXT;
import static com.calamus.easykorean.ApplicationClass.ACTION_PLAY;
import static com.calamus.easykorean.ApplicationClass.ACTON_PREVIOUS;
import static com.calamus.easykorean.ApplicationClass.CHANNEL_ID_2;
import static com.calamus.easykorean.app.AppHandler.getFileByte;
import static com.calamus.easykorean.fragments.SongFragmentTwo.songLocalLists;

public class MusicService extends Service implements MediaPlayer.OnCompletionListener, AudioManager.OnAudioFocusChangeListener {
    IBinder mBinder=new MyBinder();

    MediaPlayer mediaPlayer;
    ArrayList<SongModel> musicFiles=new ArrayList<>();
    Uri uri;
    int position=-1,songPosition;
    static ActionPlaying actionPlaying;
    MediaSessionCompat mediaSessionCompat;

    Notification notification;

    AudioManager audioManager;
    AudioAttributes playbackAttributes;
    AudioFocusRequest focusRequest;
    final Object focusLock = new Object();
    boolean playbackDelayed = false;
    boolean playbackNowAuthorized = false;
    public static boolean userPause;
    @Override
    public void onCreate() {
        super.onCreate();
        mediaSessionCompat=new MediaSessionCompat(getBaseContext(),"My Audio");
        musicFiles=songLocalLists;

        audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        playbackAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }



    public class MyBinder extends Binder{
        public MusicService getService(ActionPlaying action){
            actionPlaying=action;
            return MusicService.this;
        }
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int myPosition=intent.getIntExtra("servicePosition",-1);
        String actionName=intent.getStringExtra("ActionName");
        if(myPosition!=-1){

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
                        playMedia(myPosition);
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
                    playMedia(myPosition);
                }
            }

        }

        if(actionName!=null){
            switch (actionName){
                case "playPause":

                    if(actionPlaying!=null){
                        actionPlaying.playPauseButtonClick();
                    }

                    break;

                case "next":
                    if(actionPlaying!=null){
                        actionPlaying.nextButtonClick();
                    }
                    break;

                case "previous":
                    if(actionPlaying!=null){
                        actionPlaying.prevButtonClick();
                    }
                    break;
            }
        }
        return  START_STICKY;
    }

    private void playMedia(int startPostion) {
        userPause=false;
        musicFiles=songLocalLists;
        position=startPostion;
        if(mediaPlayer!=null){
            mediaPlayer.stop();
            mediaPlayer.release();
            if(musicFiles!=null){
                createMediaPlayer(position);
                mediaPlayer.start();
            }
        }else{
            createMediaPlayer(position);
            mediaPlayer.start();
        }
    }

    public void start(){
        mediaPlayer.start();
    }
    public boolean isPlaying(){
        return  mediaPlayer.isPlaying();
    }

    public void stop(){
        mediaPlayer.stop();
    }

    public void release(){
        mediaPlayer.release();
    }

    public  int getDuration(){
        return mediaPlayer.getDuration();
    }

    public int getCurrentPosition(){
        if(mediaPlayer!=null)
        return mediaPlayer.getCurrentPosition();
        else return 0;
    }

    public void pause(){
        mediaPlayer.pause();
    }


    public void seekTo(int position){
        mediaPlayer.seekTo(position);
    }
    public void createMediaPlayer(int position){

        songPosition=position;
        uri=musicFiles.get(position).getUri();
        mediaPlayer=MediaPlayer.create(getBaseContext(),uri);
    }

    public void onCompleted(){
        mediaPlayer.setOnCompletionListener(this);
    }


    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        if(actionPlaying!=null){
            actionPlaying.nextButtonClick();
        }

        onCompleted();
    }


    public void showNotification(int playPauseBtn,String p){



        Intent intent=new Intent(this, SongListActivity.class);
        PendingIntent contentIntent=PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_IMMUTABLE);

        Intent prevIntent=new Intent(this,NotificationReceiver.class)
                .setAction(ACTON_PREVIOUS);
        PendingIntent prevPending=PendingIntent.getBroadcast(this,0,prevIntent,PendingIntent.FLAG_IMMUTABLE);

        Intent pauseIntent=new Intent(this,NotificationReceiver.class)
                .setAction(ACTION_PLAY);
        PendingIntent pausePending=PendingIntent.getBroadcast(this,0,pauseIntent,PendingIntent.FLAG_IMMUTABLE);

        Intent nextIntent=new Intent(this, NotificationReceiver.class)
                .setAction(ACTION_NEXT);
        PendingIntent nextPending=PendingIntent.getBroadcast(this,0,nextIntent,PendingIntent.FLAG_IMMUTABLE);

        byte[] picture;
        String tempName=musicFiles.get(position).getTitle();
        String songFileName=tempName.substring(0,tempName.length()-4);
        picture=getFileByte(songFileName+".png",getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath());
        Bitmap thumb;
        if(picture!=null){
            thumb= BitmapFactory.decodeByteArray(picture,0,picture.length);
        }else{
            thumb=BitmapFactory.decodeResource(getResources(), R.drawable.bg_player);
        }

        notification=new NotificationCompat.Builder(this,CHANNEL_ID_2)
                .setSmallIcon(playPauseBtn)
                .setLargeIcon(thumb)
                .setContentTitle(musicFiles.get(songPosition).getTitle())
                .addAction(R.drawable.ic_baseline_skip_previous_24,"Previous",prevPending)
                .addAction(playPauseBtn,p,pausePending)
                .addAction(R.drawable.ic_baseline_skip_next_24,"Next",nextPending)
                .setContentIntent(contentIntent)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setDefaults(0)
                .build();

        startForeground();


    }

    public void startForeground(){
        startForeground(1,notification);
    }

    public void stopForeground(){
        stopForeground(false);
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer=null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {

        super.onTaskRemoved(rootIntent);
        stopForeground(true);
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer=null;
        this.stopSelf();

    }



    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                if(mediaPlayer!=null&&!userPause)actionPlaying.playClick();

                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                if(mediaPlayer!=null&&isPlaying())actionPlaying.pauseClick();
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                stopForeground(true);
                this.stopSelf();

                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // ... pausing or ducking depends on your app
                break;
        }
    }

}
