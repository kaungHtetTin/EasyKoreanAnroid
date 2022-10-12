package com.calamus.easykorean.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.calamus.easykorean.R;
import com.calamus.easykorean.models.VideoCategoryModel;

import java.util.ArrayList;


public class VideoCategoryAdapter extends RecyclerView.Adapter<VideoCategoryAdapter.Holder> {

    private Activity c;
    private ArrayList<VideoCategoryModel> data;
    private LayoutInflater mInflater;
    @SuppressLint("SimpleDateFormat")
    private Callback callback;
    int activeIndex;


    public VideoCategoryAdapter(Activity c, ArrayList<VideoCategoryModel> data,Callback callback) {
        this.data = data;
        this.c = c;
        this.mInflater = LayoutInflater.from(c);
        this.callback = callback;
        activeIndex=0;

    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setActiveIndex(int activeIndex) {
        this.activeIndex = activeIndex;
    }

    @Override
    public VideoCategoryAdapter.Holder onCreateViewHolder( ViewGroup parent, int viewType) {

        View view = mInflater.inflate(R.layout.item_video_category, parent, false);
        return new Holder(view);

    }

    @Override
    public void onBindViewHolder(final VideoCategoryAdapter.Holder holder, final int i) {
        String category=data.get(i).getCategory();
        holder.tv.setText(category);
        holder.callback = callback;

        if(activeIndex==i){
            holder.tv.setTextColor(c.getResources().getColor(R.color.white));
            holder.tv.setBackgroundColor(c.getResources().getColor(R.color.colorThemeLight));
        }else{
            holder.tv.setTextColor(c.getResources().getColor(R.color.colorBlack1));
            holder.tv.setBackgroundColor(c.getResources().getColor(R.color.bgVideoCategory));
        }

    }


    public class Holder extends RecyclerView.ViewHolder {
        TextView tv;
        protected Callback callback;

        public Holder(View view) {
            super(view);
            tv=view.findViewById(R.id.tv_category);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (callback != null) {
                        VideoCategoryModel model=data.get(getAbsoluteAdapterPosition());
                        callback.onCategoryClick(model.getId(),model.getCategory());

                        tv.setTextColor(c.getResources().getColor(R.color.white));
                        tv.setBackgroundColor(c.getResources().getColor(R.color.colorTheme));
                        activeIndex=getAbsoluteAdapterPosition();
                        notifyDataSetChanged();

                    }

                }
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }


    public  interface Callback {
        void onCategoryClick(String categoryID,String category);
    }
}