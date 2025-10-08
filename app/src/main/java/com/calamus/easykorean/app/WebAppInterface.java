package com.calamus.easykorean.app;

import android.app.Activity;
import android.content.Intent;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import com.calamus.easykorean.TeacherActivity;
import com.calamus.easykorean.WebSiteActivity;

public class WebAppInterface {
    Activity mContext;
    CallBack callBack;

    /** Instantiate the interface and set the context */
    public WebAppInterface(Activity c) {
        mContext = c;
    }

    public WebAppInterface(Activity c,CallBack callBack){
        this.mContext=c;
        this.callBack=callBack;
    }

    /** Show a toast from the web page */
    @JavascriptInterface
    public void showToast(String toast) {
        Intent intent=new Intent(mContext, TeacherActivity.class);
        intent.putExtra("team","Developer");
        intent.putExtra("imageUrl","https://www.calamuseducation.com/appthumbs/kommmainicon.png");
        mContext.startActivity(intent);
    }

    @JavascriptInterface
    public void videoEnded(String msg){
        callBack.onEvent();
    }

    @JavascriptInterface
    public void onVideoOrientation(String mode){
        callBack.onVideoPortrait(mode);
    }

    @JavascriptInterface
    public void onTimeUpdate(String second){
        callBack.onTimeUpdate(second);
    }


    @JavascriptInterface
    public void showInAppAd(String msg){
        Intent i = new Intent(mContext, WebSiteActivity.class);
        i.putExtra("link", Routing.PAYMENT);
        mContext.startActivity(i);
        mContext.finish();
    }

    public interface CallBack{
        void onEvent();
        void onVideoPortrait(String mode);

        void onTimeUpdate(String second);
    }
}

