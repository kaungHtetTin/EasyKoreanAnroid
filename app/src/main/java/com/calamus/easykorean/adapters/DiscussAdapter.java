package com.calamus.easykorean.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.LayoutInflater;
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

import com.calamus.easykorean.ChattingActivity;
import com.calamus.easykorean.CommentActivity;
import com.calamus.easykorean.EditProfileActivity;
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
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import me.myatminsoe.mdetect.MDetect;
import static com.calamus.easykorean.app.AppHandler.reactFormat;
import static com.calamus.easykorean.app.AppHandler.setPhotoFromRealUrl;
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

    String otherId;


    public DiscussAdapter(Activity c, ArrayList<Object> data,String otherId) {
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

        this.otherId=otherId;

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
            View view=mInflater.inflate(R.layout.item_profile,parent,false);
            return new ProfileHolder(view);
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
    }

    public class ProfileHolder extends RecyclerView.ViewHolder{
        ImageView iv,iv_User;
        CardView cardView;
        TextView tv,tv_name,tv_age,tv_education,tv_work,tv_address,tv_addFri,tv_reqFir,tv_fir,tv_msg,tv_confirm,tv_editProfile,tv_limit;

        Executor myExecutor;
        String username;

        public ProfileHolder(@NonNull View itemView) {
            super(itemView);

            myExecutor=ContextCompat.getMainExecutor(c);
            cardView=itemView.findViewById(R.id.card_writer);
            tv=itemView.findViewById(R.id.et_post_writer);
            iv=itemView.findViewById(R.id.iv_profile);
            tv_name=itemView.findViewById(R.id.tv_name);
            tv_age=itemView.findViewById(R.id.tv_age);
            tv_education=itemView.findViewById(R.id.tv_education);
            tv_work=itemView.findViewById(R.id.tv_work_at);
            tv_address=itemView.findViewById(R.id.tv_live_in);
            iv_User=itemView.findViewById(R.id.iv_User);
            tv_addFri=itemView.findViewById(R.id.tv_addFri);
            tv_reqFir=itemView.findViewById(R.id.tv_reqFir);
            tv_fir=itemView.findViewById(R.id.tv_fri);
            tv_msg=itemView.findViewById(R.id.tv_msg);
            tv_confirm=itemView.findViewById(R.id.tv_confirm);
            tv_editProfile=itemView.findViewById(R.id.tv_editProfile);
            tv_limit=itemView.findViewById(R.id.tv_limit);


            tv.setOnClickListener(v -> {

                Intent i=new Intent(c, WritePostActivity.class);
                i.putExtra("action",true);
                i.putExtra("postBody","");
                i.putExtra("postImage","");
                i.putExtra("postId","");
                c.startActivity(i);
            });


            if(imagePath!=null) AppHandler.setPhotoFromRealUrl(iv,imagePath);



            getUserData();

            tv_editProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent(c, EditProfileActivity.class);
                    c.startActivity(intent);
                }
            });



            tv_addFri.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    friendShip(otherId,Routing.ADD_FRIEND,tv_reqFir);
                    tv_addFri.setVisibility(View.GONE);
                    tv_reqFir.setVisibility(View.VISIBLE);

                }
            });

            tv_reqFir.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    friendShip(otherId,Routing.ADD_FRIEND,tv_addFri);
                    tv_reqFir.setVisibility(View.GONE);
                    tv_addFri.setVisibility(View.VISIBLE);
                }
            });

            tv_confirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    tv_confirm.setVisibility(View.GONE);
                    tv_fir.setVisibility(View.VISIBLE);
                    friendShip(otherId,Routing.CONFIRM_FRIEND,tv_fir);

                }
            });
            
            tv_fir.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MyDialog myDialog=new MyDialog(c, "Remove Friend!", "Do you really want to remove this user from your friend list?", () -> {
                        tv_fir.setVisibility(View.GONE);
                        tv_addFri.setVisibility(View.VISIBLE);
                        friendShip(otherId,Routing.UN_FRIEND,tv_addFri);
                    });
                    myDialog.showMyDialog();

                }
            });
        }

        private void friendShip(String userId,String url,TextView tv){
            new Thread(() -> {
                MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.POST, new MyHttp.Response() {
                    @Override
                    public void onResponse(String response) {
                        myExecutor.execute(new Runnable() {
                            @Override
                            public void run() {
                                Log.e("RES: ",response);
                                try{
                                    JSONObject jo=new JSONObject(response);
                                    String code=jo.getString("code");

                                    if (code.equals("err53")){
                                        tv.setVisibility(View.GONE);
                                        tv_limit.setVisibility(View.VISIBLE);
                                        tv_limit.setText("You are in maximum Friend Limit");
                                    }

                                }catch (Exception e){}

                            }
                        });
                    }
                    @Override
                    public void onError(String msg) {
                        Log.e("Err: ", msg);
                    }
                }).url(url)
                        .field("my_id",currentUserId)
                        .field("other_id",userId)
                        .field("major","korea");
                myHttp.runTask();
            }).start();
        }

        private void getUserData(){
            new Thread(() -> {
                MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.GET, new MyHttp.Response() {
                    @Override
                    public void onResponse(String response) {
                        myExecutor.execute(new Runnable() {
                            @SuppressLint("SetTextI18n")
                            @Override
                            public void run() {
                                try {
                                    JSONObject joMain=new JSONObject(response);
                                    JSONObject joProfile=joMain.getJSONObject("profile");

                                    username=joProfile.getString("learner_name");
                                    String birthday=joProfile.getString("bd_year");
                                    String education=joProfile.getString("education");
                                    String work=joProfile.getString("work");
                                    String region=joProfile.getString("region");
                                    String imageUrl=joProfile.getString("learner_image");
                                    String token=joMain.getString("token");
                                    tv_name.setText(username);

                                    if (birthday.equals("")) tv_age.setVisibility(View.GONE);
                                    else tv_age.setText("Born in " + birthday);

                                    if(education.equals(""))tv_education.setVisibility(View.GONE);
                                    else tv_education.setText(education);

                                    if(work.equals(""))tv_work.setVisibility(View.GONE);
                                    else tv_work.setText("Work at "+work);

                                    if(region.equals(""))tv_address.setVisibility(View.GONE);
                                    else tv_address.setText("Live in "+region);

                                    if(!imageUrl.equals(""))setPhotoFromRealUrl(iv_User,imageUrl);

                                    String friendship=joMain.getString("friendship");
                                    if(friendship.equals("request")){
                                        tv_addFri.setVisibility(View.GONE);
                                        tv_reqFir.setVisibility(View.VISIBLE);
                                    }

                                    if(friendship.equals("confirm")){
                                        tv_addFri.setVisibility(View.GONE);
                                        tv_confirm.setVisibility(View.VISIBLE);
                                    }

                                    if(friendship.equals("friend")){
                                        tv_addFri.setVisibility(View.GONE);
                                        tv_msg.setVisibility(View.VISIBLE);
                                        tv_fir.setVisibility(View.VISIBLE);
                                    }

                                    if(friendship.equals("me")){
                                        tv_addFri.setVisibility(View.GONE);
                                        tv_editProfile.setVisibility(View.VISIBLE);
                                    }

                                    if(friendship.equals("reqLimit")){
                                        tv_addFri.setVisibility(View.GONE);
                                        tv_limit.setVisibility(View.VISIBLE);
                                        tv_limit.setText(username+" is in maximum Request Limit");
                                    }

                                    if(friendship.equals("friLimit")){
                                        tv_addFri.setVisibility(View.GONE);
                                        tv_limit.setVisibility(View.VISIBLE);
                                        tv_limit.setText(username+" is in maximum Friend Limit");
                                    }

                                    tv_msg.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent intent=new Intent(c, ChattingActivity.class);
                                            intent.putExtra("fId",otherId);
                                            intent.putExtra("fImage",imageUrl);
                                            intent.putExtra("fName",username);
                                            intent.putExtra("token",token);
                                            c.startActivity(intent);
                                        }
                                    });

                                    iv_User.setOnClickListener(v -> {
                                        Intent intent=new Intent(c, PhotoActivity.class);
                                        intent.putExtra("image",imageUrl);
                                        intent.putExtra("des","");
                                        c.startActivity(intent);
                                    });


                                }catch (Exception e){}
                            }
                        });

                    }
                    @Override
                    public void onError(String msg) {}
                }).url(Routing.GET_PROFILE+"/"+currentUserId+"/"+otherId+"/korea");

                myHttp.runTask();
            }).start();
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

    private void editPost(String postBody, String postImage, String postId){
        Intent i=new Intent(c, WritePostActivity.class);
        i.putExtra("action",false);
        i.putExtra("postBody",postBody);
        i.putExtra("postImage",postImage);
        i.putExtra("postId",postId);
        c.startActivity(i);
    }
}