package com.calamus.easykorean.app;


/*
This is used for getting user information from server
and save the information in the local storage;
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;
import androidx.core.content.ContextCompat;

import com.calamus.easykorean.SettingActivity;
import com.calamus.easykorean.SplashScreenActivity;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.concurrent.Executor;

public class UserInformation {
    Activity c;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    String myToken;
    Executor postExecutor;
    public UserInformation(Activity c) {
        this.c = c;
        sharedPreferences=c.getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        myToken=sharedPreferences.getString("token","abcdef");
        editor=sharedPreferences.edit();
        postExecutor= ContextCompat.getMainExecutor(c);
    }

    public void getGeneralData(String phone,String auth_token){

        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.POST, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    postExecutor.execute(() -> doAsResult(response));
                }
                @Override
                public void onError(String msg) {
                    postExecutor.execute(() -> {

                    });
                }
            }).url(Routing.GET_LOGIN_USERDATA)
                    .field("phone",phone)
                    .field("token",myToken)
                    .field("auth_token",auth_token);

            myHttp.runTask();
        }).start();

    }

    public void doAsResult(String response){

        Log.e("login response ",response);
        try {

            JSONObject jsonObject=new JSONObject(response);
            boolean auth=jsonObject.getBoolean("auth");
            if(!auth) {
                editor.putBoolean("AlreadyLogin",false);
                editor.apply();
                SharedPreferences sharedPreferences1 = c.getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor1 = sharedPreferences1.edit();
                editor1.clear();
                editor1.apply();

                Intent intent = new Intent(c, SplashScreenActivity.class);
                c.startActivity(intent);
                c.finish();

                return;
            }

            String userJson=jsonObject.getString("user");
            JSONObject user=new JSONObject(userJson);
            String email=user.getString("email");
            String imageUrl=user.getString("imageUrl");
            String gameScore=user.getString("gameScore");
            String userName=user.getString("name");
            String version=jsonObject.getString("version");
            String music=jsonObject.getString("music");
            String inappads=jsonObject.getString("inappads");
            editor.putString("inappads",inappads);
            editor.putString("music",music);
            editor.putString("Username", userName);
            editor.putString("userEmail",email);
            editor.putString("version",version);
            editor.putBoolean("force_update",jsonObject.getBoolean("force_update"));
            editor.putInt("GameScore",Integer.parseInt(gameScore));
            editor.putBoolean("isVIP", user.getString("isVip").equals("1"));
            editor.putBoolean("isGoldPlan",user.getString("gold_plan").equals("1"));
            if(!imageUrl.equals("")){
                editor.putString("imageUrl",imageUrl);
            }
            editor.putString("enrollProgress",jsonObject.getString("enrollProgress"));
            String courseJson=jsonObject.getString("vipCourses");
            JSONArray courseArr=new JSONArray(courseJson);
            for(int i=0;i<courseArr.length();i++){
                JSONObject jo=courseArr.getJSONObject(i);
                String course=jo.getString("course_id");
                Log.e(i+" " ,course);
                editor.putBoolean("course"+course,true);
                editor.apply();
            }
            editor.putString("vipCourses",courseJson);
            editor.apply();
        }catch (Exception e){
            Toast.makeText(c,e.toString(),Toast.LENGTH_SHORT).show();
        }
    }


}
