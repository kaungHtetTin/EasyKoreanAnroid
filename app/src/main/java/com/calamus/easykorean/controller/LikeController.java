package com.calamus.easykorean.controller;

import com.calamus.easykorean.app.MyHttp;
import com.calamus.easykorean.app.Routing;


public class LikeController {


    public static void likeThePost(String user_id,String post_id){
        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.POST, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {}
                @Override
                public void onError(String msg) {}
            }).url(Routing.LIKE_A_POST)
                    .field("user_id",user_id)
                    .field("post_id",post_id)
                    .field("time",""+System.currentTimeMillis());
            myHttp.runTask();
        }).start();
    }

    public static void likeTheComment(String post_id,String user_id,String comment_id){

        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.POST, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {}
                @Override
                public void onError(String msg) {}
            }).url(Routing.LIKE_A_COMMENT)
                    .field("user_id",user_id)
                    .field("post_id",post_id)
                    .field("comment_id",comment_id)
                    .field("time",""+System.currentTimeMillis());
            myHttp.runTask();
        }).start();

    }

    public static void likeTheSong(String user_id,String post_id){
        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.POST, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {}
                @Override
                public void onError(String msg) {}
            }).url(Routing.LIKE_A_SONG)
                    .field("user_id",user_id)
                    .field("post_id",post_id)
                    .field("time",""+System.currentTimeMillis());
            myHttp.runTask();
        }).start();
    }

}
