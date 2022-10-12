package com.calamus.easykorean;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.calamus.easykorean.adapters.AnnouncementAdapter;
import com.calamus.easykorean.app.MyHttp;
import com.calamus.easykorean.app.Routing;
import com.calamus.easykorean.models.AnounceModel;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.concurrent.Executor;

public class AnnouncementActivity extends AppCompatActivity {

    public static RecyclerView recycler;
    SwipeRefreshLayout swipe;
    AnnouncementAdapter adapter;
    ArrayList<AnounceModel> announceList = new ArrayList<>();
    SharedPreferences share;

    LinearLayoutManager lm;
    Executor postExecutor;
    String userId;

    boolean isVip;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_four);

        share=getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        userId=share.getString("phone",null);
        isVip=share.getBoolean("isVIP",false);
        postExecutor = ContextCompat.getMainExecutor(this);
        setUpView();
        getSupportActionBar().hide();

        MobileAds.initialize(this, initializationStatus -> {});
        AdView adView = findViewById(R.id.adview);
        if(!isVip){
            adView.setVisibility(View.VISIBLE);
            AdRequest request=new AdRequest.Builder().build();
            adView.loadAd(request);
        }

        setUpMyActionBar();
    }

    @SuppressLint("SetTextI18n")
    private void  setUpMyActionBar(){
        TextView tv=findViewById(R.id.tv_appbar_title);
        ImageView iv_search=findViewById(R.id.iv_search);

        iv_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(AnnouncementActivity.this, SearchingActivity.class);
                startActivity(intent);
            }
        });

        tv.setText("Announcement");
    }


    private void setUpView(){
        recycler = findViewById(R.id.recycler_nf);
        swipe = findViewById(R.id.swipe_nf);

        lm = new LinearLayoutManager(this){};
        recycler.setLayoutManager(lm);
        // recycler.addItemDecoration(new SpacingItemDecoration(2, XUtils.toPx(Objects.requireNonNull(getActivity()), 2), true));
        recycler.setItemAnimator(new DefaultItemAnimator());

        adapter = new AnnouncementAdapter(this, announceList);
        recycler.setAdapter(adapter);
        swipe.setRefreshing(true);
        fetchAnnounceLink();

        swipe.setOnRefreshListener(() -> {
            announceList.clear();
            fetchAnnounceLink();
        });

    }



    private void fetchAnnounceLink(){
        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.GET, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    postExecutor.execute(() -> {
                        swipe.setRefreshing(false);
                        try{
                            JSONArray ja=new JSONArray(response);

                            for(int i=0;i<ja.length();i++) {
                                JSONObject jo = ja.getJSONObject(i);
                                String anounceLink=jo.getString("anounceLink");
                                String isSeen=jo.getString("seen");
                                AnounceModel model=new AnounceModel(anounceLink,isSeen);
                                announceList.add(model);

                            }
                            adapter.notifyDataSetChanged();

                        }catch (Exception ignored){

                        }
                    });
                }
                @Override
                public void onError(String msg) {
                    postExecutor.execute(() -> {
                        swipe.setRefreshing(false);
                    });
                }
            }).url(Routing.GET_ANNOUNCEMENT+"?user_id="+userId);
            myHttp.runTask();
        }).start();
    }

}