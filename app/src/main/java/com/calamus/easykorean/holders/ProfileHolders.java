package com.calamus.easykorean.holders;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.calamus.easykorean.ChattingActivity;
import com.calamus.easykorean.EditProfileActivity;
import com.calamus.easykorean.R;
import com.calamus.easykorean.UpdateBioActivity;
import com.calamus.easykorean.adapters.CourseFinishAdapter;
import com.calamus.easykorean.app.MyDialog;
import com.calamus.easykorean.app.Routing;
import com.calamus.easykorean.interfaces.OnProfileLoaded;
import com.calamus.easykorean.models.FinishCourseModel;
import com.calamus.easykorean.app.MyHttp;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.Executor;

public class ProfileHolders extends RecyclerView.ViewHolder {

    CardView card_add_friend,card_confirm,card_requested,card_friend,card_message;
    TextView tv_age,tv_education,tv_work,tv_address,tv_editProfile,tv_limit,
            tv_name,tv_bio ;
    LinearLayout layout_live_in,layout_born_in,layout_education,layout_work_at,layout_completed_course;

    RecyclerView lv;
    String username,bio,imageUrl;
    Activity c;
    Executor postExecutor;
    String currentUserId,userId;
    OnProfileLoaded callBack;

    public ProfileHolders(@NonNull View v, Activity c, String currentUserId, String userId, OnProfileLoaded callBack) {
        super(v);

        this.postExecutor= ContextCompat.getMainExecutor(c);
        this.currentUserId=currentUserId;
        this.userId=userId;
        this.c=c;
        this.callBack=callBack;

        tv_name=v.findViewById(R.id.tv_name);
        tv_bio=v.findViewById(R.id.et_bio);
        tv_age=v.findViewById(R.id.tv_age);
        tv_education=v.findViewById(R.id.tv_education);
        tv_work=v.findViewById(R.id.tv_work_at);
        tv_address=v.findViewById(R.id.tv_live_in);
        card_add_friend=v.findViewById(R.id.card_add_friend);
        card_confirm=v.findViewById(R.id.card_confirm);
        card_requested=v.findViewById(R.id.card_requested);
        card_friend=v.findViewById(R.id.card_friend);
        card_message=v.findViewById(R.id.card_message);
        tv_editProfile=v.findViewById(R.id.tv_editProfile);
        tv_limit=v.findViewById(R.id.tv_limit);
        lv=v.findViewById(R.id.lv);
        layout_completed_course=v.findViewById(R.id.layout_completed_courses);
        layout_work_at=v.findViewById(R.id.layout_work_at);
        layout_born_in=v.findViewById(R.id.layout_born_in);
        layout_education=v.findViewById(R.id.layout_out_education);
        layout_live_in=v.findViewById(R.id.layout_live_in);

        getUserData();

        tv_editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(c, EditProfileActivity.class);
                c.startActivity(intent);
            }
        });

        card_add_friend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                friendShip(userId, Routing.ADD_FRIEND,card_confirm);
                card_add_friend.setVisibility(View.GONE);
                card_requested.setVisibility(View.VISIBLE);

            }
        });

        card_requested.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                friendShip(userId,Routing.ADD_FRIEND,card_add_friend);
                card_requested.setVisibility(View.GONE);
                card_add_friend.setVisibility(View.VISIBLE);
            }
        });

        card_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                card_confirm.setVisibility(View.GONE);
                card_friend.setVisibility(View.VISIBLE);
                card_message.setVisibility(View.VISIBLE);
                friendShip(userId,Routing.CONFIRM_FRIEND,card_friend);

            }
        });

        card_friend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyDialog myDialog=new MyDialog(c, "Remove Friend!", "Do you really want to remove this user from your friend list?", () -> {
                    card_friend.setVisibility(View.GONE);
                    card_add_friend.setVisibility(View.VISIBLE);
                    friendShip(userId,Routing.UN_FRIEND,card_add_friend);
                });
                myDialog.showMyDialog();

            }
        });

        tv_bio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(c, UpdateBioActivity.class);
                intent.putExtra("bio",bio);
                intent.putExtra("userId",userId);
                intent.putExtra("profileUrl",imageUrl);
                intent.putExtra("username",username);
                c.startActivity(intent);
            }
        });
    }

    private void getUserData(){

        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.GET, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    postExecutor.execute(new Runnable() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void run() {
                            setUpProfile(response);
                        }
                    });

                }
                @Override
                public void onError(String msg) {}
            }).url(Routing.GET_PROFILE+"/"+currentUserId+"/"+userId);

            myHttp.runTask();
        }).start();
    }

    private void setUpProfile(String response){

        ArrayList<FinishCourseModel> courseLists=new ArrayList<>();
        CourseFinishAdapter myAdapter=new CourseFinishAdapter(courseLists,c);

        LinearLayoutManager lm=new LinearLayoutManager(c){};
        lv.setLayoutManager(lm);
        lv.setAdapter(myAdapter);

        try {
            JSONObject joMain=new JSONObject(response);
            JSONObject joProfile=joMain.getJSONObject("profile");
            username=joProfile.getString("learner_name");
            String birthday=joProfile.getString("bd_year");
            String education=joProfile.getString("education");
            String work=joProfile.getString("work");
            String region=joProfile.getString("region");
            imageUrl=joProfile.getString("learner_image");
            String cover_image=joProfile.getString("cover_image");
            bio=joProfile.getString("bio");

            boolean vip=joMain.getString("is_vip").equals("1");
            String token=joMain.getString("token");
            JSONArray ja=joMain.getJSONArray("enroll");

            callBack.onLoaded(cover_image,imageUrl,bio,vip);

            for(int i=0;i<ja.length();i++){
                JSONObject jo=ja.getJSONObject(i);
                String title=jo.getString("title");
                int learned=Integer.parseInt(jo.getString("learned"));
                int total=Integer.parseInt(jo.getString("total"));

                if(learned==total){
                    layout_completed_course.setVisibility(View.VISIBLE);
                    courseLists.add(new FinishCourseModel(title,learned,total));
                }
            }



            if(bio.equals("")){
                if(userId.equals(currentUserId)){
                    tv_bio.setText("Add your bio");
                    tv_bio.setTextColor(c.getResources().getColor(R.color.colorTheme));
                }else{
                    tv_bio.setVisibility(View.GONE);
                }
            }else{
                tv_bio.setText(bio);
            }



            myAdapter.notifyDataSetChanged();
            tv_name.setText(username);



            if (birthday.equals("")){
                layout_born_in.setVisibility(View.GONE);
            }
            else {
                tv_age.setText("Born in " + birthday);
            }

            if(education.equals("")){
                layout_education.setVisibility(View.GONE);
            }
            else{
                tv_education.setText(education);
            }

            if(work.equals("")){
                layout_work_at.setVisibility(View.GONE);
            }
            else{
                tv_work.setText("Work at "+work);
            }

            if(region.equals("")){
                layout_live_in.setVisibility(View.GONE);
            }
            else {
                tv_address.setText("Live in "+region);
            }

            String friendship=joMain.getString("friendship");
            if(friendship.equals("request")){
                card_add_friend.setVisibility(View.GONE);
                card_requested.setVisibility(View.VISIBLE);
            }

            if(friendship.equals("confirm")){
                card_add_friend.setVisibility(View.GONE);
                card_confirm.setVisibility(View.VISIBLE);
            }

            if(friendship.equals("friend")){
                card_add_friend.setVisibility(View.GONE);
                card_message.setVisibility(View.VISIBLE);
                card_friend.setVisibility(View.VISIBLE);
            }

            if(friendship.equals("me")){
                card_add_friend.setVisibility(View.GONE);
                tv_editProfile.setVisibility(View.VISIBLE);
            }

            if(friendship.equals("reqLimit")){
                card_add_friend.setVisibility(View.GONE);
                tv_limit.setVisibility(View.VISIBLE);
                tv_limit.setText(username+" is in maximum Request Limit");
            }

            if(friendship.equals("friLimit")){
                card_add_friend.setVisibility(View.GONE);
                tv_limit.setVisibility(View.VISIBLE);
                tv_limit.setText(username+" is in maximum Friend Limit");
            }

            card_message.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent(c, ChattingActivity.class);
                    intent.putExtra("fId",userId);
                    intent.putExtra("fImage",imageUrl);
                    intent.putExtra("fName",username);
                    intent.putExtra("token",token);
                    c.startActivity(intent);
                }
            });

        }catch (Exception ignored){}
    }

    private void friendShip(String userId,String url,CardView cv){
        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.POST, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    postExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            Log.e("RES: ",response);
                            try{
                                JSONObject jo=new JSONObject(response);
                                String code=jo.getString("code");

                                if (code.equals("err53")){
                                    cv.setVisibility(View.GONE);
                                    tv_limit.setVisibility(View.VISIBLE);
                                    tv_limit.setText("You are in maximum Friend Limit");
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
