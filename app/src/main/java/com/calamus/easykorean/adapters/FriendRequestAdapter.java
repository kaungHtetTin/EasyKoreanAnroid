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
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.calamus.easykorean.MyDiscussionActivity;
import com.calamus.easykorean.R;
import com.calamus.easykorean.SearchingActivity;
import com.calamus.easykorean.app.AppHandler;
import com.calamus.easykorean.app.Routing;
import com.calamus.easykorean.fragments.ChatTwo;
import com.calamus.easykorean.models.FriendModel;
import com.calamus.easykorean.models.NewStudentModel;
import com.calamus.easykorean.models.StudentModel;
import com.calamus.easykorean.app.MyHttp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.Executor;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.calamus.easykorean.app.AppHandler.setMyanmar;
import static com.calamus.easykorean.app.AppHandler.setPhotoFromRealUrl;


public class FriendRequestAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Activity c;
    private final ArrayList<Object> data;

    private final LayoutInflater mInflater;
    SharedPreferences sharedPreferences;
    String currentUserId;
    int friendRequestCount=0;

    @SuppressLint("SimpleDateFormat")
    public FriendRequestAdapter(Activity c, ArrayList<Object> data) {
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
        }else if(viewType==2){
            View view = mInflater.inflate(R.layout.item_friend_request, parent, false);
            return new Holder(view);
        }else if(viewType==3){
            View view=mInflater.inflate(R.layout.item_you_may_know,parent,false);
            return new PeopleHolder(view);
        }else{
            View view=mInflater.inflate(R.layout.item_see_all_request,parent,false);
            return new SeeMoreHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {

        if(data.get(position) instanceof ChatTwo.SearchBox){
            return 0;
        }else if(data.get(position) instanceof String){
            return 1;
        }else if(data.get(position) instanceof FriendModel){
            return 2;
        }else if(data.get(position) instanceof NewStudentModel){
            return 3;
        }else{
            return 4;
        }

    }


    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {

        if(data.get(position) instanceof ChatTwo.SearchBox){

        }else if(data.get(position) instanceof String){
            TitleHolder titleHolder=(TitleHolder) holder;
            String title=(String)data.get(position);
            titleHolder.tv_title.setText(title);
            if(title.equals("Friend Requests")){
                titleHolder.tv_count.setTextColor(Color.parseColor("#FF5157"));
                titleHolder.tv_count.setText(friendRequestCount+"");
            }else{
                titleHolder.tv_count.setVisibility(View.GONE);
            }

        }else if(data.get(position) instanceof FriendModel){
            FriendModel model=(FriendModel) data.get(position);
            Holder reqHolder=(Holder)holder;
            reqHolder.tv_name.setText(setMyanmar(model.getName()));
            reqHolder.tv_confirm.setVisibility(View.VISIBLE);
            reqHolder.tv_delete.setVisibility(View.VISIBLE);
            reqHolder.tv.setVisibility(View.GONE);
            reqHolder.tv_mutual_count.setText(calculateMutualCount(model.getFriendsJson()));
            setPhotoFromRealUrl(reqHolder.iv, model.getImageUrl());
        }else if(data.get(position) instanceof NewStudentModel){
            NewStudentModel model=(NewStudentModel) data.get(position);
            PeopleHolder peopleHolder=(PeopleHolder)holder;
            peopleHolder.tv_name.setText(model.getName());
            peopleHolder.tv_mutual_count.setText(calculateMutualCount(model.getFriendsJson()));
            AppHandler.setPhotoFromRealUrl(peopleHolder.iv,model.getImageUrl());
        }else{

        }
    }


    public class Holder extends RecyclerView.ViewHolder {


        final CircleImageView iv;
        final TextView tv_name,tv_confirm,tv_delete,tv_mutual_count;

        TextView tv;
        Executor myExecutor;
        public Holder(View view) {
            super(view);
            myExecutor= ContextCompat.getMainExecutor(c);

            iv=view.findViewById(R.id.iv_profile);
            tv_name=view.findViewById(R.id.tv_username2);
            tv_confirm=view.findViewById(R.id.tv_confirm);
            tv_delete=view.findViewById(R.id.tv_delete);
            tv_mutual_count=view.findViewById(R.id.tv_mutual_count);

            tv=view.findViewById(R.id.tv_msg);

            view.setOnClickListener(v -> {

                final FriendModel model=(FriendModel) data.get(getAbsoluteAdapterPosition());
                Intent intent=new Intent(c, MyDiscussionActivity.class);
                intent.putExtra("userId",model.getPhone());
                intent.putExtra("userName",model.getName());
                c.startActivity(intent);
            });

            tv_confirm.setOnClickListener(v -> {
                final FriendModel model=(FriendModel) data.get(getAbsoluteAdapterPosition());

                tv_confirm.setVisibility(View.GONE);
                tv_delete.setVisibility(View.GONE);
                tv.setVisibility(View.VISIBLE);
                friendShip(model.getPhone(),model.getName(),currentUserId, Routing.CONFIRM_FRIEND);

            });

            tv_delete.setOnClickListener(v -> {
                final FriendModel model=(FriendModel) data.get(getAbsoluteAdapterPosition());

                friendShip(model.getPhone(),model.getName(),currentUserId,Routing.REMOVE_FRIEND_REQUEST);
                tv_delete.setVisibility(View.GONE);
                tv_confirm.setVisibility(View.GONE);
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
                        .field("other_id",userId)
                        .field("major","english");
                myHttp.runTask();
            }).start();
        }

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


    public class PeopleHolder extends RecyclerView.ViewHolder {

        ImageView iv;
        TextView tv_name,tv_mutual_count,tv_add,tv_delete;


        Executor myExecutor;

        public PeopleHolder(@NonNull View view) {
            super(view);
            myExecutor= ContextCompat.getMainExecutor(c);
            iv=view.findViewById(R.id.iv_profile);
            tv_name=view.findViewById(R.id.tv_username2);
            tv_mutual_count=view.findViewById(R.id.tv_mutual_count);
            tv_add=view.findViewById(R.id.tv_confirm);
            tv_delete=view.findViewById(R.id.tv_delete);


            iv.setClipToOutline(true);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    NewStudentModel model=(NewStudentModel) data.get(getAbsoluteAdapterPosition());
                    Intent intent=new Intent(c, MyDiscussionActivity.class);
                    intent.putExtra("userId",model.getPhone());
                    intent.putExtra("userName",model.getName());
                    c.startActivity(intent);
                }
            });

            tv_add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    tv_add.setVisibility(View.GONE);
                    NewStudentModel model=(NewStudentModel) data.get(getAbsoluteAdapterPosition());
                    friendShip(model.getPhone(), Routing.ADD_FRIEND);
                }
            });

            tv_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    data.remove(getAbsoluteAdapterPosition());
                    notifyDataSetChanged();
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


                                }catch (Exception e){}
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
                        .field("major","english");
                myHttp.runTask();
            }).start();
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

    public class SeeMoreHolder extends RecyclerView.ViewHolder{

        public SeeMoreHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ChatTwo.SeeMore seeMore=(ChatTwo.SeeMore) data.get(getAbsoluteAdapterPosition());
                    seeMore.getClick().onClick();
                }
            });
        }
    }


    private String calculateMutualCount(String friendsJson){
        int result=0;
        try {
            JSONArray ja=new JSONArray(friendsJson);
            for(int i=0;i<ja.length();i++){
                JSONObject jo=ja.getJSONObject(i);
                String friId=jo.getString("fri_id");
                for(int j=0;j<data.size();j++){
                    if(data.get(j) instanceof StudentModel){
                        StudentModel model=(StudentModel) data.get(j);
                        if(model.getPhone().equals(friId)){
                            result++;
                        }
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


    public void setFriendRequestCount(int friendRequestCount) {
        this.friendRequestCount = friendRequestCount;
    }
}