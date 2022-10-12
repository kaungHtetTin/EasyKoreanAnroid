package com.calamus.easykorean.adapters;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.calamus.easykorean.CommentActivity;
import com.calamus.easykorean.MyDiscussionActivity;
import com.calamus.easykorean.R;
import com.calamus.easykorean.VimeoPlayerActivity;
import com.calamus.easykorean.app.AppHandler;
import com.calamus.easykorean.models.FriendModel;
import com.calamus.easykorean.models.SaveModel;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;


public class SearchAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Activity c;
    private final ArrayList<Object> data;
    private final LayoutInflater mInflater;
    SharedPreferences sharedPreferences;
    String currentUserId;

    @SuppressLint("SimpleDateFormat")
    public SearchAdapter(Activity c, ArrayList<Object> data) {
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
    public RecyclerView.ViewHolder onCreateViewHolder( ViewGroup parent, int viewType) {

        if(viewType==0){
            View view = mInflater.inflate(R.layout.item_search_people, parent, false);
            return new PeopleHolder(view);
        }else{
            View view = mInflater.inflate(R.layout.item_save, parent, false);

            return new PostHolder(view);
        }


    }

    @Override
    public int getItemViewType(int position) {

        if(data.get(position) instanceof FriendModel){
            return 0;
        }else{
            return 1;
        }

    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int i) {
        if(data.get(i) instanceof FriendModel){
            PeopleHolder peopleHolder=(PeopleHolder)holder;
            FriendModel model=(FriendModel)data.get(i);
            peopleHolder.tv_name.setText(model.getName());
            AppHandler.setPhotoFromRealUrl(peopleHolder.iv,model.getImageUrl());
        }else{
            PostHolder postHolder=(PostHolder)holder;
            SaveModel saveModel=(SaveModel)data.get(i);

            if(saveModel.getPost_body().length()>90){
                String sub=saveModel.getPost_body().substring(0,90);
                postHolder.tv_body.setText(sub+"...");
            }else {
                postHolder.tv_body.setText(saveModel.getPost_body());
            }

            postHolder.tv_postOwner.setText(saveModel.getOwner_name());

            if(!saveModel.getPost_image().equals("")){
                AppHandler.setPhotoFromRealUrl(postHolder.iv_save,saveModel.getPost_image());
            }else if(!saveModel.getOwner_image().equals("")){
                AppHandler.setPhotoFromRealUrl(postHolder.iv_save,saveModel.getOwner_image());
            }
        }
    }


    public class PostHolder extends RecyclerView.ViewHolder {

        TextView tv_body,tv_postOwner;
        ImageView iv_save;
        ImageButton iv_more;

        public PostHolder(View view) {
            super(view);

            iv_save=view.findViewById(R.id.iv_save);
            tv_body=view.findViewById(R.id.tv_body);
            tv_postOwner=view.findViewById(R.id.tv_postOwner);
            iv_more=view.findViewById(R.id.bt_iv_more);
            iv_save.setClipToOutline(true);
            iv_more.setVisibility(View.GONE);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SaveModel model=(SaveModel) data.get(getAbsoluteAdapterPosition());
                    go(model);

                }
            });

        }

        private void go(SaveModel model){
            if(model.getIsVideo().equals("1")){
                String videoId=model.getPost_image().substring(model.getPost_image().indexOf("vi/")+3);
                videoId=videoId.substring(0,videoId.length()-6);
                Intent intent=new Intent(c, VimeoPlayerActivity.class);
                intent.putExtra("videoTitle","");
                intent.putExtra("videoId",videoId);
                intent.putExtra("time",Long.parseLong(model.getPost_id()));
                c.startActivity(intent);

            }else {
                Intent intent=new Intent(c, CommentActivity.class);
                intent.putExtra("postId",model.getPost_id());
                intent.putExtra("time","");//for comment seen
                c.startActivity(intent);
            }
        }



    }


    public class PeopleHolder extends RecyclerView.ViewHolder {

        final CircleImageView iv;
        final TextView tv_name;

        public PeopleHolder(@NonNull View itemView) {
            super(itemView);
            iv=itemView.findViewById(R.id.iv_profile);
            tv_name=itemView.findViewById(R.id.tv_username);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final FriendModel model=(FriendModel) data.get(getAbsoluteAdapterPosition());
                    Intent intent=new Intent(c, MyDiscussionActivity.class);
                    intent.putExtra("userId",model.getPhone());
                    intent.putExtra("userName",model.getName());
                    c.startActivity(intent);
                }
            });

        }
    }

}