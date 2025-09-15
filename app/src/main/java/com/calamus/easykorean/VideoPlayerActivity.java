package com.calamus.easykorean;

import static com.calamus.easykorean.adapters.SavedVideoAdapter.relatedVideos;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;
import com.calamus.easykorean.models.SavedVideoModel;
import com.calamus.easykorean.service.MusicService;

import java.util.ArrayList;
import java.util.List;

public class VideoPlayerActivity extends Activity implements AudioManager.OnAudioFocusChangeListener, View.OnClickListener {

    private PlayerView playerView;
    private ExoPlayer player;
    private TextView title;
    private ImageView videoBack;

    private AudioManager audioManager;
    private AudioAttributes playbackAttributes;
    private AudioFocusRequest focusRequest;

    private String videoTitle;
    private Uri videoUri;
    int play_list_index;

    private long playbackPosition = 0;
    private boolean playWhenReady = true;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        initViews();
        initAudioFocus();
        getIntentData();

        autoRotateScreen(videoUri);
        stopBackgroundMusic();

        if (savedInstanceState != null) {
            playbackPosition = savedInstanceState.getLong("playbackPosition", 0);
            playWhenReady = savedInstanceState.getBoolean("playWhenReady", true);
            play_list_index = savedInstanceState.getInt("currentWindowIndex", 0);
        }

        initPlayer();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (player != null) {
            outState.putLong("playbackPosition", player.getCurrentPosition());
            outState.putBoolean("playWhenReady", player.getPlayWhenReady());
            outState.putInt("currentWindowIndex", player.getCurrentMediaItemIndex());
        }
    }

    @OptIn(markerClass = UnstableApi.class)
    private void initViews() {
        playerView = findViewById(R.id.exoplayer_view);
        title = findViewById(R.id.video_title);
        videoBack = findViewById(R.id.video_back);
        videoBack.setOnClickListener(this);
    }

    private void getIntentData() {
        videoUri = getIntent().getParcelableExtra("videoData");
        videoTitle = getIntent().getStringExtra("title");
        play_list_index = getIntent().getIntExtra("play_list_index",0);
        title.setText(videoTitle);
    }

    private void stopBackgroundMusic() {
        AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (mAudioManager.isMusicActive()) {
            Intent i = new Intent("com.android.music.musicservicecommand");
            i.putExtra("command", "pause");
            sendBroadcast(i);
        }
        Intent intent = new Intent(this, MusicService.class);
        stopService(intent);
    }

    private void autoRotateScreen(Uri uri) {
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(this, uri);
            Bitmap bmp = retriever.getFrameAtTime();
            int videoWidth = bmp.getWidth();
            int videoHeight = bmp.getHeight();
            if (videoWidth > videoHeight)
                this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            else
                this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        } catch (Exception ignored) {}
    }

    private void initAudioFocus() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        playbackAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MOVIE)
                .build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            focusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(playbackAttributes)
                    .setAcceptsDelayedFocusGain(true)
                    .setOnAudioFocusChangeListener(this)
                    .build();
            audioManager.requestAudioFocus(focusRequest);
        } else {
            audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        }
    }

    @OptIn(markerClass = UnstableApi.class)
    private void initPlayer() {
        player = new ExoPlayer.Builder(this)
                .setSeekBackIncrementMs(10000)
                .setSeekForwardIncrementMs(10000)
                .build();

        playerView.setPlayer(player);
        playerView.setKeepScreenOn(true);

        List<MediaItem> mediaItems = new ArrayList();
        for(int i = 0;i<relatedVideos.size();i++){
            SavedVideoModel model = relatedVideos.get(i);
            Uri uri= model.getUri();
            mediaItems.add(MediaItem.fromUri(uri));
        }
        player.setMediaItems(mediaItems);
        player.seekTo(play_list_index, playbackPosition);
        player.setPlayWhenReady(playWhenReady);
        player.prepare();
        player.play();

        player.addListener(new androidx.media3.common.Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if (playbackState == Player.STATE_ENDED) {

                }
            }

            @Override
            public void onPlayerError(PlaybackException error) {
                Toast.makeText(VideoPlayerActivity.this, "Video playback error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onClick(View view) {
        if (view == videoBack) {
            releasePlayer();
            finish();
        }
    }

    private void releasePlayer() {
        if (player != null) {
            player.release();
            player = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (player != null) player.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (player != null) player.play();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releasePlayer();
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        if (player == null) return;
        if (focusChange <= AudioManager.AUDIOFOCUS_LOSS) {
            player.pause();
        } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
            player.play();
        }
    }
}

