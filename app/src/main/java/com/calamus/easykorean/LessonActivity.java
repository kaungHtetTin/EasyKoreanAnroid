package com.calamus.easykorean;

import android.app.SearchManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.calamus.easykorean.adapters.LessonAdapter;
import com.calamus.easykorean.app.MyHttp;
import com.calamus.easykorean.app.Routing;
import com.calamus.easykorean.models.LessonModel;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.Executor;

public class LessonActivity extends AppCompatActivity implements android.widget.SearchView.OnQueryTextListener
{
    RecyclerView recycler;
    public SwipeRefreshLayout swipe;
    android.widget.SearchView searchView;
    SearchManager searchManager;
    MenuItem menuItemSearch;
    public static String category;
    LessonAdapter adapter;
    ArrayList<LessonModel> lessonList = new ArrayList<>();
    public static final String POST_PARCEL = "post_parcel";
    public static int resID;
    public static String picLink;
    public static String eCode,level;
    public static int fragmentId;
    boolean isVip;
    SharedPreferences sharedPreferences;
    Executor postExecutor;

    int count=0;
    private boolean loading=true;
    int visibleItemCount,totalItemCount;
    public static int pastVisibleItems;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson);
        sharedPreferences=getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        isVip=sharedPreferences.getBoolean("isVIP",false);
        postExecutor = ContextCompat.getMainExecutor(this);
        setupSearchView();
        setupViews();

        category = Objects.requireNonNull(getIntent().getExtras()).getString("cate", "");
        picLink=getIntent().getExtras().getString("picLink");
        eCode=getIntent().getExtras().getString("eCode");
        String cate=getIntent().getExtras().getString("setCate");
        level=getIntent().getExtras().getString("level");
        fragmentId=getIntent().getExtras().getInt("fragment");
        setTitle(cate);

        fetchLesson(0,false);

        MobileAds.initialize(this, initializationStatus -> {});

        AdView adView = findViewById(R.id.web_adview);
        if(!isVip){
            adView.setVisibility(View.VISIBLE);
            AdRequest request=new AdRequest.Builder().build();
            adView.loadAd(request);
        }


    }


    private void setupSearchView()
    {
        searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = new SearchView(Objects.requireNonNull(getSupportActionBar()).getThemedContext());
        searchView.setQueryHint("Search");
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setOnQueryTextListener(this);
    }

    private void setupViews()
    {
        recycler = findViewById(R.id.recycler);
        swipe=findViewById(R.id.swipe_1);
        swipe.setRefreshing(true);
        LinearLayoutManager lm = new LinearLayoutManager(this);
        recycler.setLayoutManager(lm);
        //recycler.addItemDecoration(new SpacingItemDecoration(2, XUtils.toPx(this, 2), true));
        recycler.setItemAnimator(new DefaultItemAnimator());
        adapter = new LessonAdapter(this, lessonList);
        recycler.setAdapter(adapter);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        swipe.setOnRefreshListener(() -> {

            count=0;
            loading=true;
            fetchLesson(0,true);
        });


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
                            fetchLesson(count,false);

                        }
                    }


                }
            }
        });

    }

    @Override
    public boolean onQueryTextSubmit(String query)
    {
        menuItemSearch.collapseActionView();
        if (query != null)
        {
            adapter.getFilter().filter(query);
        }
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText)
    {
        adapter.getFilter().filter(newText);

        return true;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        menuItemSearch = menu.add("Search");
        menuItemSearch.setVisible(true);
        menuItemSearch.setActionView(searchView);
        menuItemSearch.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
        menuItemSearch.setIcon(R.drawable.ic_search);
        assert searchManager != null;
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false);
        return super.onCreateOptionsMenu(menu);
    }


    private void fetchLesson(int count,boolean isRefresh){
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
                    postExecutor.execute(() -> Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show());
                }
            }).url(Routing.FETCH_LESSONS +"?count="+count+"&major=korea"+"&cate="+category);
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
                String cate=jo.getString("cate");
                String link=jo.getString("link");
                String title=jo.getString("title");
                boolean isVideo= jo.getString("isVideo").equals("1");
                boolean isVip= jo.getString("isVip").equals("1");
                long time=Long.parseLong(jo.getString("date"));
                lessonList.add(new LessonModel(cate,link,title,isVideo,isVip,time));

            }

            adapter.notifyDataSetChanged();


        }catch (Exception e){
            loading=false;
            swipe.setRefreshing(false);
            Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {

        if (item.getItemId() == android.R.id.home) {
            finish();

        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onResume() {
        super.onResume();
    }
}

