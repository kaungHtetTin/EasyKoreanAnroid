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
import android.widget.Toast;
import com.calamus.easykorean.adapters.LikeListAdapter;
import com.calamus.easykorean.app.MyHttp;
import com.calamus.easykorean.models.LikeListModel;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.Executor;

public class LikeListActivity extends AppCompatActivity {

    ArrayList<LikeListModel> likeList=new ArrayList<>();
    LikeListAdapter adapter;
    String contentId;
    Executor postExecutor;

    int count=0;
    private boolean loading=true;
    int visibleItemCount,totalItemCount;
    public static int pastVisibleItems;

    String fetch;
    SharedPreferences sharedPreferences;
    RecyclerView recyclerView;
    SwipeRefreshLayout swipe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_like_list);
        postExecutor= ContextCompat.getMainExecutor(this);

        sharedPreferences=getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        setTitle("Reacts");
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        contentId=getIntent().getExtras().getString("contentId","");
        fetch=getIntent().getExtras().getString("fetch","");

        setUpView();

        fetchLike(count,false);

    }

    private void setUpView(){
        recyclerView=findViewById(R.id.recycler);
        swipe=findViewById(R.id.swipe);

        LinearLayoutManager lm=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(lm);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        adapter=new LikeListAdapter(this,likeList);
        recyclerView.setAdapter(adapter);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
                            count+=50;
                            fetchLike(count,false);
                        }
                    }


                }
            }
        });

        swipe.setOnRefreshListener(() -> {

            count=0;
            loading=true;
            fetchLike(0,true);
        });

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();

        }
        return super.onOptionsItemSelected(item);
    }



    private void fetchLike(int count, boolean isRefresh){
        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.GET, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    postExecutor.execute(() -> {
                        if(isRefresh)likeList.clear();
                        doAsResult(response);
                    });
                }
                @Override
                public void onError(String msg) {
                    postExecutor.execute(() -> Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show());
                }
            }).url(fetch+"?count="+count+"&post_id="+contentId+"&major=ko_user_datas");
            myHttp.runTask();
        }).start();
    }

    private void doAsResult(String response){

        swipe.setRefreshing(false);
        try {
            loading=true;
            JSONArray ja=new JSONArray(response);
            for(int i=0;i<ja.length();i++){
                JSONObject jo=ja.getJSONObject(i);
                String userId=jo.getString("userId");
                String userName=jo.getString("userName");
                String userImage=jo.getString("userImage");
                String isVip=jo.getString("vip");
                likeList.add(new LikeListModel(userId,userName,userImage,isVip));
            }

            adapter.notifyDataSetChanged();

        }catch (Exception e){
            loading=false;
            swipe.setRefreshing(false);
        }
    }
}