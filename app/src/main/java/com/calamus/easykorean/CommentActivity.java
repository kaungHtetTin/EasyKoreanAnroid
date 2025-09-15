package com.calamus.easykorean;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.calamus.easykorean.app.MyImagePicker;
import com.hbisoft.pickit.PickiT;
import com.hbisoft.pickit.PickiTCallbacks;
import com.calamus.easykorean.adapters.CommentAdapter;
import com.calamus.easykorean.app.AppHandler;
import com.calamus.easykorean.app.Routing;
import com.calamus.easykorean.controller.MyCommentController;
import com.calamus.easykorean.models.CommentModel;
import com.calamus.easykorean.models.NewfeedModel;
import com.calamus.easykorean.app.MyHttp;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.Executor;

public class CommentActivity extends AppCompatActivity implements PickiTCallbacks {

    RecyclerView recycler;
    SwipeRefreshLayout swipe;
    CommentAdapter adapter;
    ArrayList<Object> postList = new ArrayList<>();
    ArrayList<CommentModel> parentComments=new ArrayList<>();
    ArrayList<CommentModel> childComments=new ArrayList<>();
    String postId,postOwnerId;
    ImageView iv_pickup,iv_cancel,iv_msg;
    SharedPreferences sharedPreferences;
    String currentUserId,currentUserName;
    String imagePath,commentImagePath="";
    ImageButton ibt;
    String timeCheck,isVip="0";
    EditText et;
    Executor postExecutor;
    String postOwnerToken,action,CorR;
    PickiT pickiT;
    String parentCommentID="0";
    MyImagePicker myImagePicker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
        sharedPreferences=getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        postId= getIntent().getExtras().getString("postId");
        timeCheck=getIntent().getExtras().getString("time");
        pickiT = new PickiT(this, this, this);
        currentUserId=sharedPreferences.getString("phone",null);
        currentUserName=sharedPreferences.getString("Username",null);
        imagePath=sharedPreferences.getString("imageUrl","");
        boolean b=sharedPreferences.getBoolean("isVIP",false);
        myImagePicker = new MyImagePicker(this);
        postExecutor= ContextCompat.getMainExecutor(this);

        if(b)isVip="1";

        setUpView();
        setUpCustomAppBar();
        Objects.requireNonNull(getSupportActionBar()).hide();

