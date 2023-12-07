package com.calamus.easykorean;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
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
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.calamus.easykorean.app.FileManager;
import com.calamus.easykorean.models.FileModel;
import com.calamus.easykorean.models.SavedVideoModel;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
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
import com.calamus.easykorean.app.MyHttp;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.Executor;

import static com.calamus.easykorean.app.AppHandler.changeUnicode;
import static com.calamus.easykorean.app.AppHandler.formatTime;
import static com.calamus.easykorean.app.AppHandler.reactFormat;
import static com.calamus.easykorean.app.AppHandler.viewCountFormat;

public class VimeoPlayerActivity extends AppCompatActivity implements AudioManager.OnAudioFocusChangeListener,PickiTCallbacks,View.OnClickListener {

    WebView wv;
    TextView tv_title, tv_mini_title, tv_react, tv_comment, tv_share, tv_view, tv_related_lesson,
            tv_description;
    RecyclerView recyclerViewLesson;
    ConstraintLayout vimeoLayout;
    ProgressBar pb_vimeo, pb_video_frame;

    String videoId, videoTitle, relatedLesson, post_description;
    String currentUserId, currentUserName;
    SharedPreferences sharedPreferences;
    ProgressBar pb;
    ViewGroup main;
    boolean isVIP;

    long a;
    String isVip = "0";

    String isLiked, postLikes;
    String timeCheck = "";

    int reactCount;
    String videoUrl = "", folderName;
    Executor postExecutor;
    NotificationController notificationController;
    PickiT pickiT;
    String parentCommentID = "0";
    boolean iframeLoaded, postLoaded, videoChannel, isDownloadedVideo;


    String rootDir;
    FileManager fileManager;
    ArrayList<FileModel> downloadedVideoFiles = new ArrayList<>();


    //Offline Exo Player
    PlayerView playerView;
    SimpleExoPlayer player;
    TextView title;
    ConcatenatingMediaSource concatenatingMediaSource;
    ImageView videoBack, lock, unlock, scaling;
    RelativeLayout root;
    private VideoPlayerActivity.ControlsMode controlsMode;

    public enum ControlsMode {
        LOCK, FULLSCREEN
    }

    Uri videoUri;
    AudioManager audioManager;
    AudioAttributes playbackAttributes;
    AudioFocusRequest focusRequest;
    final Object focusLock = new Object();
    boolean playbackDelayed = false;
    boolean playbackNowAuthorized = false;

    boolean landscape;

    RelativeLayout player_container;

