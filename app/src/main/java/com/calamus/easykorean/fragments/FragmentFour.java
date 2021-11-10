package com.calamus.easykorean.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.calamus.easykorean.R;
import com.calamus.easykorean.SearchingActivity;
import com.calamus.easykorean.adapters.NewFeedAdapter;
import com.calamus.easykorean.app.MyHttp;
import com.calamus.easykorean.app.Routing;
import com.calamus.easykorean.models.AdModel;
import com.calamus.easykorean.models.AnounceModel;
import com.calamus.easykorean.models.NewfeedModel;
import com.google.android.gms.ads.AdLoader;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import me.myatminsoe.mdetect.MDetect;


public class FragmentFour extends Fragment {
    View v;
    public static RecyclerView recycler;
    SwipeRefreshLayout swipe;
    NewFeedAdapter adapter;
    ArrayList<Object> postList = new ArrayList<>();
    SharedPreferences share;
    int count=0;
    private boolean loading=true;
    int visibleItemCount,totalItemCount;
    public static int pastVisibleItems;
    LinearLayoutManager lm;
    Executor postExecutor;
    ExecutorService myExecutor;
    String userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v=inflater.inflate(R.layout.fragment_four,container,false);
        setHasOptionsMenu(true);
        MDetect.INSTANCE.init(getActivity());
        share=getActivity().getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        userId=share.getString("phone",null);
        setupViews();
        postList.add(0,"kaung");
        postExecutor = ContextCompat.getMainExecutor(getActivity());
        myExecutor= Executors.newFixedThreadPool(3);
        fetchAnounceLink();
        testFetch(count,false);
        return v;

    }



    @Override
    public void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupViews() {
        recycler = v.findViewById(R.id.recycler_nf);
        swipe = v.findViewById(R.id.swipe_nf);

        lm = new LinearLayoutManager(getActivity()){};
        recycler.setLayoutManager(lm);
        // recycler.addItemDecoration(new SpacingItemDecoration(2, XUtils.toPx(Objects.requireNonNull(getActivity()), 2), true));
        recycler.setItemAnimator(new DefaultItemAnimator());

        adapter = new NewFeedAdapter(getActivity(), postList);
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
                            loading=false;
                            count+=10;
                            testFetch(count,false);

                        }
                    }


                }
            }
        });

        swipe.setOnRefreshListener(() -> {

            postList.add(0,"kaung");
            count=0;
            loading=true;
            testFetch(0,true);
        });

    }

    private void testFetch(int count,boolean isRefresh){

        myExecutor.execute(new Runnable() {
            @Override
            public void run() {
                MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.GET, new MyHttp.Response() {
                    @Override
                    public void onResponse(String response) {
                        postExecutor.execute(() -> {
                            if (isRefresh){
                                postList.clear();
                                postList.add(0,"kaung");
                            }
                            doAsResult(response);

                        });
                    }
                    @Override
                    public void onError(String msg) {
                        postExecutor.execute(() -> {
                            // Toast.makeText(getActivity(),msg,Toast.LENGTH_SHORT).show();
                        });
                    }
                }).url(Routing.FETCH_POST+"/"+userId+"/"+count);
                myHttp.runTask();
            }
        });
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
                String userToken=jo.getString("userToken");
                String userImage=jo.getString("userImage");
                String postId=jo.getString("postId");
                String postBody=jo.getString("body");
                String posLikes=jo.getString("postLikes");
                String postComment=jo.getString("comments");
                String postImage=jo.getString("postImage");
                String isVip=jo.getString("vip");
                String isVideo=jo.getString("has_video");
                String viewCount=jo.getString("viewCount");
                String isLike=jo.getString("is_liked");
                NewfeedModel model = new NewfeedModel(userName,userId,userToken,userImage,postId,postBody,posLikes,postComment,postImage,isVip,isVideo,viewCount,isLike);
                postList.add(model);

            }

            LoadApp();

        }catch (Exception e){
            loading=false;
            swipe.setRefreshing(false);
            //Toast.makeText(getActivity(),e.toString(),Toast.LENGTH_SHORT).show();
        }

    }

    public static boolean canExit(){
        return pastVisibleItems == 0 || pastVisibleItems>20;
    }

    public static void goToFirst(){
        recycler.smoothScrollToPosition(0);

    }


    private void fetchAnounceLink(){
        myExecutor.execute(()->{
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.GET, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    postExecutor.execute(() -> {
                        try{
                            JSONArray ja=new JSONArray(response);
                            int index=1;
                            for(int i=0;i<ja.length();i++) {
                                JSONObject jo = ja.getJSONObject(i);
                                String anounceLink=jo.getString("anounceLink");
                                String isSeen=jo.getString("seen");
                                AnounceModel model=new AnounceModel(anounceLink,isSeen);
                                if(isSeen.equals("0")){
                                    postList.add(index,model);
                                    index++;

                                }
                            }
                            adapter.notifyDataSetChanged();

                        }catch (Exception ignored){

                        }
                    });
                }
                @Override
                public void onError(String msg) {
                    postExecutor.execute(() -> {
                        //  Toast.makeText(getActivity(),msg,Toast.LENGTH_SHORT).show();
                    });
                }
            }).url(Routing.GET_ANNOUNCEMENT+"?user_id="+userId);
            myHttp.runTask();
        });
    }

    private void LoadApp(){
        myExecutor.execute(()->{
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.GET, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    postExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject jo=new JSONObject(response);
                                String id=jo.getString("id");
                                String name=jo.getString("name");
                                String description=jo.getString("description");
                                String url=jo.getString("url");
                                String cover=jo.getString("cover");
                                String icon=jo.getString("icon");
                                String type=jo.getString("type");
                                if(postList.size()>6&&postList.size()<15){
                                    postList.add(4,new AdModel(id,name,description,url,cover,icon,type));
                                }else{
                                    postList.add(new AdModel(id,name,description,url,cover,icon,type));
                                }

                                adapter.notifyDataSetChanged();

                            }catch (Exception e){
                                Log.e("Add json: ",e.toString());
                            }

                        }
                    });
                }
                @Override
                public void onError(String msg) {
                    Log.e("Add err: ", msg);
                }
            }).url(Routing.GET_APP_ADS+"/"+count);
            myHttp.runTask();
        });
    }

    public void onCreateOptionsMenu(Menu menu, @NotNull MenuInflater inflater){
        menu.add("SEARCH")
                .setIcon(R.drawable.ic_search)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);


        super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull @NotNull MenuItem item) {
        if (item.getTitle().toString().equals("SEARCH")){
            Intent intent=new Intent(getActivity(), SearchingActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        myExecutor.shutdown();
        super.onDestroy();
    }
}
