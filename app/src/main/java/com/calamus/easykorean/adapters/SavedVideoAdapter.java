package com.calamus.easykorean.adapters;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.calamus.easykorean.R;
import com.calamus.easykorean.VideoPlayerActivity;
import com.calamus.easykorean.app.MyDialog;
import com.calamus.easykorean.models.SavedVideoModel;
import java.io.File;
import java.util.ArrayList;

public class SavedVideoAdapter extends RecyclerView.Adapter<SavedVideoAdapter.Holder> {

    ArrayList<SavedVideoModel> dataLists;
    Activity c;
    private LayoutInflater mInflater;

    public SavedVideoAdapter(ArrayList<SavedVideoModel> dataLists, Activity c) {
        this.dataLists = dataLists;
        this.c = c;
        this.mInflater = LayoutInflater.from(c);
    }


    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view=mInflater.inflate(R.layout.item_saved_video,parent,false);
        return new Holder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        SavedVideoModel model=dataLists.get(position);
        holder.tvVideoName.setText(model.getName());

        if(model.getThumbnail()!=null){
            holder.iv_videoThumb.setImageBitmap(model.getThumbnail());
        }else{
            Glide.with(c)
                    .load(model.getUri()) // or URI/path
                    .into(holder.iv_videoThumb);

        }
        holder.tvDuration.setText(formatDuration(model.getDuration()));
    }

    public class Holder extends RecyclerView.ViewHolder{
        TextView tvVideoName,tvDuration;
        ImageView iv_videoThumb;

        public Holder(@NonNull View itemView) {
            super(itemView);
            tvVideoName=itemView.findViewById(R.id.videoName);
            iv_videoThumb=itemView.findViewById(R.id.iv_videoThumb);
            tvDuration=itemView.findViewById(R.id.tv_duration);
            itemView.setOnClickListener(view -> {
                SavedVideoModel model=dataLists.get(getAbsoluteAdapterPosition());
                Intent intent=new Intent(c, VideoPlayerActivity.class);
                intent.putExtra("videoData",model.getUri());
                c.startActivity(intent);
            });

            itemView.setOnLongClickListener(view -> {
                SavedVideoModel model=dataLists.get(getAbsoluteAdapterPosition());
                deleteTheSaveVideo(model.getUri(),getAbsoluteAdapterPosition());

                return false;
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


    private void deleteTheSaveVideo(Uri uri,int position){

        MyDialog myDialog=new MyDialog(c, "Delete!", "Do you really want to delete this video", () -> {
            File myVideoFile=new File(uri.getPath());
            boolean isDeleted=myVideoFile.delete();
            if(isDeleted){
                dataLists.remove(position);
                notifyDataSetChanged();
                Toast.makeText(c,"Deleted",Toast.LENGTH_SHORT).show();
            }
        });
        myDialog.showMyDialog();


    }
}
