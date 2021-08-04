package com.calamus.easykorean;

    import android.Manifest;
    import android.annotation.SuppressLint;
    import android.app.AlertDialog;
    import android.content.Context;
    import android.content.Intent;
    import android.content.SharedPreferences;
    import android.content.pm.PackageManager;
    import android.graphics.Color;
    import android.graphics.drawable.ColorDrawable;
    import android.net.Uri;
    import android.os.Build;
    import android.os.Bundle;
    import android.os.Environment;
    import android.text.Editable;
    import android.text.TextUtils;
    import android.text.TextWatcher;
    import android.view.View;
    import android.view.animation.AnimationUtils;
    import android.view.inputmethod.InputMethodManager;
    import android.webkit.WebView;
    import android.webkit.WebViewClient;
    import android.widget.EditText;
    import android.widget.ImageButton;
    import android.widget.ImageView;
    import android.widget.LinearLayout;
    import android.widget.ProgressBar;
    import android.widget.TextView;
    import android.widget.Toast;
    import androidx.annotation.NonNull;
    import androidx.annotation.Nullable;
    import androidx.cardview.widget.CardView;
    import androidx.core.app.ActivityCompat;
    import androidx.core.content.ContextCompat;
    import androidx.recyclerview.widget.DefaultItemAnimator;
    import androidx.recyclerview.widget.LinearLayoutManager;
    import androidx.recyclerview.widget.RecyclerView;
    import com.calamus.easykorean.adapters.VideoCommentAdapter;
    import com.calamus.easykorean.app.MyDialog;
    import com.calamus.easykorean.app.MyHttp;
    import com.calamus.easykorean.app.Routing;
    import com.calamus.easykorean.controller.LikeController;
    import com.calamus.easykorean.controller.MyCommentController;
    import com.calamus.easykorean.controller.NotificationController;
    import com.calamus.easykorean.models.CommentModel;
    import com.calamus.easykorean.service.DownloaderService;
    import com.google.android.gms.ads.AdRequest;
    import com.google.android.gms.ads.AdView;
    import com.google.android.gms.ads.FullScreenContentCallback;
    import com.google.android.gms.ads.LoadAdError;
    import com.google.android.gms.ads.MobileAds;
    import com.google.android.gms.ads.initialization.InitializationStatus;
    import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
    import com.google.android.gms.ads.interstitial.InterstitialAd;
    import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
    import com.google.android.youtube.player.YouTubeBaseActivity;
    import com.google.android.youtube.player.YouTubeInitializationResult;
    import com.google.android.youtube.player.YouTubePlayer;
    import com.google.android.youtube.player.YouTubePlayerView;
    import com.hbisoft.pickit.PickiT;
    import com.hbisoft.pickit.PickiTCallbacks;

    import org.json.JSONArray;
    import org.json.JSONObject;
    import java.util.ArrayList;
    import java.util.Objects;
    import java.util.concurrent.Executor;
    import me.myatminsoe.mdetect.MDetect;
    import static com.calamus.easykorean.app.AppHandler.changeUnicode;
    import static com.calamus.easykorean.app.AppHandler.commentFormat;
    import static com.calamus.easykorean.app.AppHandler.formatTime;
    import static com.calamus.easykorean.app.AppHandler.reactFormat;
    import static com.calamus.easykorean.app.AppHandler.viewCountFormat;

