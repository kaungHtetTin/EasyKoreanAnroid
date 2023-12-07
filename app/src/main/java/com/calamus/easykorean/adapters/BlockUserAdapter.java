package com.calamus.easykorean.adapters;

import static com.calamus.easykorean.app.AppHandler.setMyanmar;
import static com.calamus.easykorean.app.AppHandler.setPhotoFromRealUrl;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.calamus.easykorean.R;
import com.calamus.easykorean.app.MyDialog;
import com.calamus.easykorean.app.MyHttp;
import com.calamus.easykorean.app.Routing;
import com.calamus.easykorean.models.LikeListModel;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.concurrent.Executor;

public class BlockUserAdapter extends RecyclerView.Adapter<BlockUserAdapter.Holder>{

    private final Activity c;
    private final ArrayList<LikeListModel> data;
    private final LayoutInflater mInflater;
    Executor postExecutor;
    String currentUserId;

    public BlockUserAdapter(Activity c, ArrayList<LikeListModel> data,String currentUserId) {
        this.data = data;
        this.currentUserId=currentUserId;
        this.c = c;
        this.mInflater = LayoutInflater.from(c);
        postExecutor= ContextCompat.getMainExecutor(c);

    }


    @Override
    public int getItemCount() {
        return data.size();
    }

    @NotNull
    @Override
    public BlockUserAdapter.Holder onCreateViewHolder(@NotNull ViewGroup parent, int p2) {
        View view = mInflater.inflate(R.layout.item_blocked_user, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NotNull final BlockUserAdapter.Holder holder, final int i) {
        try {
            LikeListModel model=data.get(i);
            holder.tv_name.setText(setMyanmar(model.getUserName()));
            holder.iv_isVip.setVisibility(View.GONE);
            if(model.getIsVip().equals("1"))holder.iv_isVip.setVisibility(View.VISIBLE);

            setPhotoFromRealUrl(holder.iv_profile,model.getImageUrl());



        } catch (Exception e) {
            Toast.makeText(c, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    public class Holder extends RecyclerView.ViewHolder {

        ImageView iv_profile,iv_isVip;
        TextView tv_name;

        public Holder(final View view) {
            super(view);
            iv_profile=view.findViewById(R.id.iv_profile);
            iv_isVip=view.findViewById(R.id.iv_blueMark);
            tv_name=view.findViewById(R.id.tv_name);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LikeListModel model=data.get(getAbsoluteAdapterPosition());
                    String blocked_user_id= model.getUserId();
                    showUnBlockDialog(blocked_user_id,getAbsoluteAdapterPosition());
                }
            });
        }

        private void showUnBlockDialog(String block_user_id,int position){
            new MyDialog(c, "UNBLOCK!", "Do you really want to unblock this user!", new MyDialog.ConfirmClick() {
                @Override
                public void onConfirmClick() {
                    data.remove(position);
                    notifyDataSetChanged();
                    unblock(currentUserId,block_user_id);
                }
            }).showMyDialog();
        }

        private void unblock(String user_id,String blocked_user_id){
            new Thread(() -> {
                MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.POST, new MyHttp.Response() {
                    @Override
                    public void onResponse(String response) {
                        postExecutor.execute(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(c,response,Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                    @Override
                    public void onError(String msg) {
                        Log.e("Post hide err ",msg);
                    }
                }).url(Routing.UNBLOCK_USER)
                        .field("blocked_user_id",blocked_user_id)
                        .field("user_id",user_id);
                myHttp.runTask();
            }).start();
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

}