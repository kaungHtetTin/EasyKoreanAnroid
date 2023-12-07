package com.calamus.easykorean;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.calamus.easykorean.adapters.NotiAdapter;
import com.calamus.easykorean.app.MyHttp;
import com.calamus.easykorean.app.Routing;
import com.calamus.easykorean.models.NotiModel;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.Executor;


public class NotiListActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    SwipeRefreshLayout swipe;
    NotiAdapter adapter;
    ArrayList<NotiModel> postList = new ArrayList<>();
    String currentUserId;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    boolean isVip;
    Executor postExecutor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_noti_list);

        recyclerView=findViewById(R.id.recycler);
        swipe=findViewById(R.id.swipe);
        sharedPreferences=getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        currentUserId=sharedPreferences.getString("phone",null);
        isVip=sharedPreferences.getBoolean("isVIP",false);

        postExecutor= ContextCompat.getMainExecutor(this);

        editor=sharedPreferences.edit();
        editor.putBoolean("notiRedMark",false);
        editor.apply();

        adapter = new NotiAdapter(this, postList);
        final LinearLayoutManager gm = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(gm);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

        swipe.setRefreshing(true);
        fetchNotification(currentUserId,false);
        swipe.setOnRefreshListener(() -> {
            swipe.setRefreshing(true);
            fetchNotification(currentUserId,true);
        });


        setUpCustomAppBar();
        Objects.requireNonNull(getSupportActionBar()).hide();

    }

    private void setUpCustomAppBar(){

        TextView tv=findViewById(R.id.tv_appbar);
        ImageView iv=findViewById(R.id.iv_back);
        tv.setText("Notifications");
        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    private void fetchNotification(String currentUserId, boolean isRefresh){

        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.GET, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    postExecutor.execute(() -> {
                        if(isRefresh)postList.clear();
                        doAsResult(response);
                    });
                }
                @Override
                public void onError(String msg) {}
            }).url(Routing.FETCH_NOTIFICATION +currentUserId);
            myHttp.runTask();
        }).start();

    }

    private void doAsResult(String response){
        swipe.setRefreshing(false);
        try{
            JSONArray ja=new JSONArray(response);
            for(int i=0;i<ja.length();i++){
                JSONObject jo=ja.getJSONObject(i);
                String writerName=jo.getString("writer_name");
                String writerImage=jo.getString("writer_image");
                String postId=jo.getString("post_id");
                String postBody=jo.getString("body");
                String time=jo.getString("time");
                String seen=jo.getString("seen");
                String vip=jo.getString("vip");
                String color=jo.getString("action");
                String action=jo.getString("takingAction");
                String has_video=jo.getString("has_video");
                String postImage=jo.getString("image");
                String comment_id=jo.getString("comment_id");
                postList.add(new NotiModel(writerName,writerImage,postId,setMyanmar(postBody),seen,time,vip,action,has_video,postImage,color,comment_id));

            }
            adapter.notifyDataSetChanged();

        }catch (Exception ignored){

        }
    }

    public String setMyanmar(String s) {
        return s;
    }

}