public class MyYouTubeVideoActivity extends YouTubeBaseActivity implements PickiTCallbacks {
    String uTubeApi="AIzaSyCWaZdFaMGJw7Aos6QImUyxd1In0lIat9w";
    YouTubePlayerView youtubePlayerView;
    TextView tv1,tv3,tv4,tv_noOfComment,tv_reactCount;
    String videoId,videoTitle;
    String currentUserId,currentUserName;
    SharedPreferences sharedPreferences;
    ProgressBar pb,progressBar;
    boolean isVIP;
    LinearLayout layout;
    WebView wv;
    long a;
    String isVip="0";
    ImageButton ib_download,ib_downloaded;
    String isLiked,postLikes;
    String timeCheck="";
    ImageView iv_react;
    CardView card_reatCount;
    private InterstitialAd interstitialAd;
    int reactCount;
    String videoUrl="";
    Executor postExecutor;
    NotificationController notificationController;
    PickiT pickiT;
    @SuppressLint("StaticFieldLeak")
    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_my_you_tube_video);

        MDetect.INSTANCE.init(this);
        sharedPreferences=getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        currentUserName=sharedPreferences.getString("Username",null);
        isVIP=sharedPreferences.getBoolean("isVIP",false);
        currentUserId=sharedPreferences.getString("phone",null);
        ib_download=findViewById(R.id.ib_download);
        videoId = Objects.requireNonNull(getIntent().getExtras()).getString("videoId");
        videoTitle=getIntent().getExtras().getString("videoTitle");
        timeCheck=getIntent().getExtras().getString("cmtTime","");
        pickiT = new PickiT(this, this, this);
        postExecutor=ContextCompat.getMainExecutor(this);
        notificationController=new NotificationController(this);

        setUpView();
        if(isVIP)isVip="1";

        getVideoData();

        MobileAds.initialize(this, initializationStatus -> {});

        AdView adView = findViewById(R.id.web_adview);
        if(!isVIP){
            adView.setVisibility(View.VISIBLE);
            AdRequest request=new AdRequest.Builder().build();
            adView.loadAd(request);
            Thread timer=new Thread(){
                @Override
                public void run() {
                    super.run();
                    try {
                        sleep(10000);
                        postExecutor.execute(() -> loadAds());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };
            timer.start();
        }

        ib_download.setOnClickListener(view -> {
            if(isVIP){
                Toast.makeText(getApplicationContext(),"Start downloading",Toast.LENGTH_SHORT).show();
                Intent intent=new Intent(MyYouTubeVideoActivity.this,DownloaderService.class);
                String checkTitle=videoTitle.replace("/"," ");
                intent.putExtra("dir",getExternalFilesDir(Environment.DIRECTORY_MOVIES).getPath());
                intent.putExtra("filename",checkTitle+".mp4");
                intent.putExtra("downloadUrl",videoUrl);
                startService(intent);
            }else {
                showVIPRegistrationDialog();
            }

        });

        card_reatCount.setOnClickListener(view -> {
            Intent intent=new Intent(MyYouTubeVideoActivity.this, LikeListActivity.class);
            intent.putExtra("contentId",a+"");
            intent.putExtra("fetch", Routing.FETCH_POST_LIKE);
            startActivity(intent);
        });

    }

    @Override
    public void onBackPressed() {
        if (interstitialAd != null) {
            interstitialAd.show(MyYouTubeVideoActivity.this);
        } else {

            super.onBackPressed();
        }

    }

    private void setUpView(){
        youtubePlayerView = findViewById(R.id.youtubePlayerView);
        tv1 = findViewById(R.id.tv_title);
        TextView tv2 = findViewById(R.id.tv_time);
        tv3=findViewById(R.id.tv_view_count);
        tv4=findViewById(R.id.tv_des);
        wv=findViewById(R.id.wv_youtube);
        tv_noOfComment=findViewById(R.id.tv_onOfComment);
        tv_reactCount=findViewById(R.id.tv_reactCount);
        iv_react=findViewById(R.id.iv_react);
        card_reatCount=findViewById(R.id.card_reactCount);
        ib_downloaded=findViewById(R.id.ib_downloaded);

        layout=findViewById(R.id.yt_cmt_layout);
        progressBar=findViewById(R.id.loading_pb);

        wv.getSettings().setJavaScriptEnabled(true);
        wv.setWebViewClient(new WebViewClient() {});

        playVideo(videoId, youtubePlayerView);

        a=getIntent().getExtras().getLong("time");


        tv2.setText("Posted on "+ formatTime(a));

        //
        if(!timeCheck.equals("")&&!timeCheck.equals("0"))showCommentDialog();

        layout.setOnClickListener(v -> showCommentDialog());


        iv_react.setOnClickListener(v -> {

            LikeController.likeThePost(currentUserId,a+"");

            if(isLiked.equals("1")){
                iv_react.setBackgroundResource(R.drawable.ic_normal_react);
                reactCount--;
                if(reactCount>0){
                    tv_reactCount.setText(reactFormat(reactCount));
                    card_reatCount.setVisibility(View.VISIBLE);
                }else {
                    card_reatCount.setVisibility(View.GONE);
                }

                isLiked="0";


            }else{
                iv_react.setBackgroundResource(R.drawable.ic_react_love);
                reactCount++;
                tv_reactCount.setText(reactFormat(reactCount));
                card_reatCount.setVisibility(View.VISIBLE);

                isLiked="1";


            }

        });


        ib_downloaded.setOnClickListener(view -> {
            Intent intent=new Intent(MyYouTubeVideoActivity.this,SavedVideoActivity.class);
            startActivity(intent);
        });
    }


    public void playVideo(final String videoId, final YouTubePlayerView youTubePlayerView) {
        //initialize youtube player view
        youTubePlayerView.initialize(uTubeApi,
                new YouTubePlayer.OnInitializedListener() {
                    @Override
                    public void onInitializationSuccess(YouTubePlayer.Provider provider,
                                                        YouTubePlayer youTubePlayer, boolean b) {
                        progressBar.setVisibility(View.INVISIBLE);
                        youTubePlayer.cueVideo(videoId);

                    }

                    @SuppressLint("StaticFieldLeak")
                    @Override
                    public void onInitializationFailure(YouTubePlayer.Provider provider,
                                                        YouTubeInitializationResult youTubeInitializationResult) {
                        youTubePlayerView.setVisibility(View.INVISIBLE);
                        progressBar.setVisibility(View.INVISIBLE);
                        wv.setVisibility(View.VISIBLE);
                        wv.loadUrl(Routing.PLAY_VIDEO+videoId);

                    }
                });
    }


    public String setMyanmar(String s) {
        return MDetect.INSTANCE.getText(s);
    }


    //comment

    ArrayList<Object> postList = new ArrayList<>();

    VideoCommentAdapter adapter;
    TextView tv;
    String  imagePath,action,CorR,postOwnerId,targetToken;
    RecyclerView recyclerView;
    ImageView iv_msg,iv_cancel;
    String commentImagePath="";

    private void showCommentDialog(){

        final EditText et;
        ImageButton bt;
        ImageView iv_pickUp;
        View v=getLayoutInflater().inflate(R.layout.custom_comment_dialog,null);
        v.setAnimation(AnimationUtils.loadAnimation(this,R.anim.transit_up));
        et=v.findViewById(R.id.nf_dia_et);
        bt=v.findViewById(R.id.nf_dia_bt);
        tv=v.findViewById(R.id.nf_no_cmt_tv);
        pb=v.findViewById(R.id.nf_pb);
        iv_pickUp=v.findViewById(R.id.iv_small_profile);
        tv.setVisibility(View.INVISIBLE);
        recyclerView=v.findViewById(R.id.recycler);
        iv_msg=v.findViewById(R.id.iv_msg);
        iv_cancel=v.findViewById(R.id.iv_cancel);
        imagePath=sharedPreferences.getString("imageUrl","");

        action="3";
        CorR=" commented on your post";
        postOwnerId="10000";
        targetToken="";

        iv_pickUp.setOnClickListener(v13 -> {
            if(isPermissionGranted()){
                pickImageFromGallery();
            }else {
                takePermission();
            }
        });

        iv_cancel.setOnClickListener(v12 -> {
            iv_msg.setImageBitmap(null);
            iv_msg.setVisibility(View.GONE);
            iv_cancel.setVisibility(View.GONE);
            commentImagePath="";
        });


        bt.setOnClickListener(v1 -> {
            String content=changeUnicode(et.getText().toString());
            if(!TextUtils.isEmpty(content)||!commentImagePath.equals("")) {

                MyCommentController myCommentController=new MyCommentController(""+a,currentUserName,MyYouTubeVideoActivity.this);
                myCommentController.addCommentToHostinger(postOwnerId,currentUserId,content,action,CorR,targetToken,commentImagePath);

                et.setText("");
                tv.setVisibility(View.INVISIBLE);
                postList.add(new CommentModel("10",imagePath,currentUserName,content,System.currentTimeMillis()+"",isVip,"","","0","0",""));
                adapter.notifyItemInserted(postList.size());
                recyclerView.smoothScrollToPosition(postList.size());
                notificationController.PushNotiToAdmin(currentUserName+"commented on the video "+videoTitle);
                iv_msg.setImageBitmap(null);
                iv_msg.setVisibility(View.GONE);
                iv_cancel.setVisibility(View.GONE);
                commentImagePath="";

            }else{
                Toast.makeText(MyYouTubeVideoActivity.this, "Write a comment or select a photo", Toast.LENGTH_SHORT).show();
            }

        });




        final LinearLayoutManager lm = new LinearLayoutManager(this){};
        recyclerView.setLayoutManager(lm);
       // recyclerView.addItemDecoration(new SpacingItemDecoration(2, XUtils.toPx(Objects.requireNonNull(this), 2), true));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        adapter = new VideoCommentAdapter(this, postList, timeCheck, (name, id, token_to) -> {

            postOwnerId=id;
            targetToken=token_to;
            et.setText(name+": \n");
            action="4";
            CorR=" reply your comment on the video: ";
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
            et.setSelection(et.getText().length());
            et.requestFocus();
            et.setPadding(10,15,10,15);

        });

        et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(count==0){


                    targetToken="";
                    postOwnerId="10000";

                    action="3";
                    CorR=" commented on the video:";
                    et.setPadding(10,10,10,10);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        recyclerView.setAdapter(adapter);
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setView(v);
        final AlertDialog ad=builder.create();
        ad.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        ad.show();

        fetchPost(timeCheck);

    }

    private boolean isPermissionGranted(){

        int  readExternalStorage=ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        return  readExternalStorage== PackageManager.PERMISSION_GRANTED;

    }

    private void takePermission(){
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},101);
    }

    private void pickImageFromGallery(){
        Intent intent=new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(intent,102);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            if(requestCode==102){
                if(data!=null){
                    Uri uri=data.getData();
                    iv_msg.setVisibility(View.VISIBLE);
                    iv_cancel.setVisibility(View.VISIBLE);
                    iv_msg.setImageURI(uri);
                    if(uri!=null){
                        pickiT.getPath(data.getData(), Build.VERSION.SDK_INT);
                    }else{
                        iv_msg.setImageBitmap(null);
                        iv_msg.setVisibility(View.GONE);
                        iv_cancel.setVisibility(View.GONE);
                        commentImagePath="";
                    }
                }
            }
        }
    }

    private void fetchPost(String time){

        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.POST, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    postExecutor.execute(() -> doAsResult(response));
                }
                @Override
                public void onError(String msg) {}
            }).url(Routing.FETCH_COMMENT)
                    .field("time",time)
                    .field("post_id",a+"")
                    .field("user_id",currentUserId);
            myHttp.runTask();
        }).start();
    }

    private void doAsResult(String response){

        int moveTo=0;
        postList.clear();
        try {
            String postId;
            JSONObject jsonObject=new JSONObject(response);

            String postContent=jsonObject.getString("post");
            JSONArray ja=new JSONArray(postContent);
            JSONObject jo=ja.getJSONObject(0);
            postId=jo.getString("postId");

            String comment=jsonObject.getString("comments");
            JSONArray jaComment=new JSONArray(comment);
            for(int i=0;i<jaComment.length();i++){
                JSONObject joC=jaComment.getJSONObject(i);
                String name=joC.getString("userName");
                String commentBody=joC.getString("body");
                String imageUrl=joC.getString("userImage");
                String time=joC.getString("time");
                String tokenR=joC.getString("userToken");
                String vip=joC.getString("vip");
                String writerId=joC.getString("userId");
                String likes=joC.getString("likes");
                String isLiked=joC.getString("is_liked");
                String commentImage=joC.getString("commentImage");
                CommentModel cModel=new CommentModel(postId,imageUrl,name,commentBody,time,vip,writerId,tokenR,likes,isLiked,commentImage);
                postList.add(cModel);
                if(time.equals(timeCheck))moveTo=i+1;
            }
            pb.setVisibility(View.INVISIBLE);
            adapter.notifyDataSetChanged();
            if(moveTo>postList.size()-1){
                moveTo=postList.size()-1;
            }
            recyclerView.smoothScrollToPosition(moveTo);


        }catch (Exception e){
            pb.setVisibility(View.INVISIBLE);
            tv.setVisibility(View.VISIBLE);
        }
    }




    private void getVideoData(){
        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.POST, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    postExecutor.execute(() -> {
                        try {
                            JSONArray ja=new JSONArray(response);
                            JSONObject jo=ja.getJSONObject(0);
                            String viewCount=jo.getString("view_count");
                            String comments=jo.getString("comments");
                            videoUrl=jo.getString("video_url");
                            int noOfComments=Integer.parseInt(comments);
                            int views=Integer.parseInt(viewCount);
                            tv3.setText(viewCountFormat(views));
                            tv_noOfComment.setText(commentFormat(noOfComments));

                            videoTitle=jo.getString("title");
                            tv1.setText(setMyanmar(videoTitle));

                            isLiked=jo.getString("is_liked");
                            postLikes=jo.getString("postLikes");
                            iv_react.setVisibility(View.VISIBLE);
                            if(isLiked.equals("1")){
                                iv_react.setBackgroundResource(R.drawable.ic_react_love);
                            }

                            reactCount=Integer.parseInt(postLikes);
                            if(reactCount!=0){
                                card_reatCount.setVisibility(View.VISIBLE);
                                tv_reactCount.setText(reactFormat(reactCount));
                            }

                            if(!videoUrl.equals("")){
                                ib_download.setVisibility(View.VISIBLE);

                            }

                        }catch (Exception e){
                            //Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                @Override
                public void onError(String msg) {}
            }).url(Routing.GET_VIDEO_DATA)
                    .field("post_Id",a+"")
                    .field("user_id",currentUserId);
            myHttp.runTask();
        }).start();

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

    private  void showVIPRegistrationDialog(){
        String title="VIP Registration";
        String msg="Your need to register as a VIP to download the video.\n\nDo you want to contact the Calamus Education for VIP Registration";
        MyDialog myDialog=new MyDialog(this, title, msg, () -> {
            Intent intent=new Intent(MyYouTubeVideoActivity.this, WebSiteActivity.class);
            intent.putExtra("link", Routing.PAYMENT);
            startActivity(intent);
        });
        myDialog.showMyDialog();
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
        commentImagePath=path;
    }
}
