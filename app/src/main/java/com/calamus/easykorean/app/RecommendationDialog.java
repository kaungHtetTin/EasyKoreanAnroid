package com.calamus.easykorean.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.calamus.easykorean.R;


public class RecommendationDialog {
    Activity c;
    String info="";

    public RecommendationDialog(Activity c, String info) {
        this.c = c;
        this.info = info;
    }

    public void showDialog(){

        WebView wv;
        View v=c.getLayoutInflater().inflate(R.layout.recommendation_dialog,null);
        v.setAnimation(AnimationUtils.loadAnimation(c,R.anim.transit_up));
        AlertDialog.Builder builder=new AlertDialog.Builder(c);
        builder.setView(v);
        wv=v.findViewById(R.id.wv_recommend);
        wv.setWebViewClient(new WebViewClient());
        wv.getSettings().setJavaScriptEnabled(true);
        wv.getSettings().setDomStorageEnabled(true);
        wv.addJavascriptInterface(new WebAppInterface(c), "Android");
        wv.loadUrl("https://www.calamuseducation.com/calamus-guide/korea/recommand/"+info);

        builder.show();

    }
}
