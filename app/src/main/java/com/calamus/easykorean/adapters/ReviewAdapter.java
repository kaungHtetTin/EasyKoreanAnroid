package com.calamus.easykorean.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.calamus.easykorean.R;
import com.calamus.easykorean.app.AppHandler;
import com.calamus.easykorean.models.ReviewModel;

import java.util.ArrayList;

public class ReviewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Activity c;
    private final ArrayList<Object> data;
    private final LayoutInflater mInflater;
    SharedPreferences sharedPreferences;

    @SuppressLint("SimpleDateFormat")
    public ReviewAdapter(Activity c, ArrayList<Object> data) {
        this.data = data;
        this.c = c;
        this.mInflater = LayoutInflater.from(c);
        sharedPreferences=c.getSharedPreferences("GeneralData", Context.MODE_PRIVATE);

    }


    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder( ViewGroup parent, int viewType) {

        if(viewType==1){
            View view = mInflater.inflate(R.layout.item_review, parent, false);
            return new Holder(view);
        }else{
            View view = mInflater.inflate(R.layout.shimmer_item_review, parent, false);
            return new LoadingHolder(view);
        }

    }

    @Override
    public int getItemViewType(int position) {
        if(data.get(position) instanceof ReviewModel){
            return 1;
        }else{
            return 2;
        }

    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, final int i) {
        if(data.get(i) instanceof ReviewModel){
            Holder holder=(Holder) viewHolder;
            ReviewModel model=(ReviewModel) data.get(i);

            holder.tv_username.setText(model.getUsername());
            holder.tv_review.setText(model.getReview());

            if(model.getReview().equals(""))holder.tv_review.setVisibility(View.GONE);
            else holder.tv_review.setVisibility(View.VISIBLE);

            holder.ratingBar.setRating(model.getStar());
            AppHandler.setPhotoFromRealUrl(holder.iv_profile,model.getImageUrl());
            holder.tv_date.setText(AppHandler.formatTime(model.getTime()));

        }
    }


    public class Holder extends RecyclerView.ViewHolder {
        ImageView iv_profile;
        TextView tv_username,tv_review,tv_date;
        RatingBar ratingBar;

        public Holder(View view) {
            super(view);
            iv_profile=view.findViewById(R.id.iv_profile);
            tv_username=view.findViewById(R.id.tv_username2);
            tv_review=view.findViewById(R.id.tv_review);
            tv_date=view.findViewById(R.id.tv_date);
            ratingBar=view.findViewById(R.id.rating_bar);
        }
    }

    public class LoadingHolder extends RecyclerView.ViewHolder{

        public LoadingHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

}