package com.calamus.easykorean.fragments;

import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.calamus.easykorean.R;
import com.calamus.easykorean.adapters.SongLocalAdapter;
import com.calamus.easykorean.models.SongModel;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.Executor;

import static android.media.MediaMetadataRetriever.METADATA_KEY_DURATION;

public class SongFragmentTwo extends Fragment {

    public SongFragmentTwo() {
        // Required empty public constructor
    }

    Executor postExecutor;
    View view;
    RecyclerView recyclerView;
    SongLocalAdapter adapter;
    SwipeRefreshLayout swipe;
    public static ArrayList<SongModel> songLocalLists=new ArrayList<>();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_song_two, container, false);
        setUpView();
        return view;
    }

    private void setUpView(){
        postExecutor= ContextCompat.getMainExecutor(requireActivity());
        recyclerView=view.findViewById(R.id.recyclerSongTwo);
        swipe=view.findViewById(R.id.swipeSongLocal);
        swipe.setRefreshing(true);
        adapter=new SongLocalAdapter(getActivity(),songLocalLists);
        LinearLayoutManager lm=new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(lm);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

        songLocalLists.clear();
        adapter.notifyDataSetChanged();
        new SongLoader().start();

        swipe.setOnRefreshListener(() -> {
            songLocalLists.clear();
            adapter.notifyDataSetChanged();
            new SongLoader().start();
            swipe.setRefreshing(false);
        });
        swipe.setRefreshing(false);

    }

    class SongLoader extends Thread{
        @Override
        public void run() {
            super.run();
            File directory = requireActivity().getExternalFilesDir(Environment.DIRECTORY_MUSIC);
            File[] songFiles =directory.listFiles();
            try {
                if(songFiles!=null){
                    for(int i=songFiles.length-1;i>=0;i--){
                        Uri uri=Uri.fromFile(songFiles[i]);
                        MediaMetadataRetriever retriever=new MediaMetadataRetriever();
                        retriever.setDataSource(getActivity(),uri);
                        String duration=retriever.extractMetadata(METADATA_KEY_DURATION);
                        int finalI = i;
                        postExecutor.execute(() -> {
                            songLocalLists.add(new SongModel(uri,songFiles[finalI].getName(),Integer.parseInt(duration),null));
                            adapter.notifyDataSetChanged();
                        });
                    }
                }else{}

            }catch(Exception e){}

        }
    }
}
