package com.calamus.easykorean.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.calamus.easykorean.DayListActivity;
import com.calamus.easykorean.R;
import com.calamus.easykorean.WebSiteActivity;
import com.calamus.easykorean.app.AppHandler;
import com.calamus.easykorean.models.CourseModel;
import com.calamus.easykorean.models.FunctionModel;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.concurrent.Executor;

public class MyLearningAdapter extends   RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Activity c;
    private final ArrayList<Object> data;
    private final LayoutInflater mInflater;
    SharedPreferences sharedPreferences;
    String currentUserId;
    Executor postExecutor;

    public MyLearningAdapter(Activity c,ArrayList<Object> data){
        this.c=c;
        this.data=data;
        this.mInflater = LayoutInflater.from(c);
        sharedPreferences=c.getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        currentUserId=sharedPreferences.getString("phone",null);
        postExecutor= ContextCompat.getMainExecutor(c);

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType==1){
            View view = mInflater.inflate(R.layout.item_function, parent, false);
            return new FunctionHolder(view);
        }else if(viewType==2){
            View view=mInflater.inflate(R.layout.item_title,parent,false);
            return new TitleHolder(view);
        }else{
            View view = mInflater.inflate(R.layout.item_my_learning_course, parent, false);
            return new CourseHolder(view);
        }

    }

    @Override
    public int getItemViewType(int position) {
        if(data.get(position) instanceof FunctionModel){
            return 1;
        }else if(data.get(position) instanceof String){
            return 2;
        }else{
            return 3;
        }

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(data.get(position) instanceof FunctionModel){
            FunctionHolder functionHolder=(FunctionHolder)holder;
            FunctionModel fModel=(FunctionModel) data.get(position);
            functionHolder.tv.setText(fModel.getName());
            AppHandler.setPhotoFromRealUrl(functionHolder.iv,fModel.getUrl());

        }
        else if(data.get(position) instanceof  String){
            String title=(String)data.get(position);
            TitleHolder titleHolder=(TitleHolder)holder;
            titleHolder.tv.setText(title);
        }
        else{

            CourseHolder courseHolder=(CourseHolder) holder;

            CourseModel courseModel=(CourseModel) data.get(position);
            String title=courseModel.getTitle();
            courseHolder.tv_course_title.setText(title);

            courseHolder.mLayout.setBackgroundColor(Color.parseColor(courseModel.getColorCode()));
            courseHolder.tv_progress.setText("Completed "+courseModel.getLesson_count()+"%");

            courseHolder.pb_enroll.setProgress(courseModel.getLesson_count());
            courseHolder.pb_enroll.setProgressTintList(ColorStateList.valueOf(Color.parseColor(courseModel.getColorCode())));
            AppHandler.setPhotoFromRealUrl(courseHolder.iv_cover,courseModel.getCover_url());
        }
    }

    public class CourseHolder extends  RecyclerView.ViewHolder{

        ImageView iv_cover;
        TextView tv_course_title,tv_progress;
        ProgressBar pb_enroll;
        ConstraintLayout mLayout;
        public CourseHolder(@NonNull View view) {
            super(view);

            tv_course_title=view.findViewById(R.id.tv_course_title);
            tv_progress=view.findViewById(R.id.tv_progress);
            pb_enroll=view.findViewById(R.id.progressBar);
            iv_cover=view.findViewById(R.id.iv_course_cover);
            mLayout=view.findViewById(R.id.layout_course_item);

            tv_course_title.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            tv_course_title.setMarqueeRepeatLimit(-1);
            tv_course_title.setSingleLine(true);
            tv_course_title.setSelected(true);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    CourseModel model=(CourseModel)data.get(getAbsoluteAdapterPosition());
                    Intent intent=new Intent(c, DayListActivity.class);
                    intent.putExtra("course_id",model.getCourse_id());
                    intent.putExtra("course_title",model.getTitle());
                    intent.putExtra("theme_color",model.getColorCode());
                    c.startActivity(intent);
                }
            });
        }
    }

    public class FunctionHolder extends RecyclerView.ViewHolder {

        ImageView iv;
        TextView tv;
        RelativeLayout functionContainer;

        public FunctionHolder(@NonNull View view) {
            super(view);
            iv=view.findViewById(R.id.iv_book);
            tv=view.findViewById(R.id.tv_functionName);
            functionContainer=view.findViewById(R.id.functionContainer);
            view.setOnClickListener(v -> {

                FunctionModel fModel=(FunctionModel) data.get(getAbsoluteAdapterPosition());
                Intent intentD = new Intent(c, WebSiteActivity.class);
                intentD.putExtra("link", fModel.getLink()+"?userid="+currentUserId);
                c.startActivity(intentD);
            });
        }
    }

    public class TitleHolder extends RecyclerView.ViewHolder{

        TextView tv;
        public TitleHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            tv=itemView.findViewById(R.id.tv_item_title);
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

}
