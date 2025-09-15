package com.calamus.easykorean;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.media3.common.MediaItem;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.calamus.easykorean.app.FileManager;
import com.calamus.easykorean.app.MyImagePicker;
import com.calamus.easykorean.app.WebAppInterface;
import com.calamus.easykorean.models.FileModel;
import com.calamus.easykorean.models.SavedVideoModel;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.hbisoft.pickit.PickiT;
import com.hbisoft.pickit.PickiTCallbacks;
import com.calamus.easykorean.adapters.RelativeLessonAdapter;
import com.calamus.easykorean.adapters.VideoCommentAdapter;
import com.calamus.easykorean.app.AppHandler;
import com.calamus.easykorean.app.Routing;
import com.calamus.easykorean.controller.LikeController;
import com.calamus.easykorean.controller.MyCommentController;
import com.calamus.easykorean.controller.NotificationController;
import com.calamus.easykorean.models.CommentModel;
import com.calamus.easykorean.models.LessonModel;
import com.calamus.easykorean.service.MusicService;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.Executor;
import static com.calamus.easykorean.app.AppHandler.formatTime;
import static com.calamus.easykorean.app.AppHandler.reactFormat;
import static com.calamus.easykorean.app.AppHandler.viewCountFormat;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.calamus.easykorean.app.MyHttp;

public class VimeoPlayerActivity extends AppCompatActivity implements AudioManager.OnAudioFocusChangeListener,PickiTCallbacks,View.OnClickListener {

    WebView wv;
    TextView tv_title, tv_mini_title, tv_react, tv_comment, tv_share, tv_view, tv_related_lesson,tv_fullscreen,
            tv_description;
    RecyclerView recyclerViewLesson;
    ConstraintLayout vimeoLayout;
    ProgressBar pb_vimeo, pb_video_frame;

    String videoId1, videoTitle, relatedLesson, post_description;
    String currentUserId, currentUserName;
    SharedPreferences sharedPreferences;
    ProgressBar pb;
    ViewGroup main;
    boolean isVIP,portrait_video=false;
    ImageView iv_fullscreen;

    long a;
    String isVip = "0";

    String isLiked, postLikes;
    String timeCheck = "";

    int reactCount;
    String videoUrl1 = "", folderName;
    Executor postExecutor;
    NotificationController notificationController;
    PickiT pickiT;
    String parentCommentID = "0";
    boolean iframeLoaded, postLoaded, videoChannel, isDownloadedVideo;


    String rootDir;
    FileManager fileManager;
    ArrayList<FileModel> downloadedVideoFiles = new ArrayList<>();


    private PlayerView playerView;
    private ExoPlayer player;

    Uri videoUri;
    AudioManager audioManager;
    AudioAttributes playbackAttributes;
    AudioFocusRequest focusRequest;
    final Object focusLock = new Object();
    boolean playbackDelayed = false;
    boolean playbackNowAuthorized = false;

