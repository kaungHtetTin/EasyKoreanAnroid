package com.calamus.easykorean.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.recyclerview.widget.RecyclerView;
import com.calamus.easykorean.R;
import com.calamus.easykorean.holders.CommentHolder;
import com.calamus.easykorean.holders.PostHolder;
import com.calamus.easykorean.models.CommentModel;
import com.calamus.easykorean.models.NewfeedModel;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;


public class CommentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Activity c;
    private final ArrayList<Object> data;  //contain only parent comment
    private final LayoutInflater mInflater;
    SharedPreferences sharedPreferences;
    String imagePath,checkTime,currentUserId,postOwnerId,userName;
    private Callback callback;

    public CommentAdapter(Activity c, ArrayList<Object> data,String checkTime,Callback callback) {
        this.data = data;
        this.c = c;
        this.checkTime=checkTime;
        this.mInflater = LayoutInflater.from(c);
        this.callback=callback;
        sharedPreferences=c.getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        imagePath=sharedPreferences.getString("imageUrl",null);
        currentUserId=sharedPreferences.getString("phone",null);
        userName=sharedPreferences.getString("Username",null);

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        if(viewType==0){
            View view = mInflater.inflate(R.layout.item_newfeed, parent, false);
            return new PostHolder(view,c,currentUserId,userName,imagePath);
        }else  {
            View view = mInflater.inflate(R.layout.item_comment, parent, false);
            return new CommentHolder(view, c);

        }
    }

    @Override
    public int getItemViewType(int position) {
        return position;

    }

    @Override
    public void onBindViewHolder(@NotNull final RecyclerView.ViewHolder holder, final int i) {


        if(i==0){
            try {
                PostHolder postHolder=(PostHolder) holder;
                NewfeedModel model=(NewfeedModel)data.get(i);
                postOwnerId=model.getUserId();
                postHolder.setNewsfeedModel(model);
                postHolder.setCallBack(new PostHolder.CallBack() {
                    @Override
                    public void onPostDelete() {
                        data.remove(holder.getAbsoluteAdapterPosition());
                        notifyDataSetChanged();
                    }
                });

            } catch (Exception e) {
                Toast.makeText(c, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }else {

            CommentModel cModel=(CommentModel)data.get(i);
            CommentHolder commentHolder=(CommentHolder) holder;
            commentHolder.setCommentModel(cModel,currentUserId,userName,postOwnerId,checkTime);
            commentHolder.setCallBack(new CommentHolder.CallBack() {
                @Override
                public void onCommentDelete(int position) {
                    data.remove(position);
                    notifyDataSetChanged();
                }

                @Override
                public void onReply() {
                    if (callback != null) {
                        String parentId;
                        if(!cModel.getParentId().equals("0")) parentId=cModel.getParentId();
                        else parentId=cModel.getTime();
                        callback.onReplyClick(parentId,cModel.getName(),cModel.getWriterId(),cModel.getWriterToken());
                    }
                }
            });
        }

    }

    public  interface Callback {
        void onReplyClick(String commentID,String name, String id, String token_to);
    }
}