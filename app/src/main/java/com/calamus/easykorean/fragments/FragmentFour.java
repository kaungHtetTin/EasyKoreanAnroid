package com.calamus.easykorean.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.calamus.easykorean.MyDiscussionActivity;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.calamus.easykorean.NotiListActivity;
import com.calamus.easykorean.R;
import com.calamus.easykorean.SearchingActivity;
import com.calamus.easykorean.WritePostActivity;
import com.calamus.easykorean.adapters.NewFeedAdapter;
import com.calamus.easykorean.app.AppHandler;
import com.calamus.easykorean.app.Routing;
import com.calamus.easykorean.dialogs.MenuDialog;
import com.calamus.easykorean.models.AdModel;
import com.calamus.easykorean.models.AnounceModel;
import com.calamus.easykorean.models.LoadingModel;
import com.calamus.easykorean.models.NewfeedModel;
import com.calamus.easykorean.app.MyHttp;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class FragmentFour extends Fragment {

    View v;
    public static RecyclerView recycler;

    NewFeedAdapter adapter;
    ArrayList<Object> postList = new ArrayList<>();
    SharedPreferences share;

    int page=1;
    private boolean loading=true;
    int visibleItemCount,totalItemCount;
    public static int pastVisibleItems;
    LinearLayoutManager lm;
    Executor postExecutor;
    ExecutorService myExecutor;
    String userId,username,userProfile,userVIP;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v=inflater.inflate(R.layout.fragment_four,container,false);
        share=getActivity().getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        userId=share.getString("phone",null);
        username=share.getString("Username",null);
        userProfile=share.getString("imageUrl",null);
        userVIP=share.getBoolean("isVIP",false)?"1":"0";
        setupViews();

        postExecutor = ContextCompat.getMainExecutor(getActivity());
        myExecutor= Executors.newFixedThreadPool(3);

        fetchAnounceLink();


        addLoadingContent();
        testFetch(page,true);

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



        setUpAppBar();

        lm = new LinearLayoutManager(getActivity()){};
        recycler.setLayoutManager(lm);
        recycler.setItemAnimator(new DefaultItemAnimator());

        adapter = new NewFeedAdapter(getActivity(), postList);
        recycler.setAdapter(adapter);


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
                            testFetch(page,false);

                        }
                    }

                }
            }
        });

    }


    private void setUpAppBar(){

        ImageView iv_profile,iv_notification,iv_announcement,iv_menu,iv_noti_red_mark;
        LinearLayout layoutWriter;

        iv_profile=v.findViewById(R.id.iv_profile);
        iv_notification=v.findViewById(R.id.iv_notification);
        iv_menu=v.findViewById(R.id.iv_menu);
        layoutWriter=v.findViewById(R.id.layoutWriter);
        iv_noti_red_mark=v.findViewById(R.id.noti_red_mark);
        ImageView iv_search=v.findViewById(R.id.iv_search_newsfeed);


        iv_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MenuDialog(requireActivity()).initDialog();
            }
        });


        iv_notification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                iv_noti_red_mark.setVisibility(View.GONE);
                Intent intent=new Intent(getActivity(), NotiListActivity.class);
                startActivity(intent);
            }
        });

        iv_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), SearchingActivity.class));
            }
        });

        layoutWriter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i=new Intent(getActivity(), WritePostActivity.class);
                i.putExtra("action",true);
                i.putExtra("postBody","");
                i.putExtra("postImage","");
                i.putExtra("postId","");
                mStartForResult.launch(i);
            }
        });

        iv_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getActivity(), MyDiscussionActivity.class);
                intent.putExtra("userId",userId);
                intent.putExtra("userName",username);
                startActivity(intent);
            }
        });

        String imagePath=share.getString("imageUrl",null);
        boolean notiRedMark=share.getBoolean("notiRedMark",false);

        if(notiRedMark)iv_noti_red_mark.setVisibility(View.VISIBLE);
        else iv_noti_red_mark.setVisibility(View.GONE);

        if(imagePath!=null) AppHandler.setPhotoFromRealUrl(iv_profile,imagePath);

        CollapsingToolbarLayout toolbarLayout=v.findViewById(R.id.ctb);
        RelativeLayout toolBarContent=v.findViewById(R.id.toolbarContent);
        toolbarLayout.setTitle(Routing.APP_NAME);
        toolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppBar);
        toolbarLayout.setCollapsedTitleTextAppearance(R.style.CollapsedAppBar);
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
                    iv_search.setVisibility(View.INVISIBLE);

                }

                if (verticalOffset<-64) {
                    isShow = true;
                    iv_search.setVisibility(View.VISIBLE);
                    toolBarContent.setVisibility(View.GONE);


                } else if (isShow) {
                    isShow = false;
                    toolBarContent.setVisibility(View.VISIBLE);
                    iv_search.setVisibility(View.INVISIBLE);

                }
            }
        });



    }


    private void addLoadingContent(){
        postList.clear();
        postList.add(new LoadingModel());
        postList.add(new LoadingModel());
        postList.add(new LoadingModel());

        adapter.notifyDataSetChanged();
    }

    private void testFetch(int count,boolean isRefresh){
        myExecutor.execute(()->{
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.GET, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    postExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            if (isRefresh){
                                postList.clear();
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
                            Log.e("Fragment4Post Err ",msg);
                        }
                    });
                }
            }).url(Routing.FETCH_POST+"?mCode="+Routing.MAJOR_CODE+"&userId="+userId+"&page="+count);
            myHttp.runTask();
        });
    }

    public void doAsResult(String response){

        try {
            loading=true;
            JSONObject joPosts=new JSONObject(response);
            JSONArray ja=joPosts.getJSONArray("posts");
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
                int blocked=jo.getInt("blocked");
                int hidden=jo.getInt("hidden");
                long share=jo.getLong("share");
                int shareCount=jo.getInt("shareCount");
                NewfeedModel model = new NewfeedModel(userName,userId,userToken,userImage,postId,postBody,posLikes,postComment,postImage,isVip,isVideo,viewCount,isLike,shareCount,share);
                if(hidden!=1&blocked!=1)postList.add(model);
            }
            LoadApp();
            //  adapter.notifyDataSetChanged();
        }catch (Exception e){
            loading=false;
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
                    postExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            try{
                                JSONArray ja=new JSONArray(response);
                                int index=0;
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

                            }catch (Exception  e){

                            }
                        }
                    });
                }
                @Override
                public void onError(String msg) {
                    postExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            //  Toast.makeText(getActivity(),msg,Toast.LENGTH_SHORT).show();
                        }
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
                                if(postList.size()>5&&postList.size()<15){
                                    postList.add(3,new AdModel(id,name,description,url,cover,icon,type));
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
            }).url(Routing.GET_APP_ADS+page);
            myHttp.runTask();
        });
    }


    ActivityResultLauncher<Intent> mStartForResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent intent = result.getData();
                        String body=intent.getStringExtra("body");
                        String imagePath=intent.getStringExtra("imagePath");
                        Toast.makeText(getActivity(),"Start Uploading",Toast.LENGTH_SHORT).show();
                        uploadPost(userId,body,imagePath);
                    }
                }
            });


    private void uploadPost(String learner_id,String body,String imagePath) {
        postList.add(0,new LoadingModel());
        adapter.notifyDataSetChanged();
        new Thread(() -> {
            MyHttp myHttp = new MyHttp(MyHttp.RequesMethod.POST, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    postExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            Log.e("Post Upload Return",response);
                            try {
                                JSONObject jo=new JSONObject(response);
                                String userId=jo.getString("learner_id");
                                String postId=jo.getString("post_id");
                                String postBody=jo.getString("body");
                                String posLikes=jo.getString("post_like");
                                String postComment=jo.getString("comments");
                                String postImage=jo.getString("image");
                                String isVideo=jo.getString("has_video");
                                String viewCount=jo.getString("view_count");
                                long share=jo.getLong("share");
                                NewfeedModel model = new NewfeedModel(username,userId,"",userProfile,postId,postBody,posLikes,postComment,postImage,userVIP,isVideo,viewCount,"0",0,share);
                                postList.remove(0);
                                postList.add(0,model);
                                adapter.notifyDataSetChanged();

                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });
                }

                @Override
                public void onError(String msg) {
                    postExecutor.execute(new Runnable() {
                        @Override
                        public void run() {

                            Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }).url(Routing.ADD_POST)
                    .field("learner_id", learner_id)
                    .field("body", body)
                    .field("major", Routing.MAJOR)
                    .field("hasVideo", "0");
            if (!imagePath.isEmpty()) myHttp.file("myfile", imagePath);
            myHttp.runTask();
        }).start();

    }

}
