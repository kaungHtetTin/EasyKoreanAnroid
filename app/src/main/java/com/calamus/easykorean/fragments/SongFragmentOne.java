package com.calamus.easykorean.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.calamus.easykorean.R;
import com.calamus.easykorean.SongListActivity;
import com.calamus.easykorean.adapters.SongOnlineAdapter;
import com.calamus.easykorean.app.MyHttp;
import com.calamus.easykorean.app.Routing;
import com.calamus.easykorean.models.AdModel;
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

    int count=0;
    private boolean loading=true;
    int visibleItemCount,totalItemCount;
    public static int pastVisibleItems;
    Executor postExecutor;

    String userId;
    MenuItem item;
    RecyclerView recyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        v= inflater.inflate(R.layout.fragment_song_one, container, false);
        sharedPreferences=getActivity().getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        userId=sharedPreferences.getString("phone","0");
        setUpView();
        setHasOptionsMenu(true);

        return v;
    }

    private void setUpView() {
        swipe=v.findViewById(R.id.swipeSong);
        recyclerView=v.findViewById(R.id.recyclerSongOne);

        swipe.setRefreshing(true);

        LinearLayoutManager lm = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(lm);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        adapter = new SongOnlineAdapter(getActivity(), songOnlineLists,songOnlineLists_Pop);
        recyclerView.setAdapter(adapter);
        postExecutor = ContextCompat.getMainExecutor(getActivity());
        songOnlineLists.clear();
        songOnlineLists.add(0,new SongOnlineModel());
        fetchPopularSong();
        fetchSong(0,false);

        swipe.setOnRefreshListener(() -> {
            count=0;
            loading=true;
            fetchSong(0,true);
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
                            count+=10;
                            fetchSong(count,false);
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
                    postExecutor.execute(() -> {
                        if (isRefresh){
                            songOnlineLists.clear();
                            songOnlineLists.add(0,new SongOnlineModel());
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
            }).url(Routing.GET_SONGS +"?count="+i+"&major=korea"+"&userId="+userId);
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
            }).url(Routing.GET_POPULAR_SONG+"?major=korea&userId="+userId);
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

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        super.onCreateOptionsMenu(menu,inflater);
        menu.clear();
        inflater.inflate(R.menu.too_memu_song,menu);

        item =menu.findItem(R.id.action_search_song);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW|MenuItem.SHOW_AS_ACTION_IF_ROOM);

        SearchView searchView=new SearchView(((SongListActivity) requireActivity()).getSupportActionBar().getThemedContext());
        item.setActionView(searchView);
        searchView.setQueryHint("Search");

        searchView.setIconifiedByDefault(false);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                item.collapseActionView();
                if (query != null)
                {
                    //adapter.getFilter().filter(query);
                    loading=false;
                    swipe.setRefreshing(true);
                    searchASong(query);

                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //  adapter.getFilter().filter(newText);
                loading=false;
                swipe.setRefreshing(true);
                searchASong(newText);
                return true;
            }
        });
    }

    private void searchASong(String search){

        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.GET, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    postExecutor.execute(() -> {
                        songOnlineLists.clear();
                        songOnlineLists.add(0,new SongOnlineModel());
                        doAsResult(response);
                    });
                }
                @Override
                public void onError(String msg) {
                    postExecutor.execute(() -> {
                      //  Toast.makeText(getActivity(),msg,Toast.LENGTH_SHORT).show();
                    });
                }
            }).url(Routing.SEARCH_A_SONG+"?major=korea"+"&search="+search+"&userId="+userId);
            myHttp.runTask();
        }).start();
    }


    private void doAsResult(String response) {
        swipe.setRefreshing(false);
        try {
            loading=true;
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
                String drama=jo.getString("drama");
                String isLiked=jo.getString("is_liked");
                songOnlineLists.add(new SongOnlineModel(songId,title,artist,reactCount,commentCount,downloadCount,url,isLiked,drama));
            }

            LoadApp();

        }catch (Exception e){
            loading=false;
            swipe.setRefreshing(false);
            //  Toast.makeText(getActivity(),e.toString(),Toast.LENGTH_SHORT).show();
        }
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
            }).url(Routing.GET_APP_ADS+"/"+count);
            myHttp.runTask();
        }).start();
    }

}
