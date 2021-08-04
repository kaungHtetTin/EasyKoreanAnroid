package com.calamus.easykorean.adapters;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import com.calamus.easykorean.R;
import com.calamus.easykorean.app.AppHandler;
import com.calamus.easykorean.models.TopGamePlayerModel;

import java.util.ArrayList;

public class TopGamePlayerAdapter extends RecyclerView.Adapter<TopGamePlayerAdapter.Holder> {

    private final Activity c;
    private final ArrayList<TopGamePlayerModel> data;
    private final LayoutInflater mInflater;

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

        View view = mInflater.inflate(R.layout.item_top_game_player, parent, false);
        return new Holder(view);

    }

    @Override
    public void onBindViewHolder(final TopGamePlayerAdapter.Holder holder, final int i) {
       TopGamePlayerModel model=data.get(i);
       holder.tv.setText(model.getScore());
       AppHandler.setPhotoFromRealUrl(holder.iv,model.getImageUrl());
    }


    public class Holder extends RecyclerView.ViewHolder {
        ImageView iv;
        TextView tv;
        public Holder(View view) {
            super(view);
            iv=view.findViewById(R.id.iv_profile_game);
            tv=view.findViewById(R.id.tv_gameScore);

        }

    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

}