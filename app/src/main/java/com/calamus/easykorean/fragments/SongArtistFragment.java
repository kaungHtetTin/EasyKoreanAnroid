package com.calamus.easykorean.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.calamus.easykorean.R;
import com.calamus.easykorean.adapters.SongArtistAdapter;
import com.calamus.easykorean.app.MyHttp;
import com.calamus.easykorean.app.Routing;
import com.calamus.easykorean.models.SongArtistModel;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.concurrent.Executor;

public class SongArtistFragment extends Fragment {
    View v;
    RecyclerView recyclerView;
    private SwipeRefreshLayout swipe;
    private SongArtistAdapter adapter;
    private final ArrayList<SongArtistModel> songArtistLists = new ArrayList<>();

    int count=0;
    private boolean loading=true;
    int visibleItemCount,totalItemCount;
    public static int pastVisibleItems;
    Executor postExecutor;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        v= inflater.inflate(R.layout.fragment_song_artist, container, false);
        setUpView();
        return v;
    }

    private void setUpView(){
        recyclerView=v.findViewById(R.id.recyclerSongArtist);
        swipe=v.findViewById(R.id.swipeSongArtist);
        swipe.setRefreshing(true);

        GridLayoutManager gm = new GridLayoutManager(getActivity(), 2){};
        recyclerView.setLayoutManager(gm);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        adapter = new SongArtistAdapter(getActivity(),songArtistLists);
        recyclerView.setAdapter(adapter);
        postExecutor = ContextCompat.getMainExecutor(getActivity());
        fetchArtistList(0,false);
        swipe.setOnRefreshListener(() -> {
            count=0;
            loading=true;
            fetchArtistList(0,true);
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                pastVisibleItems=gm.findFirstVisibleItemPosition();
                if(dy>0){
                    visibleItemCount=gm.getChildCount();
                    totalItemCount=gm.getItemCount();

                    if(loading){

                        if((visibleItemCount+pastVisibleItems)>=totalItemCount-7){
                            loading=false;
                            count+=50;
                            fetchArtistList(count,false);
                        }
                    }


                }
            }
        });

    }

    private void fetchArtistList(int i, boolean isRefresh){
        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.GET, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    postExecutor.execute(() -> {
                        if (isRefresh){
                            songArtistLists.clear();

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
            }).url(Routing.GET_SONG_ARTIST +"/"+i);
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
                String artist=jo.getString("artist");
                String url=jo.getString("url");
                songArtistLists.add(new SongArtistModel(artist,url));
            }

            adapter.notifyDataSetChanged();
        }catch (Exception e){
            loading=false;
            swipe.setRefreshing(false);
            //Toast.makeText(getActivity(),e.toString(),Toast.LENGTH_SHORT).show();
        }
    }

}
