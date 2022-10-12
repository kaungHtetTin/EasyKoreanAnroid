package com.calamus.easykorean.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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
import com.calamus.easykorean.adapters.FriendRequestAdapter;
import com.calamus.easykorean.app.Routing;
import com.calamus.easykorean.interfaces.OnFriendRequestSeeMoreClick;
import com.calamus.easykorean.models.FriendModel;
import com.calamus.easykorean.models.NewStudentModel;
import com.calamus.easykorean.app.MyHttp;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.Executor;


public class ChatTwo extends Fragment {
    View v;
    private SwipeRefreshLayout swipe;
    FriendRequestAdapter adapter;
    ArrayList<Object> arrLists=new ArrayList<>();
    ArrayList<FriendModel> friends=new ArrayList<>();

    Executor postExecutor;
    String currentUserId;
    SharedPreferences sharedPreferences;

    SeeMore seeMore;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v=inflater.inflate(R.layout.fragment_chat_two,container,false);
        postExecutor= ContextCompat.getMainExecutor(getActivity());
        sharedPreferences=getActivity().getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        currentUserId=sharedPreferences.getString("phone","011");
        setUpView();
        fetchData();
        return v;
    }

    private void  setUpView(){
        swipe=v.findViewById(R.id.swipe);
        RecyclerView recycler = v.findViewById(R.id.recycler);

        LinearLayoutManager lm = new LinearLayoutManager(getActivity());
        recycler.setLayoutManager(lm);
        recycler.setItemAnimator(new DefaultItemAnimator());

        adapter = new FriendRequestAdapter(getActivity(), arrLists);
        recycler.setAdapter(adapter);


        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh()
            {

                arrLists.clear();
                friends.clear();
                fetchData();
            }
        });

        seeMore=new SeeMore(new OnFriendRequestSeeMoreClick() {
            @Override
            public void onClick() {
                arrLists.clear();
                arrLists.add(0,new SearchBox());
                arrLists.add(1,"Friend Requests");
                arrLists.addAll(friends);
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void fetchData(){
        swipe.setRefreshing(true);
        arrLists.add(0,new SearchBox()); // this is for search bar
        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.GET, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    postExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            swipe.setRefreshing(false);
                            try {
                                JSONObject joMain=new JSONObject(response);

                                if(!joMain.getString("request").equals("null")){
                                    arrLists.add(1,"Friend Requests");

                                    try {
                                        JSONArray ja= joMain.getJSONArray("request");
                                        adapter.setFriendRequestCount(ja.length());
                                        for(int i=0;i<ja.length();i++) {
                                            JSONObject jo = ja.getJSONObject(i);
                                            String userName = jo.getString("userName");
                                            String imageUrl=jo.getString("userImage");
                                            String phone=jo.getString("phone");
                                            String token=jo.getString("token");
                                            String friendJSON=jo.getString("friends");
                                            friends.add(new FriendModel(phone,userName,imageUrl,token,friendJSON));
                                            if(i<3){
                                                arrLists.add(new FriendModel(phone,userName,imageUrl,token,friendJSON));
                                            }
                                            if(i==3)arrLists.add(seeMore);

                                        }
                                    }catch (Exception e){
                                        Log.e("ErrReq: ",e.toString());
                                    }
                                    adapter.notifyDataSetChanged();
                                }

                                try {
                                    JSONArray ja= joMain.getJSONArray("people");
                                    arrLists.add("New Students You May Know");
                                    for(int i=0;i<ja.length();i++) {
                                        JSONObject jo = ja.getJSONObject(i);
                                        String userName = jo.getString("userName");
                                        String imageUrl=jo.getString("userImage");
                                        String phone=jo.getString("phone");
                                        String token=jo.getString("token");
                                        String friends=jo.getString("friends");
                                        arrLists.add(new NewStudentModel(phone,userName,imageUrl,token,friends));

                                    }
                                    adapter.notifyDataSetChanged();
                                }catch (Exception e){
                                    Log.e("ErrPeople: ",e.toString());
                                }
                            }catch (Exception e){
                                Log.e("ErrJson: ",e.toString());
                                swipe.setRefreshing(false);
                            }
                        }
                    });
                }
                @Override
                public void onError(String msg) {
                    Log.e("Err: ", msg);

                    postExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            swipe.setRefreshing(false);
                        }
                    });
                }
            }).url(Routing.GET_FRIEND_REQUEST+currentUserId);
            myHttp.runTask();
        }).start();
    }


    public class SearchBox{}
    public class SeeMore{
        OnFriendRequestSeeMoreClick click;
        public SeeMore(OnFriendRequestSeeMoreClick click){
            this.click=click;
        }

        public OnFriendRequestSeeMoreClick getClick() {
            return click;
        }
    }

}
