package com.calamus.easykorean.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.calamus.easykorean.CommentActivity;
import com.calamus.easykorean.MyYouTubeVideoActivity;
import com.calamus.easykorean.PhotoActivity;
import com.calamus.easykorean.R;
import com.calamus.easykorean.WritePostActivity;
import com.calamus.easykorean.app.AppHandler;
import com.calamus.easykorean.app.MyDialog;
import com.calamus.easykorean.app.MyHttp;
import com.calamus.easykorean.app.Routing;
import com.calamus.easykorean.models.DiscussionModel;
import com.google.android.gms.ads.formats.NativeAd;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.ads.formats.UnifiedNativeAdView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.concurrent.Executor;

import me.myatminsoe.mdetect.MDetect;

import static com.calamus.easykorean.app.AppHandler.reactFormat;
import static com.calamus.easykorean.app.AppHandler.viewCountFormat;


public  class DiscussAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

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


    public DiscussAdapter(Activity c, ArrayList<Object> data) {
        this.data = data;
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
            View view=mInflater.inflate(R.layout.item_post_writer_mini,parent,false);
            return new WriterHolder(view);
        }else if(viewType==1){
            View unifiedNativeLayoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.ad_unified, parent, false);
            return new UnifiedNativeAdViewHolder(unifiedNativeLayoutView);

        }else{
            View view = mInflater.inflate(R.layout.item_discussion, parent, false);
            return new Holder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {

        if(position==0){
            return  0;
        }else{
            if(data.get(position) instanceof UnifiedNativeAd){
                return 1;
            }else {
                return 2;
            }
        }

    }


    @Override
    public void onBindViewHolder(@NotNull final RecyclerView.ViewHolder holder, final int i) {

        if(i>0){

            if(data.get(i) instanceof UnifiedNativeAd){
                UnifiedNativeAd unifiedNativeAd = (UnifiedNativeAd) data.get(i);
                populateNativeAdView(unifiedNativeAd, ((UnifiedNativeAdViewHolder)holder).getAdView());

            }else{
                try {
                    final DiscussionModel model = (DiscussionModel) data.get(i);
                    Holder postHolder=(Holder)holder;

                    if(model.getPostImage().equals("")){
                        AppHandler.setPhotoFromRealUrl(postHolder.iv_save,model.getUserImage());
                    }else {
                        AppHandler.setPhotoFromRealUrl(postHolder.iv_save,model.getPostImage());
                    }

                    if(model.getBody().length()>80){
                        String sub=model.getBody().substring(0,80);
                        postHolder.tv_body.setText(sub+" ...");
                    }else {

                        postHolder.tv_body.setText(model.getBody());
                    }


                    int rectCount=Integer.parseInt(model.getPostLikes());
                    if(rectCount>0){
                        postHolder.tv_reactCount.setVisibility(View.VISIBLE);
                        postHolder.tv_reactCount.setText(reactFormat(rectCount));
                    }else postHolder.tv_reactCount.setVisibility(View.GONE);
                    int commentCount=Integer.parseInt(model.getComments());
                    if(commentCount>0){
                        postHolder.tv_commentCount.setVisibility(View.VISIBLE);
                        postHolder.tv_commentCount.setText(reactFormat(commentCount));
                    }else postHolder.tv_commentCount.setVisibility(View.GONE);


                    if(model.getHasVideo().equals("1")) postHolder.iv_video_circle.setVisibility(View.VISIBLE);
                    else postHolder.iv_video_circle.setVisibility(View.GONE);

                    int viewCount=Integer.parseInt(model.getVideoCount());
                    if(viewCount==0)postHolder.tv_viewCount.setText("");
                    else postHolder.tv_viewCount.setText(viewCountFormat(viewCount));

                } catch (Exception e) {
                    Toast.makeText(c, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

        }

    }

    private void populateNativeAdView(UnifiedNativeAd unifiedNativeAd, UnifiedNativeAdView adView) {
        ((TextView)adView.getHeadlineView()).setText(unifiedNativeAd.getHeadline());
        ((TextView)adView.getBodyView()).setText(unifiedNativeAd.getBody());
        ((TextView)adView.getCallToActionView()).setText(unifiedNativeAd.getCallToAction());

        NativeAd.Image icon=unifiedNativeAd.getIcon();

        if(icon==null){
            adView.getIconView().setVisibility(View.INVISIBLE);
        }else {
            ((ImageView)adView.getIconView()).setImageDrawable(icon.getDrawable());
            adView.getIconView().setVisibility(View.VISIBLE);
        }

        if(unifiedNativeAd.getStore()==null){
            adView.getStoreView().setVisibility(View.INVISIBLE);
        }else {
            adView.getStoreView().setVisibility(View.VISIBLE);
            ((TextView)adView.getStoreView()).setText(unifiedNativeAd.getStore());
        }

        if(unifiedNativeAd.getStarRating()==null){
            adView.getStarRatingView().setVisibility(View.INVISIBLE);
        }else {
            adView.getStarRatingView().setVisibility(View.VISIBLE);
            ((RatingBar)adView.getStarRatingView()).setRating(unifiedNativeAd.getStarRating().floatValue());
        }

        if(unifiedNativeAd.getAdvertiser()==null){
            adView.getAdvertiserView().setVisibility(View.INVISIBLE);
        }else {
            adView.getAdvertiserView().setVisibility(View.VISIBLE);
            ((TextView)adView.getAdvertiserView()).setText(unifiedNativeAd.getAdvertiser());
        }

        adView.setNativeAd(unifiedNativeAd);
    }



    public class Holder extends RecyclerView.ViewHolder {

        ImageView iv_save,iv_video_circle;
        ImageButton ibt_more;
        TextView tv_body,tv_reactCount,tv_commentCount,tv_viewCount;

        public Holder(View view) {
            super(view);
            iv_save=view.findViewById(R.id.iv_save);
            ibt_more=view.findViewById(R.id.bt_iv_more);
            tv_body=view.findViewById(R.id.tv_body);
            iv_video_circle=view.findViewById(R.id.iv_video_circle);
            tv_reactCount=view.findViewById(R.id.tv_reactCount);
            tv_commentCount=view.findViewById(R.id.tv_commentCount);
            tv_viewCount=view.findViewById(R.id.tv_viewCount);

            iv_save.setClipToOutline(true);

            view.setOnClickListener(view12 -> {
                DiscussionModel model=(DiscussionModel) data.get(getAbsoluteAdapterPosition());
                goTO(model);
            });

            ibt_more.setOnClickListener(view1 -> {
                DiscussionModel model=(DiscussionModel) data.get(getAbsoluteAdapterPosition());
                showMenu(view1,model,getAbsoluteAdapterPosition());
            });

        }
    }



    private void goTO(DiscussionModel model){
        if(model.getHasVideo().equals("0")){
            Intent intent=new Intent(c, CommentActivity.class);
            intent.putExtra("postId",model.getPostId());
            intent.putExtra("time","");//for comment seen
            c.startActivity(intent);
        }else{

            goVideo(model);

        }

    }

    private void goVideo(DiscussionModel model){
        String videoId=model.getPostImage().substring(model.getPostImage().indexOf("vi/")+3);
        videoId=videoId.substring(0,videoId.length()-6);
        Intent intent=new Intent(c, MyYouTubeVideoActivity.class);
        intent.putExtra("videoTitle","");
        intent.putExtra("videoId",videoId);
        intent.putExtra("time",Long.parseLong(model.getPostId()));
        c.startActivity(intent);
    }


    public class WriterHolder extends RecyclerView.ViewHolder{
        ImageView iv;
        CardView cardView;
        TextView tv;
        public WriterHolder(@NonNull View itemView) {
            super(itemView);

            cardView=itemView.findViewById(R.id.card_writer);
            tv=itemView.findViewById(R.id.et_post_writer);
            iv=itemView.findViewById(R.id.iv_profile);
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
        }
    }


    private void showMenu(View v,DiscussionModel model,int position){
        PopupMenu popup=new PopupMenu(c,v);
        if(("0"+model.getUserId()).equals(currentUserId)||model.getUserId().equals(currentUserId)){
            popup.getMenuInflater().inflate(R.menu.pop_up_private_menu,popup.getMenu());
        }else {
            popup.getMenuInflater().inflate(R.menu.pop_up_public_menu,popup.getMenu());
        }
        popup.setOnMenuItemClickListener(item -> {
            int id=item.getItemId();
            if(id==R.id.report){
                deletePostOrReport(model.getPostId(), Routing.REPORT_POST,position);
            }else if(id==R.id.save){
                savePost(model.getPostId(),model.getBody(),model.getPostImage(),model.getUserName(),model.getUserImage(),model.getHasVideo());
            }else if(id==R.id.delete){
                showDeleteDialog(model.getPostId(),position);
            }else if(id==R.id.edit){
                editPost(model.getBody(),model.getPostImage(),model.getPostId());
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
}