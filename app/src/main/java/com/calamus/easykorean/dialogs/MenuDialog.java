package com.calamus.easykorean.dialogs;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.view.View;

import androidx.fragment.app.FragmentActivity;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.calamus.easykorean.R;
import com.calamus.easykorean.SavePostActivity;
import com.calamus.easykorean.SaveWordActivity;
import com.calamus.easykorean.SavedVideoActivity;
import com.calamus.easykorean.SettingActivity;
import com.calamus.easykorean.SplashScreenActivity;
import com.calamus.easykorean.WebSiteActivity;
import com.calamus.easykorean.app.Routing;
import com.calamus.easykorean.app.StudyTimeSetter;


public class MenuDialog {

    FragmentActivity c;
    public MenuDialog(FragmentActivity c){
        this.c=c;

    }

    public void initDialog(){

        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(c,R.style.SheetDialog);
        bottomSheetDialog.setContentView(R.layout.dialog_menu);

        //setting
        bottomSheetDialog.findViewById(R.id.menu_setting).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                c.startActivity(new Intent(c, SettingActivity.class));
            }
        });

        //purchase vip plan
        bottomSheetDialog.findViewById(R.id.menu_vip).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(c, WebSiteActivity.class);
                i.putExtra("link", Routing.PAYMENT);
                c.startActivity(i);
            }
        });

        //set your study timer
        bottomSheetDialog.findViewById(R.id.menu_timer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StudyTimeSetter studyTimeSetter=new StudyTimeSetter(  c);
                studyTimeSetter.showTimePicker();
            }
        });

        //downloaded videos
        bottomSheetDialog.findViewById(R.id.menu_video).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(c,SavedVideoActivity.class);
                intent.putExtra("rootPath",c.getExternalFilesDir(Environment.DIRECTORY_MOVIES).getAbsolutePath());
                c.startActivity(intent);
            }
        });

        //save words
        bottomSheetDialog.findViewById(R.id.menu_words).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                c.startActivity(new Intent(c, SaveWordActivity.class));
            }
        });

        //save posts
        bottomSheetDialog.findViewById(R.id.menu_posts).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                c.startActivity(new Intent(c, SavePostActivity.class));
            }
        });

        //sign out
        bottomSheetDialog.findViewById(R.id.menu_sign_out).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signOut();
            }
        });


        bottomSheetDialog.show();


    }

    private void signOut() {
        SharedPreferences sharedPreferences1 = c.getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor1 = sharedPreferences1.edit();
        editor1.clear();
        editor1.apply();
        Intent intent = new Intent(c, SplashScreenActivity.class);
        c.startActivity(intent);
        c.finish();
    }

}
