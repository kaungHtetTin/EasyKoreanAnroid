package com.calamus.easykorean.fragments;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.calamus.easykorean.MainActivity;
import com.calamus.easykorean.R;
import com.calamus.easykorean.adapters.VideoAdapter;
import com.calamus.easykorean.adapters.VideoCategoryAdapter;
import com.calamus.easykorean.app.MyHttp;
import com.calamus.easykorean.app.Routing;
import com.calamus.easykorean.models.VideoModel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.Executor;


public class FragmentThree extends Fragment {
    View v;

    private SwipeRefreshLayout swipe;
    private VideoAdapter adapter;
    private final ArrayList<VideoModel> lessonList = new ArrayList<>();
    private final ArrayList<String>CategoryList=new ArrayList<>();
    private VideoCategoryAdapter categoryAdapter;
    MenuItem item;
    SharedPreferences sharedPreferences;
    String videoCategoryForm,firstCategory="";
    String currentCategory;

    int count=0;
    private boolean loading=true;
    int visibleItemCount,totalItemCount;
    public static int pastVisibleItems;
    Executor postExecutor;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v=inflater.inflate(R.layout.fragment_three,container,false);

        sharedPreferences=getActivity().getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        videoCategoryForm=sharedPreferences.getString("videoForm","");

        setUpView();

        setHasOptionsMenu(true);


        return v;


    }

    private void setUpView (){

        RecyclerView recycler = v.findViewById(R.id.recycler);
        RecyclerView recyclerViewCategory=v.findViewById(R.id.recycler_category);
        swipe=v.findViewById(R.id.swipe_1);

        LinearLayoutManager lm = new LinearLayoutManager(getActivity());
        recycler.setLayoutManager(lm);
        recycler.setItemAnimator(new DefaultItemAnimator());
        adapter = new VideoAdapter(getActivity(), lessonList);
        recycler.setAdapter(adapter);
        postExecutor = ContextCompat.getMainExecutor(getActivity());


        swipe.setOnRefreshListener(() -> {
            count=0;
            loading=true;
            fetchLesson(0,currentCategory,true);
        });

        swipe.setRefreshing(true);

        categoryAdapter=new VideoCategoryAdapter(getActivity(),CategoryList,null);

        LinearLayoutManager lmc = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        recyclerViewCategory.setLayoutManager(lmc);
        recyclerViewCategory.setItemAnimator(new DefaultItemAnimator());
        recyclerViewCategory.setAdapter(categoryAdapter);

        categoryAdapter.setCallback(category -> {
            currentCategory=category;
            loading=true;
            swipe.setRefreshing(true);
            fetchLesson(0,currentCategory,true);
        });

        setUpCategory(videoCategoryForm);

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
                            count+=30;
                            fetchLesson(count,currentCategory,false);
                        }
                    }


                }
            }
        });

    }


    private void fetchLesson(int count ,String cate,boolean isRefresh){
        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.GET, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    postExecutor.execute(() -> {
                        if (isRefresh)lessonList.clear();
                        doAsResult(response);
                    });
                }
                @Override
                public void onError(String msg) {
                    postExecutor.execute(() -> Toast.makeText(getActivity(),msg,Toast.LENGTH_SHORT).show());
                }
            }).url(Routing.FETCH_VIDEO +"?count="+count+"&major=korea"+"&cate="+cate);
            myHttp.runTask();
        }).start();
    }


    private void doAsResult(String response){
        swipe.setRefreshing(false);
        try {
            loading=true;
            JSONArray ja=new JSONArray(response);
            for(int i=0;i<ja.length();i++){
                JSONObject jo=ja.getJSONObject(i);
                String link=jo.getString("link");
                String title=jo.getString("title");
                String category=jo.getString("cate");
                long time=Long.parseLong(jo.getString("date"));
                lessonList.add(new VideoModel(title,link,time,category));

            }

            adapter.notifyDataSetChanged();

        }catch (Exception e){
            loading=false;
            swipe.setRefreshing(false);
            Toast.makeText(getActivity(),e.toString(),Toast.LENGTH_SHORT).show();
        }
    }


    private void setUpCategory(String categoryJson){
        try {
            JSONArray ja=new JSONArray(categoryJson);

            for(int i=0;i<ja.length();i++){
                JSONObject jo=ja.getJSONObject(i);
                String cate=jo.getString("cate");
                CategoryList.add(cate);

            }
            categoryAdapter.notifyDataSetChanged();
            firstCategory=ja.getJSONObject(0).getString("cate");
            currentCategory=firstCategory;
            fetchLesson(0,firstCategory,false);

        }catch (Exception e){

        }
    }


    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        super.onCreateOptionsMenu(menu,inflater);
        menu.clear();
        inflater.inflate(R.menu.too_menu,menu);

        item =menu.findItem(R.id.action_search);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW|MenuItem.SHOW_AS_ACTION_IF_ROOM);

        SearchView searchView=new SearchView(((MainActivity) requireActivity()).getSupportActionBar().getThemedContext());
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
                    searchAVideo(query);

                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
              //  adapter.getFilter().filter(newText);
                loading=false;
                swipe.setRefreshing(true);
                searchAVideo(newText);
                return true;
            }
        });
    }

    private void searchAVideo(String search){
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
                public void onError(String msg) {
                    postExecutor.execute(() -> Toast.makeText(getActivity(),msg,Toast.LENGTH_SHORT).show());
                }
            }).url(Routing.FIND_A_VIDEO+"?major=korea"+"&searching="+search);
            myHttp.runTask();
        }).start();
    }
}
