package com.calamus.easykorean.adapters;



import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;


import com.calamus.easykorean.MyDiscussionActivity;
import com.calamus.easykorean.R;
import com.calamus.easykorean.app.AppHandler;
import com.calamus.easykorean.models.TopGamePlayerModel;

import java.util.ArrayList;

public class TopGamePlayerAdapter extends RecyclerView.Adapter<TopGamePlayerAdapter.Holder> {

    private Activity c;
    private ArrayList<TopGamePlayerModel> data;
    private LayoutInflater mInflater;

    @SuppressLint("SimpleDateFormat")


    public TopGamePlayerAdapter(Activity c, ArrayList<TopGamePlayerModel> data) {
        this.data = data;
        this.c = c;
        this.mInflater = LayoutInflater.from(c);

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public TopGamePlayerAdapter.Holder onCreateViewHolder( ViewGroup parent, int viewType) {

        View view = mInflater.inflate(R.layout.item_highest_score, parent, false);
        return new Holder(view);

    }

    @Override
    public void onBindViewHolder(final TopGamePlayerAdapter.Holder holder, final int i) {
        TopGamePlayerModel model=data.get(i);
        holder.tv_name.setText(model.getName());
        holder.tv_score.setText(formatScore(model.getScore()));
        if(!model.getImageUrl().equals("")) AppHandler.setPhotoFromRealUrl(holder.iv_profile,model.getImageUrl());
        holder.tv_number.setText((i+1)+"");

    }

    public class Holder extends RecyclerView.ViewHolder {
        TextView tv_name,tv_score,tv_number;
        ImageView iv_profile;
        public Holder(View view) {
            super(view);
            tv_number=view.findViewById(R.id.tv_number);
            tv_name=view.findViewById(R.id.tv_name);
            tv_score=view.findViewById(R.id.tv_score);
            iv_profile=view.findViewById(R.id.iv_profile);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    TopGamePlayerModel model=data.get(getAbsoluteAdapterPosition());
                    Intent intent=new Intent(c, MyDiscussionActivity.class);
                    intent.putExtra("userId",model.getUserId());
                    intent.putExtra("userName",model.getName());
                    c.startActivity(intent);
                }
            });

        }

    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    private String formatScore(int score){
        if(score>1) return score+" points";
        else return score+" point";
    }
}