    @SuppressLint("StaticFieldLeak")
    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_vimeo_player);
        sharedPreferences = getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        currentUserName = sharedPreferences.getString("Username", null);
        isVIP = sharedPreferences.getBoolean("isVIP", false);
        imagePath = sharedPreferences.getString("imageUrl", null);
        currentUserId = sharedPreferences.getString("phone", null);

        videoId = Objects.requireNonNull(getIntent().getExtras()).getString("videoId");
        timeCheck = getIntent().getExtras().getString("cmtTime", "0");
        relatedLesson = getIntent().getExtras().getString("lessonJSON", null);
        post_description = getIntent().getExtras().getString("post_description", "");
        videoChannel = getIntent().getExtras().getBoolean("videoChannel");
        isDownloadedVideo = getIntent().getBooleanExtra("downloaded", false);
        folderName = getIntent().getExtras().getString("folderName", "");

        rootDir = getExternalFilesDir(Environment.DIRECTORY_MOVIES).getPath();
        fileManager = new FileManager(this);
        Log.e("Root dir", rootDir + "/" + folderName);
        fileManager.loadFiles(new File(rootDir + "/" + folderName), new FileManager.OnFileLoading() {
            @Override
            public void onLoaded(ArrayList<FileModel> files) {
                downloadedVideoFiles.addAll(files);
                setRelatedLesson();
            }
        });


        a = getIntent().getExtras().getLong("time");


        pickiT = new PickiT(this, this, this);
        postExecutor = ContextCompat.getMainExecutor(this);
        notificationController = new NotificationController(this);

        setUpView();
        if (isVIP) isVip = "1";

        getVideoData();

        Intent intent = new Intent(this, MusicService.class);
        stopService(intent);

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

        vimeoLayout.setVisibility(View.GONE);
        tv_description.setText(post_description);


        wv.getSettings().setJavaScriptEnabled(true);
        wv.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView view, String url) {
                iframeLoaded = true;
                loadVideoContent();
            }
        });

        defineVideoViewHeight();
        if (isDownloadedVideo) {
            wv.setVisibility(View.GONE);
            playerView.setVisibility(View.VISIBLE);
            videoUri=(Uri)getIntent().getExtras().get("localVideoUri");
            playVideo();
            iframeLoaded=true;
            postLoaded=true;
            loadVideoContent();

        } else {
            playerView.setVisibility(View.GONE);
            wv.setVisibility(View.VISIBLE);
            wv.loadUrl(Routing.PLAY_VIDEO + "?post_id=" + a);
        }


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

    }


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
        title = findViewById(R.id.video_title);
        videoBack = findViewById(R.id.video_back);
        lock = findViewById(R.id.lock);
        unlock = findViewById(R.id.unlock);
        root = findViewById(R.id.root_layout);
        scaling = findViewById(R.id.scaling);
        scaling.setOnClickListener(changeLandscapeListener);

        title.setText(videoTitle);
        videoBack.setOnClickListener(this);
        lock.setOnClickListener(this);
        unlock.setOnClickListener(this);

        player = new SimpleExoPlayer.Builder(this)
                .setSeekBackIncrementMs(10000)
                .setSeekForwardIncrementMs(10000)
                .build();


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
            scaling.setOnClickListener(changeLandscapeListener);
            scaling.setImageResource(R.drawable.fullscreen);
        } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Log.d("tag", "Landscape");
            player_container.setLayoutParams(new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            defineVideoViewHeight();
            landscape=true;
            scaling.setOnClickListener(changePortraitListener);
            scaling.setImageResource(R.drawable.baseline_close_fullscreen_24);
        } else
            Log.w("tag", "other: " + orientation);
    }

    private void defineVideoViewHeight() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        int height = (width * 9) / 16;
        RelativeLayout.LayoutParams layoutParams=new RelativeLayout.LayoutParams(width,height);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT,RelativeLayout.TRUE);
        playerView.setLayoutParams(layoutParams);
        wv.setLayoutParams(layoutParams);
    }


    private void setRelatedLesson() {
        if (relatedLesson == null) {
            recyclerViewLesson.setVisibility(View.GONE);
            tv_related_lesson.setVisibility(View.GONE);
        } else {
            ArrayList<LessonModel> relatedLessons = new ArrayList<>();
            RelativeLessonAdapter lessonAdapter = new RelativeLessonAdapter(this, relatedLessons, a, videoChannel, new RelativeLessonAdapter.CallBack() {
                @Override
                public void onClick(LessonModel model) {
                    iframeLoaded = false;
                    postLoaded = false;
                    if (model.isDownloaded()) {
                        wv.setVisibility(View.GONE);
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
                    videoId = model.getLink();
                    a = model.getTime();
                    timeCheck = "0";
                    getVideoData();
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
                                videoUrl = jo.getString("video_url");
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
                sharePost(currentUserId, AppHandler.changeUnicode(et_share.getText().toString()), shareId);
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

        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(
                this, Util.getUserAgent(this, "app"));

        concatenatingMediaSource = new ConcatenatingMediaSource();
        MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(Uri.parse(String.valueOf(videoUri))));

        concatenatingMediaSource.addMediaSource(mediaSource);

        playerView.setPlayer(player);
        playerView.setKeepScreenOn(true);
        player.prepare(concatenatingMediaSource);
        player.seekTo(0, C.TIME_UNSET);
        playError();
    }

    private void playError() {
        player.addListener(new Player.Listener() {
            @Override
            public void onPlayerError(PlaybackException error) {
                Player.Listener.super.onPlayerError(error);
                Toast.makeText(getApplicationContext(), "Playing Playing Error", Toast.LENGTH_SHORT).show();
            }
        });

        player.setPlayWhenReady(true);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (player.isPlaying()) {
            player.stop();
        }
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
            case R.id.lock:
                controlsMode = VideoPlayerActivity.ControlsMode.FULLSCREEN;
                root.setVisibility(View.VISIBLE);
                lock.setVisibility(View.INVISIBLE);
                Toast.makeText(this, "Unlocked", Toast.LENGTH_SHORT).show();
                break;
            case R.id.unlock:
                controlsMode = VideoPlayerActivity.ControlsMode.LOCK;
                root.setVisibility(View.INVISIBLE);
                lock.setVisibility(View.VISIBLE);
                Toast.makeText(this, "Locked", Toast.LENGTH_SHORT).show();
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
                if (isPermissionGranted()) {
                    pickImageFromGallery();
                } else {
                    takePermission();
                }
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
                String content = changeUnicode(et.getText().toString());
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

    private boolean isPermissionGranted() {

        int readExternalStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        return readExternalStorage == PackageManager.PERMISSION_GRANTED;

    }

    private void takePermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 101);
    }

    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(intent, 102);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 102) {
                if (data != null) {
                    Uri uri = data.getData();
                    iv_msg.setVisibility(View.VISIBLE);
                    iv_cancel.setVisibility(View.VISIBLE);
                    iv_msg.setImageURI(uri);
                    if (uri != null) {
                        pickiT.getPath(data.getData(), Build.VERSION.SDK_INT);
                    } else {
                        iv_msg.setImageBitmap(null);
                        iv_msg.setVisibility(View.GONE);
                        iv_cancel.setVisibility(View.GONE);
                        commentImagePath = "";
                    }
                }
            }
        }
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