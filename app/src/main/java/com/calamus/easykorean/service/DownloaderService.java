package com.calamus.easykorean.service;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.calamus.easykorean.MainActivity;
import com.calamus.easykorean.R;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;


public class DownloaderService extends Service {

    public static final String CHANNEL_ID="kaung";
    private static final String CHANNEL_NAME="kaung";
    private static final String CHANNEL_DESC="kaung Notification";

    String downloadUrl;
    String filename;
    String dir;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        downloadUrl=intent.getStringExtra("downloadUrl");
        filename=intent.getStringExtra("filename");
        dir=intent.getStringExtra("dir");
      //  Notify(this,"Downloading",filename+" is downloading");
        new DownloadFileFromUrl(downloadUrl,filename,this).start();
        //return super.onStartCommand(intent, flags, startId);
       return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();


    }

    @Override
    public void onDestroy() {
        stopForeground(true);
        Toast.makeText(getApplicationContext(),"Complete",Toast.LENGTH_SHORT).show();
        super.onDestroy();

    }

    class DownloadFileFromUrl extends Thread{

        String downloadUrl;
        File storagePath;
        String filename;
        Notification notification;
        NotificationManagerCompat notificationManagerCompat;
        NotificationCompat.Builder mBuilder;

        public DownloadFileFromUrl(String url,String filename,Context mContext){
            this.downloadUrl=url;
            this.filename=filename;
            Notify(mContext,"Downloading",filename);
        }

        @Override
        public void run() {
            super.run();

            int count;

            long m;

            try{
                URL url=new URL(downloadUrl);
                URLConnection connection=url.openConnection();
                connection.connect();

                InputStream input=new BufferedInputStream(url.openStream(),8192);
                storagePath = new File(dir,filename);

                OutputStream output=new FileOutputStream(storagePath);
                byte[] data =new byte[1024];
                long total =0;
                while ((count=input.read(data))!=-1){
                    total+=count;
                    m= total *100/connection.getContentLength();
                    mBuilder.setProgress(100,(int)m,false);
                    mBuilder.setContentTitle("Download : "+m+" %");
                    notificationManagerCompat.notify(filename.length(),mBuilder.build());
                    output.write(data,0,count);

                }

                mBuilder.setProgress(100,100,false);
                mBuilder.setContentTitle("Download : "+100+" % ");
                mBuilder.setContentText(filename+" is completely downloaded");
                notificationManagerCompat.notify(filename.length(),mBuilder.build());

                output.flush();
                output.close();
                input.close();
                onDestroy();
            }catch (Exception e){
                mBuilder.setProgress(100,0,false);
                mBuilder.setContentTitle("Download");
                mBuilder.setContentText("Error downloading - "+filename);
                notificationManagerCompat.notify(filename.length(),mBuilder.build());
                Log.e("DownLoadErr : ",e.toString());
            }

        }


        private void Notify(Context mContext,String title,String message){
            mBuilder=new NotificationCompat.Builder(mContext,CHANNEL_ID);

            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.putExtra("message", message);

            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            final PendingIntent resultPendingIntent =
                    PendingIntent.getActivity(
                            mContext,
                            0,
                            intent,
                            PendingIntent.FLAG_CANCEL_CURRENT
                    );

            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
                NotificationChannel channel=new NotificationChannel(CHANNEL_ID,CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
                channel.setDescription(CHANNEL_DESC);
                NotificationManager manager=mContext.getSystemService(NotificationManager.class);
                manager.createNotificationChannel(channel);
            }


            notification = mBuilder.setSmallIcon(R.mipmap.kommmainicon)
                    .setAutoCancel(true)
                    .setContentTitle(title)
                    .setContentIntent(resultPendingIntent)
                    .setSmallIcon(R.mipmap.kommmainicon)
                    .setContentText(message)
                    .setSound(null)
                    .build();
             notificationManagerCompat=NotificationManagerCompat.from(mContext);

        }

    }
}
