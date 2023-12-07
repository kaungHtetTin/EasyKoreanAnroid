package com.calamus.easykorean.adapters;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.calamus.easykorean.DayListActivity;
import com.calamus.easykorean.LessonActivity;
import com.calamus.easykorean.R;
import com.calamus.easykorean.app.AppHandler;
import com.calamus.easykorean.models.DayItemModel;
import com.calamus.easykorean.models.DayModel;
import com.calamus.easykorean.models.ExtraCourseModel;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class DayAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Activity c;
    private final ArrayList<Object> data;
    private final LayoutInflater mInflater;
    String themeColor;
    String courseTitle;

    public DayAdapter(Activity c, ArrayList<Object> data,String courseTitle,String themeColor){
        this.data=data;
        this.c=c;
        this.mInflater= LayoutInflater.from(c);
        this.courseTitle=courseTitle;
        this.themeColor=themeColor;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType==1){
            View view=mInflater.inflate(R.layout.item_title,parent,false);
            return new TitleHolder(view);
        }else if(viewType==2){
            View view = mInflater.inflate(R.layout.item_lesson_category, parent, false);
            return new MainCategory(view);
        }else{
            View view = mInflater.inflate(R.layout.item_day, parent, false);
            return new DayHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(data.get(position) instanceof  String) return 1;
        else  if (data.get(position) instanceof ExtraCourseModel) return 2;
        else return 3;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int p) {
        int position=holder.getAbsoluteAdapterPosition();
        if(data.get(position) instanceof String){
            String title=(String)data.get(position);
            TitleHolder titleHolder=(TitleHolder)holder;
            titleHolder.tv.setText(title);
        }else if (data.get(position) instanceof ExtraCourseModel){

            ExtraCourseModel model=(ExtraCourseModel) data.get(position);
            MainCategory mainCategory=(MainCategory)holder;
            mainCategory.tv.setText(model.getTitle());
            AppHandler.setPhotoFromRealUrl(mainCategory.iv,model.getImage_url());

        }else {
            try{

                DayModel model=(DayModel) data.get(position);
                DayHolder dayHolder=(DayHolder)holder;
                dayHolder.tv.setText(" Day "+model.getDay());

                ArrayList<DayItemModel> dayItemModels=new ArrayList<>();

                DayItemAdapter adapter=new DayItemAdapter(c, dayItemModels);

                LinearLayoutManager lm=new LinearLayoutManager(c);
                dayHolder.recyclerView.setAdapter(adapter);
                dayHolder.recyclerView.setLayoutManager(lm);
                dayHolder.recyclerView.setItemAnimator(new DefaultItemAnimator());

                JSONArray ja=model.getJsonArray();
                int count=0;
                int totalDuration=0;
                for(int i=0;i<ja.length();i++){
                    JSONObject jo=ja.getJSONObject(i);
                    String title=jo.getString("lesson_title");
                    int duration=jo.getInt("duration");
                    boolean learned=jo.getString("learned").equals("1");
                    if(learned)count++;
                    totalDuration+=duration;
                    dayItemModels.add(new DayItemModel(title,learned));
                }

                if(count==0) {
                    dayHolder.tv_progress.setText("Start Now");
                }
                else if(count>0 && count<ja.length()){
                    dayHolder.tv_progress.setText("On Going");
                }else{
                    dayHolder.tv_progress.setText("Completed");
                }
                dayHolder.tv_duration.setText(AppHandler.formatDuration(totalDuration));

                adapter.notifyDataSetChanged();

            }catch (Exception ignored){

            }
        }

    }

    public class DayHolder extends RecyclerView.ViewHolder{
        CardView dayContainer;
        TextView tv,tv_progress,tv_duration;
        RecyclerView recyclerView;
        RelativeLayout bottomLayout;
        LinearLayout clickLayout;

        public DayHolder(View view){
            super(view);
            tv=view.findViewById(R.id.tv_day);
            dayContainer=view.findViewById(R.id.card);
            tv_progress=view.findViewById(R.id.tv_progress);
            tv_duration=view.findViewById(R.id.tv_duration);
            recyclerView=view.findViewById(R.id.recycler);
            bottomLayout=view.findViewById(R.id.layoutBottomBar);
            clickLayout=view.findViewById(R.id.clickLayout);
            bottomLayout.setBackgroundColor(Color.parseColor(themeColor)); //for dynamic theme color


            clickLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(c, LessonActivity.class);
                    DayModel model=(DayModel) data.get(getAbsoluteAdapterPosition());
                    i.putExtra("category_id", "0");
                    i.putExtra("category_title","Day - "+model.getDay());
                    i.putExtra("course_title",courseTitle);
                    i.putExtra("level",model.getCourse_id());
                    i.putExtra("fragment",1);
                    i.putExtra("isDayPlan",true);
                    i.putExtra("day",model.getDay());
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    c.startActivity(i);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }


    public class TitleHolder extends RecyclerView.ViewHolder{

        TextView tv;
        public TitleHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            tv=itemView.findViewById(R.id.tv_item_title);
        }
    }


    public class MainCategory extends RecyclerView.ViewHolder{
        CardView cardView;
        ImageView iv;
        TextView tv;



        public MainCategory(@NonNull @NotNull View view) {
            super(view);

            iv=view.findViewById(R.id.iv_book);
            tv=view.findViewById(R.id.tv_category);
            cardView=view.findViewById(R.id.card_View);


            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(c, LessonActivity.class);
                    ExtraCourseModel model=(ExtraCourseModel) data.get(getAbsoluteAdapterPosition());
                    i.putExtra("category_id", model.getId());
                    i.putExtra("category_title",model.getTitle());
                    i.putExtra("course_title",courseTitle);
                    i.putExtra("level",DayListActivity.course_id);
                    i.putExtra("fragment",1);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    c.startActivity(i);
                }
            });

        }
    }

}
