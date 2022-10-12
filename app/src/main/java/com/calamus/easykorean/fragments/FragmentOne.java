package com.calamus.easykorean.fragments;

import static com.calamus.easykorean.SplashScreenActivity.MESSAGE_ARRIVE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.calamus.easykorean.DownloadingListActivity;
import com.calamus.easykorean.service.DownloaderService;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.calamus.easykorean.ClassRoomActivity;
import com.calamus.easykorean.MyDiscussionActivity;
import com.calamus.easykorean.R;
import com.calamus.easykorean.adapters.MainAdapter;
import com.calamus.easykorean.app.AppHandler;
import com.calamus.easykorean.dialogs.MenuDialog;
import com.calamus.easykorean.models.CourseModel;
import com.calamus.easykorean.models.FunctionModel;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.Executor;

public class FragmentOne extends Fragment {
    private View v;

    private final ArrayList<Object> courseList = new ArrayList<>();
    SharedPreferences sharedPreferences;
    String enrollJson,imagePath,mainCourse;
    String username,phone;
    MainAdapter adapter;
    Executor postExecutor;
    ImageView iv_downloading;
    boolean isVip;
    Thread animateThread;  // for downloading icon
    boolean onLifeCyclePause=false;

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_one, container, false);

        sharedPreferences=getActivity().getSharedPreferences("GeneralData", Context.MODE_PRIVATE);

        enrollJson=sharedPreferences.getString("enrollProgress",null);
        imagePath=sharedPreferences.getString("imageUrl",null);
        mainCourse=sharedPreferences.getString("mainCourse",null);
        username=sharedPreferences.getString("Username",null);
        phone=sharedPreferences.getString("phone",null);
        isVip=sharedPreferences.getBoolean("isVIP",false);
        postExecutor= ContextCompat.getMainExecutor(getActivity());

        setAppBar();
        setupViews();

        courseList.add(0,"vip");

        if(mainCourse!=null)setCourse();



        return v;
    }

    private void setupViews() {
        RecyclerView recycler;
        recycler = v.findViewById(R.id.recycler_frag_one);

        GridLayoutManager gm = new GridLayoutManager(getActivity(), 3){
            @Override
            public boolean canScrollVertically() {
                return true;
            }
        };

        gm.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if(courseList.get(position) instanceof  FunctionModel){
                    return  1;
                }
                else{
                    return 3;
                }
            }
        });

        recycler.setLayoutManager(gm);
        recycler.setItemAnimator(new DefaultItemAnimator());

        adapter = new MainAdapter(getActivity(), courseList);
        recycler.setAdapter(adapter);

    }

    private void setAppBar(){
        TextView tv_name=v.findViewById(R.id.tv_course_title);
        ImageView iv_blueMark=v.findViewById(R.id.iv_blueMark);
        ImageView iv_profile=v.findViewById(R.id.iv_profile);
        ImageView iv_messenger=v.findViewById(R.id.iv_messenger);
        ImageView iv_menu=v.findViewById(R.id.iv_menu);
        ImageView iv_noti_red_mark=v.findViewById(R.id.noti_red_mark);
        if(MESSAGE_ARRIVE)iv_noti_red_mark.setVisibility(View.VISIBLE);
        else iv_noti_red_mark.setVisibility(View.GONE);

        iv_downloading=v.findViewById(R.id.iv_downloading);
        iv_downloading.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //animateThread=null;
                startActivity(new Intent(getActivity(),DownloadingListActivity.class));
            }
        });



        if(username!=null)tv_name.setText(getFistName(username));

        if (isVip)iv_blueMark.setVisibility(View.VISIBLE);
        AppHandler.setPhotoFromRealUrl(iv_profile,imagePath);

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


        RelativeLayout toolBarContent=v.findViewById(R.id.toolbarContent);
        CollapsingToolbarLayout toolbarLayout=v.findViewById(R.id.ctb);

        toolbarLayout.setTitle(getGreetingSpeech());
        toolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppBarFragmentOne);
        toolbarLayout.setCollapsedTitleTextAppearance(R.style.CollapsedAppBar);

        iv_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getActivity(), MyDiscussionActivity.class);
                intent.putExtra("userId",phone);
                intent.putExtra("userName",username);
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

    @Override
    public void onResume() {
        onLifeCyclePause=false;
        if(DownloaderService.downloaderLists.size()>0){
            iv_downloading.setVisibility(View.VISIBLE);
            animateDownloadIcon(iv_downloading);
        }else{
            iv_downloading.setVisibility(View.GONE);
        }

        super.onResume();
    }

    @Override
    public void onPause() {
        onLifeCyclePause=true;
        super.onPause();
    }

    private void animateDownloadIcon(ImageView iv){
        animateThread=new Thread(new Runnable() {
            @Override
            public void run() {
                int counter=0;
                while (DownloaderService.downloaderLists.size()>0&&!onLifeCyclePause) {
                    int finalCounter = counter;
                    postExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            if(finalCounter %2==0){
                                iv.setImageResource(R.drawable.ic_menu_downloading_red_stroke);
                            }else{
                                iv.setImageResource(R.drawable.ic_menu_downloading);
                            }
                        }
                    });
                    try {
                        Thread.sleep(700);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    counter++;
                }
            }
        });
        animateThread.start();

    }

    private String getFistName(String name){
        if(name.contains(" ")){
            name=name.substring(0,name.indexOf(" "));
        }
        return name+" ..";
    }


    private void setCourse(){
        JSONArray progressArr=null;
        try {
            JSONArray ja=new JSONArray(mainCourse);
            if(enrollJson!=null){
                Log.e("enrollJson: ",enrollJson);
                progressArr=new JSONArray(enrollJson);
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

                int progress=0;
                if(progressArr!=null){
                    for(int j=0;j<progressArr.length();j++){
                        JSONObject jo2=progressArr.getJSONObject(j);
                        String course_id=jo2.getString("course_id");
                        int learned=Integer.parseInt(jo2.getString("learned"));
                        int total=Integer.parseInt(jo2.getString("total"));
                        if(course_id.equals(id)){
                            progress=(learned*100)/total;
                        }
                    }
                }
                courseList.add(new CourseModel(id,title,description,cover_url,colorCode,progress,duration,isVip));
            }

            adapter.notifyDataSetChanged();

        }catch (Exception e){
            Log.e("CourseFetchErr ",e.toString());
        }
    }

    private String getGreetingSpeech(){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int hour=calendar.get(Calendar.HOUR_OF_DAY);

        if(hour<=11){
            return "Good Morning!";
        }else if(hour<=16){
            return "Good Afternoon!";
        }else{
            return "Good Evening!";
        }
    }
}
