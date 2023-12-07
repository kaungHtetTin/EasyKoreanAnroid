package com.calamus.easykorean.fragments;

import static com.calamus.easykorean.SplashScreenActivity.MESSAGE_ARRIVE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.calamus.easykorean.app.Routing;
import com.calamus.easykorean.models.EnrollModel;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.calamus.easykorean.ClassRoomActivity;
import com.calamus.easykorean.R;
import com.calamus.easykorean.adapters.MyLearningAdapter;
import com.calamus.easykorean.dialogs.MenuDialog;
import com.calamus.easykorean.models.CourseModel;
import com.calamus.easykorean.models.FunctionModel;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;


public class FragmentFive extends Fragment {

    private View v;
    SharedPreferences sharedPreferences;
    RecyclerView recyclerView;
    ArrayList<Object> data =new ArrayList<>();
    ArrayList<CourseModel> purchasedCourses=new ArrayList<>();
    ArrayList<EnrollModel>  enrollCourse=new ArrayList<>();
    MyLearningAdapter adapter;
    String functionJson,enrollJson,mainCourse,vipCourses;

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_five, container, false);

        sharedPreferences = getActivity().getSharedPreferences("GeneralData", Context.MODE_PRIVATE);

        functionJson=sharedPreferences.getString("functions",null);
        enrollJson=sharedPreferences.getString("enrollProgress",null);
        mainCourse=sharedPreferences.getString("mainCourse",null);
        vipCourses=sharedPreferences.getString("vipCourses",null);

        setEnrollCourse();
        setAppBar();
        setUpView();



        return v;
    }


    private void setUpView(){
        recyclerView=v.findViewById(R.id.recyclerView);
        GridLayoutManager gm = new GridLayoutManager(getActivity(), 3){
            @Override
            public boolean canScrollVertically() {
                return true;
            }
        };

        gm.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if(data.get(position) instanceof FunctionModel){
                    return  1;
                }
                else{
                    return 3;
                }
            }
        });

        recyclerView.setLayoutManager(gm);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        adapter=new MyLearningAdapter(getActivity(),data);
        recyclerView.setAdapter(adapter);

        setFunction();
        getPurchaseCourse();

    }


    private void setAppBar(){
        RelativeLayout toolBarContent=v.findViewById(R.id.toolbarContent);
        CollapsingToolbarLayout toolbarLayout=v.findViewById(R.id.ctb);
        ImageView iv_messenger=v.findViewById(R.id.iv_messenger);
        ImageView iv_menu=v.findViewById(R.id.iv_menu);

        ImageView iv_noti_red_mark=v.findViewById(R.id.noti_red_mark);
        if(MESSAGE_ARRIVE)iv_noti_red_mark.setVisibility(View.VISIBLE);
        else iv_noti_red_mark.setVisibility(View.GONE);

        toolbarLayout.setTitle(Routing.APP_NAME);
        toolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppBar);
        toolbarLayout.setCollapsedTitleTextAppearance(R.style.CollapsedAppBar);

        iv_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MenuDialog(requireActivity()).initDialog();
            }
        });

        iv_messenger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getActivity(), ClassRoomActivity.class);
                intent.putExtra("action","");
                iv_noti_red_mark.setVisibility(View.GONE);
                startActivity(intent);
            }
        });

        AppBarLayout mAppBarLayout =v.findViewById(R.id.app_bar_layout);
        mAppBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {

                Log.e("scrollRange ",verticalOffset+"");

                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                    toolBarContent.setVisibility(View.VISIBLE);
                }
                if (verticalOffset<-64) {
                    isShow = true;
                    toolBarContent.setVisibility(View.GONE);

                } else if (isShow) {
                    isShow = false;
                    toolBarContent.setVisibility(View.VISIBLE);

                }
            }
        });

    }



    private void setFunction(){
        try{
            JSONArray ja2=new JSONArray(functionJson);
            for(int i=0;i<ja2.length();i++) {
                JSONObject jo = ja2.getJSONObject(i);
                String name=jo.getString("title");
                String link=jo.getString("link_url");
                String pic=jo.getString("image_url");
                data.add(new FunctionModel(name,link,pic));
            }
            adapter.notifyDataSetChanged();
        }catch (Exception ignored){}
    }

    private void getPurchaseCourse(){
        JSONArray vipCourseArr=null;
        try {
            JSONArray ja=new JSONArray(mainCourse);
            if(vipCourses!=null){
                Log.e("vipCourses: ",vipCourses);
                vipCourseArr=new JSONArray(vipCourses);
            }
            for(int i=0;i<ja.length();i++){
                JSONObject jo=ja.getJSONObject(i);
                String id=jo.getString("course_id");
                String title=jo.getString("title");
                String cover_url=jo.getString("cover_url");
                String description=jo.getString("description");
                String colorCode=jo.getString("background_color");
                boolean isVip=jo.getString("is_vip").equals("1");
                int duration=jo.getInt("duration");

                if(vipCourseArr!=null){
                    for(int j=0;j<vipCourseArr.length();j++){
                        JSONObject jo2=vipCourseArr.getJSONObject(j);
                        String course_id=jo2.getString("course_id");
                        if(course_id.equals(id)){
                            purchasedCourses.add(new CourseModel(id,title,description,cover_url,colorCode,getVIPCourseProgress(course_id),duration,isVip));
                            break;
                        }
                    }
                }
            }

            setCourse();

        }catch (Exception e){
            Log.e("CourseFetchErr ",e.toString());
        }
    }

    private void setCourse() {
        if(purchasedCourses.size()==0){
            data.add("No Purchased Course");
        }else if(purchasedCourses.size()==1){
            data.add("Purchased Course");
        }else{
            data.add("Purchased Courses");
        }
        data.addAll(purchasedCourses);

        adapter.notifyDataSetChanged();
    }



    private void setEnrollCourse(){
        if(enrollJson!=null){
            try {
                JSONArray progressArr=new JSONArray(enrollJson);
                for(int i=0;i<progressArr.length();i++){
                    JSONObject jo2=progressArr.getJSONObject(i);
                    String course_id=jo2.getString("course_id");
                    int learned=Integer.parseInt(jo2.getString("learned"));
                    int total=Integer.parseInt(jo2.getString("total"));
                    enrollCourse.add(new EnrollModel(course_id,learned,total));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    private int getVIPCourseProgress(String course_id){
        int result=0;
        for(int i=0;i<enrollCourse.size();i++){
            EnrollModel model=enrollCourse.get(i);
            if(model.getCourse_id().equals(course_id)){
                result=(model.getLearned()*100)/model.getTotal();
                break;
            }
        }

        return result;
    }

}
