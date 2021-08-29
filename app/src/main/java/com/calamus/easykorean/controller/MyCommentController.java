package com.calamus.easykorean.controller;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;
import com.calamus.easykorean.app.MyHttp;
import com.calamus.easykorean.app.Routing;

public class MyCommentController {

    String postId,currentUserName;
    Activity c;
    SharedPreferences sharedPreferences;

    public MyCommentController(String postId, String currentUserName, Activity c) {

        this.postId = postId;
        this.currentUserName = currentUserName;
        this.c = c;
        sharedPreferences=c.getSharedPreferences("GeneralData", Context.MODE_PRIVATE);

    }

    public void addCommentToHostinger(String postOwnerId, String writer_id, String body, String action, String CorR, String tokenPC,String commentImagePath){

        String time=System.currentTimeMillis()+"";
        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.POST, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    if(!postOwnerId.equals(writer_id)){
                        NotificationController notificationController=new NotificationController(c);
                        notificationController.sendNotification(currentUserName+CorR,tokenPC,"Easy Korean","1");
                    }

                    Log.e("AddCommentRes: ", response);
                }
                @Override
                public void onError(String msg) {
                    Log.e("AddCommentErr: ", msg);
                }
            }).url(Routing.ADD_COMMENT)
                    .field("post_id",postId)
                    .field("writer_id",writer_id)
                    .field("owner_id",postOwnerId)
                    .field("body",body)
                    .field("action",action)
                    .field("time",time);
            if(!commentImagePath.equals(""))myHttp .file("myfile",commentImagePath);
            myHttp.runTask();
        }).start();
    }



    public void deleteComment(String postId, String cmtTime){
        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.POST, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {}
                @Override
                public void onError(String msg) {}
            }).url(Routing.DELETE_COMMENT)
                    .field("time",cmtTime)
                    .field("postId",postId);
            myHttp.runTask();
        }).start();

        Toast.makeText(c,"Deleted",Toast.LENGTH_SHORT).show();
    }


}
