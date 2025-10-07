package com.calamus.easykorean;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.calamus.easykorean.adapters.DayAdapter;
import com.calamus.easykorean.app.Routing;
import com.calamus.easykorean.models.DayModel;
import com.calamus.easykorean.models.ExtraCourseModel;
import com.calamus.easykorean.app.MyHttp;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class DayListActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ArrayList<Object> dayLists=new ArrayList<>();
    DayAdapter adapter;
    String course_title;
    Executor postExecutor;
    SwipeRefreshLayout swipe;
    public static String course_id;
    ExecutorService myExecutor;
    SharedPreferences sharedPreferences;
    String userId;
    boolean isVip;
    String themeColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day_list);
        sharedPreferences=getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        userId=sharedPreferences.getString("phone","");
        isVip=sharedPreferences.getBoolean("isVIP",false);
        postExecutor= ContextCompat.getMainExecutor(this);
        myExecutor= Executors.newFixedThreadPool(3);
        course_title= getIntent().getExtras().getString("course_title");
        course_id=getIntent().getExtras().getString("course_id");
        themeColor=getIntent().getExtras().getString("theme_color");
        setUpView();
        setUpCustomAppBar();
        Objects.requireNonNull(getSupportActionBar()).hide();
        dayLists.add(0,"Lessons By  Categories");
        fetchCategoryByCourse();

        startCourse();

        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //fetchDaysByCourse();
                swipe.setRefreshing(false);
            }
        });
    }

    private void setUpCustomAppBar(){
        TextView tv=findViewById(R.id.tv_appbar);
        ImageView iv=findViewById(R.id.iv_back);
        tv.setText(course_title);

        tv.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        tv.setMarqueeRepeatLimit(-1);
        tv.setSingleLine(true);
        tv.setSelected(true);

        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void setUpView(){

        recyclerView=findViewById(R.id.recyclerView);
        swipe=findViewById(R.id.swipe);

        adapter=new DayAdapter(this,dayLists,course_title,themeColor);
        GridLayoutManager gm = new GridLayoutManager(this, 4){};
        recyclerView.setLayoutManager(gm);
        recyclerView.setAdapter(adapter);

        gm.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                Object o=dayLists.get(position);
                if(o instanceof String ){
                    return 4;
                }else if(o instanceof DayModel)
                    return 2;
                else {
                    return 1;
                }
            }
        });

    }

    private void fetchDaysByCourse(){
        myExecutor.execute(()->{
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.GET, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    postExecutor.execute(() -> {
                        swipe.setRefreshing(false);
                        doAsResult(response);

                    });
                }
                @Override
                public void onError(String msg) {

                    postExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            swipe.setRefreshing(false);
                            dayLists.add("The daily plan will be available soon.");
                            adapter.notifyDataSetChanged();
                        }
                    });
                }
            }).url(Routing.GET_DAY_PLAN+"userid="+userId+"&course="+course_id);
            myHttp.runTask();
        });

    }

    private void fetchCategoryByCourse(){
        swipe.setRefreshing(true);
        myExecutor.execute(()->{
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.GET, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    postExecutor.execute(() -> {

                        try{
                            JSONArray ja=new JSONArray(response);
                            for(int a=0;a<ja.length();a++){
                                JSONObject jo2=ja.getJSONObject(a);
                                String title=jo2.getString("category_title");
                                String image_url=jo2.getString("image_url");
                                String id=jo2.getString("id");
                                dayLists.add(new ExtraCourseModel(title,id,image_url));

                            }
                            adapter.notifyDataSetChanged();

                        }catch (Exception e){

                        }

                        fetchDaysByCourse();

                    });
                }
                @Override
                public void onError(String msg) {
                    postExecutor.execute(() -> Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show());
                }
            }).url(Routing.GET_CATEGORY_BY_COURSE+"?courseId="+course_id);
            myHttp.runTask();
        });

    }

    private void startCourse(){
        myExecutor.execute(()->{
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.POST, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    Log.e("startCourse ",response);
                }
                @Override
                public void onError(String msg) {
                    Log.e("startCourse ",msg);
                }
            })
                    .field("user_id",userId)
                    .field("course_id",course_id)
                    .url(Routing.START_A_COURSE);
            myHttp.runTask();
        });
    }

    private void doAsResult(String response){
        swipe.setRefreshing(false);
        try {

            JSONArray ja=new JSONArray(response);
            dayLists.add("Lessons By Daily Plan");
            for(int i=0;i<ja.length();i++){
                JSONArray jsonArray=ja.getJSONArray(i);
                int day=i+1;
                dayLists.add(new DayModel(day+"",course_id,jsonArray));
            }
            adapter.notifyDataSetChanged();

        }catch (Exception e){

            swipe.setRefreshing(false);
            // Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_SHORT).show();
        }
    }
}