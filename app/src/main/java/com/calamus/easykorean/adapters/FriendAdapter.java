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
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.calamus.easykorean.ChattingActivity;
import com.calamus.easykorean.MyDiscussionActivity;
import com.calamus.easykorean.R;
import com.calamus.easykorean.app.MyDialog;
import com.calamus.easykorean.app.MyHttp;
import com.calamus.easykorean.app.Routing;
import com.calamus.easykorean.models.FriendModel;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import de.hdodenhof.circleimageview.CircleImageView;
import static com.calamus.easykorean.app.AppHandler.setMyanmar;

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.Holder> {

    private final Activity c;
    private final ArrayList<FriendModel> data;
    private final LayoutInflater mInflater;
    SharedPreferences sharedPreferences;
    String currentUserId;

    @SuppressLint("SimpleDateFormat")
    public FriendAdapter(Activity c, ArrayList<FriendModel> data) {
        sharedPreferences=c.getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        this.data = data;
        this.c = c;
        this.mInflater = LayoutInflater.from(c);
        currentUserId=sharedPreferences.getString("phone","011");

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public FriendAdapter.Holder onCreateViewHolder( ViewGroup parent, int viewType) {

        View view = mInflater.inflate(R.layout.item_friend, parent, false);

        return new Holder(view);

    }

    @Override
    public void onBindViewHolder(final FriendAdapter.Holder holder, final int i) {
        try{
            FriendModel model=data.get(i);
            holder.tv_name.setText(setMyanmar(model.getName()));
            holder.bt_remove.setTextColor(Color.WHITE);
            Picasso.get()
                    .load(model.getImageUrl())
                    .centerInside()
                    .fit()
                    .error(R.drawable.ic_baseline_account_circle_24)
                    .into(holder.iv, new Callback() {
                        @Override
                        public void onSuccess() {
                            holder.container.stopShimmer();
                            holder.container.hideShimmer();
                        }

                        @Override
                        public void onError(Exception e) {
                            holder.container.stopShimmer();
                            holder.container.hideShimmer();
                        }
                    });



        }catch (Exception e){

        }
    }


    public class Holder extends RecyclerView.ViewHolder {
        final ShimmerFrameLayout container;
        final CircleImageView iv;
        final TextView tv_name;
        final Button bt_remove;
        Button bt_chat;

        Executor myExecutor;
        public Holder(View view) {
            super(view);
            myExecutor= ContextCompat.getMainExecutor(c);
            container=view.findViewById(R.id.shimmer_view_container);
            iv=view.findViewById(R.id.iv_profile);
            tv_name=view.findViewById(R.id.tv_name);
            bt_remove=view.findViewById(R.id.bt_remove);
            bt_chat=view.findViewById(R.id.bt_chat);

            view.setOnClickListener(v -> {
                final FriendModel model=data.get(getAbsoluteAdapterPosition());
                Intent intent=new Intent(c, MyDiscussionActivity.class);
                intent.putExtra("userId",model.getPhone());
                intent.putExtra("userName",model.getName());
                c.startActivity(intent);
            });

            bt_remove.setOnClickListener(v -> {
                final FriendModel model=data.get(getAbsoluteAdapterPosition());
                MyDialog myDialog=new MyDialog(c, "Remove Friend!", "Do you really want to remove this user from your friend list?", () -> {
                    friendShip(model.getPhone(),currentUserId,Routing.UN_FRIEND);
                    data.remove(getAbsoluteAdapterPosition());
                    notifyDataSetChanged();
                });
                myDialog.showMyDialog();

            });


            bt_chat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final FriendModel model=data.get(getAbsoluteAdapterPosition());
                    Intent intent=new Intent(c, ChattingActivity.class);
                    intent.putExtra("fId",model.getPhone());
                    intent.putExtra("fImage",model.getImageUrl());
                    intent.putExtra("fName",model.getName());
                    intent.putExtra("token",model.getToken());
                    c.startActivity(intent);
                }
            });

        }

        private void friendShip(String userId,String currentUserId,String url){
            new Thread(() -> {
                MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.POST, new MyHttp.Response() {
                    @Override
                    public void onResponse(String response) {
                        myExecutor.execute(new Runnable() {
                            @Override
                            public void run() {
                                Log.e("Res: ",response);
                            }
                        });
                    }
                    @Override
                    public void onError(String msg) {
                        Log.e("Err: ", msg);
                    }
                }).url(url)
                        .field("my_id",currentUserId)
                        .field("other_id",userId)
                        .field("major","korea");
                myHttp.runTask();
            }).start();
        }

    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }


}