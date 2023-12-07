package com.calamus.easykorean.service;

import static com.calamus.easykorean.ApplicationClass.CHANNEL_ID_3;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.calamus.easykorean.MainActivity;
import com.calamus.easykorean.R;
import com.calamus.easykorean.app.Routing;

import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

public class DownloaderService extends Service {

    String downloadUrl;
    String filename;
    String dir;
    String intentMessage;
    ReentrantLock lock;
    public static ArrayList<Downloader> downloaderLists=new ArrayList<>();
    Notification notification;
    NotificationManagerCompat notificationManagerCompat;
    NotificationCompat.Builder mBuilder;

    @Override
    public void onCreate() {
        lock=new ReentrantLock();
        super.onCreate();

    }

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
        intentMessage=intent.getStringExtra("intentMessage");

        Downloader downloader=new Downloader(this, downloadUrl, dir, filename, intentMessage, lock,
                new Downloader.DownloadCompleteListener() {
                    @Override
                    public void onProgress(long threadId,int p) {

                    }

                    @Override
                    public void onPause(Downloader downloader) {

                    }

                    @Override
                    public void onCancel(Downloader downloader) {
                        removeDownloadThread(downloader.getId());
                    }

                    @Override
                    public void onDownloadComplete(long threadId) {
                        Log.e("ThreadID "+threadId,"Completed");
                        removeDownloadThread(threadId);
                    }

                    @Override
                    public void onFail(Downloader downloaderThread, String msg) {
                        Log.e("ThreadID "+downloaderThread.getId()+" Err ",msg);
                        removeDownloadThread(downloaderThread.getId());
                    }
                });
        downloaderLists.add(downloader);
        int threadCount= downloaderLists.size();

        showNotification(getApplicationContext(), threadCount +" downloading ...");
        downloader.start();

        return START_STICKY;
    }


    private void removeDownloadThread(long threadId){
        for(int i=0;i<downloaderLists.size();i++){
            Downloader downloader=downloaderLists.get(i);
            if(downloader.getId()==threadId){
                downloaderLists.remove(i);
            }
        }
        if(downloaderLists.size()==0){
            stopForeground();
            stopSelf();
        }

        int threadCount= downloaderLists.size();
        showNotification(getApplicationContext(), threadCount +" downloading ...");
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
        super.onDestroy();

    }

    public void startForeground(){
        startForeground(1,notification);
    }

    public void stopForeground(){
        stopForeground(false);
    }

    private void showNotification(Context mContext, String message){
        mBuilder=new NotificationCompat.Builder(mContext,CHANNEL_ID_3);
        Intent intent = new Intent(mContext, MainActivity.class);
        intent.putExtra("message", "downloadingLists");

        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        final PendingIntent resultPendingIntent;
        resultPendingIntent = PendingIntent.getActivity(
                mContext,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_MUTABLE
        );

        notification = mBuilder.setSmallIcon(R.mipmap.kommmainicon)
                .setAutoCancel(true)
                .setContentTitle(Routing.APP_NAME)
                .setContentIntent(resultPendingIntent)
                .setSmallIcon(R.mipmap.kommmainicon)
                .setContentText(message)
                .setSound(null)
                .build();
        notificationManagerCompat= NotificationManagerCompat.from(mContext);
        startForeground();
    }

}
