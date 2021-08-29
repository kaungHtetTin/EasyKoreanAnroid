package com.calamus.easykorean;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import com.calamus.easykorean.adapters.NotiAdapter;
import com.calamus.easykorean.app.MyHttp;
import com.calamus.easykorean.app.Routing;
import com.calamus.easykorean.models.NotiModel;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.Executor;
import me.myatminsoe.mdetect.MDetect;


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

        MDetect.INSTANCE.init(this);
        recyclerView=findViewById(R.id.recycler);
        swipe=findViewById(R.id.swipe_1);
        sharedPreferences=getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        currentUserId=sharedPreferences.getString("phone",null);
        isVip=sharedPreferences.getBoolean("isVIP",false);

        postExecutor= ContextCompat.getMainExecutor(this);

        editor=sharedPreferences.edit();
        editor.putBoolean("notiRedMark",false);
        editor.apply();

        setTitle("Notifications");
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

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);


        MobileAds.initialize(this, initializationStatus -> {});

        AdView adView = findViewById(R.id.adview);
        if(!isVip){
            adView.setVisibility(View.VISIBLE);
            AdRequest request=new AdRequest.Builder().build();
            adView.loadAd(request);

        }

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();

        }
        return super.onOptionsItemSelected(item);
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
            }).url(Routing.FETCH_NOTIFICATION +"?user_id="+currentUserId);
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

        }catch (Exception e){

        }
    }

    public String setMyanmar(String s) {

        return MDetect.INSTANCE.getText(s);
    }

}