    boolean landscape,fullscreenMode;
    RelativeLayout player_container;
    ArrayList<LessonModel> relatedLessons = new ArrayList<>();
    RelativeLessonAdapter lessonAdapter;
    private InterstitialAd mInterstitialAd=null;
    MyImagePicker myImagePicker;
    long playbackPosition;
    boolean playWhenReady;
    @SuppressLint("StaticFieldLeak")
    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_vimeo_player);
        sharedPreferences = getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        postExecutor = ContextCompat.getMainExecutor(this);
        currentUserName = sharedPreferences.getString("Username", null);
        isVIP = sharedPreferences.getBoolean("isVIP", false);
        imagePath = sharedPreferences.getString("imageUrl", null);
        currentUserId = sharedPreferences.getString("phone", null);

        // videoId = Objects.requireNonNull(getIntent().getExtras()).getString("videoId");
        timeCheck = getIntent().getExtras().getString("cmtTime", "0");
        relatedLesson = getIntent().getExtras().getString("lessonJSON", null);
        post_description = getIntent().getExtras().getString("post_description", "");
        videoChannel = getIntent().getExtras().getBoolean("videoChannel");
        isDownloadedVideo = getIntent().getBooleanExtra("downloaded", false);
        folderName = getIntent().getExtras().getString("folderName", "");

        myImagePicker = new MyImagePicker(VimeoPlayerActivity.this);

        a = getIntent().getExtras().getLong("time");
        videoUri=(Uri)getIntent().getExtras().get("localVideoUri");

        if (bundle != null) {
            playbackPosition = bundle.getLong("playbackPosition", 0);
            playWhenReady = bundle.getBoolean("playWhenReady", true);
            a = bundle.getLong("time",getIntent().getExtras().getLong("time"));
            String sUri = bundle.getString("video_uri","");
            videoUri = Uri.parse(sUri);
        }

        rootDir = getExternalFilesDir(Environment.DIRECTORY_MOVIES).getPath();
        fileManager = new FileManager(this);

        if(!isVIP){
            MobileAds.initialize(this, new OnInitializationCompleteListener() {
                @Override
                public void onInitializationComplete(InitializationStatus initializationStatus) {
                    loadAd();
                }
            });

        }
        setUpView();
        if(bundle != null)  wv.restoreState(bundle);  // only after setUpView()


        Log.e("Root dir", rootDir + "/" + folderName);
        fileManager.loadFiles(new File(rootDir + "/" + folderName), new FileManager.OnFileLoading() {
            @Override
            public void onLoaded(ArrayList<FileModel> files) {
                downloadedVideoFiles.addAll(files);
                setRelatedLesson();
            }
        });


        pickiT = new PickiT(this, this, this);
        notificationController = new NotificationController(this);


        if (isVIP) isVip = "1";

        getVideoData();

        Intent intent = new Intent(this, MusicService.class);
        stopService(intent);

        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {

                if(fullscreenMode){
                    if(portrait_video){
                        player_container.setLayoutParams(new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        defineVideoViewHeight();
                    }
                    fullscreenMode = false;
                    return;
                }
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                if (player.isPlaying()) {
                    player.stop();
                }

                if (mInterstitialAd != null) {
                    mInterstitialAd.show(VimeoPlayerActivity.this);
                } else {
                    Log.d("TAG", "The interstitial ad wasn't ready yet.");
                    finish();
                }
            }
        });

    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (player != null) {
            outState.putLong("playbackPosition", player.getCurrentPosition());
            outState.putBoolean("playWhenReady", player.getPlayWhenReady());
            outState.putInt("currentWindowIndex", player.getCurrentMediaItemIndex());
        }
        wv.saveState(outState);
        outState.putLong("time",a);
        if(videoUri != null)  outState.putString("video_uri",videoUri.toString());
    }

    private void setUpView() {
        setUpExoPlayer();
        wv = findViewById(R.id.wv_vimeo);
        main = findViewById(R.id.main);
        tv_title = findViewById(R.id.tv_info_header);
        tv_mini_title = findViewById(R.id.tv_mini_title);
        tv_view = findViewById(R.id.tv_view_count);
        tv_react = findViewById(R.id.tv_react);
        tv_comment = findViewById(R.id.tv_comment);
        tv_share = findViewById(R.id.tv_share);
        tv_description = findViewById(R.id.tv_description);
        tv_related_lesson = findViewById(R.id.tv_related_lesson);
        recyclerViewLesson = findViewById(R.id.recyclerView);
        vimeoLayout = findViewById(R.id.vimeo_layout);
        pb_vimeo = findViewById(R.id.pb_vimeo);
        pb_video_frame = findViewById(R.id.pb_video_frame);
        player_container=findViewById(R.id.player_container);
        iv_fullscreen = findViewById(R.id.iv_fullscreen);
        tv_fullscreen = findViewById(R.id.tv_fullscreen);

        vimeoLayout.setVisibility(View.GONE);
        tv_description.setText(post_description);

        wv.getSettings().setJavaScriptEnabled(true);
        wv.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView view, String url) {
                iframeLoaded = true;
                loadVideoContent();
            }
        });
        wv.addJavascriptInterface(new WebAppInterface(this, new WebAppInterface.CallBack() {
            @Override
            public void onEvent() {
                postExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        int nextIndex=getCurrentIndex()+1;
                        if(nextIndex>=0&&nextIndex<relatedLessons.size()){
                            LessonModel model=relatedLessons.get(nextIndex);
                            if(model.isVideo()){
                                lessonAdapter.setNowPlayingId(model.getTime());
                                playNext(model);

                            }
                        }
                    }
                });
            }

            @Override
            public void onVideoPortrait(String mode) {
                setLayoutParameterForVideoPlayer(mode.equals("p"));
            }
        }), "Android");


        if (isDownloadedVideo) {
            wv.setVisibility(View.GONE);
            playerView.setVisibility(View.VISIBLE);
            playVideo();
            iframeLoaded=true;
            postLoaded=true;
            loadVideoContent();

        } else {
            videoUri = null;
            playerView.setVisibility(View.GONE);
            wv.setVisibility(View.VISIBLE);
            wv.loadUrl(Routing.PLAY_VIDEO + "?post_id=" + a);
        }
        defineVideoViewHeight();


        if (!timeCheck.equals("") && !timeCheck.equals("0")) showCommentDialog();

        tv_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCommentDialog();
            }
        });

        tv_react.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                LikeController.likeThePost(currentUserId, a + "");

                if (isLiked.equals("1")) {
                    tv_react.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_normal_react, 0, 0, 0);
                    reactCount--;
                    if (reactCount > 0) {
                        tv_react.setText(reactFormat(reactCount));
                    } else {
                        tv_react.setText("");
                    }

                    isLiked = "0";

                } else {
                    tv_react.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_react_love, 0, 0, 0);
                    reactCount++;
                    tv_react.setText(reactFormat(reactCount));

                    isLiked = "1";
                }

            }
        });

        tv_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String info = "Share this video lesson";
                showSharePostDialog(info, a + "");
            }
        });

        iv_fullscreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                makeFullScreen();
            }
        });

        tv_fullscreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                makeFullScreen();
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
                        Toast.makeText(getApplicationContext(),"Ad fail"+ loadAdError.toString(),Toast.LENGTH_SHORT).show();
                    }
                });
    }


    @OptIn(markerClass = UnstableApi.class)
    private void setUpExoPlayer() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        playbackAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();


        videoTitle = "";
        //  autoRotateOnScreen(videoUri);

        AudioManager mAudioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);

        if (mAudioManager.isMusicActive()) {
            Intent i = new Intent("com.android.music.musicservicecommand");
            i.putExtra("command", "pause");
            sendBroadcast(i);
        }

        Intent intent = new Intent(this, MusicService.class);
        stopService(intent);


        playerView = findViewById(R.id.exoplayer_view);

        player = new ExoPlayer.Builder(this)
                .setSeekBackIncrementMs(10000)
                .setSeekForwardIncrementMs(10000)
                .build();
        player.seekTo(playbackPosition);
        player.setPlayWhenReady(playWhenReady);

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
                    player.setPlayWhenReady(true);
                    player.getPlaybackState();
                } else if (res == AudioManager.AUDIOFOCUS_REQUEST_DELAYED) {
                    playbackDelayed = true;
                    playbackNowAuthorized = false;
                }

            }
        } else {
            int result = audioManager.requestAudioFocus(this,
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN);
            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                player.setPlayWhenReady(true);
                player.getPlaybackState();
            }
        }

        player.addListener(new androidx.media3.common.Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if (playbackState == androidx.media3.common.Player.STATE_ENDED) {
                    postExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            int nextIndex=getCurrentIndex()+1;
                            if(nextIndex>=0&&nextIndex<relatedLessons.size()){
                                LessonModel model=relatedLessons.get(nextIndex);
                                if(model.isVideo()){
                                    lessonAdapter.setNowPlayingId(model.getTime());
                                    playNext(model);
                                }
                            }
                        }
                    });
                }
            }

            @Override
            public void onPlayerError(androidx.media3.common.PlaybackException error) {
                Toast.makeText(VimeoPlayerActivity.this, "Video playback error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        Log.d("tag", "config changed");
        super.onConfigurationChanged(newConfig);

        int orientation = newConfig.orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            Log.d("tag", "Portrait");
            player_container.setLayoutParams(new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            defineVideoViewHeight();
            landscape=false;
            //   scaling.setOnClickListener(changeLandscapeListener);
            //  scaling.setImageResource(R.drawable.fullscreen);
        } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Log.d("tag", "Landscape");
            player_container.setLayoutParams(new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            defineVideoViewHeight();
            landscape=true;
            // scaling.setOnClickListener(changePortraitListener);
            // scaling.setImageResource(R.drawable.baseline_close_fullscreen_24);
        } else
            Log.w("tag", "other: " + orientation);
    }


    private void defineVideoViewHeight() {

        if(isDownloadedVideo){
            try {
                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(this, videoUri);
                Bitmap bmp = retriever.getFrameAtTime();
                int videoWidth = bmp.getWidth();
                int videoHeight = bmp.getHeight();
                setLayoutParameterForVideoPlayer(videoWidth <= videoHeight);

            } catch (Exception e) {
                Log.e("VideoFrame", e.toString());
                setLayoutParameterForVideoPlayer(false);
                Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_SHORT).show();

            }
        }else{
            setLayoutParameterForVideoPlayer(false);
        }

    }

    private void makeFullScreen(){
        fullscreenMode = true;
        if (portrait_video){
            player_container.setLayoutParams(new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            defineVideoViewHeight();
        }else{
            landscape = true;
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
    }

    private void setLayoutParameterForVideoPlayer(boolean portrait){

        portrait_video = portrait;
        postExecutor.execute(new Runnable() {
            @Override
            public void run() {
                int width, height;
                DisplayMetrics displayMetrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                width = displayMetrics.widthPixels;
                height = (width * 9) / 16;
                if(portrait){
                    height = width-50;
                }
                RelativeLayout.LayoutParams layoutParams=new RelativeLayout.LayoutParams(width,height);
                layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT,RelativeLayout.TRUE);
                playerView.setLayoutParams(layoutParams);
                wv.setLayoutParams(layoutParams);

                if(portrait){
                    iv_fullscreen.setVisibility(View.GONE);
                    tv_fullscreen.setVisibility(View.GONE);
                }else{
                    iv_fullscreen.setVisibility(View.VISIBLE);
                    tv_fullscreen.setVisibility(View.VISIBLE);
                }
            }
        });
    }


    private void setRelatedLesson() {
        if (relatedLesson == null) {
            recyclerViewLesson.setVisibility(View.GONE);
            tv_related_lesson.setVisibility(View.GONE);
        } else {

            lessonAdapter = new RelativeLessonAdapter(this, relatedLessons, a, videoChannel, new RelativeLessonAdapter.CallBack() {
                @Override
                public void onClick(LessonModel model) {
                    playNext(model);
                }

                @Override
                public void onDownloadClick() {
                    setSnackBar("Start Downloading");
                }
            });
            LinearLayoutManager lm = new LinearLayoutManager(this);
            recyclerViewLesson.setLayoutManager(lm);
            recyclerViewLesson.setAdapter(lessonAdapter);

            try {
                JSONArray ja = new JSONArray(relatedLesson);
                for (int i = 0; i < ja.length(); i++) {
                    JSONObject jo = ja.getJSONObject(i);
                    String id = jo.getString("id");
                    String link = jo.getString("link");
                    String title = jo.getString("title");
                    String title_mini = jo.getString("title_mini");
                    String image_url = jo.getString("image_url");
                    String thumbnail = jo.getString("thumbnail");
                    boolean isVideo = jo.getString("isVideo").equals("1");
                    boolean isVip = jo.getString("isVip").equals("1");
                    boolean learned = jo.getString("learned").equals("1");
                    long time = jo.getLong("date");
                    int duration = jo.getInt("duration");

                    LessonModel model = new LessonModel(id, link, title, title_mini, isVideo, isVip, time, learned,
                            image_url, thumbnail, duration, folderName);

                    if (isVideo) {
                        String checkTitle = title.replace("/", " ");
                        checkTitle = checkTitle + ".mp4";
                        Log.e("downloadVideo list zie ", downloadedVideoFiles.size() + "");
                        if (downloadedVideoFiles.size() > 0) {
                            for (int j = 0; j < downloadedVideoFiles.size(); j++) {
                                FileModel file = downloadedVideoFiles.get(j);
                                if (file.getFile().getName().equals(checkTitle)) {
                                    model.setDownloaded(true);
                                    model.setVideoModel((SavedVideoModel) file);
                                    Log.e("Downloaded ", checkTitle + " is downloaded");

                                }
                            }
                        }
                    }

                    relatedLessons.add(model);
                }
                lessonAdapter.notifyDataSetChanged();

            } catch (Exception e) {

                Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
            }

        }
    }


    private void playNext(LessonModel model){
        iframeLoaded = false;
        postLoaded = false;
        //  videoId = model.getLink();
        a = model.getTime();
        timeCheck = "0";
        isDownloadedVideo = model.isDownloaded();
        if (isDownloadedVideo) {
            wv.setVisibility(View.GONE);
            wv.loadUrl("");
            playerView.setVisibility(View.VISIBLE);
            videoUri=model.getVideoModel().getUri();
            iframeLoaded = true;
            postLoaded = true;
            playVideo();
            loadVideoContent();
        } else {
            player.stop();
            pb_video_frame.setVisibility(View.VISIBLE);
            wv.setVisibility(View.VISIBLE);
            playerView.setVisibility(View.GONE);
            wv.loadUrl(Routing.PLAY_VIDEO + "?post_id=" + a);
        }
        getVideoData();
        defineVideoViewHeight();
    }

    public String setMyanmar(String s) {
        return s;
    }

    private void setSnackBar(String s) {
        final Snackbar sb = Snackbar.make(main, s, Snackbar.LENGTH_INDEFINITE);
        sb.setAction("View", v -> startActivity(new Intent(VimeoPlayerActivity.this,
                        DownloadingListActivity.class)))
                .setActionTextColor(Color.WHITE)
                .show();
    }

    private void getVideoData() {
        new Thread(() -> {
            MyHttp myHttp = new MyHttp(MyHttp.RequesMethod.POST, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    postExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONArray ja = new JSONArray(response);
                                JSONObject jo = ja.getJSONObject(0);
                                String viewCount = jo.getString("view_count");
                                String comments = jo.getString("comments");
                                int noOfComments = Integer.parseInt(comments);
                                int views = Integer.parseInt(viewCount);
                                tv_view.setText(viewCountFormat(views) + "  .  " + formatTime(a));

                                tv_comment.setText(reactFormat(noOfComments));
                                videoTitle = jo.getString("title");
                                tv_title.setText(setMyanmar(videoTitle));
                                isLiked = jo.getString("is_liked");
                                postLikes = jo.getString("postLikes");

                                String title_mini = jo.getString("title_mini");
                                if (title_mini.equals("null")) tv_mini_title.setText("");
                                else tv_mini_title.setText(title_mini);

                                if (isLiked.equals("1")) {
                                    tv_react.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_react_love, 0, 0, 0);
                                }

                                reactCount = Integer.parseInt(postLikes);
                                if (reactCount != 0) {
                                    tv_react.setText(reactFormat(reactCount));
                                } else {
                                    tv_react.setText("");
                                }

                                int shareCount = jo.getInt("shareCount");
                                if (shareCount == 0) tv_share.setText("");
                                else tv_share.setText("  " + reactFormat(shareCount));

                                postLoaded = true;
                                loadVideoContent();

                            } catch (Exception e) {
                                //Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

                @Override
                public void onError(String msg) {
                    Log.e("YoutubeActivity 652: ", msg);
                }
            }).url(Routing.GET_VIDEO_DATA)
                    .field("post_Id", a + "")
                    .field("user_id", currentUserId);
            myHttp.runTask();
        }).start();

    }

    private int getCurrentIndex(){
        for(int i=0;i<relatedLessons.size();i++){
            LessonModel model=relatedLessons.get(i);
            if(model.getTime()==a){
                return i;
            }
        }
        return -1;
    }


    @Override
    public void PickiTonUriReturned() {

    }

    @Override
    public void PickiTonStartListener() {

    }

    @Override
    public void PickiTonProgressUpdate(int progress) {

    }

    @Override
    public void PickiTonCompleteListener(String path, boolean wasDriveFile, boolean wasUnknownProvider, boolean wasSuccessful, String Reason) {
        commentImagePath = path;
    }

    @Override
    protected void onDestroy() {
        wv.destroy();
        super.onDestroy();
    }

    private void loadVideoContent() {
        if (iframeLoaded && postLoaded) {
            pb_vimeo.setVisibility(View.GONE);
            pb_video_frame.setVisibility(View.GONE);
            vimeoLayout.setVisibility(View.VISIBLE);
        }
    }

    private void showSharePostDialog(String shareInfo, String shareId) {

        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(VimeoPlayerActivity.this, R.style.SheetDialog);
        bottomSheetDialog.setContentView(R.layout.dialog_share_post);

        ImageView iv_profile;
        TextView tv_name, tv_share_info;
        EditText et_share;
        CardView card_share_now;

        iv_profile = bottomSheetDialog.findViewById(R.id.iv_profile);
        tv_name = bottomSheetDialog.findViewById(R.id.tv_name);
        tv_share_info = bottomSheetDialog.findViewById(R.id.tv_share_info);
        et_share = bottomSheetDialog.findViewById(R.id.et_share);
        card_share_now = bottomSheetDialog.findViewById(R.id.card_share_now);

        AppHandler.setPhotoFromRealUrl(iv_profile, imagePath);
        tv_name.setText(currentUserName);
        tv_share_info.setText(shareInfo);

        card_share_now.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sharePost(currentUserId, et_share.getText().toString(), shareId);
                bottomSheetDialog.dismiss();
            }
        });


        bottomSheetDialog.show();

    }

    private void sharePost(String learner_id, String body, String shareId) {

        new Thread(() -> {
            MyHttp myHttp = new MyHttp(MyHttp.RequesMethod.POST, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    postExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Shared", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onError(String msg) {

                }
            }).url(Routing.ADD_POST)
                    .field("learner_id", learner_id)
                    .field("body", body)
                    .field("major", Routing.MAJOR)
                    .field("share", shareId)
                    .field("hasVideo", "0");
            myHttp.runTask();
        }).start();


    }

    // Exoplayer Section

    private void autoRotateOnScreen(Uri videoUri) {
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            Bitmap bmp;
            retriever.setDataSource(this, videoUri);
            bmp = retriever.getFrameAtTime();

            int videoWidth = bmp.getWidth();
            int videoHeight = bmp.getHeight();
            if (videoWidth > videoHeight)
                this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            else this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

        } catch (Exception ignored) {
        }

    }

    @Override
    public void onAudioFocusChange(int i) {

    }


    private void playVideo() {

        MediaItem mediaItem = MediaItem.fromUri(videoUri);

        playerView.setPlayer(player);
        playerView.setKeepScreenOn(true);

        player.setMediaItem(mediaItem);
        player.prepare();
        player.play();
    }

    @Override
    protected void onPause() {
        super.onPause();
        player.setPlayWhenReady(false);
        player.getPlaybackState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        player.setPlayWhenReady(true);
        player.getPlaybackState();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        player.setPlayWhenReady(true);
        player.getPlaybackState();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.video_back:
                if(landscape){
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }else{
                    if (player != null) {
                        player.release();
                    }
                    finish();
                }
                break;
        }
    }

    View.OnClickListener changeLandscapeListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
    };

    View.OnClickListener changePortraitListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    };


    //comment ///////////////////////////////////////////////////

    ArrayList<Object> postList = new ArrayList<>();
    ArrayList<CommentModel> parentComments = new ArrayList<>();
    ArrayList<CommentModel> childComments = new ArrayList<>();

    VideoCommentAdapter adapter;
    TextView tv;
    String imagePath, action, CorR, postOwnerId, targetToken;

    ImageView iv_msg, iv_cancel;
    String commentImagePath = "";
    RecyclerView recyclerView;

    private void showCommentDialog() {
        postList.clear();
        parentComments.clear();
        childComments.clear();

        final EditText et;
        ImageButton bt;
        ImageView iv_pickup;
        View v = getLayoutInflater().inflate(R.layout.custom_comment_dialog, null);
        v.setAnimation(AnimationUtils.loadAnimation(this, R.anim.transit_up));
        et = v.findViewById(R.id.nf_dia_et);
        bt = v.findViewById(R.id.nf_dia_bt);
        tv = v.findViewById(R.id.nf_no_cmt_tv);
        pb = v.findViewById(R.id.nf_pb);
        iv_pickup = v.findViewById(R.id.iv_small_profile);
        tv.setVisibility(View.INVISIBLE);
        recyclerView = v.findViewById(R.id.recycler);
        iv_msg = v.findViewById(R.id.iv_msg);
        iv_cancel = v.findViewById(R.id.iv_cancel);

        action = "3";
        CorR = " commented on your post";
        postOwnerId = "10000";
        targetToken = "";


        iv_pickup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myImagePicker.pick(new MyImagePicker.Callback() {
                    @Override
                    public void onResult(Uri uri) {
                        iv_msg.setVisibility(View.VISIBLE);
                        iv_cancel.setVisibility(View.VISIBLE);
                        iv_msg.setImageURI(uri);
                        if(uri!=null){
                            pickiT.getPath(uri, Build.VERSION.SDK_INT);
                        }else {
                            iv_msg.setImageBitmap(null);
                            iv_msg.setVisibility(View.GONE);
                            iv_cancel.setVisibility(View.GONE);
                            commentImagePath = "";
                        }
                    }
                });
            }
        });

        iv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iv_msg.setImageBitmap(null);
                iv_msg.setVisibility(View.GONE);
                iv_cancel.setVisibility(View.GONE);
                commentImagePath = "";
            }
        });


        final LinearLayoutManager lm = new LinearLayoutManager(this) {
        };
        recyclerView.setLayoutManager(lm);
        // recyclerView.addItemDecoration(new SpacingItemDecoration(2, XUtils.toPx(Objects.requireNonNull(this), 2), true));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        adapter = new VideoCommentAdapter(this, postList, timeCheck, new VideoCommentAdapter.Callback() {
            @Override
            public void onReplyClick(String commentID, String name, String id, String token_to) {

                parentCommentID = commentID;
                postOwnerId = id;
                targetToken = token_to;
                et.setText(name + ": \n");
                action = "4";
                CorR = " reply your comment on the video: ";
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                et.setSelection(et.getText().length());
                et.requestFocus();
                et.setPadding(10, 15, 10, 15);

                Log.e("Parent ID ", parentCommentID);

            }
        });

        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = et.getText().toString();
                if (!TextUtils.isEmpty(content) || !commentImagePath.equals("")) {

                    MyCommentController myCommentController = new MyCommentController("" + a, currentUserName, VimeoPlayerActivity.this);
                    myCommentController.addCommentToHostinger(postOwnerId, currentUserId, content, action, CorR, targetToken, commentImagePath, parentCommentID);

                    et.setText("");
                    tv.setVisibility(View.INVISIBLE);
                    postList.add(new CommentModel("10", parentCommentID, imagePath, currentUserName, content, System.currentTimeMillis() + "", isVip, "", "", "0", "0", ""));
                    adapter.notifyItemInserted(postList.size());
                    recyclerView.smoothScrollToPosition(postList.size());
                    notificationController.PushNotiToAdmin(currentUserName + "commented on the video " + videoTitle);

                    parentCommentID = "0";
                    iv_msg.setImageBitmap(null);
                    iv_msg.setVisibility(View.GONE);
                    iv_cancel.setVisibility(View.GONE);
                    commentImagePath = "";

                } else {
                    Toast.makeText(VimeoPlayerActivity.this, "Write a comment or select a photo", Toast.LENGTH_SHORT).show();
                }

            }
        });


        et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count == 0) {

                    parentCommentID = "0";
                    targetToken = "";
                    postOwnerId = "10000";
                    action = "3";
                    CorR = " commented on the video:";
                    et.setPadding(10, 10, 10, 10);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        recyclerView.setAdapter(adapter);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(v);
        final AlertDialog ad = builder.create();
        ad.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        ad.show();

        fetchPost(timeCheck);

    }

    private void fetchPost(String time) {

        new Thread(() -> {
            MyHttp myHttp = new MyHttp(MyHttp.RequesMethod.GET, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    postExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            doAsResult(response);
                            Log.e("VC Response : ", response);
                        }
                    });
                }

                @Override
                public void onError(String msg) {
                    Log.e("VC Error: ", msg);
                }
            }).url(Routing.FETCH_COMMENT + "?mCode=" + Routing.MAJOR_CODE + "&postId=" + a + "&time=" + time + "&userId=" + currentUserId); // a = postId
            myHttp.runTask();
        }).start();
    }

    private void doAsResult(String response) {
        int moveTo = 0;
        try {
            String postId;
            JSONObject jsonObject = new JSONObject(response);

            String postContent = jsonObject.getString("post");
            JSONArray ja = new JSONArray(postContent);
            JSONObject jo = ja.getJSONObject(0);
            postId = jo.getString("postId");

            String comment = jsonObject.getString("comments");
            JSONArray jaComment = new JSONArray(comment);
            for (int i = 0; i < jaComment.length(); i++) {
                JSONObject joC = jaComment.getJSONObject(i);
                String name = joC.getString("userName");
                String commentBody = joC.getString("body");
                String imageUrl = joC.getString("userImage");
                String time = joC.getString("time");
                String tokenR = joC.getString("userToken");
                String vip = joC.getString("vip");
                String writerId = joC.getString("userId");
                String likes = joC.getString("likes");
                String isLiked = joC.getString("is_liked");
                String commentImage = joC.getString("commentImage");
                String parentID = joC.getString("parent");

                CommentModel cModel = new CommentModel(postId, parentID, imageUrl, name, commentBody, time, vip, writerId, tokenR, likes, isLiked, commentImage);
                if (parentID.equals("0")) parentComments.add(cModel);
                else childComments.add(cModel);
                if (time.equals(timeCheck)) moveTo = i + 1;

            }

            sortingComment();
            pb.setVisibility(View.INVISIBLE);
            adapter.notifyDataSetChanged();
            if (moveTo > postList.size() - 1) {
                moveTo = postList.size() - 1;
            }

            recyclerView.smoothScrollToPosition(moveTo);

        } catch (Exception e) {
            pb.setVisibility(View.INVISIBLE);
            tv.setVisibility(View.VISIBLE);
            Log.e("VC JSON Error: ", e.toString());
        }
    }

    private void sortingComment() {
        for (int i = 0; i < parentComments.size(); i++) {
            CommentModel parent = parentComments.get(i);
            postList.add(parent);
            for (int j = 0; j < childComments.size(); j++) {
                CommentModel child = childComments.get(j);
                if (parent.getTime().equals(child.getParentId())) {
                    postList.add(child);
                }
            }
        }
    }
}