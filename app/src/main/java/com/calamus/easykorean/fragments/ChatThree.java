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
import com.calamus.easykorean.adapters.FriendAdapter;
import com.calamus.easykorean.app.MyHttp;
import com.calamus.easykorean.app.Routing;
import com.calamus.easykorean.models.FriendModel;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.concurrent.Executor;

public class ChatThree extends Fragment {
    View v;
    private SwipeRefreshLayout swipe;
    FriendAdapter adapter;
    ArrayList<FriendModel> arrLists=new ArrayList<>();
    Executor postExecutor;
    String currentUserId;
    SharedPreferences sharedPreferences;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v=inflater.inflate(R.layout.fragment_chat_three,container,false);
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

        adapter = new FriendAdapter(getActivity(), arrLists);
        recycler.setAdapter(adapter);


        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh()
            {
                arrLists.clear();
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
                                JSONArray ja=new JSONArray(response);
                                for(int i=0;i<ja.length();i++) {
                                    JSONObject jo = ja.getJSONObject(i);
                                    String userName = jo.getString("userName");
                                    String imageUrl=jo.getString("userImage");
                                    String phone=jo.getString("phone");
                                    String token=jo.getString("token");
                                    arrLists.add(new FriendModel(phone,userName,imageUrl,token));
                                }
                                adapter.notifyDataSetChanged();
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
            }).url(Routing.GET_FRIENDS+currentUserId+"/korea");
            myHttp.runTask();
        }).start();
    }
}
