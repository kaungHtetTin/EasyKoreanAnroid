package com.calamus.easykorean.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.calamus.easykorean.R;
import com.calamus.easykorean.models.CategoryModel;
import com.calamus.easykorean.models.CourseModel;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;


public class MainAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Activity c;
    private final ArrayList<Object> data;
    private final LayoutInflater mInflater;
    SharedPreferences sharedPreferences;
    public MainAdapter(Activity c, ArrayList<Object> data) {
        this.data = data;
        this.c = c;
        this.mInflater = LayoutInflater.from(c);
        sharedPreferences=c.getSharedPreferences("GeneralData", Context.MODE_PRIVATE);

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int p2) {
        if(p2==0){
            View view = mInflater.inflate(R.layout.fragment_one, parent, false);
            return new FunctionHolder(view);
        }
        View view = mInflater.inflate(R.layout.item_course, parent, false);
        return new Holder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NotNull RecyclerView.ViewHolder holder, final int p) {
        if(p==0){
            String jsonCategory=(String)data.get(p);
            FunctionHolder functionHolder=(FunctionHolder)holder;

            ArrayList<CategoryModel> categoryListing=new ArrayList<>();
            FunctionAdapter adapter=new FunctionAdapter(c,categoryListing);
            LinearLayoutManager lm = new LinearLayoutManager(c, LinearLayoutManager.HORIZONTAL, false);
            functionHolder.recyclerView.setLayoutManager(lm);
            functionHolder.recyclerView.setItemAnimator(new DefaultItemAnimator());
            functionHolder.recyclerView.setAdapter(adapter);

            functionHolder.recyclerView.setAnimation(AnimationUtils.loadAnimation(c,R.anim.transit_left));

            try{
                JSONArray ja2=new JSONArray(jsonCategory);
                for(int i=0;i<ja2.length();i++) {
                    JSONObject jo = ja2.getJSONObject(i);
                    String cate=jo.getString("link");
                    String pic=jo.getString("pic");
                    categoryListing.add(new CategoryModel(cate,"",pic,""));
                }
                adapter.notifyDataSetChanged();
            }catch (Exception ignored){}

        }else {
            try {

                Holder courseHolder=(Holder)holder;

                if(p%2==0) courseHolder.recyclerView.setAnimation(AnimationUtils.loadAnimation(c,R.anim.transit_left));
                else courseHolder.recyclerView.setAnimation(AnimationUtils.loadAnimation(c,R.anim.transit_right));

                CourseModel courseModel=(CourseModel) data.get(p);
                String title=courseModel.getTitle();
                boolean isMember=sharedPreferences.getBoolean(title,false);
                courseHolder.tv.setText(title);

                int enrollProgress=courseModel.getEnrollProgress();
                courseHolder.pb_enroll.setProgress(enrollProgress);
                courseHolder.tv_value.setText(enrollProgress+" %");

                if(enrollProgress==100)courseHolder.ivCerti.setBackgroundResource(R.drawable.certi_on);
                else courseHolder.ivCerti.setBackgroundResource(R.drawable.certi_off);


                if(isMember)courseHolder.ivMember.setBackgroundResource(R.drawable.bg_circle_green);
                else courseHolder.ivMember.setBackgroundResource(R.drawable.bg_circle_white);

                if(p==1)courseHolder.ivMember.setBackgroundResource(R.drawable.bg_circle_green);

                String subjectJson=courseModel.getSubject();
                ArrayList<CategoryModel> categoryListing=new ArrayList<>();

                CourseAdapter adapter=new CourseAdapter(c,categoryListing,title);
                LinearLayoutManager lm = new LinearLayoutManager(c, LinearLayoutManager.HORIZONTAL, false);
                courseHolder.recyclerView.setLayoutManager(lm);
                courseHolder.recyclerView.setItemAnimator(new DefaultItemAnimator());
                courseHolder.recyclerView.setAdapter(adapter);

                try{
                    JSONArray ja2=new JSONArray(subjectJson);
                    for(int i=0;i<ja2.length();i++) {
                        JSONObject jo = ja2.getJSONObject(i);
                        String cate=jo.getString("category");
                        String code=jo.getString("category_id");
                        String pic=jo.getString("pic");
                        String eCode=jo.getString("eCode");
                        categoryListing.add(new CategoryModel(cate,code,pic,eCode));
                    }
                    adapter.notifyDataSetChanged();
                }catch (Exception e){}

            } catch (Exception e) {
              //  Toast.makeText(c, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public class Holder extends RecyclerView.ViewHolder {

        ImageView ivMember,ivCerti;
        TextView tv,tv_value;
        RecyclerView recyclerView;
        ProgressBar pb_enroll;

        public Holder(View view) {
            super(view);

            tv=view.findViewById(R.id.tv_course);
            recyclerView=view.findViewById(R.id.recyclerCourse);
            ivMember=view.findViewById(R.id.iv_isMember);
            pb_enroll=view.findViewById(R.id.pb_enroll);
            tv_value=view.findViewById(R.id.tv_value);
            ivCerti=view.findViewById(R.id.iv_certi);

        }
    }

    public class FunctionHolder extends RecyclerView.ViewHolder {

        RecyclerView recyclerView;
        public FunctionHolder(@NonNull View itemView) {
            super(itemView);
            recyclerView=itemView.findViewById(R.id.recycler_frag_one);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }
}
