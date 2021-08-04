package com.calamus.easykorean.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.calamus.easykorean.R;
import com.calamus.easykorean.adapters.NewFeedAdapter;
import com.calamus.easykorean.app.MyHttp;
import com.calamus.easykorean.app.Routing;
import com.calamus.easykorean.models.AnounceModel;
import com.calamus.easykorean.models.NewfeedModel;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import me.myatminsoe.mdetect.MDetect;


public class FragmentFour extends Fragment {
    View v;
    public static RecyclerView recycler;
    SwipeRefreshLayout swipe;
    NewFeedAdapter adapter;
    ArrayList<Object> postList = new ArrayList<>();
    SharedPreferences share;

    int count=0;
    private boolean loading=true;
    int visibleItemCount,totalItemCount;
    public static int pastVisibleItems;

    LinearLayoutManager lm;
    Executor postExecutor;
    String userId,selection;
    public static final int number_of_ads=1;
    private static final String AD_UNIT_ID="ca-app-pub-2472405866346270/3806485083";
    AdLoader adLoader;
    final List<UnifiedNativeAd> nativeAds=new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v=inflater.inflate(R.layout.fragment_four,container,false);

        MDetect.INSTANCE.init(getActivity());
        share=getActivity().getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        setupViews();
        postList.add(0,"kaung");
        postExecutor = ContextCompat.getMainExecutor(getActivity());
        fetchAnounceLink();
        testFetch(count,false);
        return v;

    }

    public FragmentFour(String selection,String userId) {

        this.selection=selection;
        this.userId=userId;
    }

    @Override
    public void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupViews() {
        recycler = v.findViewById(R.id.recycler_nf);
        swipe = v.findViewById(R.id.swipe_nf);

        lm = new LinearLayoutManager(getActivity()){};
        recycler.setLayoutManager(lm);
        // recycler.addItemDecoration(new SpacingItemDecoration(2, XUtils.toPx(Objects.requireNonNull(getActivity()), 2), true));
        recycler.setItemAnimator(new DefaultItemAnimator());

        adapter = new NewFeedAdapter(getActivity(), postList);
        recycler.setAdapter(adapter);
        swipe.setRefreshing(true);

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
                            nativeAds.clear();
                            loading=false;
                            count+=10;
                            testFetch(count,false);

                        }
                    }


                }
            }
        });

        swipe.setOnRefreshListener(() -> {

            nativeAds.clear();
            postList.add(0,"kaung");
            count=0;
            loading=true;
            testFetch(0,true);
        });

    }

    private void testFetch(int count,boolean isRefresh){

        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.GET, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    postExecutor.execute(() -> {
                        if (isRefresh){
                            postList.clear();
                            postList.add(0,"kaung");
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
            }).url(Routing.FETCH_POST+"?count="+count+"&userId="+userId+"&major=korea"+"&selection="+selection);
            myHttp.runTask();
        }).start();
    }

    public void doAsResult(String response){

        swipe.setRefreshing(false);

        try {
            loading=true;
            JSONArray ja=new JSONArray(response);
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
                NewfeedModel model = new NewfeedModel(userName,userId,userToken,userImage,postId,postBody,posLikes,postComment,postImage,isVip,isVideo,viewCount,isLike);
                postList.add(model);

            }

            adapter.notifyDataSetChanged();
            loadNativeAds();

        }catch (Exception e){
            loading=false;
            swipe.setRefreshing(false);
            //Toast.makeText(getActivity(),e.toString(),Toast.LENGTH_SHORT).show();
        }

    }

    public static boolean canExit(){
        return pastVisibleItems == 0 || pastVisibleItems>20;
    }

    public static void goToFirst(){
        recycler.smoothScrollToPosition(0);

    }

    private void loadNativeAds() {
        AdLoader.Builder builder=new AdLoader.Builder(getActivity(),AD_UNIT_ID);
        adLoader=builder.forUnifiedNativeAd(unifiedNativeAd -> {
            nativeAds.add(unifiedNativeAd);

            if(!adLoader.isLoading()){
                insertAdsMenuItem();

            }
        }).withAdListener(new AdListener(){
            @Override
            public void onAdFailedToLoad(LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);

                if(!adLoader.isLoading()){
                    insertAdsMenuItem();

                }
            }
        }).build();

        adLoader.loadAds(new AdRequest.Builder().build(),number_of_ads);
    }

    private void insertAdsMenuItem() {
        if(nativeAds.size()<0){
            return;
        }
        postList.addAll(nativeAds);
        adapter.notifyDataSetChanged();

    }

    private void fetchAnounceLink(){
        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.GET, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    postExecutor.execute(() -> {
                        try{
                            JSONArray ja=new JSONArray(response);
                            int index=1;
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
                    });
                }
                @Override
                public void onError(String msg) {
                    postExecutor.execute(() -> {
                        //  Toast.makeText(getActivity(),msg,Toast.LENGTH_SHORT).show();
                    });
                }
            }).url(Routing.GET_ANNOUNCEMENT+"?major=korea&userId="+userId);
            myHttp.runTask();
        }).start();
    }

}
