package com.calamus.easykorean.controller;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import com.calamus.easykorean.app.MyHttp;
import com.calamus.easykorean.app.Routing;

public class NotificationController {
    Activity c;
    SharedPreferences sharedPreferences;

    public NotificationController(Activity c) {
        this.c = c;
        sharedPreferences=c.getSharedPreferences("GeneralData", Context.MODE_PRIVATE);

    }


    public void sendNotification(String message, String token,String title,String action){
        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.POST, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {}
                @Override
                public void onError(String msg) {

                }
            }).url(Routing.PUSH_NOTIFICATION)
                    .field("title",title)
                    .field("message",message)
                    .field("to",token)
                    .field("action",action);

            myHttp.runTask();
        }).start();

    }


    public void PushNotiToAdmin(String message){

        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.POST, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {}
                @Override
                public void onError(String msg) {}
            }).url(Routing.PUSH_NOTIFICATION_TOPIC)
                    .field("title","Hey Admin")
                    .field("message",message)
                    .field("to",Routing.PUSH_NOTIFICATION_TO_TEACHER);
            myHttp.runTask();
        }).start();
    }

}
