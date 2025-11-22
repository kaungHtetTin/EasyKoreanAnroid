package com.calamus.easykorean.adapters;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.calamus.easykorean.R;
import com.calamus.easykorean.SavedVideoActivity;
import com.calamus.easykorean.VideoPlayerActivity;
import com.calamus.easykorean.models.FileModel;
import com.calamus.easykorean.models.FolderModel;
import com.calamus.easykorean.models.SavedVideoModel;

import java.util.ArrayList;

public class SavedVideoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    ArrayList<FileModel> dataLists;
    Activity c;
    private LayoutInflater mInflater;
    boolean directoryChoosing;
    CallBack callBack;

    public static ArrayList<SavedVideoModel> relatedVideos=new ArrayList<>();

    public SavedVideoAdapter(ArrayList<FileModel> dataLists, Activity c,boolean directoryChoosing,CallBack callBack) {
        this.dataLists = dataLists;
        this.c = c;
        this.callBack=callBack;
        this.directoryChoosing=directoryChoosing;
        this.mInflater = LayoutInflater.from(c);
        relatedVideos.clear();
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if(viewType==1){
            View view=mInflater.inflate(R.layout.item_saved_video,parent,false);
            return new VideoHolder(view);
        }else{
            View view=mInflater.inflate(R.layout.item_folder,parent,false);
            return new FolderHolder(view);
        }

    }

    @Override
    public int getItemViewType(int position) {
        if(dataLists.get(position) instanceof SavedVideoModel){
            return 1;
        }else{
            return 2;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(dataLists.get(position) instanceof SavedVideoModel){
            SavedVideoModel model=(SavedVideoModel) dataLists.get(position);
            relatedVideos.add(model);
            VideoHolder videoHolder=(VideoHolder)holder;
            videoHolder.tvVideoName.setText(model.getName());
            if(model.getThumbnail()!=null){
                videoHolder.iv_videoThumb.setImageBitmap(model.getThumbnail());
            }else{
                Glide.with(c)
                        .load(model.getUri()) // or URI/path
                        .into(videoHolder.iv_videoThumb);
            }
            videoHolder.tvDuration.setText(formatDuration(model.getDuration()));

            if(model.getSelectedState()==0){
                animateOnSelected(videoHolder.card_videoThumb,0,300);
                animateOnSelected(videoHolder.tvVideoName,0,300);
                videoHolder.iv_selector.setImageResource(R.drawable.ic_file_unselected);
            }else if(model.getSelectedState()==1){
                animateOnSelected(videoHolder.card_videoThumb,120,300);
                animateOnSelected(videoHolder.tvVideoName,120,300);
                videoHolder.iv_selector.setImageResource(R.drawable.ic_file_unselected);
            }else if(model.getSelectedState()==2){
                videoHolder.iv_selector.setImageResource(R.drawable.ic_file_selected);
                animateOnSelected(videoHolder.card_videoThumb,120,1);
                animateOnSelected(videoHolder.tvVideoName,120,1);
            }



        }else{
            FolderModel folderModel=(FolderModel) dataLists.get(position);
            FolderHolder folderHolder=(FolderHolder) holder;
            folderHolder.tv_folderName.setText(folderModel.getFile().getName());

            if(folderModel.getSelectedState()==0){
                animateOnSelected(folderHolder.iv_folder,0,300);
                animateOnSelected(folderHolder.tv_folderName,0,300);
                folderHolder.iv_selector.setImageResource(R.drawable.ic_file_unselected);
            }else if(folderModel.getSelectedState()==1){
                animateOnSelected(folderHolder.iv_folder,120,300);
                animateOnSelected(folderHolder.tv_folderName,120,300);
                folderHolder.iv_selector.setImageResource(R.drawable.ic_file_unselected);
            }else if(folderModel.getSelectedState()==2){
                folderHolder.iv_selector.setImageResource(R.drawable.ic_file_selected);
                animateOnSelected(folderHolder.iv_folder,120,1);
                animateOnSelected(folderHolder.tv_folderName,120,1);
            }

        }
    }

    public class VideoHolder extends RecyclerView.ViewHolder{

        TextView tvVideoName,tvDuration;
        ImageView iv_videoThumb,iv_selector;
        CardView card_videoThumb;

        public VideoHolder(@NonNull View itemView) {
            super(itemView);
            tvVideoName=itemView.findViewById(R.id.videoName);
            iv_videoThumb=itemView.findViewById(R.id.iv_videoThumb);
            tvDuration=itemView.findViewById(R.id.tv_duration);
            iv_selector=itemView.findViewById(R.id.iv_selector);
            card_videoThumb=itemView.findViewById(R.id.card_videoThumb);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SavedVideoModel model=(SavedVideoModel) dataLists.get(getAbsoluteAdapterPosition());
                    if(model.getSelectedState()==0){
                        Intent intent=new Intent(c, VideoPlayerActivity.class);
                        intent.putExtra("videoData",model.getUri());
                        intent.putExtra("title",model.getName());
                        intent.putExtra("play_list_index",getAbsoluteAdapterPosition());
                        c.startActivity(intent);
                    }else{
                        callBack.onSelected(getAbsoluteAdapterPosition());
                    }
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if(!directoryChoosing)callBack.onLongClick(getAbsoluteAdapterPosition());
                    return false;
                }
            });

        }
    }

    public class FolderHolder extends RecyclerView.ViewHolder{
        TextView tv_folderName;
        ImageView iv_selector,iv_folder;
        public FolderHolder(@NonNull View itemView) {
            super(itemView);
            tv_folderName=itemView.findViewById(R.id.tv_folderName);
            iv_selector=itemView.findViewById(R.id.iv_selector);
            iv_folder=itemView.findViewById(R.id.iv_folder);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FolderModel model=(FolderModel) dataLists.get(getAbsoluteAdapterPosition());
                    if(directoryChoosing){
                        callBack.onDirectoryChosen(model);
                    }else{
                        if(model.getSelectedState()==0){
                            Intent intent=new Intent(c, SavedVideoActivity.class);
                            intent.putExtra("rootPath",model.getFile().getAbsolutePath());
                            c.startActivity(intent);
                        }else{
                            callBack.onSelected(getAbsoluteAdapterPosition());
                        }
                    }
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if(!directoryChoosing)callBack.onLongClick(getAbsoluteAdapterPosition());
                    return false;
                }
            });

        }
    }

    @Override
    public int getItemCount() {
        return dataLists.size();
    }

    private String formatDuration(int time){

        int sec = time/1000;
        int second = sec % 60;
        int minute = sec / 60;
        if (minute >= 60) {
            int hour = minute / 60;
            minute %= 60;
            return hour + ":" + (minute < 10 ? "0" + minute : minute) + ":" + (second < 10 ? "0" + second : second);
        }
        return (minute < 10 ? "0" + minute : minute) + ":" + (second < 10 ? "0" + second : second);
    }


    private void animateOnSelected(View v,int x,int duration){

        ObjectAnimator moveX = ObjectAnimator.ofFloat(v, "translationX", x);
        moveX.setDuration(duration);
        AnimatorSet move = new AnimatorSet();
        move.play(moveX);
        move.start();
    }

    public interface  CallBack{
        void onLongClick(int position);
        void onSelected(int position);
        void onDirectoryChosen(FolderModel model);
    }
}
