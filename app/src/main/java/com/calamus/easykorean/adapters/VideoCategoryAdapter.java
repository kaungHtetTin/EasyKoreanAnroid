package com.calamus.easykorean.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.calamus.easykorean.R;
import java.util.ArrayList;

public class VideoCategoryAdapter extends RecyclerView.Adapter<VideoCategoryAdapter.Holder> {

    private final Activity c;
    private final ArrayList<String> data;
    private final LayoutInflater mInflater;
    @SuppressLint("SimpleDateFormat")
    private Callback callback;

    public VideoCategoryAdapter(Activity c, ArrayList<String> data,Callback callback) {
        this.data = data;
        this.c = c;
        this.mInflater = LayoutInflater.from(c);
        this.callback = callback;

    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }


    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public VideoCategoryAdapter.Holder onCreateViewHolder( ViewGroup parent, int viewType) {

        View view = mInflater.inflate(R.layout.item_video_category, parent, false);
        return new Holder(view);

    }

    @Override
    public void onBindViewHolder(final VideoCategoryAdapter.Holder holder, final int i) {
        String category=data.get(i);
        holder.tv.setText(category);
        holder.callback = callback;

    }



    public class Holder extends RecyclerView.ViewHolder {
        TextView tv;
        protected Callback callback;

        public Holder(View view) {
            super(view);
            tv=view.findViewById(R.id.tv_category);
            view.setOnClickListener(v -> {
                if (callback != null) {
                    callback.onCategoryClick(data.get(getAbsoluteAdapterPosition()));
                }
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public  interface Callback {
        void onCategoryClick(String category);

    }

}