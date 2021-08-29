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
import com.calamus.easykorean.app.MyHttp;
import com.calamus.easykorean.app.Routing;
import com.calamus.easykorean.models.FriendModel;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.concurrent.Executor;


public class ChatTwo extends Fragment {
    View v;
    private SwipeRefreshLayout swipe;
    FriendRequestAdapter adapter;
    ArrayList<FriendModel> arrLists=new ArrayList<>();
    ArrayList<FriendModel> peopleLists=new ArrayList<>();
    Executor postExecutor;
    String currentUserId;
    SharedPreferences sharedPreferences;

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
        arrLists.add(new FriendModel("","","",""));
        adapter = new FriendRequestAdapter(getActivity(), arrLists,peopleLists);
        recycler.setAdapter(adapter);


        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh()
            {
                arrLists.clear();
                arrLists.add(new FriendModel("","","",""));
                peopleLists.clear();
                fetchData();
            }
        });


    }

    private void fetchData(){
        swipe.setRefreshing(true);
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

                                try {
                                    JSONArray ja= joMain.getJSONArray("people");
                                    for(int i=0;i<ja.length();i++) {
                                        JSONObject jo = ja.getJSONObject(i);
                                        String userName = jo.getString("userName");
                                        String imageUrl=jo.getString("userImage");
                                        String phone=jo.getString("phone");
                                        String token=jo.getString("token");
                                        peopleLists.add(new FriendModel(phone,userName,imageUrl,token));
                                    }
                                    adapter.notifyDataSetChanged();
                                }catch (Exception e){
                                    Log.e("ErrPeople: ",e.toString());
                                }

                                if(!joMain.getString("request").equals("null")){
                                    try {
                                        JSONArray ja= joMain.getJSONArray("request");
                                        for(int i=0;i<ja.length();i++) {
                                            JSONObject jo = ja.getJSONObject(i);
                                            String userName = jo.getString("userName");
                                            String imageUrl=jo.getString("userImage");
                                            String phone=jo.getString("phone");
                                            String token=jo.getString("token");
                                            arrLists.add(new FriendModel(phone,userName,imageUrl,token));
                                        }

                                    }catch (Exception e){
                                        Log.e("ErrReq: ",e.toString());
                                    }
                                    adapter.notifyDataSetChanged();
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
            }).url(Routing.GET_FRIEND_REQUEST+currentUserId+"/korea");
            myHttp.runTask();
        }).start();
    }
}