        action="0";
        CorR=" commented on your post";


    }

    private void setUpCustomAppBar(){
        TextView tv=findViewById(R.id.tv_appbar);
        ImageView iv=findViewById(R.id.iv_back);
        tv.setText("Comments");
        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void setUpView(){
        swipe=findViewById(R.id.swipe_nf);
        recycler=findViewById(R.id.recycler_nf);
        iv_pickup=findViewById(R.id.iv_small_profile);
        ibt=findViewById(R.id.nf_dia_bt);
        et=findViewById(R.id.et_cmt);
        iv_cancel=findViewById(R.id.iv_cancel);
        iv_msg=findViewById(R.id.iv_msg);
        iv_msg.setClipToOutline(true);
        final LinearLayoutManager lm = new LinearLayoutManager(this){};
        recycler.setLayoutManager(lm);

        recycler.setItemAnimator(new DefaultItemAnimator());

        adapter = new CommentAdapter(this, postList, timeCheck, (commentID,name, id, token_to) -> {

            postOwnerId=id;
            postOwnerToken=token_to;
            parentCommentID=commentID;
            et.setText(name+": \n");
            action="1";
            CorR=" reply your comment on the post: ";
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
            et.setSelection(et.getText().length());
            et.requestFocus();
            et.setPadding(10,15,10,15);

            Log.e("Parent ID ",parentCommentID);

        });
        recycler.setAdapter(adapter);
        swipe.setRefreshing(true);

        fetchCommentFromHostinger(timeCheck,false);

        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                fetchCommentFromHostinger(""+0,true);
            }
        });


        iv_pickup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myImagePicker.pick(new MyImagePicker.Callback() {
                    @Override
                    public void onResult(Uri uri) {
                        if(uri!=null){
                            iv_msg.setVisibility(View.VISIBLE);
                            iv_cancel.setVisibility(View.VISIBLE);
                            iv_msg.setImageURI(uri);
                            pickiT.getPath(uri, Build.VERSION.SDK_INT);
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
                commentImagePath="";
            }
        });



        et.setOnClickListener(v -> recycler.smoothScrollToPosition(postList.size()-1));

        ibt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content=et.getText().toString();
                if(!TextUtils.isEmpty(content)||!commentImagePath.equals("")){

                    MyCommentController myCommentController=new MyCommentController(postId,currentUserName,CommentActivity.this);
                    myCommentController.addCommentToHostinger(postOwnerId,currentUserId,content,action,CorR,postOwnerToken,commentImagePath,parentCommentID);
                    et.setText("");
                    postList.add(new CommentModel(postId,parentCommentID,imagePath,currentUserName,content,System.currentTimeMillis()+"",isVip,"","","0","0",""));
                    adapter.notifyItemInserted(postList.size());
                    recycler.smoothScrollToPosition(postList.size());
                    parentCommentID="0";
                    iv_msg.setImageBitmap(null);
                    iv_msg.setVisibility(View.GONE);
                    iv_cancel.setVisibility(View.GONE);
                    commentImagePath="";

                }else{
                    Toast.makeText(getApplicationContext(),"Please write a comment or select a photo",Toast.LENGTH_SHORT
                    ).show();
                }
            }
        });

        et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(count==0){
                    if(postList.size()>0){  // this condition for poor connection and not loaded the post
                        NewfeedModel pModel=(NewfeedModel) postList.get(0);
                        postOwnerToken=pModel.getUserToken();
                        postOwnerId=pModel.getUserId();
                        parentCommentID="0";
                        action="0";
                        CorR=" commented on your post";
                        et.setPadding(10,10,10,10);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    private void fetchCommentFromHostinger(String time ,boolean isRefresh){
        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.GET, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    postExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            if(isRefresh) {
                                postList.clear();
                                childComments.clear();
                                parentComments.clear();
                            }
                            doAsResult(response);
                        }
                    });
                }
                @Override
                public void onError(String msg) {

                    Log.e("CmtErro : ",msg);
                }
            }).url(Routing.FETCH_COMMENT+"?mCode="+Routing.MAJOR_CODE+"&postId="+postId+"&time="+time+"&userId="+currentUserId);
            myHttp.runTask();
        }).start();
    }


    private void doAsResult(String response){
        swipe.setRefreshing(false);
        int moveTo=0;
        try {
            JSONObject jsonObject=new JSONObject(response);
            String postContent=jsonObject.getString("post");
            String comment=jsonObject.getString("comments");
            JSONArray ja=new JSONArray(postContent);
            JSONObject jo=ja.getJSONObject(0);
            String userName=jo.getString("userName");
            postOwnerId=jo.getString("userId");
            postOwnerToken=jo.getString("userToken");
            String userImage=jo.getString("userImage");
            String postId=jo.getString("postId");
            String postBody=jo.getString("body");
            String posLikes=jo.getString("postLikes");
            String postComment=jo.getString("comments");
            String postImage=jo.getString("postImage");
            String isVip=jo.getString("vip");
            String isLike=jo.getString("is_liked");
            long share=jo.getLong("share");
            int shareCount=jo.getInt("shareCount");
            NewfeedModel model = new NewfeedModel(userName,postOwnerId,postOwnerToken,userImage,postId,postBody,posLikes,postComment,postImage,isVip,"0","0",isLike,shareCount,share);
            postList.add(0,model);
            adapter.notifyDataSetChanged();

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
                String parentId=joC.getString("parent");
                CommentModel cModel=new CommentModel(postId,parentId,imageUrl,name,commentBody,time,vip,writerId,tokenR,likes,isLiked,commentImage);
                if(parentId.equals("0")) parentComments.add(cModel);
                else childComments.add(cModel);
                if(time.equals(timeCheck))moveTo=i+1;
            }

            sortingComment();
            adapter.notifyDataSetChanged();
            if(moveTo>postList.size()-1){
                moveTo=postList.size()-1;
            }
            recycler.smoothScrollToPosition(moveTo);

        }catch (Exception e){
            Log.e("CmtErrJson : ",e.toString());
        }
    }

    private void sortingComment(){
        for(int i=0;i<parentComments.size();i++){
            CommentModel parent=parentComments.get(i);
            postList.add(parent);
            for(int j=0;j<childComments.size();j++){
                CommentModel child=childComments.get(j);
                if(parent.getTime().equals(child.getParentId())){
                    postList.add(child);
                }
            }
        }
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