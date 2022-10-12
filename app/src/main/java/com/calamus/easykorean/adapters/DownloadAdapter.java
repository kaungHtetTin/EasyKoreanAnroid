package com.calamus.easykorean.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.calamus.easykorean.R;
import com.calamus.easykorean.service.Downloader;

import java.text.DecimalFormat;
import java.util.ArrayList;

import me.myatminsoe.mdetect.MDetect;

public class DownloadAdapter extends RecyclerView.Adapter<DownloadAdapter.Holder> {

    private final Activity c;
    private final ArrayList<Downloader> data;
    private final LayoutInflater mInflater;
    final SharedPreferences sharedPreferences;
    final String currentUserName;
    public DownloadAdapter(Activity c, ArrayList<Downloader> data){
        this.data=data;
        this.c=c;
        this.mInflater= LayoutInflater.from(c);
        sharedPreferences=c.getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        currentUserName=sharedPreferences.getString("userName",null);
        MDetect.INSTANCE.init(c);
    }


    @NonNull
    @Override
    public DownloadAdapter.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=mInflater.inflate(R.layout.item_downloading_list,parent,false);

        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DownloadAdapter.Holder holder, int position) {
        try{
            Downloader downloader=data.get(position);
            holder.tv_filename.setText(downloader.getFilename());
            holder.pb_download.setProgress((int)downloader.getProgress());
            if(downloader.getProgress()==0){
                holder.tv_progress.setText("0% pending");
            }else if(downloader.getProgress()==100){
                holder.tv_progress.setText("100% completed");
            }else{
                holder.tv_progress.setText(downloader.getProgress() +"% downloading");
            }

            holder.tv_fileSize.setText(formatFileSize(downloader.getFileSize()));


            holder.iv_cancel_reset.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    downloader.setUserCancel(true);
                }
            });

        }catch (Exception e){

        }


    }

    public class Holder extends RecyclerView.ViewHolder{
        TextView tv_filename,tv_progress,tv_fileSize;
        ProgressBar pb_download;
        ImageView iv_cancel_reset;
        public Holder(View view){
            super(view);
            tv_filename=view.findViewById(R.id.tv_filename);
            tv_progress=view.findViewById(R.id.tv_progress);
            tv_fileSize=view.findViewById(R.id.tv_file_size);
            pb_download=view.findViewById(R.id.pb_download_progress);
            iv_cancel_reset=view.findViewById(R.id.iv_cancel_reset);

        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }


    private String formatFileSize(long l){
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        return decimalFormat.format((double)l/1000000) +"MB";
    }
}
