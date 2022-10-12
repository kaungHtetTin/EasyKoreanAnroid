package com.calamus.easykorean.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.calamus.easykorean.DetailActivity;
import com.calamus.easykorean.R;
import com.calamus.easykorean.VimeoPlayerActivity;
import com.calamus.easykorean.WebSiteActivity;
import com.calamus.easykorean.app.AppHandler;
import com.calamus.easykorean.app.MyDialog;
import com.calamus.easykorean.app.Routing;
import com.calamus.easykorean.models.LessonModel;
import com.calamus.easykorean.service.DownloaderService;
import com.calamus.easykorean.app.MyHttp;

import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.concurrent.Executor;

import me.myatminsoe.mdetect.MDetect;

import static com.calamus.easykorean.LessonActivity.course_id;
import static com.calamus.easykorean.LessonActivity.fragmentId;
import static com.calamus.easykorean.app.AppHandler.setMyanmar;

public class LessonAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Activity c;
    private ArrayList<Object> data;
    private LayoutInflater mInflater;
    SharedPreferences share;
    SharedPreferences.Editor editor;
    String currentUserId;
    Executor postExecutor;
    String lessonJSON=null;
    CallBack callBack;

    public LessonAdapter(Activity c, ArrayList<Object> data,CallBack callBack) {
        this.data = data;
        this.callBack=callBack;
        this.c = c;
        this.mInflater = LayoutInflater.from(c);
        share=c.getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        currentUserId=share.getString("phone","901");
        MDetect.INSTANCE.init(c);
        postExecutor= ContextCompat.getMainExecutor(c);

    }


    @Override
    public int getItemCount() {
        return data.size();
    }

    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int p2) {
        if(p2==1){
            View view = mInflater.inflate(R.layout.list_item_lesson, parent, false);
            return new Holder(view);
        }else {
            View view = mInflater.inflate(R.layout.item_native_ads, parent, false);
            return new NativeAdsHolder(view,c);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(data.get(position) instanceof LessonModel) return 1;
        else return 2;
    }


    @Override
    public void onBindViewHolder(@NotNull final RecyclerView.ViewHolder holder, final int i) {
        try {

            if(data.get(i) instanceof LessonModel){
                Holder lessonHolder=(Holder)holder;
                LessonModel model = (LessonModel) data.get(i);
                lessonHolder.tvTitle.setText(setMyanmar(model.getTitle()));

                lessonHolder.tv_title_mini.setText(setMyanmar(model.getTitle_mini()));

                if(model.isLearned()){
                    lessonHolder.tvLearned.setText("Learned");
                    lessonHolder.tvLearned.setTextColor(c.getResources().getColor(R.color.learned));
                }
                else  {
                    lessonHolder.tvLearned.setText("Learn Now!");
                    lessonHolder.tvLearned.setTextColor(c.getResources().getColor(R.color.colorBlack1));
                }

                if(model.isVideo()){
                    lessonHolder.ibt_download.setVisibility(View.VISIBLE);
                    lessonHolder.ivVideoCircle.setVisibility(View.VISIBLE);
                    AppHandler.setPhotoFromRealUrl(lessonHolder.iv,model.getThumbnail());
                }else {
                    lessonHolder.ivVideoCircle.setVisibility(View.INVISIBLE);

                    AppHandler.setPhotoFromRealUrl(lessonHolder.iv,model.getImage_url());
                    lessonHolder.ibt_download.setVisibility(View.GONE);
                }

                lessonHolder.ivVipMark.setVisibility(View.GONE);
                if(model.isVip())lessonHolder.ivVipMark.setVisibility(View.VISIBLE);

                lessonHolder.tv_duration.setText(AppHandler.formatDuration(model.getDuration()));

            }else {
                NativeAdsHolder nativeAdsHolder=(NativeAdsHolder)holder;
                com.google.android.gms.ads.nativead.NativeAd nativeAd=(com.google.android.gms.ads.nativead.NativeAd)data.get(i);
                nativeAdsHolder.populateNativeAdView(nativeAd, nativeAdsHolder.adView);
                nativeAdsHolder.adView.setNativeAd(nativeAd);
                nativeAdsHolder.frameLayout.removeAllViews();
                nativeAdsHolder.frameLayout.addView(nativeAdsHolder.adView);
            }




        } catch (Exception e) {
            Toast.makeText(c, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    public class Holder extends RecyclerView.ViewHolder {
        TextView tvTitle,tvLearned,tv_title_mini,tv_duration;
        ImageView iv,ivVipMark,ivVideoCircle;
        ImageView ibt_download;

        public Holder(final View view) {
            super(view);

            tvTitle = view.findViewById(R.id.tv_lesson_title);
            tv_title_mini=view.findViewById(R.id.tv_title_mini);
            iv = view.findViewById(R.id.iv_lesson);
            ivVipMark=view.findViewById(R.id.iv_vipMark);
            tvLearned=view.findViewById(R.id.tv_learned);
            ibt_download=view.findViewById(R.id.ib_download);
            ivVideoCircle=view.findViewById(R.id.iv_video_circle);
            tv_duration=view.findViewById(R.id.tv_duration);

            tvTitle.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            tvTitle.setMarqueeRepeatLimit(-1);
            tvTitle.setSingleLine(true);
            tvTitle.setSelected(true);
            editor=share.edit();

            ibt_download.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LessonModel model = (LessonModel) data.get(getAbsoluteAdapterPosition());
                    boolean isMember=share.getBoolean("course"+course_id,false);
                    boolean isVip=share.getBoolean("isVIP",false);
                    if((isMember && fragmentId==1)||(isVip&&fragmentId!=1)){
                        Toast.makeText(c,"Preparation for download",Toast.LENGTH_SHORT).show();
                        downloadPreparation(model.getTime()+"",model.getTitle(),model.getCategory());
                    }else{
                        String msg="Your need to register as a VIP to download the video.\n\nDo you want to contact the Calamus Education for VIP Registration";
                        showVIPRegistrationDialog(msg);
                    }
                }
            });

            view.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View p1) {
                    LessonModel model = (LessonModel) data.get(getAbsoluteAdapterPosition());
                    boolean isMember=share.getBoolean("course"+course_id,false);
                    Log.e("CourseId ",""+course_id);
                    if(fragmentId==1){
                        if(!model.isVip()||isMember){
                            go(model);
                            tvLearned.setText("Learned");
                            tvLearned.setTextColor(Color.GRAY);
                            model.setLearned(true);
                        }else{
                            String msg="This Lesson is only for VIP user.\n\nDo you want to contact the Calamus Education for VIP Registration";
                            showVIPRegistrationDialog(msg);
                        }
                    }else{
                        boolean isVip=share.getBoolean("isVIP",false);
                        if(!model.isVip()||isVip){
                            go(model);
                            tvLearned.setText("Learned");
                            tvLearned.setTextColor(Color.GRAY);
                            model.setLearned(true);
                        }else{
                            String msg="This Lesson is only for VIP user.\n\nDo you want to contact the Calamus Education for VIP Registration";
                            showVIPRegistrationDialog(msg);
                        }
                    }

                }
            });

        }

        private void downloadPreparation(String postId,String title,String lessonCategory){
            new Thread(() -> {
                MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.GET, new MyHttp.Response() {
                    @Override
                    public void onResponse(String response) {
                        postExecutor.execute(() -> {
                            Intent intent=new Intent(c, DownloaderService.class);
                            String checkTitle=title.replace("/"," ");
                            intent.putExtra("dir",c.getExternalFilesDir(Environment.DIRECTORY_MOVIES).getPath()+"/"+lessonCategory);
                            intent.putExtra("filename",checkTitle+".mp4");
                            intent.putExtra("downloadUrl",response);
                            intent.putExtra("intentMessage","downloadVideo");
                            c.startService(intent);
                            callBack.onDownloadClick();

                        });
                    }
                    @Override
                    public void onError(String msg) {
                        postExecutor.execute(() -> {
                            Toast.makeText(c,"Cannot Download",Toast.LENGTH_SHORT).show();
                        });
                    }
                }).url(Routing.GET_VIDEO_URL+"?postId="+postId);
                myHttp.runTask();
            }).start();
        }

    }


    private void go(LessonModel model){

        final Intent i;
        if(model.isVideo()){
            i = new Intent(c, VimeoPlayerActivity.class);
            i.putExtra("videoId",model.getLink());
            i.putExtra("time",model.getTime());
            if(lessonJSON!=null)i.putExtra("lessonJSON",lessonJSON);
        }else {
            i = new Intent(c, DetailActivity.class);
            i.putExtra("link",model.getLink());
            i.putExtra("title",model.getTitle());
            i.putExtra("lessonId",model.getId());
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        c.startActivity(i);
    }


    public void setLessonJSON(String lessonJSON){
        this.lessonJSON=lessonJSON;
    }

    private  void showVIPRegistrationDialog(String msg){
        String title="VIP Registration";
        MyDialog myDialog=new MyDialog(c, title, msg, () -> {
            Intent intent=new Intent(c, WebSiteActivity.class);
            intent.putExtra("link", Routing.PAYMENT);
            c.startActivity(intent);
        });
        myDialog.showMyDialog();
    }

    public interface CallBack{
        void onDownloadClick();
    }
}