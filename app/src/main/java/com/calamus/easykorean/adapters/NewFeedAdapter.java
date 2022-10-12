package com.calamus.easykorean.adapters;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.calamus.easykorean.CommentActivity;
import com.calamus.easykorean.LikeListActivity;
import com.calamus.easykorean.MyDiscussionActivity;
import com.calamus.easykorean.PhotoActivity;
import com.calamus.easykorean.R;
import com.calamus.easykorean.VimeoPlayerActivity;
import com.calamus.easykorean.WritePostActivity;
import com.calamus.easykorean.app.MyDialog;
import com.calamus.easykorean.app.Routing;
import com.calamus.easykorean.app.WebAppInterface;
import com.calamus.easykorean.holders.PostHolder;
import com.calamus.easykorean.models.AdModel;
import com.calamus.easykorean.models.AnounceModel;
import com.calamus.easykorean.models.LoadingModel;
import com.calamus.easykorean.models.NewfeedModel;
import com.calamus.easykorean.app.MyHttp;

import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import me.myatminsoe.mdetect.MDetect;

import static com.calamus.easykorean.app.AppHandler.myAdClick;
import static com.calamus.easykorean.app.AppHandler.setPhotoFromRealUrl;


public  class NewFeedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Activity c;
    public static ArrayList<Object> data;
    private final LayoutInflater mInflater;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    String imagePath,currentUserId,userName;
    boolean notiRedMark;
    String dbdir;
    String dbPath;
    SQLiteDatabase db;


    public NewFeedAdapter(Activity c, ArrayList<Object> data) {
        NewFeedAdapter.data = data;
        this.c = c;
        this.mInflater = LayoutInflater.from(c);
        MDetect.INSTANCE.init(c);
        sharedPreferences=c.getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        imagePath=sharedPreferences.getString("imageUrl",null);
        notiRedMark=sharedPreferences.getBoolean("notiRedMark",false);
        currentUserId=sharedPreferences.getString("phone",null);
        userName=sharedPreferences.getString("Username",null);
        editor=sharedPreferences.edit();

        dbdir=c.getFilesDir().getPath()+"/databases/";
        dbPath=dbdir+"post.db";
        db=SQLiteDatabase.openDatabase(dbPath,null,SQLiteDatabase.OPEN_READWRITE);

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        if(viewType==0){
            View view = mInflater.inflate(R.layout.item_newfeed, parent, false);
            return new PostHolder(view,c,currentUserId,userName,imagePath);
        }else if(viewType==1){
            View view = mInflater.inflate(R.layout.item_anounce, parent, false);
            return new AnounceHolder(view);
        }else if(viewType==2){
            View view = mInflater.inflate(R.layout.shimmer_item_newfeed, parent, false);
            return new LoadingHolder(view);
        }else{
            View view = mInflater.inflate(R.layout.item_appads, parent, false);
            return new AdHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {


        if(data.get(position) instanceof NewfeedModel){
            return 0;
        }else if(data.get(position) instanceof AnounceModel){
            return 1;
        }else if(data.get(position) instanceof LoadingModel) {
            return 2;
        }else{
            return 3;
        }
    }


    @Override
    public void onBindViewHolder(@NotNull final RecyclerView.ViewHolder holder, final int i) {


        if(data.get(i) instanceof NewfeedModel){
            PostHolder postHolder=(PostHolder) holder;

            postHolder.setNewsfeedModel((NewfeedModel) data.get(i));
            postHolder.setCallBack(new PostHolder.CallBack() {
                @Override
                public void onPostDelete() {
                    data.remove(holder.getAbsoluteAdapterPosition());
                    notifyDataSetChanged();
                }
            });

        }else if(data.get(i) instanceof AnounceModel){
            AnounceHolder anounceHolder=(AnounceHolder)holder;
            AnounceModel model=(AnounceModel) data.get(i);
            anounceHolder.wv.loadUrl(model.getlinkAounce()+"/"+currentUserId);
        }else if(data.get(i) instanceof AdModel){
            AdHolder adHolder=(AdHolder)holder;
            AdModel model=(AdModel)data.get(i);
            adHolder.tvAppName.setText(model.getAppName());
            adHolder.tvAppDes.setText(model.getAppDes());
            setPhotoFromRealUrl(adHolder.ivCover,model.getAppCover());
            setPhotoFromRealUrl(adHolder.ivIcon,model.getAppIcon());

            if(!model.getType().equals("")){
                adHolder.tv_action.setVisibility(View.VISIBLE);
                adHolder.tv_action.setText(model.getType());
            }else {
                adHolder.tv_action.setVisibility(View.GONE);
            }

            adHolder.tv_action.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    c.startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse(model.getLink())));
                    myAdClick(model.getId());
                }
            });
        }

    }


    public class Holder extends RecyclerView.ViewHolder {
        TextView tv_userName,tv_time,tv_body,tv_read_more,tvCount;
        TextView tv_comment,tv_react;
        ImageView iv_profile,iv_post,iv_blueMark;
        LinearLayout iv_play_cycle;
        ImageButton bt_iv_more;


        public Holder(View view) {
            super(view);
            tv_time = view.findViewById(R.id.tv_time);
            tv_read_more=view.findViewById(R.id.tv_readmore);
            iv_profile = view.findViewById(R.id.iv_profile);
            tv_userName=view.findViewById(R.id.tv_username);
            tv_body=view.findViewById(R.id.tv_body);
            iv_post=view.findViewById(R.id.iv_post_image);
            iv_blueMark=view.findViewById(R.id.iv_blueMark);
            tv_comment=view.findViewById(R.id.tv_comment);

            iv_play_cycle=view.findViewById(R.id.video_cycle);
            bt_iv_more=view.findViewById(R.id.bt_iv_more);
            tvCount=view.findViewById(R.id.tv_view_count);
            tv_react=view.findViewById(R.id.tv_react);


            iv_post.setClipToOutline(true);


            iv_post.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    NewfeedModel model = (NewfeedModel) data.get(getAbsoluteAdapterPosition());
                    if(model.getIsVideo().equals("1")){
                        goVideo(model);

                    }else {
                        goPhoto(model.getPostImage(),model.getPostBody(),iv_post);
                    }
                }
            });

            iv_profile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    NewfeedModel model = (NewfeedModel) data.get(getAbsoluteAdapterPosition());
                    Intent intent=new Intent(c, MyDiscussionActivity.class);
                    intent.putExtra("userId",model.getUserId());
                    intent.putExtra("userName",model.getUserName());
                    c.startActivity(intent);
                }
            });

            bt_iv_more.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    NewfeedModel model=(NewfeedModel)data.get(getAbsoluteAdapterPosition());
                    showMenu(v,model,getAbsoluteAdapterPosition());
                }
            });

            tv_comment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    NewfeedModel model = (NewfeedModel) data.get(getAbsoluteAdapterPosition());
                    goTO(model);
                }
            });

            tv_react.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    NewfeedModel model = (NewfeedModel) data.get(getAbsoluteAdapterPosition());
                    Intent intent=new Intent(c, LikeListActivity.class);
                    intent.putExtra("contentId",model.getPostId());
                    intent.putExtra("fetch", Routing.FETCH_POST_LIKE);
                    c.startActivity(intent);
                }
            });

            tv_userName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    NewfeedModel model = (NewfeedModel) data.get(getAbsoluteAdapterPosition());
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
            c.startActivity(intent);
        }

        private void goPhoto(String imageUrl, String des,ImageView iv){
            Intent intent=new Intent(c, PhotoActivity.class);
            intent.putExtra("image",imageUrl);
            intent.putExtra("des",des);
            ActivityOptionsCompat optionsCompat=ActivityOptionsCompat.makeSceneTransitionAnimation(c,iv, ViewCompat.getTransitionName(iv));
            c.startActivity(intent,optionsCompat.toBundle());
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
                    }

                    return true;
                }
            });
            popup.show();
        }

        private void showDeleteDialog(String postId,int position){


            MyDialog myDialog=new MyDialog(c, "Delete Post!", "Do you really want to delete", new MyDialog.ConfirmClick() {
                @Override
                public void onConfirmClick() {
                    deletePostOrReport(postId,Routing.DELETE_POST,position);
                    data.remove(position);
                    notifyDataSetChanged();
                }
            });
            myDialog.showMyDialog();
        }

        private void deletePostOrReport(String postId,String link,int position){
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
                    public void onError(String msg) {}
                }).url(link)
                        .field("postId",postId);
                myHttp.runTask();
            }).start();
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
    }

    public class AdHolder extends RecyclerView.ViewHolder{

        ImageView ivCover,ivIcon;
        TextView tvAppName,tvAppDes;
        TextView tv_action;
        public AdHolder(View view){
            super(view);
            ivCover=view.findViewById(R.id.iv_appCover);
            ivIcon=view.findViewById(R.id.iv_appIcon);
            tvAppName=view.findViewById(R.id.tv_appName);
            tvAppDes=view.findViewById(R.id.tv_appDes);
            tv_action=view.findViewById(R.id.tv_action);
        }
    }

    public class LoadingHolder extends RecyclerView.ViewHolder{
        public LoadingHolder(View view){
            super(view);
        }
    }

    public class AnounceHolder extends RecyclerView.ViewHolder {

        WebView wv;
        RelativeLayout announceContainer;
        public AnounceHolder(View view) {
            super(view);
            wv=view.findViewById(R.id.wv_anounce);
            announceContainer=view.findViewById(R.id.announceContainer);
            WebSettings settings = wv.getSettings();
            settings.setJavaScriptEnabled(true);
            wv.addJavascriptInterface(new WebAppInterface(c), "Android");
            settings.setCacheMode(WebSettings.LOAD_DEFAULT);

            wv.setWebViewClient(new WebViewClient(){
                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    announceContainer.setVisibility(View.VISIBLE);
                }
            });

        }
    }
}