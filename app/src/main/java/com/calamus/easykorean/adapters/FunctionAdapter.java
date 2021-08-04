package com.calamus.easykorean.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.recyclerview.widget.RecyclerView;


import com.calamus.easykorean.R;
import com.calamus.easykorean.WebSiteActivity;
import com.calamus.easykorean.app.AppHandler;
import com.calamus.easykorean.models.CategoryModel;

import java.util.ArrayList;


public class FunctionAdapter extends RecyclerView.Adapter<FunctionAdapter.Holder> {

    private final Activity c;
    private final ArrayList<CategoryModel> data;
    private final LayoutInflater mInflater;
    SharedPreferences sharedPreferences;
    String currentUserId;

    @SuppressLint("SimpleDateFormat")


    public FunctionAdapter(Activity c, ArrayList<CategoryModel> data) {
        this.data = data;
        this.c = c;
        this.mInflater = LayoutInflater.from(c);
        sharedPreferences=c.getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        currentUserId=sharedPreferences.getString("phone",null);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public FunctionAdapter.Holder onCreateViewHolder( ViewGroup parent, int viewType) {

        View view = mInflater.inflate(R.layout.item_function, parent, false);
        return new Holder(view);

    }

    @Override
    public void onBindViewHolder(final FunctionAdapter.Holder holder, final int i) {
        CategoryModel cModel=data.get(i);
        AppHandler.setPhotoFromRealUrl(holder.iv,cModel.getPic());
    }


    public class Holder extends RecyclerView.ViewHolder {
        ImageView iv;
        public Holder(View view) {
            super(view);
            iv=view.findViewById(R.id.mainItemIv);
            view.setOnClickListener(v -> {

                CategoryModel cModel=data.get(getAbsoluteAdapterPosition());
                Intent intentD = new Intent(c, WebSiteActivity.class);
                intentD.putExtra("link", cModel.getCate()+"?userid="+currentUserId);
                c.startActivity(intentD);
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }


}