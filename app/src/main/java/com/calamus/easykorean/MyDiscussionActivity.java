package com.calamus.easykorean;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.calamus.easykorean.app.MyDialog;
import com.google.android.material.appbar.AppBarLayout;
import com.calamus.easykorean.adapters.DiscussAdapter;
import com.calamus.easykorean.app.AppHandler;
import com.calamus.easykorean.app.Routing;
import com.calamus.easykorean.holders.ProfileHolders;
import com.calamus.easykorean.interfaces.OnProfileLoaded;
import com.calamus.easykorean.models.NewfeedModel;
import com.calamus.easykorean.app.MyHttp;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.Executor;


public class MyDiscussionActivity extends AppCompatActivity {

    //profile view
    public static RecyclerView recycler;

    DiscussAdapter adapter;
    ArrayList<Object> postList = new ArrayList<>();
    SharedPreferences share;

    int count=1;
    private boolean loading=true;
    int visibleItemCount,totalItemCount;
    public static int pastVisibleItems;
    LinearLayoutManager lm;
    Executor postExecutor;
    String currentUserId,userId,userName,myBio,profileUrl,coverUrl=null;
    boolean isVip;

    ImageView iv_cover_photo,iv_profile,iv_blueMark,iv_back,iv_edit_cover_photo;
    TextView tv_add_cover;
    RelativeLayout layout_profile_pic;
    ProgressBar pb;
    float centerX;
    int picInitWidth,picInitHeight,distanceBetweenProfileAndBackKey,targetProfileX,initialProfileX,
            displacementX,verticalOffsetRange,targetProfileY,actionBarOffset;

