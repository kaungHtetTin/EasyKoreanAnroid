package com.calamus.easykorean;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import com.calamus.easykorean.app.MyHttp;
import com.calamus.easykorean.app.Routing;
import com.calamus.easykorean.app.UserInformation;
import com.google.firebase.installations.FirebaseInstallations;
import com.google.firebase.messaging.FirebaseMessaging;
import org.json.JSONObject;
import java.io.File;
import static android.content.ContentValues.TAG;

public class SplashScreenActivity extends AppCompatActivity {

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    boolean autoLogin=false;
    SQLiteDatabase db;
    String dbdir;
    String dbName="post.db";
    String phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        handleShareData();
        getMessagingToken(phone);
        createSQLiteDatabase();
        getForm();
        getWordOfTheDay(Routing.GET_WORD_OF_THE_DAY);//get word of the day
        setScreen();

    }


    private void handleShareData(){
        sharedPreferences=getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        editor=sharedPreferences.edit();
        phone=sharedPreferences.getString("phone",null);
        autoLogin=sharedPreferences.getBoolean("AlreadyLogin",false);

    }


    private void getMessagingToken(String phone){
        FirebaseInstallations.getInstance().getId();
       // FirebaseMessaging.getInstance().subscribeToTopic("easy_korean_users");
        FirebaseMessaging.getInstance().setAutoInitEnabled(true);
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                        return;
                    }
                    // Get new FCM registration token
                    String token = task.getResult();

                    if(autoLogin){
                        UserInformation userInformation=new UserInformation(SplashScreenActivity.this);
                        userInformation.getGeneralData(phone);
                    }

                    editor.putString("token",token);
                    editor.apply();

                });
    }

    private void createSQLiteDatabase(){
        dbdir=getFilesDir().getPath()+"/databases/";
        File dir=new File(dbdir);
        if(!dir.exists()){
            dir.mkdir();
        }
        String dbPath=dbdir+"post.db";
        File dbFile=new File(dbPath);
        if(!dbFile.exists()){
            db=SQLiteDatabase.openOrCreateDatabase(dbdir+dbName,null);
            String query="Create Table SavePost (id INTEGER PRIMARY KEY, post_id TEXT,post_body TEXT,post_image TEXT,owner_name TEXT,owner_image TEXT,isVideo TEXT)";
            String query2="Create Table SaveWord (id INTEGER PRIMARY KEY, wordJSON TEXT,time TEXT)";
            db.execSQL(query);
            db.execSQL(query2);
        }
    }

    private void setScreen(){
        Thread timer=new Thread(){
            @Override
            public void run() {
                super.run();
                try {
                    sleep(2000);
                    checkLoging();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        timer.start();
    }

    private void checkLoging(){

        if(autoLogin){
            Intent i=new Intent(SplashScreenActivity.this, MainActivity.class);
            i.putExtra("message","splash");
            startActivity(i);
            finish();

        }else {
            Intent intent=new Intent(SplashScreenActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void getForm(){
        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.GET, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject jo=new JSONObject(response);
                        String firstForm=jo.getString("firstform");
                        editor.putString("firstForm",firstForm);
                        String secondForm=jo.getString("secondform");
                        editor.putString("secondForm",secondForm);
                        String functionForm=jo.getString("function");
                        editor.putString("functionForm",functionForm);
                        String videoForm=jo.getString("videoform");
                        editor.putString("videoForm",videoForm);
                        editor.apply();

                    }catch (Exception e){}

                }
                @Override
                public void onError(String msg) {}
            }).url(Routing.GET_FORM);
            myHttp.runTask();
        }).start();
    }

    private void getWordOfTheDay(String w){
        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.GET, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    editor.putString("wordOfTheDay",response);
                    editor.apply();
                }
                @Override
                public void onError(String msg) {}
            }).url(w);
            myHttp.runTask();
        }).start();
    }
}