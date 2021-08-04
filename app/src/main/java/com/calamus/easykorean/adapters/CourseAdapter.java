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

import com.calamus.easykorean.LessonActivity;
import com.calamus.easykorean.R;
import com.calamus.easykorean.app.AppHandler;
import com.calamus.easykorean.models.CategoryModel;

import java.util.ArrayList;

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.Holder> {

    private final Activity c;
    private final ArrayList<CategoryModel> data;
    private final LayoutInflater mInflater;
    String level;
    @SuppressLint("SimpleDateFormat")


    public CourseAdapter(Activity c, ArrayList<CategoryModel> data,String level) {
        this.data = data;
        this.c = c;
        this.mInflater = LayoutInflater.from(c);
        this.level=level;

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public CourseAdapter.Holder onCreateViewHolder( ViewGroup parent, int viewType) {

        View view = mInflater.inflate(R.layout.item_main_course, parent, false);
        return new Holder(view);

    }

    @Override
    public void onBindViewHolder(final CourseAdapter.Holder holder, final int i) {
        CategoryModel cModel=data.get(i);
        AppHandler.setPhotoFromRealUrl(holder.iv,cModel.getPic());
        holder.tv.setText(cModel.getCate());
    }


    public class Holder extends RecyclerView.ViewHolder {
        ImageView iv;
        TextView tv;
        public Holder(View view) {
            super(view);
            iv=view.findViewById(R.id.mainItemIv);
            tv=view.findViewById(R.id.tv_category);

            view.setOnClickListener(v -> {

                CategoryModel cModel=data.get(getAbsoluteAdapterPosition());
                Intent intent=new Intent(c, LessonActivity.class);
                intent.putExtra("cate", cModel.getCode());
                intent.putExtra("setCate",cModel.getCate());
                intent.putExtra("picLink",cModel.getPic());
                intent.putExtra("eCode",cModel.geteCode());
                intent.putExtra("level",level);
                intent.putExtra("fragment",1);
                c.startActivity(intent);

            });

        }

    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

}