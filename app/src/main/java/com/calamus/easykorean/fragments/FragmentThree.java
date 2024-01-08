package com.calamus.easykorean.fragments;


import static com.calamus.easykorean.SplashScreenActivity.MESSAGE_ARRIVE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.calamus.easykorean.app.FileManager;
import com.calamus.easykorean.models.FileModel;
import com.calamus.easykorean.models.SavedVideoModel;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.snackbar.Snackbar;
import com.calamus.easykorean.ClassRoomActivity;
import com.calamus.easykorean.DownloadingListActivity;
import com.calamus.easykorean.R;
import com.calamus.easykorean.adapters.VideoAdapter;
import com.calamus.easykorean.adapters.VideoCategoryAdapter;
import com.calamus.easykorean.dialogs.MenuDialog;
import com.calamus.easykorean.app.MyHttp;
import com.calamus.easykorean.app.Routing;
import com.calamus.easykorean.models.VideoCategoryModel;
import com.calamus.easykorean.models.VideoModel;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.Executor;



public class FragmentThree extends Fragment {
    View v;

    ViewGroup main;
    private VideoAdapter adapter;
    private final ArrayList<VideoModel> lessonList = new ArrayList<>();
    private final ArrayList<VideoCategoryModel>CategoryList=new ArrayList<>();
    private VideoCategoryAdapter categoryAdapter;

    SharedPreferences sharedPreferences;
    String videoCategoryForm,firstCategory="",currentCategory;
    String currentCategoryID,currentUserId;

    int page=1;
    private boolean loading=true;
    int visibleItemCount,totalItemCount;
    public int pastVisibleItems;
    Executor postExecutor;

    String rootDir;
    FileManager fileManager;
    ArrayList<FileModel> downloadedVideoFiles =new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v=inflater.inflate(R.layout.fragment_three,container,false);

        sharedPreferences=getActivity().getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        videoCategoryForm=sharedPreferences.getString("videoChannels","");
        currentUserId=sharedPreferences.getString("phone","");
        postExecutor = ContextCompat.getMainExecutor(getActivity());

        rootDir=getActivity().getExternalFilesDir(Environment.DIRECTORY_MOVIES).getPath();
        fileManager=new FileManager(getActivity());




        setUpView();
        setUpAppBar();

