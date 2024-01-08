package com.calamus.easykorean.holders;

import static com.calamus.easykorean.app.AppHandler.formatTime;
import static com.calamus.easykorean.app.AppHandler.reactFormat;
import static com.calamus.easykorean.app.AppHandler.setMyanmar;
import static com.calamus.easykorean.app.AppHandler.viewCountFormat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.calamus.easykorean.CommentActivity;
import com.calamus.easykorean.LikeListActivity;
import com.calamus.easykorean.MyDiscussionActivity;
import com.calamus.easykorean.PhotoActivity;
import com.calamus.easykorean.R;
import com.calamus.easykorean.VimeoPlayerActivity;
import com.calamus.easykorean.WritePostActivity;
import com.calamus.easykorean.app.AppHandler;
import com.calamus.easykorean.app.MyDialog;
import com.calamus.easykorean.app.Routing;
import com.calamus.easykorean.controller.LikeController;
import com.calamus.easykorean.controller.NotificationController;
import com.calamus.easykorean.models.NewfeedModel;
import com.calamus.easykorean.app.MyHttp;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.concurrent.Executor;

public class PostHolder extends RecyclerView.ViewHolder {
    TextView tv_userName,tv_time,tv_body,tv_read_more,tvCount,tv_share;
    TextView tv_comment,tv_react;
    ImageView iv_profile,iv_post,iv_blueMark;
    LinearLayout iv_play_cycle;
    RelativeLayout layout_share;
    ImageButton bt_iv_more;
    CardView bottomCard;

    // share content
    Executor postExecutor;
    Activity c;
    String currentUserId,currentUserName,currentUserImage;
    String dbdir;
    String dbPath;
    SQLiteDatabase db;

    NewfeedModel shareContent=null;
    public CallBack callBack;
    View view;

    public PostHolder(@NonNull View view, Activity c, String currentUserId, String currentUserName, String currentUserImage) {
        super(view);
        this.c=c;
        this.currentUserId=currentUserId;
        this.currentUserName=currentUserName;
        this.view=view;
        this.currentUserImage=currentUserImage;

        postExecutor=ContextCompat.getMainExecutor(c);
        dbdir=c.getFilesDir().getPath()+"/databases/";
        dbPath=dbdir+"post.db";
        db=SQLiteDatabase.openDatabase(dbPath,null,SQLiteDatabase.OPEN_READWRITE);

        tv_time = view.findViewById(R.id.tv_time);
        tv_read_more=view.findViewById(R.id.tv_readmore);
        iv_profile = view.findViewById(R.id.iv_profile);
        tv_userName=view.findViewById(R.id.tv_username2);
        tv_body=view.findViewById(R.id.tv_body);
        iv_post=view.findViewById(R.id.iv_post_image);
        iv_blueMark=view.findViewById(R.id.iv_blueMark);
        tv_comment=view.findViewById(R.id.tv_comment);
        tv_share=view.findViewById(R.id.tv_share);
        bottomCard=view.findViewById(R.id.bottom_Card);


        iv_play_cycle=view.findViewById(R.id.video_cycle);
        bt_iv_more=view.findViewById(R.id.bt_iv_more);
        tvCount=view.findViewById(R.id.tv_view_count);
        tv_react=view.findViewById(R.id.tv_react);
        layout_share=view.findViewById(R.id.layout_share);

        iv_post.setClipToOutline(true);




    }


    public void setNewsfeedModel(NewfeedModel model) {
        bindData(model);
        setUIEvents(model);

        if(model.getShare()!=0){
            getShareContent(model.getShare()+"");
        }else{
            layout_share.setVisibility(View.GONE);
            bottomCard.setVisibility(View.GONE);
        }

    }

