package com.calamus.easykorean.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.calamus.easykorean.R;
import com.calamus.easykorean.app.AppHandler;
import com.calamus.easykorean.models.DayItemModel;

import java.util.ArrayList;

import me.myatminsoe.mdetect.MDetect;

public class DayItemAdapter extends RecyclerView.Adapter<DayItemAdapter.Holder> {

    private final Activity c;
    private final ArrayList<DayItemModel> data;
    private final LayoutInflater mInflater;
    final SharedPreferences sharedPreferences;
    final String currentUserName;

    public DayItemAdapter(Activity c, ArrayList<DayItemModel> data){
        this.data=data;
        this.c=c;
        this.mInflater= LayoutInflater.from(c);
        sharedPreferences=c.getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        currentUserName=sharedPreferences.getString("userName",null);
        MDetect.INSTANCE.init(c);

    }


    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=mInflater.inflate(R.layout.item_day_lesson,parent,false);

        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        try{
            DayItemModel model=data.get(position);
            holder.tv.setText(AppHandler.setMyanmar(model.getTitle()));

            if(model.isLearned()){
                holder.iv.setBackgroundResource(R.drawable.ic_learned);
            }else{
                holder.iv.setBackgroundResource(R.drawable.ic_not_learned);
            }



        }catch (Exception ignored){

        }


    }

    public class Holder extends RecyclerView.ViewHolder{

        TextView tv;
        ImageView iv;
        public Holder(View view){
            super(view);
            tv=view.findViewById(R.id.tv_info_header);
            iv=view.findViewById(R.id.iv_learn);


            tv.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            tv.setMarqueeRepeatLimit(-1);
            tv.setSingleLine(true);
            tv.setSelected(true);


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

    public interface  CallBack{
        void onClick();
    }

}
