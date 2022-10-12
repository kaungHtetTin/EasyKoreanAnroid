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
import com.calamus.easykorean.models.CommentModel;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import me.myatminsoe.mdetect.MDetect;



public class VideoCommentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Activity c;
    private final ArrayList<Object> data; //contain only parent comment
    private final LayoutInflater mInflater;

    SharedPreferences sharedPreferences;
    String imagePath,checkTime,currentUserId,userName;
    private Callback callback;

    public VideoCommentAdapter(Activity c, ArrayList<Object>data,String checkTime,Callback callback) {
        this.data = data;
        this.c = c;
        this.checkTime=checkTime;
        this.mInflater = LayoutInflater.from(c);
        this.callback=callback;
        MDetect.INSTANCE.init(c);
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

        View view = mInflater.inflate(R.layout.item_comment, parent, false);
        return new CommentHolder(view,c);
    }

    @Override
    public void onBindViewHolder(@NotNull final RecyclerView.ViewHolder holder, final int i) {

        try{
            CommentModel cModel=(CommentModel)data.get(i);
            CommentHolder commentHolder=(CommentHolder) holder;
            commentHolder.setCommentModel(cModel,currentUserId,userName,"10000",checkTime);
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

        }catch (Exception e){
            Toast.makeText(c,e.toString(),Toast.LENGTH_SHORT).show();
        }

    }


    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public  interface Callback {
        void onReplyClick(String parentCommentID,String name, String id, String token_to);

    }

}