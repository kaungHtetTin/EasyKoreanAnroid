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
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.calamus.easykorean.MyDiscussionActivity;
import com.calamus.easykorean.R;
import com.calamus.easykorean.SearchingActivity;
import com.calamus.easykorean.app.AppHandler;
import com.calamus.easykorean.app.Routing;
import com.calamus.easykorean.models.FriendModel;
import com.calamus.easykorean.app.MyHttp;
import java.util.ArrayList;
import java.util.concurrent.Executor;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.calamus.easykorean.app.AppHandler.setMyanmar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class FriendAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

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
    public RecyclerView.ViewHolder onCreateViewHolder( ViewGroup parent, int viewType) {

        if(viewType==0){
            View view=mInflater.inflate(R.layout.item_search_bar,parent,false);
            return new SearchBoxHolder(view);
        }else if(viewType==1){
            View view = mInflater.inflate(R.layout.item_title_chat, parent, false);
            return new TitleHolder(view);
        }else{
            View view = mInflater.inflate(R.layout.item_friend, parent, false);
            return new Holder(view);
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, final int i) {
        if(i==0){

        }else if(i==1){
            TitleHolder holder=(TitleHolder) viewHolder;
            int friendCount=data.size()-2;
            holder.tv_count.setText(friendCount+"");
            if(friendCount==0){
                holder.tv_title.setText("No friend");
                holder.tv_count.setText("");
            }else if(friendCount==1){
                holder.tv_title.setText("Friend");
            }else{
                holder.tv_title.setText("Friends");
            }
        }else{
//            try{
            Holder holder=(Holder)viewHolder;
            FriendModel model=data.get(i);
            holder.tv_mutual.setText(calculateMutualCount(model.getFriendsJson()));
            holder.tv_name.setText(setMyanmar(model.getName()));
            AppHandler.setPhotoFromRealUrl(holder.iv,model.getImageUrl());

//            }catch (Exception e){
//
//            }
        }
    }


    public class Holder extends RecyclerView.ViewHolder {

        final CircleImageView iv;
        final TextView tv_name,tv_mutual;


        Executor myExecutor;
        public Holder(View view) {
            super(view);
            myExecutor= ContextCompat.getMainExecutor(c);

            iv=view.findViewById(R.id.iv_profile);
            tv_name=view.findViewById(R.id.tv_username2);
            tv_mutual=view.findViewById(R.id.tv_mutual);

            view.setOnClickListener(v -> {
                final FriendModel model=data.get(getAbsoluteAdapterPosition());
                Intent intent=new Intent(c, MyDiscussionActivity.class);
                intent.putExtra("userId",model.getPhone());
                intent.putExtra("userName",model.getName());
                c.startActivity(intent);
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
                        .field("major", Routing.MAJOR);
                myHttp.runTask();
            }).start();
        }

    }

    private String calculateMutualCount(String friendsJson){
        int result=0;
        try {
            JSONArray ja=new JSONArray(friendsJson);
            for(int i=0;i<ja.length();i++){
                JSONObject jo=ja.getJSONObject(i);
                String friId=jo.getString("fri_id");
                for(int j=2;j<data.size();j++){   // j starting with 2 because index 0 is search bar & index 1 is title
                    FriendModel model=data.get(j);
                    if(model.getPhone().equals(friId)){
                        result++;
                    }
                }
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(result==0){
            return "No mutual friend";
        }else if(result==1){
            return "1 mutual friend";
        }else{
            return result+" mutual friends";
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }


    public class SearchBoxHolder extends RecyclerView.ViewHolder{

        public SearchBoxHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    c.startActivity(new Intent(c, SearchingActivity.class));
                }
            });
        }
    }

    public class TitleHolder extends RecyclerView.ViewHolder{

        TextView tv_title,tv_count;

        public TitleHolder(@NonNull View itemView) {
            super(itemView);
            tv_title=itemView.findViewById(R.id.tv_item_title);
            tv_count=itemView.findViewById(R.id.tv_count);
        }
    }
}