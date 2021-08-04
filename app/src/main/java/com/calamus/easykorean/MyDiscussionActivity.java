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
import com.calamus.easykorean.adapters.DiscussAdapter;
import com.calamus.easykorean.app.MyHttp;
import com.calamus.easykorean.app.Routing;
import com.calamus.easykorean.models.DiscussionModel;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;
import me.myatminsoe.mdetect.MDetect;

public class MyDiscussionActivity extends AppCompatActivity {

    public static RecyclerView recycler;
    SwipeRefreshLayout swipe;
    DiscussAdapter adapter;
    ArrayList<Object> postList = new ArrayList<>();
    SharedPreferences share;

    int count=0;
    private boolean loading=true;
    int visibleItemCount,totalItemCount;
    public static int pastVisibleItems;

    LinearLayoutManager lm;
    Executor postExecutor;
    String currentUserId,userId,userName;
    public static final int number_of_ads=1;
    private static final String AD_UNIT_ID="ca-app-pub-2472405866346270/3806485083";
    AdLoader adLoader;
    final List<UnifiedNativeAd> nativeAds=new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_four);
        MDetect.INSTANCE.init(this);
        share=getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        currentUserId=share.getString("phone","");

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        postExecutor= ContextCompat.getMainExecutor(this);

        userId=getIntent().getExtras().getString("userId","");
        userName=getIntent().getExtras().getString("userName","");

        if(!userId.equals(currentUserId)){
            setTitle("Discussion");
        }else{
            setTitle("My Discussion");
        }

        setupViews();
        testFetch(count,false);
    }


    private void setupViews() {
        recycler = findViewById(R.id.recycler_nf);
        swipe = findViewById(R.id.swipe_nf);

        lm = new LinearLayoutManager(this){};
        recycler.setLayoutManager(lm);
        // recycler.addItemDecoration(new SpacingItemDecoration(2, XUtils.toPx(Objects.requireNonNull(getActivity()), 2), true));
        recycler.setItemAnimator(new DefaultItemAnimator());

        postList.add(0,"kaung");
        adapter = new DiscussAdapter(this, postList);
        recycler.setAdapter(adapter);
        swipe.setRefreshing(true);

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
                            nativeAds.clear();
                            loading=false;
                            count+=10;
                            testFetch(count,false);

                        }
                    }
                }
            }
        });

        swipe.setOnRefreshListener(() -> {

            nativeAds.clear();
            postList.add(0,"kaung");
            count=0;
            loading=true;
            testFetch(0,true);
        });

    }

    private void testFetch(int count,boolean isRefresh){

        // Toast.makeText(getApplicationContext(),web_link+fetchDiscussion,Toast.LENGTH_SHORT).show();
        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.GET, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    postExecutor.execute(() -> {
                        if (isRefresh){
                            postList.clear();
                            postList.add(0,"kaung");
                        }
                        doAsResult(response);
                        // Toast.makeText(getApplicationContext(),response,Toast.LENGTH_SHORT).show();
                    });
                }
                @Override
                public void onError(String msg) {
                    postExecutor.execute(() -> {
                        //Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show();
                    });
                }
            }).url(Routing.FETCH_DISCUSSION +"?count="+count+"&userId="+userId+"&major=korea");
            myHttp.runTask();
        }).start();
    }

    public void doAsResult(String response){

        swipe.setRefreshing(false);

        try {
            loading=true;
            JSONArray ja=new JSONArray(response);
            for(int i=0;i<ja.length();i++){
                JSONObject jo=ja.getJSONObject(i);
                String userName=jo.getString("userName");
                String userId=jo.getString("userId");
                String userImage=jo.getString("userImage");
                String postId=jo.getString("postId");
                String postBody=jo.getString("body");
                String postLikes=jo.getString("postLikes");
                String postComment=jo.getString("comments");
                String postImage=jo.getString("postImage");
                String isVideo=jo.getString("has_video");
                String viewCount=jo.getString("viewCount");
                DiscussionModel model=new DiscussionModel(userId,userName,userImage,postId,postBody,postLikes,postComment,viewCount,isVideo,postImage);
                postList.add(model);

            }

            adapter.notifyDataSetChanged();
            loadNativeAds();

        }catch (Exception e){
            loading=false;
            swipe.setRefreshing(false);
            Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_SHORT).show();
        }

    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadNativeAds() {
        AdLoader.Builder builder=new AdLoader.Builder(this,AD_UNIT_ID);
        adLoader=builder.forUnifiedNativeAd(unifiedNativeAd -> {
            nativeAds.add(unifiedNativeAd);

            if(!adLoader.isLoading()){
                insertAdsMenuItem();

            }
        }).withAdListener(new AdListener(){
            @Override
            public void onAdFailedToLoad(LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);

                if(!adLoader.isLoading()){
                    insertAdsMenuItem();

                }
            }
        }).build();

        adLoader.loadAds(new AdRequest.Builder().build(),number_of_ads);
    }

    private void insertAdsMenuItem() {
        if(nativeAds.size()<0){
            return;
        }
        postList.addAll(nativeAds);
        adapter.notifyDataSetChanged();

    }
}