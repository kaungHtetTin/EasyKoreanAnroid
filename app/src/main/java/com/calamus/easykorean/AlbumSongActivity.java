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
import android.os.Environment;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.calamus.easykorean.adapters.SongOnlineAdapter;
import com.calamus.easykorean.app.Routing;
import com.calamus.easykorean.models.AdModel;
import com.calamus.easykorean.models.SongOnlineModel;
import com.calamus.easykorean.app.MyHttp;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.Executor;

public class AlbumSongActivity extends AppCompatActivity {

    ImageView iv_back;
    TextView tv_appbar;
    RecyclerView recyclerView;
    private SwipeRefreshLayout swipe;
    private SongOnlineAdapter adapter;
    private final ArrayList<Object> songOnlineLists = new ArrayList<>();
    private final ArrayList<SongOnlineModel> songOnlineLists_Pop = new ArrayList<>();
    SharedPreferences sharedPreferences;

    int page=1;
    private boolean loading=true;
    int visibleItemCount,totalItemCount;
    public static int pastVisibleItems;
    Executor postExecutor;
    String userId;
    MenuItem item;
    public static File[] localSongs;
    String artist;
    boolean isVip;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_song);
        sharedPreferences=getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        userId=sharedPreferences.getString("phone","0");
        artist=getIntent().getStringExtra("artist");
        isVip=sharedPreferences.getBoolean("isVIP",false);
        getSupportActionBar().hide();

        setUpView();


    }

    private void setUpView() {
        swipe=findViewById(R.id.swipeSong);
        recyclerView=findViewById(R.id.recyclerSongOne);
        iv_back=findViewById(R.id.iv_back);
        tv_appbar=findViewById(R.id.tv_appbar);

        tv_appbar.setText(artist);
        swipe.setRefreshing(true);

        LinearLayoutManager lm = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(lm);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        adapter = new SongOnlineAdapter(this, songOnlineLists,songOnlineLists_Pop);
        recyclerView.setAdapter(adapter);
        postExecutor = ContextCompat.getMainExecutor(this);
        songOnlineLists.clear();
        songOnlineLists.add(0,new SongOnlineModel());
        fetchPopularSong();
        fetchSong(1,false);

        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh()
            {
                page=1;
                loading=true;
                localSongs=getLocalSongs();
                fetchSong(1,true);
            }
        });

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
                            page++;
                            fetchSong(page,false);
                        }
                    }


                }
            }
        });

    }

    private void fetchSong(int i, boolean isRefresh) {
        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.GET, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    postExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            if (isRefresh){
                                songOnlineLists.clear();
                                songOnlineLists.add(0,new SongOnlineModel());
                            }
                            doAsResult(response);
                        }
                    });
                }
                @Override
                public void onError(String msg) {
                    postExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            //  Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }).url(Routing.GET_SONG_BY_ARTIST+"?artist="+artist+"&userId="+userId+"&page="+i);
            myHttp.runTask();
        }).start();
    }

    private void fetchPopularSong() {
        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.GET, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    postExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            doAsResultPop(response);
                        }
                    });
                }
                @Override
                public void onError(String msg) {
                    postExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            // Toast.makeText(getActivity(),msg,Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }).url(Routing.GET_POPULAR_SONG_BY_ARTIST+"?artist="+artist+"&userId="+userId);
            myHttp.runTask();
        }).start();
    }

    private void doAsResultPop(String response) {

        try {

            JSONArray ja=new JSONArray(response);
            for(int i=0;i<ja.length();i++){
                JSONObject jo=ja.getJSONObject(i);
                String songId=jo.getString("song_id");
                String title=jo.getString("title");
                String artist=jo.getString("artist");
                String reactCount=jo.getString("like_count");
                String commentCount=jo.getString("comment_count");
                String downloadCount=jo.getString("download_count");
                String url=jo.getString("url");
                String isLiked=jo.getString("is_liked");
                String drama=jo.getString("drama");
                songOnlineLists_Pop.add(new SongOnlineModel(songId,title,artist,reactCount,commentCount,downloadCount,url,isLiked,drama));
            }

            adapter.notifyDataSetChanged();
        }catch (Exception e){

            // Toast.makeText(this,e.toString(),Toast.LENGTH_SHORT).show();
        }
    }


    private void doAsResult(String response) {
        swipe.setRefreshing(false);
        try {
            loading=true;
            JSONObject joSongs=new JSONObject(response);
            JSONArray ja=joSongs.getJSONArray("songs");
            for(int i=0;i<ja.length();i++){
                JSONObject jo=ja.getJSONObject(i);
                String songId=jo.getString("song_id");
                String title=jo.getString("title");
                String artist=jo.getString("artist");
                String reactCount=jo.getString("like_count");
                String commentCount=jo.getString("comment_count");
                String downloadCount=jo.getString("download_count");
                String url=jo.getString("url");
                String isLiked=jo.getString("is_liked");
                String drama=jo.getString("drama");
                songOnlineLists.add(new SongOnlineModel(songId,title,artist,reactCount,commentCount,downloadCount,url,isLiked,drama));
            }

            LoadApp();

        }catch (Exception e){
            loading=false;
            swipe.setRefreshing(false);
            // Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_SHORT).show();
        }
    }


    public File[] getLocalSongs(){
        File directory = getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        File[] songFiles =directory.listFiles();
        return songFiles;
    }

    private void LoadApp(){

        new Thread(() -> {
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
                                if(songOnlineLists.size()>6&&songOnlineLists.size()<12){
                                    songOnlineLists.add(4,new AdModel(id,name,description,url,cover,icon,type));
                                }else{
                                    songOnlineLists.add(new AdModel(id,name,description,url,cover,icon,type));
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
            }).url(Routing.GET_APP_ADS+"/"+page);
            myHttp.runTask();
        }).start();
    }

}