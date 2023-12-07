package com.calamus.easykorean.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView;
import com.calamus.easykorean.R;
import com.calamus.easykorean.holders.PostHolder;
import com.calamus.easykorean.holders.ProfileHolders;
import com.calamus.easykorean.models.NewfeedModel;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;

public  class DiscussAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Activity c;
    public static ArrayList<Object> data;
    private final LayoutInflater mInflater;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    String imagePath,currentUserId,userName;
    boolean notiRedMark;
    String dbdir;
    String dbPath;
    SQLiteDatabase db;
    String otherId;
    ProfileHolders profileHolder;

    public DiscussAdapter(Activity c,ProfileHolders profileHolder ,ArrayList<Object> data,String otherId) {
        this.data = data;
        this.c = c;
        this.mInflater = LayoutInflater.from(c);
        this.profileHolder=profileHolder;
        sharedPreferences=c.getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        imagePath=sharedPreferences.getString("imageUrl",null);
        notiRedMark=sharedPreferences.getBoolean("notiRedMark",false);
        currentUserId=sharedPreferences.getString("phone",null);
        userName=sharedPreferences.getString("Username",null);
        editor=sharedPreferences.edit();

        this.otherId=otherId;

        dbdir=c.getFilesDir().getPath()+"/databases/";
        dbPath=dbdir+"post.db";
        db=SQLiteDatabase.openDatabase(dbPath,null,SQLiteDatabase.OPEN_READWRITE);

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {

        if(viewType==0){
            return  profileHolder;
        }else{
            View view = mInflater.inflate(R.layout.item_newfeed, parent, false);
            return new PostHolder(view,c,currentUserId,userName,imagePath);
        }
    }

    @Override
    public int getItemViewType(int position) {

        if(position==0){
            return 0;
        }else {
            return 1;
        }

    }


    @Override
    public void onBindViewHolder(@NotNull final RecyclerView.ViewHolder holder, final int i) {

        if(i !=0){
            PostHolder postHolder=(PostHolder) holder;
            postHolder.setNewsfeedModel((NewfeedModel) data.get(i));
            postHolder.setCallBack(new PostHolder.CallBack() {
                @Override
                public void onPostDelete() {
                    data.remove(holder.getAbsoluteAdapterPosition());
                    notifyDataSetChanged();
                }
            });
        }

    }

}