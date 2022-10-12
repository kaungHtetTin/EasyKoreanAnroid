package com.calamus.easykorean;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.calamus.easykorean.adapters.ReviewAdapter;
import com.calamus.easykorean.app.AppHandler;
import com.calamus.easykorean.app.Routing;
import com.calamus.easykorean.models.LoadingModel;
import com.calamus.easykorean.models.ReviewModel;
import com.calamus.easykorean.app.MyHttp;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.Executor;

public class ReviewsActivity extends AppCompatActivity {


    String course_id,course_title;
    RecyclerView recyclerView;
    Toolbar toolbar;

    ArrayList<Object> reviews =new ArrayList<>();
    ReviewAdapter  adapter;

    int page=1;
    int star=0;
    private boolean loading=true;
    int visibleItemCount,totalItemCount;
    public int pastVisibleItems;
    Executor postExecutor;

    TextView tv_star_all,tv_star1,tv_star2,tv_star3,tv_star4,tv_star5;
    int [] textViews={
            R.id.tv_star_all,
            R.id.tv_star1,
            R.id.tv_star_2,
            R.id.tv_star_3,
            R.id.tv_star_4,
            R.id.tv_star5
    };

    ImageView iv_back,iv_reviews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reviews);
        course_id=getIntent().getExtras().getString("course_id");
        course_title=getIntent().getExtras().getString("course_title");
        postExecutor= ContextCompat.getMainExecutor(this);



        setUpView();
    }

    private void setUpView(){
        recyclerView=findViewById(R.id.recycler);
        iv_back=findViewById(R.id.iv_back);
        iv_reviews=findViewById(R.id.iv_reviews);
        tv_star_all=findViewById(R.id.tv_star_all);
        tv_star1=findViewById(R.id.tv_star1);
        tv_star2=findViewById(R.id.tv_star_2);
        tv_star3=findViewById(R.id.tv_star_3);
        tv_star4=findViewById(R.id.tv_star_4);
        tv_star5=findViewById(R.id.tv_star5);

        LinearLayoutManager lm=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(lm);
        adapter=new ReviewAdapter(this,reviews);
        recyclerView.setAdapter(adapter);
        addLoadingItem();

        setUpAppBar();

        fetchReviews(page,star,true);

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
                            fetchReviews(page,star,false);
                        }
                    }
                }
            }
        });

        tv_star_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                star=0;
                page=1;
                fetchReviews(page,star,true);
            }
        });

        tv_star1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                star=1;
                page=1;
                fetchReviews(page,star,true);
            }
        });

        tv_star2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                star=2;
                page=1;
                fetchReviews(page,star,true);
            }
        });

        tv_star3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                star=3;
                page=1;
                fetchReviews(page,star,true);
            }
        });

        tv_star4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                star=4;
                page=1;
                fetchReviews(page,star,true);
            }
        });

        tv_star5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                star=5;
                page=1;
                fetchReviews(page,star,true);
            }
        });

        AppHandler.setPhotoFromRealUrl(iv_reviews,"https://www.calamuseducation.com/uploads/icons/reviews.png");
    }

    private void setUpAppBar(){
        CollapsingToolbarLayout toolbarLayout=findViewById(R.id.ctb);
        RelativeLayout toolBarContent=findViewById(R.id.toolbarContent);
        toolbarLayout.setTitle(course_title);
        toolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppBar);
        toolbarLayout.setCollapsedTitleTextAppearance(R.style.CollapsedAppBar);

        toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        AppBarLayout mAppBarLayout =findViewById(R.id.app_bar_layout);
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

    private void fetchReviews(int page,int star,boolean isRefresh){
        Log.e("page ",page+"");
        if(isRefresh)addLoadingItem();
        changeTextViewStyle(star);
        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.GET, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    Log.e("Reviews Res",response);
                    postExecutor.execute(() -> {
                        if (isRefresh)reviews.clear();
                        try{
                            doAsResult(response);
                        }catch (Exception e){}
                    });
                }
                @Override
                public void onError(String msg) {
                    Log.e("Reviews Err ",msg);
                }
            }).url(Routing.RATING+"?course_id="+course_id+"&star="+star+"&page="+page+"&mCode="+Routing.MAJOR_CODE);
            myHttp.runTask();
        }).start();
    }

    private void doAsResult(String response){

        try {
            loading=true;
            JSONArray ja=new JSONArray(response);
            for(int i=0;i<ja.length();i++){
                JSONObject jo=ja.getJSONObject(i);
                String userName=jo.getString("userName");
                String userId=jo.getString("userId");
                String userImage=jo.getString("userImage");
                boolean vip=jo.getInt("vip")==1;
                int star=jo.getInt("star");
                String review=jo.getString("review");
                long time= jo.getLong("time");
                reviews.add(new ReviewModel(userId,userName,userImage,review,star,time,vip));
            }

            adapter.notifyDataSetChanged();

        }catch (Exception e){
            loading=false;

            Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_SHORT).show();
        }
    }

    private void addLoadingItem(){
        reviews.clear();
        adapter.notifyDataSetChanged();
        for(int i=0;i<4;i++){
            reviews.add(new LoadingModel());
        }
        adapter.notifyDataSetChanged();
    }

    private void changeTextViewStyle(int star){
        for(int i=0;i<textViews.length;i++){
            TextView tv=findViewById(textViews[i]);
            if(i==star){
                tv.setBackgroundResource(R.drawable.bg_rating_star_bar_green);
                if(i!=0) tv.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_one_star_green, 0);
                tv.setTextColor(Color.parseColor("#4CAF50"));
            }else{
                findViewById(textViews[i]).setBackgroundResource(R.drawable.bg_rating_star_bar);
                if(i!=0) tv.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_one_star_black, 0);
                tv.setTextColor(getResources().getColor(R.color.colorBlack1));
            }
        }
    }


}