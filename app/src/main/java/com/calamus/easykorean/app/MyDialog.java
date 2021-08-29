package com.calamus.easykorean.app;

import android.app.Activity;
import android.app.AlertDialog;
import com.calamus.easykorean.R;

/*
This class is used to create alert dialog

 */
public class MyDialog {

    private final Activity c;
    private final String title;
    private final String msg;
    ConfirmClick confirmClick;

    public MyDialog(Activity c, String title, String msg,ConfirmClick confirmClick) {
        this.c = c;
        this.title = title;
        this.msg = msg;
        this.confirmClick=confirmClick;
    }

    public void showMyDialog(){
        final AlertDialog ad = new AlertDialog.Builder(c).create();
        ad.setTitle(title);
        ad.setIcon(R.mipmap.kommmainicon);
        ad.setMessage(msg);
        ad.setButton(AlertDialog.BUTTON_POSITIVE, "Yes", (dialog, which) -> confirmClick.onConfirmClick());
        ad.setButton(AlertDialog.BUTTON_NEGATIVE, "No", (dialog, which) -> ad.dismiss());
        ad.show();
    }

    public interface ConfirmClick{
         void onConfirmClick();
    }

}
