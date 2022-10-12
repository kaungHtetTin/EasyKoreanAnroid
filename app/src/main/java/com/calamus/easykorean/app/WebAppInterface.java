package com.calamus.easykorean.app;

import android.app.Activity;
import android.content.Intent;
import android.webkit.JavascriptInterface;

import com.calamus.easykorean.TeacherActivity;
import com.calamus.easykorean.WebSiteActivity;

public class WebAppInterface {
    Activity mContext;

    /** Instantiate the interface and set the context */
    public WebAppInterface(Activity c) {
        mContext = c;
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
    public void showInAppAd(String msg){
        Intent i = new Intent(mContext, WebSiteActivity.class);
        i.putExtra("link", Routing.PAYMENT);
        mContext.startActivity(i);
        mContext.finish();
    }

}

