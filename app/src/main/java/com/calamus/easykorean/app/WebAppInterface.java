package com.calamus.easykorean.app;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.webkit.JavascriptInterface;

import com.calamus.easykorean.TeacherActivity;

public class WebAppInterface {
    Context mContext;

    /** Instantiate the interface and set the context */
    public WebAppInterface(Context c) {
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

}

