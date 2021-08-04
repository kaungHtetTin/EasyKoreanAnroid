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
import com.calamus.easykorean.adapters.AnnouncementAdapter;
import com.calamus.easykorean.app.MyHttp;
import com.calamus.easykorean.app.Routing;
import com.calamus.easykorean.models.AnounceModel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_four);
        setTitle("Announcement");

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        share=getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        userId=share.getString("phone",null);

        postExecutor = ContextCompat.getMainExecutor(this);
        setUpView();

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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
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

                        }catch (Exception  e){

                        }
                    });
                }
                @Override
                public void onError(String msg) {
                    postExecutor.execute(() -> {

                    });
                }
            }).url(Routing.GET_ANNOUNCEMENT +"?major=korea&userId="+userId);
            myHttp.runTask();
        }).start();
    }

}