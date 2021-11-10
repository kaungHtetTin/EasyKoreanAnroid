package com.calamus.easykorean.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.calamus.easykorean.MyDiscussionActivity;
import com.calamus.easykorean.R;
import com.calamus.easykorean.app.AppHandler;
import com.calamus.easykorean.app.MyHttp;
import com.calamus.easykorean.app.Routing;
import com.calamus.easykorean.models.FriendModel;
import com.facebook.shimmer.ShimmerFrameLayout;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.Executor;

public class PeopleAdapter extends RecyclerView.Adapter<PeopleAdapter.Holder> {

    private final Activity c;
    private final ArrayList<FriendModel> data;
    private final LayoutInflater mInflater;
    String currentUserId;
    SharedPreferences sharedPreferences;

    @SuppressLint("SimpleDateFormat")
    public PeopleAdapter(Activity c, ArrayList<FriendModel> data) {
        this.data = data;
        this.c = c;
        this.mInflater = LayoutInflater.from(c);
        sharedPreferences=c.getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        currentUserId=sharedPreferences.getString("phone",null);
    }


    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public PeopleAdapter.Holder onCreateViewHolder( ViewGroup parent, int viewType) {

        View view = mInflater.inflate(R.layout.item_you_may_know, parent, false);
        return new Holder(view);

    }

    @Override
    public void onBindViewHolder(final PeopleAdapter.Holder holder, final int i) {
        FriendModel model=data.get(i);
        holder.tv_name.setText(model.getName());
        AppHandler.setPhotoFromRealUrl(model.getImageUrl(),holder.iv,holder.container);


    }



    public class Holder extends RecyclerView.ViewHolder {

        ImageView iv;
        TextView tv_name,tv_addFri,tv_reqFri,tv_limit;
        final ShimmerFrameLayout container;
        Executor myExecutor;

        public Holder(View view) {
            super(view);
            myExecutor= ContextCompat.getMainExecutor(c);
            iv=view.findViewById(R.id.iv_profile);
            tv_name=view.findViewById(R.id.tv_name);
            tv_addFri=view.findViewById(R.id.tv_addFri);
            tv_reqFri=view.findViewById(R.id.tv_reqFir);
            tv_limit=view.findViewById(R.id.tv_limit);
            container=view.findViewById(R.id.shimmer_view_container);
            iv.setClipToOutline(true);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FriendModel model=data.get(getAbsoluteAdapterPosition());
                    Intent intent=new Intent(c, MyDiscussionActivity.class);
                    intent.putExtra("userId",model.getPhone());
                    intent.putExtra("userName",model.getName());
                    c.startActivity(intent);
                }
            });

            tv_addFri.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    tv_addFri.setVisibility(View.GONE);
                    tv_reqFri.setVisibility(View.VISIBLE);
                    FriendModel model=data.get(getAbsoluteAdapterPosition());
                    friendShip(model.getPhone(), Routing.ADD_FRIEND);
                }
            });

            tv_reqFri.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    tv_addFri.setVisibility(View.VISIBLE);
                    tv_reqFri.setVisibility(View.GONE);
                    FriendModel model=data.get(getAbsoluteAdapterPosition());
                    friendShip(model.getPhone(), Routing.ADD_FRIEND);

                }
            });

        }

        private void friendShip(String userId,String url){
            new Thread(() -> {
                MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.POST, new MyHttp.Response() {
                    @Override
                    public void onResponse(String response) {
                        myExecutor.execute(new Runnable() {
                            @Override
                            public void run() {
                                try{
                                    JSONObject jo=new JSONObject(response);
                                    String code=jo.getString("code");
                                    if (code.equals("err53") || code.equals("err54")){
                                        tv_limit.setVisibility(View.VISIBLE);
                                        tv_addFri.setVisibility(View.GONE);
                                    }

                                }catch (Exception ignored){}
                            }
                        });
                    }
                    @Override
                    public void onError(String msg) {
                        Log.e("Err: ", msg);
                    }
                }).url(url)
                        .field("my_id",currentUserId)
                        .field("other_id",userId);
                myHttp.runTask();
            }).start();
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }


}