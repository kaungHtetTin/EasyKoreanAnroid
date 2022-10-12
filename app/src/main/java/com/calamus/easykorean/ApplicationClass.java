package com.calamus.easykorean;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class ApplicationClass extends Application {
    public static String CHANNEL_ID_1="easyEnglishChannel1";
    public static String CHANNEL_ID_2="easyEnglishChannel2";
    public static String CHANNEL_ID_3="easyEnglishDownloaderChannel";

    public static final String ACTON_PREVIOUS="actionprevious";
    public static final String ACTION_NEXT="actionnext";
    public static final String ACTION_PLAY="actionplay";
    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            NotificationChannel channel=new NotificationChannel(CHANNEL_ID_1,"Channel(1)", NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("Channel 1 Desc ...");

            NotificationChannel channe2=new NotificationChannel(CHANNEL_ID_2,"Channel(2)", NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("Channel 2 Desc ...");

            NotificationChannel channel3=new NotificationChannel(CHANNEL_ID_3,"Downloader", NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("Downloader Notification");

            NotificationManager notificationManager=getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
            notificationManager.createNotificationChannel(channe2);
            notificationManager.createNotificationChannel(channel3);
        }
    }

}
