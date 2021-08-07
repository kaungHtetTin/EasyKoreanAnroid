package com.calamus.easykorean.app;


import android.widget.ImageView;

import com.calamus.easykorean.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import me.myatminsoe.mdetect.MDetect;
import me.myatminsoe.mdetect.Rabbit;

public class AppHandler {

    public static void setPhotoFromRealUrl(ImageView iv, String url){
        Picasso.get()
                .load(url)
                .centerInside()
                .fit()
                .error(R.drawable.ic_baseline_account_circle_24)
                .into(iv, new Callback() {
                    @Override
                    public void onSuccess() {}
                    @Override
                    public void onError(Exception e) {

                    }
                });
    }

    public static String changeUnicode(String s){

        if(MDetect.INSTANCE.isUnicode()){
            return s;
        }else {
            return Rabbit.zg2uni(s);
        }
    }

    public static String setMyanmar(String s) {

        return MDetect.INSTANCE.getText(s);
    }

    public static  String viewCountFormat(int i){
        DecimalFormat decimalFormat = new DecimalFormat("0.0");
        if(i==0){
            return "No View";

        }else if(i==1){
            return "1 View";
        }else if(i>=1000&&i<1000000){
            double j=(double) i/1000;

            return  decimalFormat.format(j)+"k Views";
        }else if(i>=1000000){
            return decimalFormat.format((double)i/1000000) +"M Views";
        }else{
            return  i+" Views";
        }
    }

    public static  String commentFormat(int i){
        DecimalFormat decimalFormat = new DecimalFormat("0.0");
        if(i==0){
            return "No Comment";

        }else if(i==1){
            return "1 Comment";
        }else if(i>=1000&&i<1000000){
            double j=(double) i/1000;

            return  decimalFormat.format(j)+"k Comments";
        }else if(i>=1000000){
            return decimalFormat.format((double)i/1000000) +"M Comments";
        }else{
            return  i+" Comments";
        }
    }


    public static String reactFormat(int a){
        DecimalFormat decimalFormat = new DecimalFormat("0.0");
        if((double) a >=1000&& (double) a <1000000){
            double j= (double) a /1000;

            return  decimalFormat.format(j)+"k";
        }else if((double) a >=1000000){
            return decimalFormat.format((double) a /1000000 )+"M";
        }else{
            return  a +"";
        }
    }

    public static int setNotificationIcon(int i){
        if(i==0||i==3){
            return R.drawable.ic_speech_bubble_875;
        }else if(i==1||i==4){
            return R.drawable.ic_baseline_reply_24;
        }else if(i==2){
            return R.drawable.ic_book;
        }else if(i==5||i==6){
            return R.drawable.ic_react_love;
        }else {
            return R.drawable.ic_feather;
        }
    }

    public static String formatTime( long time){
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm");
        Date resultdate = new Date(time);
        long currentTime=System.currentTimeMillis();
        long timeDifference=currentTime-time;
        long s=1000;
        long min=s*60;
        long hour=min*60;
        long day=hour*24;
        if(timeDifference<min)return timeDifference/s+" s ago";
        else if(timeDifference>min&&timeDifference<hour) return timeDifference/min+" min ago";
        else if(timeDifference>hour&&timeDifference<day) return timeDifference/hour+ " h ago";
        else return sdf.format(resultdate);

    }

    public static  byte[] getFileByte(String title, String dir){
        byte [] buffer;

        try {
            InputStream is=new BufferedInputStream(new FileInputStream(dir+"/"+title));
            int size=is.available();
            buffer=new byte[size];
            is.read(buffer);
            is.close();

            return  buffer;

        }catch (Exception e){
            return null;
        }
    }

    public static  String downloadFormat(int i){
        DecimalFormat decimalFormat = new DecimalFormat("0.0");
        if(i==0){
            return "";

        }else if(i==1){
            return "1 download";
        }else if(i>=1000&&i<1000000){
            double j=(double) i/1000;

            return  decimalFormat.format(j)+"k downloads";
        }else if(i>=1000000){
            return decimalFormat.format((double)i/1000000) +"M downloads";
        }else{
            return  i+" downloads";
        }
    }

    public static void recordAClick(String user_id,String record){
        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.POST, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {}
                @Override
                public void onError(String msg) {}
            }).url(Routing.RECORD_A_CLICK)
                    .field("user_id",user_id)
                    .field("record",record);
            myHttp.runTask();
        }).start();
    }

}