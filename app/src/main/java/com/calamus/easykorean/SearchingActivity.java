package com.calamus.easykorean;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.calamus.easykorean.adapters.SearchAdapter;
import com.calamus.easykorean.app.MyHttp;
import com.calamus.easykorean.app.Routing;
import com.calamus.easykorean.models.FriendModel;
import com.calamus.easykorean.models.SaveModel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.Executor;

public class SearchingActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ImageButton ibt_back,ibt_search;
    EditText et_search;
    SearchAdapter adapter;
    ArrayList<Object> searchList = new ArrayList<>();
    LinearLayoutManager lm;
    Executor postExecutor;
    SwipeRefreshLayout swipe;
    TextView tv_noResult;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searching);
        getSupportActionBar().hide();
        postExecutor= ContextCompat.getMainExecutor(this);
        setUpView();

    }

    private void setUpView(){
        recyclerView=findViewById(R.id.recycler);
        ibt_back=findViewById(R.id.ibt_back);
        ibt_search=findViewById(R.id.ibt_search);
        et_search=findViewById(R.id.et_search);
        tv_noResult=findViewById(R.id.tv_noResult);
        swipe=findViewById(R.id.swipe);
        swipe.setRefreshing(false);
        lm = new LinearLayoutManager(this){};
        recyclerView.setLayoutManager(lm);
        // recycler.addItemDecoration(new SpacingItemDecoration(2, XUtils.toPx(Objects.requireNonNull(getActivity()), 2), true));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        adapter = new SearchAdapter(this,searchList);
        recyclerView.setAdapter(adapter);


        ibt_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ibt_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!TextUtils.isEmpty(et_search.getText().toString())){
                    swipe.setRefreshing(true);
                    searchList.clear();
                    adapter.notifyDataSetChanged();
                    tv_noResult.setVisibility(View.GONE);
                    search(et_search.getText().toString());
                }
            }
        });

        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                tv_noResult.setVisibility(View.GONE);
                et_search.setText("");
                searchList.clear();
                adapter.notifyDataSetChanged();
                swipe.setRefreshing(false);
            }
        });

    }

    private void search(String searching){
        Log.e("S ","Start Searching");
        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.GET, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    postExecutor.execute(new Runnable() {
                        @Override
                        public void run(){
                            swipe.setRefreshing(false);
                            try {
                                JSONObject joMain=new JSONObject(response);
                                JSONArray jaUsers=joMain.getJSONArray("user");
                                JSONArray jaPost=joMain.getJSONArray("post");
                                boolean userResult=handleUserResult(jaUsers);
                                boolean postResult=handlePostResult(jaPost);

                                if(!userResult&&!postResult)tv_noResult.setVisibility(View.VISIBLE);

                            }catch (Exception e){
                                Log.e("ErrRes: ",e.toString());
                            }

                        }
                    });
                }
                @Override
                public void onError(String msg) {
                    postExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            Log.e("ErrSearch : ",msg);
                            swipe.setRefreshing(false);
                        }
                    });
                }
            }).url(Routing.SEARCHING+"korea/"+searching);
            myHttp.runTask();
        }).start();

    }

    private boolean handleUserResult(JSONArray ja){
        try {
            for(int i=0;i<ja.length();i++){
                JSONObject jo=ja.getJSONObject(i);
                String userName=jo.getString("userName");
                String userId=jo.getString("userId");
                String userImage=jo.getString("userImage");
                searchList.add(new FriendModel(userId,userName,userImage,""));
            }
            adapter.notifyDataSetChanged();
            return true;
        }catch (Exception e){
            Log.e("SearchUserErr : ",e.toString());
            return false;
        }
    }

    private boolean handlePostResult(JSONArray ja){
        try {
            for(int i=0;i<ja.length();i++){
                JSONObject jo=ja.getJSONObject(i);
                String userName=jo.getString("userName");
                String userId=jo.getString("userId");
                String userImage=jo.getString("userImage");
                String body=jo.getString("body");
                String postImage=jo.getString("postImage");
                String has_video=jo.getString("has_video");
                String postId=jo.getString("postId");
                searchList.add(new SaveModel(postId,body,postImage,userName,userImage,has_video));
            }
            adapter.notifyDataSetChanged();
            return  true;
        }catch (Exception e){
            Log.e("SearchPostErr : ",e.toString());
            return false;
        }
    }
}