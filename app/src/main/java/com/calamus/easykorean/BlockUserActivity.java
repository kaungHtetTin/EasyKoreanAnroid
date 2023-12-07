package com.calamus.easykorean;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.calamus.easykorean.adapters.BlockUserAdapter;
import com.calamus.easykorean.app.MyHttp;
import com.calamus.easykorean.app.Routing;
import com.calamus.easykorean.models.LikeListModel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.Executor;

public class BlockUserActivity extends AppCompatActivity {

    SharedPreferences sharedPreferences;
    RecyclerView recyclerView;
    SwipeRefreshLayout swipe;
    String currentUserId;
    Executor postExecutor;
    BlockUserAdapter adapter;
    ArrayList<LikeListModel> blockUsers=new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_block_user);
        getSupportActionBar().hide();
        setUpCustomAppBar();
        postExecutor= ContextCompat.getMainExecutor(this);
        sharedPreferences=getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        currentUserId=sharedPreferences.getString("phone","");
        setUpView();;
    }

    private void setUpCustomAppBar(){
        TextView tv=findViewById(R.id.tv_appbar);
        ImageView iv=findViewById(R.id.iv_back);
        tv.setText("Blocked users");
        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }


    private void setUpView(){
        recyclerView=findViewById(R.id.recycler);
        swipe=findViewById(R.id.swipe);

        swipe.setRefreshing(true);

        LinearLayoutManager lm=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(lm);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        adapter=new BlockUserAdapter(this,blockUsers,currentUserId);
        recyclerView.setAdapter(adapter);

        fetchBlockUsers();
    }

    private void fetchBlockUsers(){
        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.GET, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    postExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            swipe.setRefreshing(false);
                            doAsResult(response);
                        }
                    });

                }
                @Override
                public void onError(String msg) {
                    swipe.setRefreshing(false);
                    Log.e("Post hide err ",msg);
                }
            }).url(Routing.BLOCK_USER+"?user_id="+currentUserId);
            myHttp.runTask();
        }).start();
    }

    private void doAsResult(String response){
        try{
            JSONArray ja=new JSONArray(response);
            for(int i=0;i<ja.length();i++){
                JSONObject jo=ja.getJSONObject(i);
                String userName=jo.getString("learner_name");
                String userId=jo.getString("learner_phone");
                String profileUrl=jo.getString("learner_image");
                LikeListModel model=new LikeListModel(userId,userName,profileUrl,"0");
                blockUsers.add(model);
            }

            adapter.notifyDataSetChanged();
        }catch (Exception e){

        }

    }
}