        return v;


    }

    private void setUpView (){
        main=v.findViewById(R.id.main);
        RecyclerView recycler = v.findViewById(R.id.recycler);
        LinearLayoutManager lm = new LinearLayoutManager(getActivity());
        recycler.setLayoutManager(lm);
        recycler.setItemAnimator(new DefaultItemAnimator());
        adapter = new VideoAdapter(getActivity(), lessonList, new VideoAdapter.CallBack() {
            @Override
            public void onDownloadClick() {
                setSnackBar("Start Downloading");
            }
        });
        recycler.setAdapter(adapter);

        addLoadingItem();
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
                            page++;
                            fetchLesson(page,currentCategoryID,false);
                        }
                    }

                }
            }
        });

    }

    private void setUpAppBar(){
        CollapsingToolbarLayout toolbarLayout=v.findViewById(R.id.ctb);
        RelativeLayout toolBarContent=v.findViewById(R.id.toolbarContent);
        RecyclerView recyclerViewCategory=v.findViewById(R.id.layout_star_bar);
        ImageView iv_messenger=v.findViewById(R.id.iv_messenger);
        ImageView iv_menu=v.findViewById(R.id.iv_menu);

        ImageView iv_noti_red_mark=v.findViewById(R.id.noti_red_mark);
        if(MESSAGE_ARRIVE)iv_noti_red_mark.setVisibility(View.VISIBLE);
        else iv_noti_red_mark.setVisibility(View.GONE);

        toolbarLayout.setTitle(Routing.APP_NAME);
        toolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppBar);
        toolbarLayout.setCollapsedTitleTextAppearance(R.style.CollapsedAppBar);

        iv_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MenuDialog(requireActivity()).initDialog();
            }
        });

        iv_messenger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getActivity(), ClassRoomActivity.class);
                intent.putExtra("action","");
                iv_noti_red_mark.setVisibility(View.GONE);
                startActivity(intent);
            }
        });




        categoryAdapter=new VideoCategoryAdapter(getActivity(),CategoryList,null);

        LinearLayoutManager lmc = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        recyclerViewCategory.setLayoutManager(lmc);
        recyclerViewCategory.setItemAnimator(new DefaultItemAnimator());
        recyclerViewCategory.setAdapter(categoryAdapter);

        categoryAdapter.setCallback(new VideoCategoryAdapter.Callback() {
            @Override
            public void onCategoryClick(String categoryID, String category) {
                currentCategoryID=categoryID;
                currentCategory=category;
                loading=true;
                page=1;
                loadDownloadedVideo();
                addLoadingItem();
                fetchLesson(1,currentCategoryID,true);
            }
        });

        setUpCategory(videoCategoryForm);

        AppBarLayout mAppBarLayout =v.findViewById(R.id.app_bar_layout);
        mAppBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                Log.e("scrollRange ",verticalOffset+"");
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                    toolBarContent.setVisibility(View.VISIBLE);


                }

                if (verticalOffset<-64) {
                    isShow = true;
                    toolBarContent.setVisibility(View.GONE);



                } else if (isShow) {
                    isShow = false;
                    toolBarContent.setVisibility(View.VISIBLE);


                }
            }
        });

    }

    private void loadDownloadedVideo(){
        fileManager.loadFiles(new File(rootDir + "/" + currentCategory), new FileManager.OnFileLoading() {
            @Override
            public void onLoaded(ArrayList<FileModel> files) {
                downloadedVideoFiles.clear();
                downloadedVideoFiles.addAll(files);
            }
        });
    }

    private void addLoadingItem(){
        lessonList.clear();
        adapter.notifyDataSetChanged();
        for(int i=0;i<3;i++){
            lessonList.add(new VideoModel("","",0,"",false,"",0));
        }
        adapter.notifyDataSetChanged();
    }

    private void fetchLesson(int count ,String cate,boolean isRefresh){

        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.GET, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    postExecutor.execute(() -> {
                        if (isRefresh)lessonList.clear();
                        try{
                            String videos=new JSONObject(response).getString("videos");
                            doAsResult(videos);
                        }catch (Exception e){}
                    });
                }
                @Override
                public void onError(String msg) {
                    // postExecutor.execute(() -> Toast.makeText(getActivity(),msg,Toast.LENGTH_SHORT).show());
                }
            }).url(Routing.FETCH_VIDEO +"?category="+cate+"&userId="+currentUserId+"&page="+count);
            myHttp.runTask();
        }).start();
    }


    private void doAsResult(String response){

        try {
            loading=true;
            JSONArray ja=new JSONArray(response);
            for(int i=0;i<ja.length();i++){
                JSONObject jo=ja.getJSONObject(i);
                String link=jo.getString("link");
                String title=jo.getString("title");
                boolean learned=jo.getString("learned").equals("1");
                long time=Long.parseLong(jo.getString("date"));
                String thumbnail=jo.getString("thumbnail");
                int duration=jo.getInt("duration");

                VideoModel model =new VideoModel(title,link,time,currentCategory,learned,thumbnail,duration);
                String checkTitle=title.replace("/"," ");
                checkTitle=checkTitle+".mp4";
                if(downloadedVideoFiles.size()>0){
                    for(int j=0;j<downloadedVideoFiles.size();j++){
                        FileModel file=downloadedVideoFiles.get(j);
                        if(file.getFile().getName().equals(checkTitle)){
                            model.setDownloaded(true);
                            model.setVideoModel((SavedVideoModel) file);
                            Log.e("Downloaded ", checkTitle + " is downloaded");

                        }
                    }
                }

                lessonList.add(model);
            }

            adapter.notifyDataSetChanged();

        }catch (Exception e){
            loading=false;

            Toast.makeText(getActivity(),e.toString(),Toast.LENGTH_SHORT).show();
        }
    }


    private void setUpCategory(String categoryJson){
        try {
            JSONArray ja=new JSONArray(categoryJson);

            for(int i=0;i<ja.length();i++){
                JSONObject jo=ja.getJSONObject(i);
                String cate=jo.getString("category");
                String cate_id=jo.getString("category_id");
                CategoryList.add(new VideoCategoryModel(cate,cate_id));

            }
            categoryAdapter.notifyDataSetChanged();
            firstCategory=ja.getJSONObject(0).getString("category_id");
            currentCategoryID=firstCategory;
            currentCategory=ja.getJSONObject(0).getString("category");
            addLoadingItem();
            loadDownloadedVideo();
            fetchLesson(1,firstCategory,true);

        }catch (Exception ignored){

        }
    }



    private void searchAVideo(String search){
        Log.e("Search ",search);
        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.GET, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    postExecutor.execute(() -> {
                        lessonList.clear();
                        doAsResult(response);
                    });
                }
                @Override
                public void onError(String msg) {}
            }).url(Routing.FIND_A_VIDEO+"?search="+search+"&userId="+currentUserId);
            myHttp.runTask();
        }).start();
    }

    private void setSnackBar(String s){
        final Snackbar sb=Snackbar.make(main,s,Snackbar.LENGTH_INDEFINITE);
        sb.setAction("View", v -> startActivity(new Intent(getActivity(),
                        DownloadingListActivity.class)))
                .setActionTextColor(Color.WHITE)
                .show();
    }
}
