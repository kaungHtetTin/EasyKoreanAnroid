package com.calamus.easykorean.app;

import android.util.Log;
import com.calamus.easykorean.interfaces.DownloadComplete;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class Downloader extends Thread {
    String downloadUrl;
    File storagePath;
    boolean isDownloaded;
    int m;
    DownloadComplete complete;

    public Downloader( String downloadUrl, File storagePath){
        this.downloadUrl=downloadUrl;
        this.storagePath=storagePath;

    }

    public Downloader( String downloadUrl, File storagePath,DownloadComplete complete){
        this.downloadUrl=downloadUrl;
        this.storagePath=storagePath;
        this.complete=complete;

    }

    @Override
    public void run() {
        super.run();

        try{
            URL url=new URL(downloadUrl);
            URLConnection connection=url.openConnection();
            connection.connect();
            InputStream input=new BufferedInputStream(url.openStream(),8192);
            int count;
            OutputStream output=new FileOutputStream(storagePath);
            byte[] data =new byte[1024];
            long total =0;
            while ((count=input.read(data))!=-1){

                total+=count;
                m=(int) total*100/connection.getContentLength();
                output.write(data,0,count);
            }

            output.flush();
            output.close();
            input.close();
            isDownloaded=true;
            if(complete!=null)complete.actionOnDownloadComplete();
        }catch (Exception e){
            Log.e("DownloadError : ",e.toString());

        }
    }

    public boolean isDownloaded() {
        return isDownloaded;
    }

    public int getM() {
        return m;
    }
}
