package com.calamus.easykorean.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.calamus.easykorean.MyDiscussionActivity;
import com.calamus.easykorean.R;
import com.calamus.easykorean.app.MyHttp;
import com.calamus.easykorean.app.Routing;
import com.calamus.easykorean.models.FriendModel;
import com.facebook.shimmer.ShimmerFrameLayout;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.Executor;
import de.hdodenhof.circleimageview.CircleImageView;
import static com.calamus.easykorean.app.AppHandler.setMyanmar;
import static com.calamus.easykorean.app.AppHandler.setPhotoFromRealUrl;


public class FriendRequestAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Activity c;
    private final ArrayList<FriendModel> data;
    private final ArrayList<FriendModel> people;
    private final LayoutInflater mInflater;
    SharedPreferences sharedPreferences;
    String currentUserId;

    @SuppressLint("SimpleDateFormat")
    public FriendRequestAdapter(Activity c, ArrayList<FriendModel> data,ArrayList<FriendModel> people) {
        sharedPreferences=c.getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        this.data = data;
        this.c = c;
        this.people=people;
        this.mInflater = LayoutInflater.from(c);
        currentUserId=sharedPreferences.getString("phone","011");

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder( ViewGroup parent, int viewType) {

        if(viewType==0){
            View view = mInflater.inflate(R.layout.item_you_may_know_container, parent, false);
            return new PeopleHolder(view);
        }else{
            View view = mInflater.inflate(R.layout.item_friend_request, parent, false);

            return new Holder(view);
        }


    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int i) {
       if(i==0){
           PeopleHolder  peopleHolder=(PeopleHolder) holder;
           PeopleAdapter adapter=new PeopleAdapter(c,people);
           LinearLayoutManager lm = new LinearLayoutManager(c, LinearLayoutManager.HORIZONTAL, false);
           peopleHolder.recyclerView.setLayoutManager(lm);
           peopleHolder.recyclerView.setItemAnimator(new DefaultItemAnimator());
           peopleHolder.recyclerView.setAdapter(adapter);
           adapter.notifyDataSetChanged();

       }else{
           try{
               FriendModel model=data.get(i);
               Holder reqHolder=(Holder)holder;
               reqHolder.tv_name.setText(setMyanmar(model.getName()));
               reqHolder.bt_like.setVisibility(View.VISIBLE);
               reqHolder.bt_remove.setVisibility(View.VISIBLE);
               reqHolder.tv.setVisibility(View.GONE);

              setPhotoFromRealUrl(model.getImageUrl(),reqHolder.iv,reqHolder.container);

           }catch (Exception e){
               Log.e("Err: ",e.toString());
           }
       }
    }


    public class Holder extends RecyclerView.ViewHolder {

        final ShimmerFrameLayout container;
        final CircleImageView iv;
        final TextView tv_name;
        final Button bt_like;
        final Button bt_remove;
        final CardView cardView;
        TextView tv;
        Executor myExecutor;
        public Holder(View view) {
            super(view);
            myExecutor= ContextCompat.getMainExecutor(c);
            container=view.findViewById(R.id.shimmer_view_container);
            iv=view.findViewById(R.id.iv_profile);
            tv_name=view.findViewById(R.id.tv_name);
            bt_like=view.findViewById(R.id.bt_like);
            bt_remove=view.findViewById(R.id.bt_remove);
            cardView=view.findViewById(R.id.noti_card);
            tv=view.findViewById(R.id.tv_msg);

            view.setOnClickListener(v -> {
                cardView.setBackgroundColor(Color.WHITE);
                final FriendModel model=data.get(getAbsoluteAdapterPosition());
                Intent intent=new Intent(c, MyDiscussionActivity.class);
                intent.putExtra("userId",model.getPhone());
                intent.putExtra("userName",model.getName());
                c.startActivity(intent);
            });

            bt_like.setOnClickListener(v -> {
                final FriendModel model=data.get(getAbsoluteAdapterPosition());

                bt_like.setVisibility(View.GONE);
                bt_remove.setVisibility(View.GONE);
                tv.setVisibility(View.VISIBLE);
                friendShip(model.getPhone(),model.getName(),currentUserId, Routing.CONFIRM_FRIEND);

            });

            bt_remove.setOnClickListener(v -> {
                final FriendModel model=data.get(getAbsoluteAdapterPosition());

                friendShip(model.getPhone(),model.getName(),currentUserId,Routing.REMOVE_FRIEND_REQUEST);
                bt_remove.setVisibility(View.GONE);
                bt_like.setVisibility(View.GONE);
                tv.setVisibility(View.VISIBLE);
                tv.setText("Removed");

            });

        }

        private void friendShip(String userId,String username,String currentUserId,String url){
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

                                    if (code.equals("err53")){
                                        tv.setText("You are in maximum Friend Limit");
                                    }else if(code.equals("err54")){
                                        tv.setText(username+" is in maximum Friend Limit");
                                    }else{
                                        tv.setText("You are now friend");
                                    }

                                }catch (Exception e){
                                    tv.setText("You are now friend");
                                }

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




    public class PeopleHolder extends RecyclerView.ViewHolder {

        RecyclerView recyclerView;
        public PeopleHolder(@NonNull View itemView) {
            super(itemView);
            recyclerView=itemView.findViewById(R.id.recyclerYouMayKnow);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }
}