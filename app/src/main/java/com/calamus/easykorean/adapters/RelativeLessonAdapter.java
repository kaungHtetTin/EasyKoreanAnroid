package com.calamus.easykorean.adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.calamus.easykorean.DetailActivity;
import com.calamus.easykorean.R;
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
import static com.calamus.easykorean.LessonActivity.course_id;
import static com.calamus.easykorean.LessonActivity.fragmentId;
import static com.calamus.easykorean.app.AppHandler.setMyanmar;
public class RelativeLessonAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private Activity c;
    private ArrayList<LessonModel> data;
    private LayoutInflater mInflater;
    SharedPreferences share;
    SharedPreferences.Editor editor;
    String currentUserId;
    Executor postExecutor;
    CallBack callBack;
    boolean videoChannel;
    long nowPlayingId;
    boolean isMember;

    LessonModel clickedLesson;

    public RelativeLessonAdapter(Activity c, ArrayList<LessonModel> data,long nowPlayingId,boolean videoChannel,CallBack callBack) {
        this.data = data;
        this.c = c;
        this.nowPlayingId=nowPlayingId;
        this.mInflater = LayoutInflater.from(c);
        this.callBack=callBack;
        this.videoChannel=videoChannel;
        share=c.getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        currentUserId=share.getString("phone","901");
        postExecutor= ContextCompat.getMainExecutor(c);
        isMember=share.getBoolean("course"+course_id,false);

    }


    @Override
    public int getItemCount() {
        return data.size();
    }

    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int p2) {
        View view = mInflater.inflate(R.layout.item_related_lesson, parent, false);
        return new Holder(view);
    }

    @Override
    public int getItemViewType(int position) {
        return  position;
    }


    @Override
    public void onBindViewHolder(@NotNull final RecyclerView.ViewHolder holder, final int i) {
        try {

            Holder lessonHolder=(Holder)holder;
            LessonModel model = data.get(i);
            lessonHolder.tvTitle.setText(setMyanmar(model.getTitle()));

            if(isMember){
               lessonHolder.iv_lock.setVisibility(View.GONE);
            }else{
                if(model.isVip()){
                    lessonHolder.iv_lock.setVisibility(View.VISIBLE);
                }else{
                    lessonHolder.iv_lock.setVisibility(View.GONE);
                }
            }

            if(nowPlayingId==model.getTime()){
                lessonHolder.layout.setBackgroundColor(c.getResources().getColor(R.color.selectedVideo));
            }else{
                lessonHolder.layout.setBackgroundColor(c.getResources().getColor(R.color.appBar));
            }

            if(model.isLearned()){
                lessonHolder.iv_learned.setVisibility(View.VISIBLE);
            }
            else  {
                lessonHolder.iv_learned.setVisibility(View.GONE);
            }

            if(model.isVideo()){
                lessonHolder.iv_video_circle.setVisibility(View.VISIBLE);
                AppHandler.setPhotoFromRealUrl(lessonHolder.iv,model.getThumbnail());
                if(model.isDownloaded()){
                    lessonHolder.ibt_play.setVisibility(View.VISIBLE);
                    lessonHolder.ibt_download.setVisibility(View.GONE);
                }else{
                    lessonHolder.ibt_download.setVisibility(View.VISIBLE);
                }


            }else {
                lessonHolder.iv_video_circle.setVisibility(View.GONE);
                AppHandler.setPhotoFromRealUrl(lessonHolder.iv,model.getImage_url());
                lessonHolder.ibt_download.setVisibility(View.GONE);
            }

            lessonHolder.tv_duration.setText(AppHandler.formatDuration(model.getDuration()));


        } catch (Exception e) {
            Toast.makeText(c, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    public class Holder extends RecyclerView.ViewHolder {
        TextView tvTitle,tv_duration;
        ImageView iv,iv_learned,iv_video_circle,iv_lock;
        ImageView ibt_download,ibt_play;
        ConstraintLayout layout;

        public Holder(final View view) {
            super(view);

            tvTitle = view.findViewById(R.id.tv_lesson_title);
            iv = view.findViewById(R.id.iv);
            iv_learned=view.findViewById(R.id.iv_learned);
            ibt_download=view.findViewById(R.id.ib_download);
            ibt_play=view.findViewById(R.id.ibt_play);
            tv_duration=view.findViewById(R.id.tv_duration);
            iv_video_circle=view.findViewById(R.id.iv_video_circle);
            iv_lock=view.findViewById(R.id.iv_lock);
            layout=view.findViewById(R.id.layoutRelatedLesson);
            iv.setClipToOutline(true);
            tvTitle.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            tvTitle.setMarqueeRepeatLimit(-1);
            tvTitle.setSingleLine(true);
            tvTitle.setSelected(true);
            editor=share.edit();

            ibt_download.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LessonModel model =  data.get(getAbsoluteAdapterPosition());
                    boolean isMember=share.getBoolean("course"+course_id,false);
                    boolean isVip=share.getBoolean("isVIP",false);
                    if(isMember||(isVip&&videoChannel)||(isVip&&fragmentId!=1)){
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
                    clickedLesson =data.get(getAbsoluteAdapterPosition());
                    boolean isVip=share.getBoolean("isVIP",false);

                    if(fragmentId==1){
                        if(isMember){
                            go(clickedLesson);
                            clickedLesson.setLearned(true);
                        }else{
                            if(clickedLesson.isVip()){
                                String msg="This Lesson is only for VIP user.\n\nDo you want to contact the Calamus Education for VIP Registration";
                                showVIPRegistrationDialog(msg);
                            }else{
                                go(clickedLesson);
                                clickedLesson.setLearned(true);
                            }
                        }

                    }else{
                        if(isVip){
                            go(clickedLesson);
                            clickedLesson.setLearned(true);
                        }else{
                            if(clickedLesson.isVip()){
                                String msg="This Lesson is only for VIP user.\n\nDo you want to contact the Calamus Education for VIP Registration";
                                showVIPRegistrationDialog(msg);
                            }else{
                                go(clickedLesson);
                                clickedLesson.setLearned(true);
                            }
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

        if(model.isVideo()){
            callBack.onClick(model);
            nowPlayingId=model.getTime();
            notifyDataSetChanged();
        }else {
            final Intent i = new Intent(c, DetailActivity.class);
            i.putExtra("link",model.getLink());
            i.putExtra("title",model.getTitle());
            i.putExtra("lessonId",model.getId());
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            c.startActivity(i);
        }
    }

    public void setNowPlayingId(long nowPlayingId) {
        this.nowPlayingId = nowPlayingId;
        notifyDataSetChanged();
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
        void onClick(LessonModel model);
        void onDownloadClick();
    }
}