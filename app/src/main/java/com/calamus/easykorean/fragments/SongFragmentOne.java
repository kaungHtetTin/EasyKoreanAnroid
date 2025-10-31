package com.calamus.easykorean.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.calamus.easykorean.R;
import com.calamus.easykorean.adapters.SongOnlineAdapter;
import com.calamus.easykorean.app.MyHttp;
import com.calamus.easykorean.app.Routing;
import com.calamus.easykorean.models.SongOnlineModel;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.concurrent.Executor;


public class SongFragmentOne extends Fragment {
    View v;
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
    RecyclerView recyclerView;
    EditText et_search;
    ImageButton ibt_search;
    boolean searchMode = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        v= inflater.inflate(R.layout.fragment_song_one, container, false);
        sharedPreferences=getActivity().getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        userId=sharedPreferences.getString("phone","0");
        setUpView();

        return v;
    }

    private void setUpView() {
        swipe=v.findViewById(R.id.swipeSong);
        recyclerView=v.findViewById(R.id.recyclerSongOne);
        et_search = v.findViewById(R.id.et_search);
        ibt_search = v.findViewById(R.id.ibt_search);

        swipe.setRefreshing(true);

        LinearLayoutManager lm = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(lm);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        adapter = new SongOnlineAdapter(getActivity(), songOnlineLists,songOnlineLists_Pop);
        recyclerView.setAdapter(adapter);
        postExecutor = ContextCompat.getMainExecutor(getActivity());
        songOnlineLists.clear();
        songOnlineLists.add(0,"Add Some thing");
        fetchPopularSong();
        fetchSong(1,false);

        swipe.setOnRefreshListener(() -> {
            page=1;
            loading=true;
            searchMode = false;
            fetchSong(1,true);
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                pastVisibleItems=lm.findFirstVisibleItemPosition();
                if(!searchMode){
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
            }
        });

        ibt_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String search = et_search.getText().toString();
                if(!TextUtils.isEmpty(search)){
                    searchASong(search);
                }else{
                    Toast.makeText(getActivity(),"Please enter the search box",Toast.LENGTH_SHORT).show();
                }
            }
        });

        et_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().equals("")){
                    page=1;
                    loading=true;
                    searchMode = false;
                    swipe.setRefreshing(true);
                    songOnlineLists.clear();
                    adapter.notifyDataSetChanged();

                    fetchSong(1,true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    private void fetchSong(int i, boolean isRefresh) {
        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.GET, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    postExecutor.execute(() -> {
                        if (isRefresh){
                            songOnlineLists.clear();
                            songOnlineLists.add(0,"Add Some thing");
                        }
                        doAsResult(response);
                    });
                }
                @Override
                public void onError(String msg) {
                    postExecutor.execute(() -> {
                       Log.e("SongFetch : ",msg);
                    });
                }
            }).url(Routing.GET_SONGS +"?userId="+userId+"&page="+i);
            myHttp.runTask();
        }).start();
    }

    private void fetchPopularSong() {
        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.GET, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    postExecutor.execute(() -> doAsResultPop(response));
                }
                @Override
                public void onError(String msg) {
                    postExecutor.execute(() -> {
                        // Toast.makeText(getActivity(),msg,Toast.LENGTH_SHORT).show();
                    });
                }
            }).url(Routing.GET_POPULAR_SONG+"?userId="+userId);
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

          //  Toast.makeText(getActivity(),e.toString(),Toast.LENGTH_SHORT).show();
        }
    }

    private void searchASong(String search){
        swipe.setRefreshing(true);
        searchMode = true;
        songOnlineLists.clear();
        adapter.notifyDataSetChanged();
        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.GET, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    postExecutor.execute(() -> {
                        songOnlineLists.clear();
                       // songOnlineLists.add(0,new SongOnlineModel());
                        doAsResult(response);
                    });
                }
                @Override
                public void onError(String msg) {
                    postExecutor.execute(() -> {
                      //  Toast.makeText(getActivity(),msg,Toast.LENGTH_SHORT).show();
                    });
                }
            }).url(Routing.SEARCH_A_SONG+"?search="+search+"&userId="+userId);
            myHttp.runTask();
        }).start();
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
                String drama=jo.getString("drama");
                String isLiked=jo.getString("is_liked");
                songOnlineLists.add(new SongOnlineModel(songId,title,artist,reactCount,commentCount,downloadCount,url,isLiked,drama));
            }

            adapter.notifyDataSetChanged();

        }catch (Exception e){
            loading=false;
            swipe.setRefreshing(false);
            //  Toast.makeText(getActivity(),e.toString(),Toast.LENGTH_SHORT).show();
        }
    }

}