    boolean vipProfile=false;
    Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_discussion);
        share=getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        isVip=share.getBoolean("isVIP",false);
        currentUserId=share.getString("phone","");

        postExecutor= ContextCompat.getMainExecutor(this);
        userId=getIntent().getExtras().getString("userId","");
        userName=getIntent().getExtras().getString("userName","");

        picInitWidth=getResources().getDimensionPixelSize(R.dimen.profile_dimension);
        picInitHeight=getResources().getDimensionPixelSize(R.dimen.profile_dimension);
        distanceBetweenProfileAndBackKey=getResources().getDimensionPixelSize(R.dimen.profile_to_backkey_distance);
        verticalOffsetRange=getResources().getDimensionPixelSize(R.dimen.cover_photo_height);
        actionBarOffset=getResources().getDimensionPixelSize(R.dimen.actionbar_offset);


        setupViews();
        Objects.requireNonNull(getSupportActionBar()).hide();
        testFetch(count,true);

    }

    private void setupViews() {

        recycler = findViewById(R.id.recycler_nf);
        iv_cover_photo=findViewById(R.id.iv_cover_photo);
        iv_profile=findViewById(R.id.iv_profile);
        tv_add_cover=findViewById(R.id.tv_add_cover_photo);
        iv_blueMark=findViewById(R.id.iv_blueMark);
        layout_profile_pic=findViewById(R.id.layout_profile_picture);
        iv_back=findViewById(R.id.iv_back);
        iv_edit_cover_photo=findViewById(R.id.iv_edit_cover);
        toolbar=findViewById(R.id.toolbar);
        pb=findViewById(R.id.pb);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager windowmanager = (WindowManager)getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        windowmanager.getDefaultDisplay().getMetrics(displayMetrics);
        int deviceWidth = displayMetrics.widthPixels;
        centerX=deviceWidth/2.0f;

        targetProfileX=iv_back.getRight()+distanceBetweenProfileAndBackKey;
        initialProfileX=(int)centerX-(picInitWidth/2);
        displacementX=initialProfileX-targetProfileX;


        lm = new LinearLayoutManager(this){};
        recycler.setLayoutManager(lm);
        // recycler.addItemDecoration(new SpacingItemDecoration(2, XUtils.toPx(Objects.requireNonNull(tth), 2), true));
        recycler.setItemAnimator(new DefaultItemAnimator());

        View view = LayoutInflater.from(this).inflate(R.layout.item_profile,null , false);
        ProfileHolders profileHolder=new ProfileHolders(view, this, currentUserId, userId, new OnProfileLoaded() {
            @Override
            public void onLoaded(String cover_url, String profile_url,String bio,boolean vip) {
                vipProfile=vip;
                myBio=bio;
                profileUrl=profile_url;

                if(cover_url.equals("")){
                    if(currentUserId.equals(userId)) tv_add_cover.setVisibility(View.VISIBLE);
                }else{
                    AppHandler.setPhotoFromRealUrl(iv_cover_photo,cover_url);
                    if(currentUserId.equals(userId)) {
                        iv_edit_cover_photo.setImageResource(R.drawable.ic_baseline_camera_alt_24);
                        iv_edit_cover_photo.setVisibility(View.VISIBLE);
                    }
                    coverUrl=cover_url;
                }

                if(!profile_url.equals("")) AppHandler.setPhotoFromRealUrl(iv_profile,profile_url);
                if(vip)iv_blueMark.setVisibility(View.VISIBLE);
            }
        });

        iv_edit_cover_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isPermissionGranted()){
                    pickImageFromGallery();
                }else {
                    takePermission();
                }
            }
        });

        iv_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MyDiscussionActivity.this,PhotoActivity.class);
                intent.putExtra("image",profileUrl);
                startActivity(intent);
            }
        });

        iv_cover_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(coverUrl!=null){
                    Intent intent=new Intent(MyDiscussionActivity.this,PhotoActivity.class);
                    intent.putExtra("image",coverUrl);
                    startActivity(intent);
                }
            }
        });

        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        tv_add_cover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isPermissionGranted()){
                    pickImageFromGallery();
                }else {
                    takePermission();
                }
            }
        });

        changeProfilePictureDimensions(picInitWidth,picInitHeight);

        AppBarLayout mAppBarLayout =findViewById(R.id.app_bar_layout);
        mAppBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            boolean b=true;
            float pictureX;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {

                if(b){
                    pictureX=layout_profile_pic.getX();
                    Log.e("verticalOffset ",verticalOffset+"");
                    b=false;
                }else{
                    float picWidth=picInitWidth/2.0f;
                    float floatingX=centerX+calculateX(verticalOffset)-picWidth;
                    if(floatingX>iv_back.getRight()){
                        layout_profile_pic.setX(floatingX);
                    }else{
                        layout_profile_pic.setX(targetProfileX);
                    }
                }

                if(verticalOffset<-100) {
                    isShow = true;
                    iv_blueMark.setVisibility(View.INVISIBLE);
                    targetProfileY=toolbar.getHeight()/2 - (int)(picInitWidth*0.366f)/2+actionBarOffset;
                    Log.e("ToolBarHeight ",toolbar.getHeight()+"");
                    animateProfile( targetProfileY,0.366f);


                } else if (isShow) {
                    isShow = false;
                    if(vipProfile)iv_blueMark.setVisibility(View.VISIBLE);
                    animateProfile(0,1f);

                }

            }
        });

        adapter = new DiscussAdapter(this,profileHolder, postList,userId);
        recycler.setAdapter(adapter);

        recycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                pastVisibleItems=lm.findFirstVisibleItemPosition();
                if(dy>0){
                    visibleItemCount=lm.getChildCount();
                    totalItemCount=lm.getItemCount();

                    if(loading){
                        if((visibleItemCount+pastVisibleItems)>=totalItemCount-7){
                            loading=false;
                            count++;
                            testFetch(count,false);

                        }
                    }
                }
            }
        });
    }

    private void pickImageFromGallery(){
        mGetContent.launch("image/*");
    }

    private boolean isPermissionGranted(){
        int  readExternalStorage;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            readExternalStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES);
        }else{
            readExternalStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        return  readExternalStorage==PackageManager.PERMISSION_GRANTED;
    }

    private void takePermission(){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_MEDIA_IMAGES},101);
        }else{
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},101);
        }

    }


    ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri uri) {
                    if(uri!=null){
                        Intent intent=new Intent(MyDiscussionActivity.this,UpdateCoverPhotoActivity.class);
                        intent.putExtra("imageUri", uri.toString());
                        intent.putExtra("myBio",myBio);
                        intent.putExtra("userId",currentUserId);
                        intent.putExtra("profileUrl",profileUrl);
                        intent.putExtra("username",userName);
                        startActivity(intent);
                    }else{
                        Toast.makeText(getApplicationContext(),"No photo is selected!",Toast.LENGTH_SHORT).show();
                    }
                }
            });


    private void testFetch(int count,boolean isRefresh){
      //  Log.e("Route ",Routing.FETCH_A_USER_POST+"?userId="+userId+"&page="+count+"&mCode="+Routing.MAJOR_CODE+"&myId="+currentUserId);
        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.GET, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    postExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            if (isRefresh){
                                postList.clear();
                                postList.add(0,"kaung");
                                pb.setVisibility(View.GONE);

                            }
                            doAsResult(response);
                            // Toast.makeText(getApplicationContext(),response,Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                @Override
                public void onError(String msg) {
                    postExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            //Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }).url(Routing.FETCH_A_USER_POST+"?userId="+userId+"&page="+count+"&mCode="+Routing.MAJOR_CODE+"&myId="+currentUserId);
            myHttp.runTask();
        }).start();
    }

    public void doAsResult(String response){

        try {
            JSONObject joPosts=new JSONObject(response);
            JSONArray ja=joPosts.getJSONArray("posts");
            int jL=ja.length();
            if(jL>0){
                for(int i=0;i<ja.length();i++){
                    JSONObject jo=ja.getJSONObject(i);
                    String userName=jo.getString("userName");
                    String userId=jo.getString("userId");
                    String userToken=jo.getString("userToken");
                    String userImage=jo.getString("userImage");
                    String postId=jo.getString("postId");
                    String postBody=jo.getString("body");
                    String posLikes=jo.getString("postLikes");
                    String postComment=jo.getString("comments");
                    String postImage=jo.getString("postImage");
                    String isVip=jo.getString("vip");
                    String isVideo=jo.getString("has_video");
                    String viewCount=jo.getString("viewCount");
                    String isLike=jo.getString("is_liked");
                    int hidden=jo.getInt("hidden");
                    int blocked=jo.getInt("blocked");
                    long share=jo.getLong("share");
                    int shareCount=jo.getInt("shareCount");
                    Log.e("discuss post id ",postId);
                    NewfeedModel model = new NewfeedModel(userName,userId,userToken,userImage,postId,postBody,posLikes,postComment,postImage,isVip,isVideo,viewCount,isLike,shareCount,share);
                    if(hidden!=1 && blocked!=1) postList.add(model);
                }

                //if(!isVip)loadNativeAds();
                adapter.notifyDataSetChanged();
                loading=true;
            }else {
                loading=false;
            }
        }catch (Exception e){
            loading=false;
            adapter.notifyDataSetChanged();
            Log.e("DiscussErr ",e.toString());
        }

    }

    private void changeProfilePictureDimensions(int width,int height){
        android.view.ViewGroup.LayoutParams layoutParams = iv_profile.getLayoutParams();
        layoutParams.width = width;
        layoutParams.height = height;
        iv_profile.setLayoutParams(layoutParams);

    }

    private void animateProfile(int y,float size){
        ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(iv_profile, "scaleX", size);
        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(iv_profile, "scaleY", size);
        scaleDownX.setDuration(300);
        scaleDownY.setDuration(300);

//        ObjectAnimator moveX = ObjectAnimator.ofFloat(layout_profile_pic, "translationX", x);
        ObjectAnimator moveY = ObjectAnimator.ofFloat(layout_profile_pic, "translationY", y);
        //  moveX.setDuration(50);
        moveY.setDuration(300);

        AnimatorSet scaleDown = new AnimatorSet();
        AnimatorSet move = new AnimatorSet();

        scaleDown.play(scaleDownX).with(scaleDownY);
        // move.play(moveX);
        move.play(moveY);

        scaleDown.start();
        move.start();
    }

    private float calculateX(int offset){
        float result;
        result =((float) displacementX/verticalOffsetRange)*offset;
        Log.e(offset+" cal Result ",result+"");
        return result;
    }
}

