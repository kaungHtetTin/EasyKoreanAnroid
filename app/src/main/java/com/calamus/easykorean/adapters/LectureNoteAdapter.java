package com.calamus.easykorean.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.calamus.easykorean.R;
import com.calamus.easykorean.app.AppHandler;
import com.calamus.easykorean.models.AppModel;
import com.calamus.easykorean.models.LectureNoteModel;

import java.util.ArrayList;

public class LectureNoteAdapter extends RecyclerView.Adapter<LectureNoteAdapter.Holder> {

    private final Activity c;
    private final ArrayList<LectureNoteModel> data;
    private final LayoutInflater mInflater;
    final SharedPreferences sharedPreferences;
    final String currentUserName;
    public LectureNoteAdapter(Activity c, ArrayList<LectureNoteModel> data){
        this.data=data;
        this.c=c;
        this.mInflater= LayoutInflater.from(c);
        sharedPreferences=c.getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        currentUserName=sharedPreferences.getString("userName",null);
    }


    @NonNull
    @Override
    public LectureNoteAdapter.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=mInflater.inflate(R.layout.item_lecture_note,parent,false);

        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LectureNoteAdapter.Holder holder, int position) {
        try{
            LectureNoteModel model = data.get(position);
            String time = model.getTime();
            String note = model.getNote();
            holder.tv_time.setText(formatTime(time));
            holder.tv_note.setText(note);

        }catch (Exception ignored){

        }


    }

    public class Holder extends RecyclerView.ViewHolder{
        TextView tv_note,tv_time;
        public Holder(View view){
            super(view);
            tv_note = view.findViewById(R.id.tv_note);
            tv_time = view.findViewById(R.id.tv_time);

        }
    }

    private String formatTime(String time){
        String result = "";

        int total_second = Integer.parseInt(time);
        int hour = total_second/(60*60);
        int temp = total_second%(60*60);
        int min = temp/60;
        int second = temp%60;

        String h = hour<10? "0"+hour: hour+"";
        String m = min<10? "0"+min: ""+min;
        String s = second < 10? "0"+second: second+"";

        result = String.format("%s:%s:%s", h,m,s);
        return result;
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }


}
