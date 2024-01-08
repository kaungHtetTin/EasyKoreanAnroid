package com.calamus.easykorean;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.calamus.easykorean.app.FileManager;
import com.calamus.easykorean.models.FileModel;
import com.calamus.easykorean.models.SavedVideoModel;
import com.google.android.material.snackbar.Snackbar;
import com.calamus.easykorean.adapters.LessonAdapter;
import com.calamus.easykorean.app.Routing;
import com.calamus.easykorean.models.LessonModel;
import com.calamus.easykorean.app.MyHttp;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.Executor;


public class LessonActivity extends AppCompatActivity
{
    RecyclerView recycler;
    LinearLayout shimmerLayout;
    public SwipeRefreshLayout swipe;
    TextView tv_title;
    ViewGroup main;

    public static String category;
    LessonAdapter adapter;
    ArrayList<Object> lessonList = new ArrayList<>();
    public static String course_id;
    public static int fragmentId;
    boolean isVip;
    SharedPreferences sharedPreferences;
    Executor postExecutor;

    int page=1;
    private boolean loading=true;
    int visibleItemCount,totalItemCount;
    public static int pastVisibleItems;
    String currentUserId;

    boolean isDayPlan;
    String day;
    String appBarTitle;
    String category_title;

    String rootDir;
    FileManager fileManager;
    ArrayList<FileModel> downloadedVideoFiles =new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson);
        sharedPreferences=getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        isVip=sharedPreferences.getBoolean("isVIP",false);
        currentUserId=sharedPreferences.getString("phone",null);
        postExecutor = ContextCompat.getMainExecutor(this);
        Objects.requireNonNull(getSupportActionBar()).hide();

        category = Objects.requireNonNull(getIntent().getExtras()).getString("category_id", "");
        appBarTitle=getIntent().getExtras().getString("course_title");
        category_title=getIntent().getExtras().getString("category_title");
        course_id=getIntent().getExtras().getString("level");
        fragmentId=getIntent().getExtras().getInt("fragment");
        isDayPlan=getIntent().getExtras().getBoolean("isDayPlan",false);
        day=getIntent().getExtras().getString("day",0+"");

        setUpCustomAppBar();
        setupViews();

        if(isDayPlan)fetchLessonByDayPlan();
        else fetchLesson(1,false);


        rootDir=getExternalFilesDir(Environment.DIRECTORY_MOVIES).getPath();
        fileManager=new FileManager(this);

        Log.e("root ",rootDir + "/" + appBarTitle);
        fileManager.loadFiles(new File(rootDir + "/" + appBarTitle), new FileManager.OnFileLoading() {
            @Override
            public void onLoaded(ArrayList<FileModel> files) {
                downloadedVideoFiles.addAll(files);
            }
        });

    }


    private void setupViews()
    {
        tv_title=findViewById(R.id.tv_info_header);
        recycler = findViewById(R.id.recycler);
        swipe=findViewById(R.id.swipe);
        main=findViewById(R.id.main);
        shimmerLayout=findViewById(R.id.shimmer_layout);
        swipe.setRefreshing(false);

        recycler.setVisibility(View.GONE);
        shimmerLayout.setVisibility(View.VISIBLE);

        tv_title.setText(category_title);

        LinearLayoutManager lm = new LinearLayoutManager(this);
        recycler.setLayoutManager(lm);
        recycler.setItemAnimator(new DefaultItemAnimator());
        adapter = new LessonAdapter(this, lessonList,appBarTitle, new LessonAdapter.CallBack() {
            @Override
            public void onDownloadClick() {
                setSnackBar("Start Downloading");
            }
        });
        recycler.setAdapter(adapter);


        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh()
            {

                recycler.setVisibility(View.GONE);
                shimmerLayout.setVisibility(View.VISIBLE);
                swipe.setRefreshing(false);

                if(isDayPlan){
                    fetchLessonByDayPlan();
                }else{
                    page=1;
                    loading=true;
                    fetchLesson(1,true);
                }


            }
        });


        recycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                pastVisibleItems=lm.findFirstVisibleItemPosition();

                if(dy>0){
                    visibleItemCount=lm.getChildCount();
                    totalItemCount=lm.getItemCount();

                    if(loading){

                        if((visibleItemCount+pastVisibleItems)>=totalItemCount-7){

                            loading=false;
                            page++;
                            if(!isDayPlan)fetchLesson(page,false);

                        }
                    }


                }
            }
        });

    }

    private void setUpCustomAppBar(){

        TextView tv=findViewById(R.id.tv_appbar);
        ImageView iv=findViewById(R.id.iv_back);
        tv.setText(appBarTitle);
        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    private void setSnackBar(String s){
        final Snackbar sb=Snackbar.make(main,s,Snackbar.LENGTH_INDEFINITE);
        sb.setAction("View", v -> startActivity(new Intent(LessonActivity.this,
                        DownloadingListActivity.class)))
                .setActionTextColor(Color.WHITE)
                .show();
    }


    private void fetchLesson(int count,boolean isRefresh){
        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.GET, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    postExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            if (isRefresh)lessonList.clear();
                            try {
                                JSONObject jo=new JSONObject(response);
                                String lessons=jo.getString("lessons");
                                doAsResult(lessons);
                            }catch (Exception e){}

                        }
                    });
                }
                @Override
                public void onError(String msg) {
                    postExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }).url(Routing.FETCH_LESSONS+"?category="+category+"&userId="+currentUserId+"&page="+count);
            myHttp.runTask();
        }).start();
    }


    private void fetchLessonByDayPlan(){
        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.GET, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    postExecutor.execute(() -> {
                        lessonList.clear();
                        doAsResult(response);
                    });
                }
                @Override
                public void onError(String msg) {
                    postExecutor.execute(() -> Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show());
                }
            }).url(Routing.GET_LESSONS_BY_DAY_PLAN+"course_id="+course_id+"&day="+day+"&userId="+currentUserId);
            myHttp.runTask();
        }).start();
    }

    private void doAsResult(String response){
        adapter.setLessonJSON(response);
        try {
            recycler.setVisibility(View.VISIBLE);
            shimmerLayout.setVisibility(View.GONE);

            JSONArray ja=new JSONArray(response);
            for(int i=0;i<ja.length();i++){
                JSONObject jo=ja.getJSONObject(i);
                String id=jo.getString("id");
                String link=jo.getString("link");
                String title=jo.getString("title");
                String title_mini=jo.getString("title_mini");
                String image_url=jo.getString("image_url");
                String thumbnail=jo.getString("thumbnail");
                boolean isVideo= jo.getString("isVideo").equals("1");
                boolean isVip= jo.getString("isVip").equals("1");
                boolean learned=jo.getString("learned").equals("1");
                long time=jo.getLong("date");
                int duration=jo.getInt("duration");

                LessonModel model=new LessonModel(id,link,title,title_mini,isVideo,isVip,time,learned,
                        image_url,thumbnail,duration,appBarTitle);

                if(isVideo){
                    String checkTitle=title.replace("/"," ");
                    checkTitle=checkTitle+".mp4";
                    if(downloadedVideoFiles.size()>0){
                        for(int j=0;j<downloadedVideoFiles.size();j++){
                            FileModel file=downloadedVideoFiles.get(j);
                            if(file.getFile().getName().equals(checkTitle)){
                                model.setDownloaded(true);
                                model.setVideoModel((SavedVideoModel) file);
                                Log.e("Downloaded ", checkTitle + " is downloaded");

                            }
                        }
                    }
                }

                Log.e("Downloaded model ",model.isDownloaded()+" ");
                lessonList.add(model);

            }

            adapter.notifyDataSetChanged();
            loading=true;

        }catch (Exception e){
            loading=false;
            Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_SHORT).show();
        }
    }
}

