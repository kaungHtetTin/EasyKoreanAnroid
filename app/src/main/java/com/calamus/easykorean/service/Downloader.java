package com.calamus.easykorean.service;

import static com.calamus.easykorean.ApplicationClass.CHANNEL_ID_3;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
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
import java.util.concurrent.locks.ReentrantLock;

public class Downloader extends Thread{


    String downloadUrl;
    String dir;
    String filename;
    String intentMessage;
    long progress;
    long fileSize;
    Context c;
    DownloadCompleteListener callBack;
    ReentrantLock lock;
    long threadId;
    boolean pending=true,userCancel=false;

    Notification notification;
    NotificationManagerCompat notificationManagerCompat;
    NotificationCompat.Builder mBuilder;

    public Downloader(Context c, String downloadUrl, String dir, String filename, String intentMessage,
                      ReentrantLock lock, DownloadCompleteListener callBack){
        this.c=c;
        this.downloadUrl=downloadUrl;
        this.dir=dir;
        this.filename=filename;
        this.intentMessage=intentMessage;
        this.lock=lock;
        this.callBack=callBack;

        Notify(c,"Downloading",filename);
        threadId=getId();
    }


    @Override
    public void run() {
        super.run();
        int count;
        Log.e("ThreadID "+threadId," Wait for downloading");
        this.lock.lock();
        pending=false;

        Log.e("ThreadID "+threadId," start downloading");
        try{
            URL url=new URL(downloadUrl);
            URLConnection connection=url.openConnection();
            connection.connect();

            InputStream input=new BufferedInputStream(url.openStream(),8192);
            File folder=new File(dir);
            if(!folder.exists()) folder.mkdirs();
            File storagePath= new File(dir,filename);

            OutputStream output=new FileOutputStream(storagePath);
            byte[] data =new byte[1024];
            long total =0;
            fileSize=connection.getContentLength();
            while ((count=input.read(data))!=-1){
                if(userCancel){
                    this.lock.unlock();
                    if(storagePath.exists())storagePath.delete();
                    return;
                }
                total+=count;
                progress=total*100/fileSize;
                mBuilder.setProgress(100,(int)progress,false);
                mBuilder.setContentTitle("Download : "+progress+" %");
                callBack.onProgress(threadId,(int)progress);
                notificationManagerCompat.notify(filename.length(),mBuilder.build());
                output.write(data,0,count);
            }


            output.flush();
            output.close();
            input.close();
            callBack.onDownloadComplete(threadId);
            mBuilder.setProgress(100,100,false);
            mBuilder.setContentTitle("Download : "+100+" % ");
            mBuilder.setContentText(filename+" is completely downloaded");
            notificationManagerCompat.notify(filename.length(),mBuilder.build());

        }catch (Exception e){
            Log.e("DownloadErr ",e.toString());
            callBack.onFail(this,e.toString());
            mBuilder.setProgress(0,0,false);
            mBuilder.setContentTitle("Download");
            mBuilder.setContentText("Error downloading - "+filename);
            notificationManagerCompat.notify(filename.length(),mBuilder.build());

        }
        this.lock.unlock();
    }


    private void Notify(Context mContext, String title, String message){
        mBuilder=new NotificationCompat.Builder(mContext,CHANNEL_ID_3);

        Intent intent = new Intent(mContext, MainActivity.class);
        intent.putExtra("message", intentMessage);

        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        final PendingIntent resultPendingIntent;
        resultPendingIntent = PendingIntent.getActivity(
                mContext,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE
        );

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

    public long getProgress() {
        return progress;
    }

    public long getFileSize() {
        return fileSize;
    }

    public String getFilename() {
        return filename;
    }

    public interface DownloadCompleteListener{
        void onProgress(long threadId,int p);
        void onDownloadComplete(long threadId);
        void onFail(Downloader downloader,String msg);
        void onPause(Downloader downloader);
        void onCancel(Downloader downloader);
    }

    public void setUserCancel(boolean userCancel) {
        this.userCancel = userCancel;
        callBack.onCancel(this);
    }
}