    private void bindData(NewfeedModel model){
        try {


            long time=Long.parseLong(model.getPostId());

            tv_time.setText(formatTime(time));
            tv_userName.setText(setMyanmar(model.getUserName()));


            iv_profile.setVisibility(View.GONE);

            if(!model.getUserImage().equals("")){
                iv_profile.setVisibility(View.VISIBLE);
                AppHandler.setPhotoFromRealUrl(iv_profile,model.getUserImage());
            }else{
                iv_profile.setBackgroundResource(R.drawable.ic_baseline_account_circle_24);

            }
            iv_blueMark.setVisibility(View.GONE);
            iv_post.setVisibility(View.GONE);

            if(!model.getPostImage().equals("")){
                iv_post.setVisibility(View.VISIBLE);
                AppHandler.setPhotoFromRealUrl(iv_post,model.getPostImage());
            }else{
                iv_post.setVisibility(View.GONE);
            }

            iv_play_cycle.setVisibility(View.GONE);

            if(model.getIsVideo().equals("1")){
                iv_play_cycle.setVisibility(View.VISIBLE);
            }

            int views=Integer.parseInt(model.getViewCount());
            if(views==0)tvCount.setText("");
            else tvCount.setText(viewCountFormat(views));

            if(model.getPostBody().length()>200){
                String sub=model.getPostBody().substring(0,200);
                tv_body.setText(setMyanmar(sub));
                tv_read_more.setVisibility(View.VISIBLE);
            }else{
                tv_body.setText(setMyanmar(model.getPostBody()));
                tv_read_more.setVisibility(View.GONE);
            }

            tv_read_more.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    tv_body.setText(setMyanmar(model.getPostBody()));
                    tv_read_more.setVisibility(View.INVISIBLE);
                }
            });

            if(model.getIsVip().equals("1")){
                iv_blueMark.setVisibility(View.VISIBLE);
            }

            int commentNumber=Integer.parseInt(model.getPostComments());

            tv_comment.setText(reactFormat(commentNumber));
            tv_react.setText(reactFormat(Integer.parseInt(model.getPostLikes())));

            tv_react.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_normal_react, 0, 0, 0);
            if(model.getIsLike().equals("1")){
                tv_react.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_react_love, 0, 0, 0);
            }



            tv_react.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    LikeController.likeThePost(currentUserId,model.getPostId());

                    if(model.getIsLike().equals("1")){
                        tv_react.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_normal_react, 0, 0, 0);

                        int rectCount=Integer.parseInt(model.getPostLikes());
                        rectCount--;
                        if(rectCount>0){
                            tv_react.setText(reactFormat(rectCount));
                            tv_react.setVisibility(View.VISIBLE);
                        }else {
                            tv_react.setText("");
                        }

                        model.setIsLike("0");
                        model.setPostLikes(rectCount+"");

                    }else{

                        tv_react.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_react_love, 0, 0, 0);
                        int rectCount=Integer.parseInt(model.getPostLikes());
                        rectCount++;
                        tv_react.setText(reactFormat(rectCount));
                        tv_react.setVisibility(View.VISIBLE);

                        if(!model.getUserId().equals(currentUserId)){
                            NotificationController notificationController=new NotificationController(c);
                            notificationController.sendNotification(currentUserName+" reacted your post.",model.getUserToken(),Routing.APP_NAME,"1");
                        }
                        model.setIsLike("1");
                        model.setPostLikes(rectCount+"");
                    }

                }
            });

            if(model.getShareCount()==0){
                tv_share.setText("");
            }else{
                tv_share.setText(model.getShareCount()+"");
            }

            tv_share.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String info="Share "+model.getUserName()+"'s discussion";
                    showSharePostDialog(info,model.getPostId());
                }
            });

        } catch (Exception e) {
            // Toast.makeText(c, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }




    private void setUIEvents(NewfeedModel model){


        iv_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               if(model.getShare()!=0){
                  if(shareContent!=null){
                      if(shareContent.getIsVideo().equals("1")){
                          goVideo(shareContent);
                      }else {
                          goPhoto(shareContent.getPostImage(),shareContent.getPostBody(),iv_post);
                      }
                  }
               }else{
                   if(model.getIsVideo().equals("1")){
                       goVideo(model);
                   }else {
                       goPhoto(model.getPostImage(),model.getPostBody(),iv_post);
                   }
               }
            }
        });

        iv_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(c, MyDiscussionActivity.class);
                intent.putExtra("userId",model.getUserId());
                intent.putExtra("userName",model.getUserName());
                c.startActivity(intent);
            }
        });

        bt_iv_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMenu(v,model,getAbsoluteAdapterPosition());
            }
        });

        tv_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goTO(model);
            }
        });



        tv_userName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(c, MyDiscussionActivity.class);
                intent.putExtra("userId",model.getUserId());
                intent.putExtra("userName",model.getUserName());
                c.startActivity(intent);
            }
        });
    }

    private void goTO(NewfeedModel model){

        if(model.getIsVideo().equals("0")){
            Intent intent=new Intent(c, CommentActivity.class);
            intent.putExtra("postId",model.getPostId());
            intent.putExtra("time",""+0);//for comment seen
            c.startActivity(intent);
        }else{

            goVideo(model);

        }

    }

    private void goVideo(NewfeedModel model){
        String videoId=model.getPostImage().substring(model.getPostImage().indexOf("vi/")+3);
        videoId=videoId.substring(0,videoId.length()-6);
        Intent intent=new Intent(c, VimeoPlayerActivity.class);
        intent.putExtra("videoId",videoId);
        intent.putExtra("time",Long.parseLong(model.getPostId()));
        intent.putExtra("post_description",model.getPostBody());
        c.startActivity(intent);
    }

    private void goPhoto(String imageUrl, String des,ImageView iv){
        Intent intent=new Intent(c, PhotoActivity.class);
        intent.putExtra("image",imageUrl);
        intent.putExtra("des",des);
       // ActivityOptionsCompat optionsCompat=ActivityOptionsCompat.makeSceneTransitionAnimation(c,iv, ViewCompat.getTransitionName(iv));
        c.startActivity(intent);
    }

    private void showMenu(View v,NewfeedModel model,int position){
        PopupMenu popup=new PopupMenu(c,v);
        if(("0"+model.getUserId()).equals(currentUserId)||model.getUserId().equals(currentUserId)){
            popup.getMenuInflater().inflate(R.menu.pop_up_private_menu,popup.getMenu());
        }else {
            popup.getMenuInflater().inflate(R.menu.pop_up_public_menu,popup.getMenu());
        }
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @SuppressLint("Recycle")
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id=item.getItemId();
                if(id==R.id.report){
                    deletePostOrReport(model.getPostId(),Routing.REPORT_POST,position);
                }else if(id==R.id.save){
                    savePost(model.getPostId(),model.getPostBody(),model.getPostImage(),model.getUserName(),model.getUserImage(),model.getIsVideo());
                }else if(id==R.id.delete){
                    showDeleteDialog(model.getPostId(),position);
                }else if(id==R.id.edit){
                    editPost(model.getPostBody(),model.getPostImage(),model.getPostId());
                }else if(id==R.id.react){
                    showReact(model);
                }else if(id==R.id.hide_post){
                    showHidePostDialog(model.getPostId());
                }

                return true;
            }
        });
        popup.show();
    }

    private void showHidePostDialog(String postId){
        MyDialog myDialog=new MyDialog(c, "Hide Post!", "Do you really want to hide this post", new MyDialog.ConfirmClick() {
            @Override
            public void onConfirmClick() {
                hidePost(postId);
            }
        });
        myDialog.showMyDialog();
    }

    private void hidePost(String postId){
        callBack.onPostDelete();
        Executor postExecutor= ContextCompat.getMainExecutor(c);
        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.POST, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    postExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(c,response,Toast.LENGTH_SHORT).show();
                        }
                    });

                }
                @Override
                public void onError(String msg) {
                    Log.e("Post hide err ",msg);
                }
            }).url(Routing.HIDE_POST)
                    .field("post_id",postId)
                    .field("user_id",currentUserId);
            myHttp.runTask();
        }).start();
    }

    private void showReact(NewfeedModel model){
            Intent intent=new Intent(c, LikeListActivity.class);
            intent.putExtra("contentId",model.getPostId());
            intent.putExtra("fetch", Routing.FETCH_POST_LIKE);
            c.startActivity(intent);
    }

    private void showDeleteDialog(String postId,int position){

        MyDialog myDialog=new MyDialog(c, "Delete Post!", "Do you really want to delete", new MyDialog.ConfirmClick() {
            @Override
            public void onConfirmClick() {
                deletePostOrReport(postId,Routing.DELETE_POST,position);
                if(callBack!=null){
                    callBack.onPostDelete();
                }
            }
        });
        myDialog.showMyDialog();
    }

    private void deletePostOrReport(String postId,String link,int position){

        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.POST, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    postExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(c,response,Toast.LENGTH_SHORT).show();
                        }
                    });

                }
                @Override
                public void onError(String msg) {}
            }).url(link)
                    .field("postId",postId);
            myHttp.runTask();
        }).start();
    }

    public void getShareContent(String postId){

        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.GET, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    postExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            try {

                                JSONArray ja=new JSONArray(response);
                                JSONObject jo=ja.getJSONObject(0);
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

                                long share= 0;
                                int shareCount=jo.getInt("shareCount");
                                NewfeedModel model = new NewfeedModel(userName,userId,userToken,userImage,postId,postBody,posLikes,postComment,postImage,isVip,isVideo,viewCount,"0",shareCount,share);
                                setShareContentView(model);
                                shareContent=model;
                                layout_share.setVisibility(View.VISIBLE);

                            }catch (Exception e){
                                Log.e("ShareContentJSONErr ",e.toString());
                            }

                        }
                    });

                }
                @Override
                public void onError(String msg) {
                    Log.e("ShareResErr ",msg);
                }
            }).url(Routing.GET_SHARE_CONTENT+"?mCode="+Routing.MAJOR_CODE+"&postId="+postId);
            myHttp.runTask();
        }).start();
    }

    private void setShareContentView(NewfeedModel model){
        TextView tv_share_username,tv_share_time,tv_share_read_more,tv_share_body;
        ImageView iv_share_profile,iv_share_blue_mark;


        tv_share_username=view.findViewById(R.id.tv_share_username);
        tv_share_body=view.findViewById(R.id.tv_share_body);
        tv_share_time=view.findViewById(R.id.tv_share_time);
        tv_share_read_more=view.findViewById(R.id.tv_share_read_more);

        iv_share_profile=view.findViewById(R.id.iv_share_profile);
        iv_share_blue_mark=view.findViewById(R.id.iv_share_blue_mark);


        long time=Long.parseLong(model.getPostId());

        tv_share_time.setText(formatTime(time));
        tv_share_username.setText(setMyanmar(model.getUserName()));
        //   postHolder.tv_body.setText(setMyanmar(model.getPostBody()));

        if(!model.getUserImage().equals("")){
            AppHandler.setPhotoFromRealUrl(iv_share_profile,model.getUserImage());
        }else{
            iv_share_profile.setBackgroundResource(R.drawable.ic_baseline_account_circle_24);
        }
        iv_share_blue_mark.setVisibility(View.GONE);

        if(model.getIsVip().equals("1")){
            iv_share_blue_mark.setVisibility(View.VISIBLE);
        }

        if(!model.getPostImage().equals("")){
            iv_post.setVisibility(View.VISIBLE);
            bottomCard.setVisibility(View.VISIBLE);
            AppHandler.setPhotoFromRealUrl(iv_post,model.getPostImage());
        }else{
            iv_post.setVisibility(View.GONE);
            bottomCard.setVisibility(View.GONE);
        }

        iv_play_cycle.setVisibility(View.GONE);

        if(model.getIsVideo().equals("1")){
            iv_play_cycle.setVisibility(View.VISIBLE);
        }


        if(model.getPostBody().length()>200){
            String sub=model.getPostBody().substring(0,200);
            tv_share_body.setText(setMyanmar(sub));
            tv_share_read_more.setVisibility(View.VISIBLE);
        }else{
            tv_share_read_more.setVisibility(View.GONE);
            tv_share_body.setText(setMyanmar(model.getPostBody()));
        }

        layout_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goTO(model);
            }
        });
    }

    private void savePost(String postId,String postBody, String postImage,String ownerName,String ownerImage,String isVideo){
        ContentValues cv=new ContentValues();
        cv.put("post_id",postId);
        cv.put("post_body",postBody);
        cv.put("post_image",postImage);
        cv.put("owner_name",ownerName);
        cv.put("owner_image",ownerImage);
        cv.put("isVideo",isVideo);
        db.insert("SavePost",null,cv);
        Toast.makeText(c,"Saved",Toast.LENGTH_SHORT).show();
    }

    private void editPost(String postBody,String postImage,String postId){
        Intent i=new Intent(c, WritePostActivity.class);
        i.putExtra("action",false);
        i.putExtra("postBody",postBody);
        i.putExtra("postImage",postImage);
        i.putExtra("postId",postId);
        c.startActivity(i);
    }

    public void setCallBack(CallBack callBack) {
        this.callBack = callBack;
    }

    public interface  CallBack{
        void onPostDelete();
    }

    private void showSharePostDialog(String shareInfo,String shareId){

        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(c,R.style.SheetDialog);
        bottomSheetDialog.setContentView(R.layout.dialog_share_post);

        ImageView iv_profile;
        TextView tv_name,tv_share_info;
        EditText et_share;
        CardView card_share_now;

        iv_profile=bottomSheetDialog.findViewById(R.id.iv_profile);
        tv_name=bottomSheetDialog.findViewById(R.id.tv_name);
        tv_share_info=bottomSheetDialog.findViewById(R.id.tv_share_info);
        et_share=bottomSheetDialog.findViewById(R.id.et_share);
        card_share_now=bottomSheetDialog.findViewById(R.id.card_share_now);

        AppHandler.setPhotoFromRealUrl(iv_profile,currentUserImage);
        tv_name.setText(currentUserName);
        tv_share_info.setText(shareInfo);

        card_share_now.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sharePost(currentUserId,et_share.getText().toString(),shareId);
                bottomSheetDialog.dismiss();
            }
        });


        bottomSheetDialog.show();

    }

    private void sharePost(String learner_id,String body,String shareId){

        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.POST, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    postExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                           Toast.makeText(c,"Shared",Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                @Override
                public void onError(String msg) {

                }
            }).url(Routing.ADD_POST)
                    .field("learner_id",learner_id)
                    .field("body",body)
                    .field("major",Routing.MAJOR)
                    .field("share",shareId)
                    .field("hasVideo","0");
            myHttp.runTask();
        }).start();


    }

}
