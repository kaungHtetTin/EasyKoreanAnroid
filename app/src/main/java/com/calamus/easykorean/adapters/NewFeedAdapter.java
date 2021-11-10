package com.calamus.easykorean.adapters;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.calamus.easykorean.AnnouncementActivity;
import com.calamus.easykorean.CommentActivity;
import com.calamus.easykorean.LikeListActivity;
import com.calamus.easykorean.MyDiscussionActivity;
import com.calamus.easykorean.MyYouTubeVideoActivity;
import com.calamus.easykorean.NotiListActivity;
import com.calamus.easykorean.PhotoActivity;
import com.calamus.easykorean.R;
import com.calamus.easykorean.WritePostActivity;
import com.calamus.easykorean.app.AppHandler;
import com.calamus.easykorean.app.MyDialog;
import com.calamus.easykorean.app.MyHttp;
import com.calamus.easykorean.app.Routing;
import com.calamus.easykorean.controller.LikeController;
import com.calamus.easykorean.controller.NotificationController;
import com.calamus.easykorean.models.AdModel;
import com.calamus.easykorean.models.AnounceModel;
import com.calamus.easykorean.models.NewfeedModel;

import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import me.myatminsoe.mdetect.MDetect;
import static com.calamus.easykorean.app.AppHandler.commentFormat;
import static com.calamus.easykorean.app.AppHandler.formatTime;
import static com.calamus.easykorean.app.AppHandler.myAdClick;
import static com.calamus.easykorean.app.AppHandler.reactFormat;
import static com.calamus.easykorean.app.AppHandler.setMyanmar;
import static com.calamus.easykorean.app.AppHandler.viewCountFormat;

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
            View view=mInflater.inflate(R.layout.item_post_writer,parent,false);
            return new WriterHolder(view);
        }else if(viewType==1){

            View view = mInflater.inflate(R.layout.item_newfeed, parent, false);
            return new Holder(view);
        }else if(viewType==2){
            View view = mInflater.inflate(R.layout.item_anounce, parent, false);
            return new AnounceHolder(view);
        }else{
            View view = mInflater.inflate(R.layout.item_appads, parent, false);
            return new AdHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {

        if(position==0){
            return  0;
        }else{
            if(data.get(position) instanceof NewfeedModel){
                return 1;
            }else if(data.get(position) instanceof AnounceModel){
                return 2;
            }else {
                return 3;
            }
        }

    }


    @Override
    public void onBindViewHolder(@NotNull final RecyclerView.ViewHolder holder, final int i) {

        if(i==0){
            WriterHolder holder1=(WriterHolder)holder;
            if(notiRedMark){ holder1.iv_notiRedMark.setVisibility(View.VISIBLE);}
        }else{

            if(data.get(i) instanceof NewfeedModel){
                try {
                    final NewfeedModel model = (NewfeedModel) data.get(i);
                    Holder postHolder=(Holder)holder;
                    long time=Long.parseLong(model.getPostId());

                    postHolder.tv_time.setText(formatTime(time));
                    postHolder.tv_userName.setText(setMyanmar(model.getUserName()));
                    postHolder.tv_body.setText(setMyanmar(model.getPostBody()));

                    postHolder.iv_profile.setVisibility(View.GONE);

                    if(!model.getUserImage().equals("")){
                        postHolder.iv_profile.setVisibility(View.VISIBLE);
                        AppHandler.setPhotoFromRealUrl(postHolder.iv_profile,model.getUserImage());
                    }else{
                        postHolder.iv_profile.setBackgroundResource(R.drawable.ic_baseline_account_circle_24);
                        //postHolder.iv_profile.setVisibility(View.VISIBLE);
                    }

                    if(imagePath!=null)AppHandler.setPhotoFromRealUrl(postHolder.iv_small,imagePath);
                    postHolder.iv_blueMark.setVisibility(View.GONE);
                    postHolder.iv_post.setVisibility(View.GONE);

                    if(!model.getPostImage().equals("")){
                        postHolder.iv_post.setVisibility(View.VISIBLE);
                        AppHandler.setPhotoFromRealUrl(postHolder.iv_post,model.getPostImage());
                    }else{
                        postHolder.iv_post.setVisibility(View.GONE);
                    }

                    postHolder.iv_play_cycle.setVisibility(View.GONE);

                    if(model.getIsVideo().equals("1")){
                        postHolder.iv_play_cycle.setVisibility(View.VISIBLE);
                    }

                    int views=Integer.parseInt(model.getViewCount());
                    if(views==0)postHolder.tvCount.setText("");
                    else postHolder.tvCount.setText(viewCountFormat(views));

                    if(model.getPostBody().length()>200){
                        String sub=model.getPostBody().substring(0,200);
                        postHolder.tv_body.setText(sub);
                        postHolder.tv_read_more.setVisibility(View.VISIBLE);
                    }

                    postHolder.tv_read_more.setOnClickListener(v -> {
                        postHolder.tv_body.setText(model.getPostBody());
                        postHolder.tv_read_more.setVisibility(View.INVISIBLE);
                    });

                    if(model.getIsVip().equals("1")){
                        postHolder.iv_blueMark.setVisibility(View.VISIBLE);
                    }

                    int commentNumber=Integer.parseInt(model.getPostComments());
                    postHolder.tvViewCmt.setText(commentFormat(commentNumber));


                    postHolder.iv_react.setBackgroundResource(R.drawable.ic_normal_react);
                    if(model.getIsLike().equals("1")){
                        postHolder.iv_react.setBackgroundResource(R.drawable.ic_react_love);
                    }


                    postHolder.iv_react.setOnClickListener(v -> {

                        LikeController.likeThePost(currentUserId,model.getPostId());

                        if(model.getIsLike().equals("1")){
                            postHolder.iv_react.setBackgroundResource(R.drawable.ic_normal_react);
                            int rectCount=Integer.parseInt(model.getPostLikes());
                            rectCount--;
                            if(rectCount>0){
                                postHolder.tvReactCount.setText(reactFormat(rectCount));
                                postHolder.card_reactCount.setVisibility(View.VISIBLE);
                            }else {
                                postHolder.card_reactCount.setVisibility(View.GONE);
                            }

                            model.setIsLike("0");
                            model.setPostLikes(rectCount+"");

                        }else{
                            postHolder.iv_react.setBackgroundResource(R.drawable.ic_react_love);
                            int rectCount=Integer.parseInt(model.getPostLikes());
                            rectCount++;
                            postHolder.tvReactCount.setText(reactFormat(rectCount));
                            postHolder.card_reactCount.setVisibility(View.VISIBLE);

                            if(!model.getUserId().equals(currentUserId)){
                                NotificationController notificationController=new NotificationController(c);
                                notificationController.sendNotification(userName+" reacted your post.",model.getUserToken(),Routing.APP_NAME,"1");
                            }
                            model.setIsLike("1");
                            model.setPostLikes(rectCount+"");
                        }



                    });

                    postHolder.card_reactCount.setVisibility(View.GONE);
                    if (!model.getPostLikes().equals("0")){
                        postHolder.tvReactCount.setText(reactFormat(Integer.parseInt(model.getPostLikes())));
                        postHolder.card_reactCount.setVisibility(View.VISIBLE);
                    }else{
                        postHolder.card_reactCount.setVisibility(View.GONE);
                    }

                } catch (Exception e) {
                    // Toast.makeText(c, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }else if(data.get(i) instanceof AnounceModel){
                AnounceHolder anounceHolder=(AnounceHolder)holder;
                AnounceModel model=(AnounceModel) data.get(i);
                anounceHolder.wv.loadUrl(model.getlinkAounce()+"/"+currentUserId);
            }else if(data.get(i) instanceof AdModel){
                AdHolder adHolder=(AdHolder)holder;
                AdModel model=(AdModel)data.get(i);
                adHolder.tvAppName.setText(model.getAppName());
                adHolder.tvAppDes.setText(model.getAppDes());
                AppHandler.setPhotoFromRealUrl(adHolder.ivCover,model.getAppCover());
                AppHandler.setPhotoFromRealUrl(adHolder.ivIcon,model.getAppIcon());

                if(!model.getType().equals("")){
                    adHolder.btAppInstall.setVisibility(View.VISIBLE);
                    adHolder.btAppInstall.setText(model.getType());
                }else {
                    adHolder.btAppInstall.setVisibility(View.GONE);
                }

                adHolder.btAppInstall.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        c.startActivity(new Intent(Intent.ACTION_VIEW,
                                Uri.parse(model.getLink())));
                        myAdClick(model.getId());
                    }
                });
            }

        }

    }

    public class Holder extends RecyclerView.ViewHolder {
        TextView tv_userName,tv_time,tv_body,tv_read_more,tvViewCmt,tvCount,tvReactCount;
        ImageView iv_profile,iv_post,iv_small,iv_blueMark,iv_cmt,iv_react;
        LinearLayout iv_play_cycle;
        ImageButton bt_iv_more;
        LinearLayout mlayout;
        public CardView card,card_reactCount;


        public Holder(View view) {
            super(view);
            tv_time = view.findViewById(R.id.newfeedtime);
            tv_read_more=view.findViewById(R.id.tv_readmore);
            iv_profile = view.findViewById(R.id.newfeediv);
            tv_userName=view.findViewById(R.id.newfeedtitle);
            tv_body=view.findViewById(R.id.tv_body);
            iv_post=view.findViewById(R.id.newfeedImage);
            iv_small=view.findViewById(R.id.iv_small_profile);
            iv_blueMark=view.findViewById(R.id.iv_blueMark);
            iv_cmt=view.findViewById(R.id.icon_cmt);
            mlayout=view.findViewById(R.id.nf_cmt_layout);
            card=view.findViewById(R.id.card_View);
            iv_play_cycle=view.findViewById(R.id.video_cycle);
            tvViewCmt=view.findViewById(R.id.nf_cmt_tv);
            bt_iv_more=view.findViewById(R.id.bt_iv_more);
            tvCount=view.findViewById(R.id.nf_view_tv);
            iv_react=view.findViewById(R.id.iv_react);
            tvReactCount=view.findViewById(R.id.tv_reactCount);
            card_reactCount=view.findViewById(R.id.card_reactCount);

            mlayout.setOnClickListener(v -> {
                NewfeedModel model = (NewfeedModel) data.get(getAbsoluteAdapterPosition());
                goTO(model);
            });

            tvViewCmt.setOnClickListener(v -> {
                NewfeedModel model = (NewfeedModel) data.get(getAbsoluteAdapterPosition());
                goTO(model);
            });


            iv_post.setOnClickListener(v -> {
                NewfeedModel model = (NewfeedModel) data.get(getAbsoluteAdapterPosition());
                if(model.getIsVideo().equals("1")){
                    goVideo(model);

                }else {
                    goPhoto(model.getPostImage(),model.getPostBody());
                }
            });

            iv_profile.setOnClickListener(v -> {
                NewfeedModel model = (NewfeedModel) data.get(getAbsoluteAdapterPosition());
                Intent intent=new Intent(c, MyDiscussionActivity.class);
                intent.putExtra("userId",model.getUserId());
                intent.putExtra("userName",model.getUserName());
                c.startActivity(intent);
            });

            bt_iv_more.setOnClickListener(v -> {
                NewfeedModel model=(NewfeedModel)data.get(getAbsoluteAdapterPosition());
                showMenu(v,model,getAbsoluteAdapterPosition());
            });

            iv_cmt.setOnClickListener(v -> {
                NewfeedModel model = (NewfeedModel) data.get(getAbsoluteAdapterPosition());
                goTO(model);
            });

            card_reactCount.setOnClickListener(view12 -> {
                NewfeedModel model = (NewfeedModel) data.get(getAbsoluteAdapterPosition());
                Intent intent=new Intent(c, LikeListActivity.class);
                intent.putExtra("contentId",model.getPostId());
                intent.putExtra("fetch", Routing.FETCH_POST_LIKE);
                c.startActivity(intent);
            });

            tv_userName.setOnClickListener(view1 -> {
                NewfeedModel model = (NewfeedModel) data.get(getAbsoluteAdapterPosition());
                Intent intent=new Intent(c, MyDiscussionActivity.class);
                intent.putExtra("userId",model.getUserId());
                intent.putExtra("userName",model.getUserName());
                c.startActivity(intent);
            });

        }

        private void goTO(NewfeedModel model){
            if(model.getIsVideo().equals("0")){
                Intent intent=new Intent(c, CommentActivity.class);
                intent.putExtra("postId",model.getPostId());
                intent.putExtra("time",0+"");//for comment seen
                c.startActivity(intent);
            }else{

                goVideo(model);

            }

        }

        private void goVideo(NewfeedModel model){
            String videoId=model.getPostImage().substring(model.getPostImage().indexOf("vi/")+3);
            videoId=videoId.substring(0,videoId.length()-6);
            Intent intent=new Intent(c, MyYouTubeVideoActivity.class);
            intent.putExtra("videoTitle","");
            intent.putExtra("videoId",videoId);
            intent.putExtra("time",Long.parseLong(model.getPostId()));
            c.startActivity(intent);

        }

        private void goPhoto(String imageUrl, String des){
            Intent intent=new Intent(c, PhotoActivity.class);
            intent.putExtra("image",imageUrl);
            intent.putExtra("des",des);
            c.startActivity(intent);

        }
    }



    public class AnounceHolder extends RecyclerView.ViewHolder {

        WebView wv;
        CardView cardView;
        public AnounceHolder(View view) {
            super(view);
            wv=view.findViewById(R.id.wv_anounce);
            cardView=view.findViewById(R.id.card_View);
            WebSettings settings = wv.getSettings();
            settings.setJavaScriptEnabled(true);
            settings.setCacheMode(WebSettings.LOAD_DEFAULT);

            wv.setWebViewClient(new WebViewClient(){
                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    cardView.setVisibility(View.VISIBLE);
                }
            });

        }
    }




    public class WriterHolder extends RecyclerView.ViewHolder{
        ImageView iv,iv_noti,iv_notiRedMark,iv_announcement,iv_calamus;
        CardView cardView;
        TextView tv;
        public WriterHolder(@NonNull View itemView) {
            super(itemView);

            cardView=itemView.findViewById(R.id.card_writer);
            tv=itemView.findViewById(R.id.et_post_writer);
            iv=itemView.findViewById(R.id.iv_profile);
            iv_noti=itemView.findViewById(R.id.iv_noti);
            iv_notiRedMark=itemView.findViewById(R.id.noti_red_mark);
            iv_announcement=itemView.findViewById(R.id.announcement);
            iv_calamus=itemView.findViewById(R.id.calamus_discussion);
            tv.setOnClickListener(v -> {

                Intent i=new Intent(c, WritePostActivity.class);
                i.putExtra("action",true);
                i.putExtra("postBody","");
                i.putExtra("postImage","");
                i.putExtra("postId","");
                c.startActivity(i);
            });


            if(imagePath!=null) AppHandler.setPhotoFromRealUrl(iv,imagePath);

            iv.setOnClickListener(v -> {
                Intent intent=new Intent(c, PhotoActivity.class);
                intent.putExtra("image",imagePath);
                intent.putExtra("des","");
                c.startActivity(intent);
            });

            iv_noti.setOnClickListener(v -> {
                iv_notiRedMark.setVisibility(View.GONE);
                Intent intent=new Intent(c, NotiListActivity.class);
                c.startActivity(intent);
            });

            iv_calamus.setOnClickListener(view -> {
                Intent intent=new Intent(c, MyDiscussionActivity.class);
                intent.putExtra("userId","10000");
                intent.putExtra("userName","Calamus");
                c.startActivity(intent);
            });

            iv_announcement.setOnClickListener(view -> {
                Intent intent=new Intent(c, AnnouncementActivity.class);
                c.startActivity(intent);
            });
        }
    }


    private void showMenu(View v,NewfeedModel model,int position){
        PopupMenu popup=new PopupMenu(c,v);
        if(("0"+model.getUserId()).equals(currentUserId)||model.getUserId().equals(currentUserId)){
            popup.getMenuInflater().inflate(R.menu.pop_up_private_menu,popup.getMenu());
        }else {
            popup.getMenuInflater().inflate(R.menu.pop_up_public_menu,popup.getMenu());
        }
        popup.setOnMenuItemClickListener(item -> {
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
        });
        popup.show();
    }

    private void showDeleteDialog(String postId,int position){


        MyDialog myDialog=new MyDialog(c, "Delete Post!", "Do you really want to delete", () -> {
            deletePostOrReport(postId,Routing.DELETE_POST,position);
            data.remove(position);
            notifyDataSetChanged();
        });
        myDialog.showMyDialog();
    }

    private void deletePostOrReport(String postId,String link,int position){
        Executor postExecutor= ContextCompat.getMainExecutor(c);
        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.POST, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    postExecutor.execute(() -> Toast.makeText(c,response,Toast.LENGTH_SHORT).show());

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

    public class AdHolder extends RecyclerView.ViewHolder{

        ImageView ivCover,ivIcon;
        TextView tvAppName,tvAppDes;
        Button btAppInstall;
        public AdHolder(View view){
            super(view);
            ivCover=view.findViewById(R.id.iv_appCover);
            ivIcon=view.findViewById(R.id.iv_appIcon);
            tvAppName=view.findViewById(R.id.tv_appName);
            tvAppDes=view.findViewById(R.id.tv_appDes);
            btAppInstall=view.findViewById(R.id.bt_appInstall);
        }
    }

}