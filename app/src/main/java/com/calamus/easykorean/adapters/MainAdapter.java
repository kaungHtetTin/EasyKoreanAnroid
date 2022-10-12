package com.calamus.easykorean.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.calamus.easykorean.CourseRatingActivity;
import com.calamus.easykorean.DayListActivity;
import com.calamus.easykorean.R;
import com.calamus.easykorean.WebSiteActivity;
import com.calamus.easykorean.app.AppHandler;
import com.calamus.easykorean.app.MyHttp;
import com.calamus.easykorean.app.Routing;
import com.calamus.easykorean.models.CourseModel;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.Executor;

public class MainAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Activity c;
    private final ArrayList<Object> data;
    private final LayoutInflater mInflater;
    SharedPreferences sharedPreferences;
    String currentUserId;
    Executor postExecutor;
    public MainAdapter(Activity c, ArrayList<Object> data) {
        this.data = data;
        this.c = c;
        this.mInflater = LayoutInflater.from(c);
        sharedPreferences=c.getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        currentUserId=sharedPreferences.getString("phone",null);
        postExecutor= ContextCompat.getMainExecutor(c);

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int p2) {
        if(p2==0){
            View view=mInflater.inflate(R.layout.item_vip_purchase,parent,false);
            return new  VipPurchaseHolder(view);
        }else{
            View view = mInflater.inflate(R.layout.item_course_new, parent, false);
            return new Holder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {

        if(position==0){
            return 0;
        }else{
            return 1;
        }

    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NotNull RecyclerView.ViewHolder holder, final int p) {

        if(p==0){
            VipPurchaseHolder vipPurchaseHolder=(VipPurchaseHolder) holder;
            String url="https://www.calamuseducation.com/uploads/icons/easykoreanvipbanner.png";
            AppHandler.setPhotoFromRealUrl(vipPurchaseHolder.iv,url);

        }else if(data.get(p) instanceof CourseModel) {
            try {

                Holder courseHolder=(Holder)holder;

                CourseModel courseModel=(CourseModel) data.get(p);
                String title=courseModel.getTitle();
                courseHolder.tv_course_title.setText(title);
                courseHolder.tv_description.setText(courseModel.getDescription());
                courseHolder.tv_day.setText(courseModel.getDuration()+" Days");
                courseHolder.tv_start_course.setTextColor(Color.parseColor(courseModel.getColorCode()));

                courseHolder.mLayout.setBackgroundColor(Color.parseColor(courseModel.getColorCode()));


                if(courseModel.getLesson_count()!=0){
                    courseHolder.tv_start_course.setText("Enter");
                    courseHolder.tv_progress.setText("Completed "+courseModel.getLesson_count()+"%");

                }else{
                    courseHolder.tv_progress.setText("Let's start the course now!");
                }

                courseHolder.pb_enroll.setProgress(courseModel.getLesson_count());
                courseHolder.pb_enroll.setProgressTintList(ColorStateList.valueOf(Color.parseColor(courseModel.getColorCode())));

                if(courseModel.getLesson_count()==100){

                }else {

                }

                AppHandler.setPhotoFromRealUrl(courseHolder.iv_cover,courseModel.getCover_url());

                if(courseModel.isVip()){

                }else{

                }

                // boolean isMember=sharedPreferences.getBoolean("course"+courseModel.getCourse_id(),false);

                fetchReview(courseModel,((Holder) holder).tv_rating);

            } catch (Exception e) {
                Toast.makeText(c, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public class Holder extends RecyclerView.ViewHolder {

        ImageView iv_cover,iv_rating;
        TextView tv_course_title,tv_description,tv_progress,tv_day,tv_start_course,tv_rating;
        ProgressBar pb_enroll;
        LinearLayout layout_start_course;
        ConstraintLayout mLayout;

        public Holder(View view) {
            super(view);
            iv_rating=view.findViewById(R.id.iv_rating);
            tv_course_title=view.findViewById(R.id.tv_course_title);
            tv_progress=view.findViewById(R.id.tv_progress);
            tv_description=view.findViewById(R.id.tv_course_description);
            tv_day=view.findViewById(R.id.tv_day);
            tv_start_course=view.findViewById(R.id.tv_start_course);
            tv_rating=view.findViewById(R.id.tv_rating);
            pb_enroll=view.findViewById(R.id.progressBar);
            iv_cover=view.findViewById(R.id.iv_course_cover);
            layout_start_course=view.findViewById(R.id.layout_start_course);
            mLayout=view.findViewById(R.id.layout_course_item);

            tv_description.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            tv_description.setMarqueeRepeatLimit(-1);
            tv_description.setSingleLine(true);
            tv_description.setSelected(true);

            tv_course_title.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            tv_course_title.setMarqueeRepeatLimit(-1);
            tv_course_title.setSingleLine(true);
            tv_course_title.setSelected(true);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CourseModel model=(CourseModel)data.get(getAbsoluteAdapterPosition());
                    Intent intent=new Intent(c, DayListActivity.class);
                    intent.putExtra("course_id",model.getCourse_id());
                    intent.putExtra("course_title",model.getTitle());
                    intent.putExtra("theme_color",model.getColorCode());
                    c.startActivity(intent);
                }
            });

            iv_rating.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    CourseModel model=(CourseModel)data.get(getAbsoluteAdapterPosition());
                    Intent intent=new Intent(c, CourseRatingActivity.class);
                    intent.putExtra("course_id",model.getCourse_id());
                    c.startActivity(intent);
                }
            });

            layout_start_course.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
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



    public class VipPurchaseHolder extends RecyclerView.ViewHolder{

        ImageView iv;
        public VipPurchaseHolder(@NonNull View itemView) {
            super(itemView);
            iv=itemView.findViewById(R.id.iv);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(c, WebSiteActivity.class);
                    i.putExtra("link", Routing.PAYMENT);
                    c.startActivity(i);
                }
            });
        }
    }

    private void getCourseData(String courseId,int duration,TextView tv,Button bt,int progress){
        new Thread(() -> {
            MyHttp myHttp = new MyHttp(MyHttp.RequesMethod.GET, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    postExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject jo=new JSONObject(response);
                                String startDate=jo.getString("start_date");
                                tv.setText("The course will end on "+calculateEndDate(startDate,duration));
                                Log.e("EndDate ",calculateEndDate(startDate,duration));
                                bt.setVisibility(View.INVISIBLE);
                            }catch (Exception e){
                                if (progress<1)bt.setVisibility(View.VISIBLE);
                            }


                        }
                    });
                }


                @Override
                public void onError(String msg) {
                    Log.e("startCourse ", msg);
                }
            }).url(Routing.GET_COURSE_ENROLL+"?user_id="+currentUserId+"&course_id="+courseId);
            myHttp.runTask();
        }).start();
    }

    private String calculateEndDate(String startDate,int duration){
        String result="";
        String [] arr=startDate.split("-");
        int year=Integer.parseInt(arr[0]);
        int month=Integer.parseInt(arr[1]);
        int day=Integer.parseInt(arr[2]);

        month--;

        Calendar c = Calendar.getInstance();
        c.set(year,month,day);
        c.add(Calendar.DATE,duration);

        String [] months={"Jan","Feb","March","April","May","Jun","Jul","Aug","Sept","Oct","Nov","Dec"};

        result=c.get(Calendar.DAY_OF_MONTH)+" - "+months[c.get(Calendar.MONTH)]+" - "+c.get(Calendar.YEAR);

        return result;
    }

    private void fetchReview(CourseModel model,TextView tv){
        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.GET, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    postExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            try{
                                int total_star=0,total_rating=0;
                                JSONObject joMain=new JSONObject(response);
                                JSONArray jaRating=joMain.getJSONArray("rating");
                                for(int i=0;i<jaRating.length();i++){
                                    JSONObject joRating=jaRating.getJSONObject(i);
                                    float star=(float) joRating.getDouble("star");
                                    int rating=joRating.getInt("total_rating");
                                    double temp=star*rating;
                                    total_star+=temp;
                                    total_rating+=rating;
                                }
                                if(total_rating!=0){
                                    DecimalFormat decimalFormat = new DecimalFormat("0.0");
                                    float rating=(float) total_star/total_rating;
                                    String mainRating=decimalFormat.format(rating);
                                    tv.setText(mainRating);

                                }else{
                                    tv.setText("0.0");
                                }
                            }catch (Exception e){}
                        }
                    });
                }
                @Override
                public void onError(String msg) {
                    Log.e("CourseRating Err : ",msg);
                }
            }).url(Routing.GET_COURSE_RATING+"?user_id="+currentUserId+"&course_id="+model.getCourse_id());
            myHttp.runTask();
        }).start();
    }
}
