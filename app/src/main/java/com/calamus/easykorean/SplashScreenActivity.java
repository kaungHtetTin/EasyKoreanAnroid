package com.calamus.easykorean;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.calamus.easykorean.app.MyHttp;
import com.calamus.easykorean.app.Routing;
import com.calamus.easykorean.app.UserInformation;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.installations.FirebaseInstallations;
import com.google.firebase.messaging.FirebaseMessaging;
import org.json.JSONObject;
import java.io.File;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static android.content.ContentValues.TAG;

public class SplashScreenActivity extends AppCompatActivity {

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    boolean autoLogin=false;
    SQLiteDatabase db;
    String dbdir;
    String dbName="post.db";
    String phone,auth_token;

    String dbName2="conservation.db";
    public static ValueEventListener mListener=null;
    private DatabaseReference dbc,dbn;
    ExecutorService myExecutor;


    public static boolean MESSAGE_ARRIVE=false;
    public static boolean TEACHER_MESSAGE=false;
    public static boolean DEVELOPER_MESSAGE=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        dbc= FirebaseDatabase.getInstance().getReference().child(Routing.MAJOR).child("Conservation");
        dbn= FirebaseDatabase.getInstance().getReference().child(Routing.MAJOR).child("notification");
        myExecutor= Executors.newFixedThreadPool(3);
        handleShareData();
        getMessagingToken();

        createSQLiteDatabase();
        getAppForm();
        getWordOfTheDay(Routing.GET_WORD_OF_THE_DAY);//get word of the day
        setScreen();

        if(autoLogin&&phone!=null){
            UserInformation userInformation=new UserInformation(SplashScreenActivity.this);
            userInformation.getGeneralData(phone,auth_token);
        }

    }

    private void handleShareData(){
        sharedPreferences=getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        editor=sharedPreferences.edit();
        phone=sharedPreferences.getString("phone",null);
        autoLogin=sharedPreferences.getBoolean("AlreadyLogin",false);
        auth_token=sharedPreferences.getString("auth_token","abcd");

    }


    private void getMessagingToken(){
        FirebaseInstallations.getInstance().getId();
        FirebaseMessaging.getInstance().setAutoInitEnabled(true);
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                        return;
                    }
                    // Get new FCM registration token
                    String token = task.getResult();
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
        String dbPath2=dbdir+dbName2;
        File dbFile2=new File(dbPath2);
        if(!dbFile2.exists()){
            db=SQLiteDatabase.openOrCreateDatabase(dbPath2,null);
            String query="Create Table Conservations (id INTEGER PRIMARY KEY, fri_id TEXT,fri_name TEXT, fri_image TEXT,msg_body TEXT,time TEXT,senderId TEXT,seen INTEGER,token TEXT,my_id TEXT)";
            String query2="Create Table Chats (id INTEGER PRIMARY KEY,chatRoom TEXT, senderId TEXT,msg TEXT,time TEXT,imageUrl TEXT,seen INTEGER)";
            db.execSQL(query);
            db.execSQL(query2);

        }else{
            if (autoLogin&&mListener==null&&phone!=null){
                fetchConservation(Long.parseLong(phone)+"");
            }
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
        if(sharedPreferences.getBoolean("AlreadyLogin",false)){
            Intent i=new Intent(SplashScreenActivity.this, MainActivity.class);
            i.putExtra("message","splash");
            startActivity(i);
        }else{
            editor.putBoolean("AlreadyLogin",false);
            editor.apply();
            Intent intent=new Intent(SplashScreenActivity.this, LoginActivity.class);
            startActivity(intent);
        }
        finish();
    }

    private void getAppForm(){
        myExecutor.execute(()->{
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.GET, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject jo=new JSONObject(response);
                        editor.putString("mainCourse",jo.getString("mainCourse"));
                        editor.putString("additionalLessons",jo.getString("additionalLessons"));
                        editor.putString("functions",jo.getString("functions"));
                        editor.putString("videoChannels",jo.getString("videoChannels"));
                        editor.putString("kDramas",jo.getString("kDramas"));
                        editor.apply();

                    }catch (Exception ignored){}

                }
                @Override
                public void onError(String msg) {}
            }).url(Routing.GET_APP_FORM);
            myHttp.runTask();
        });
    }

    private void getWordOfTheDay(String w){
        myExecutor.execute(()->{
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
        });
    }


    //chatting methods

    private boolean isConservationExist(String fri){
        db=SQLiteDatabase.openOrCreateDatabase(dbdir+dbName2,null);
        String query="SELECT*FROM Conservations WHERE fri_id="+fri+" and my_id="+Long.parseLong(phone);
        Cursor cursor=db.rawQuery(query,null);
        return cursor.getCount()>0;
    }

    private void fetchConservation(String myId) {
        Log.e("ConFet: ", myId);
        mListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {

                    for (DataSnapshot dss : snapshot.getChildren()) {
                        String senderId = (String) dss.child("senderId").getValue();
                        String msg = (String) dss.child("message").getValue();
                        String time = (String) dss.child("time").getValue();
                        String name = (String) dss.child("userName").getValue();
                        String image = (String) dss.child("imageUrl").getValue();
                        String token = (String) dss.child("token").getValue();
                        long seen = (long) dss.child("seen").getValue();


                        if (!msg.equals("") && !image.equals("")) {
                            makeConservation(msg, senderId, time, name, image, myId, token, (int) seen);
                        }
                    }

                } catch (Exception e) {
                    //   Log.e(" ConFet241: ",e.toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        dbc.child(myId).addValueEventListener(mListener);
        checkMessageArrive(myId);
    }

    private void checkMessageArrive(String myId){
        dbn.child(myId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child("message_arrive").exists()){
                    MESSAGE_ARRIVE = (Boolean) snapshot.child("message_arrive").getValue();
                }else{
                    MESSAGE_ARRIVE=false;
                }

                if(snapshot.child("teacher_message").exists()){
                    TEACHER_MESSAGE= (Boolean) snapshot.child("teacher_message").getValue();
                }else{
                    TEACHER_MESSAGE=false;
                }

                if(snapshot.child("developer_message").exists()){
                    DEVELOPER_MESSAGE= (Boolean) snapshot.child("developer_message").getValue();
                }else{
                    DEVELOPER_MESSAGE=false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }


    private void makeConservation(String msgBody, String senderId, String time, String name, String image, String myId, String token, int seen){
        db=SQLiteDatabase.openOrCreateDatabase(dbdir+dbName2,null);

        if(isConservationExist(senderId)){
            ContentValues cv=new ContentValues();
            if(!msgBody.equals("block5241")){
                cv.put("fri_name",name);
                cv.put("fri_image",image);
                cv.put("msg_body",msgBody);
                cv.put("time",time);
                cv.put("token",token);
                cv.put("seen",seen);
                db.update("Conservations",cv,"fri_id="+senderId,null);
            }else{
                cv.put("fri_name","Calamus User");
                cv.put("fri_image",image);
                cv.put("msg_body",name +" blocked you");
                cv.put("time",time);
                cv.put("token","");
                cv.put("seen",seen);
                cv.put("fri_id","5241");
                db.update("Conservations",cv,"fri_id="+senderId+" and my_id="+myId,null);
            }
        }else{

            if(!msgBody.equals("block5241")){
                ContentValues cv=new ContentValues();
                cv.put("fri_id",senderId);
                cv.put("fri_name",name);
                cv.put("fri_image",image);
                cv.put("msg_body",msgBody);
                cv.put("time",time);
                cv.put("senderId",senderId);
                cv.put("token",token);
                cv.put("seen",seen);
                cv.put("my_id",Long.parseLong(phone));
                db.insert("Conservations",null,cv);
            }
        }

        updateConservationRealtime();
        if(!image.equals(""))deleteConservationOnFirebase(myId,senderId);
    }

    private void deleteConservationOnFirebase(String myId,String fri){

        Query applesQuery = dbc.child(myId).child(fri);

        applesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot appleSnapshot: dataSnapshot.getChildren()) {
                    appleSnapshot.getRef().removeValue();

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "onCancelled", databaseError.toException());
            }
        });
    }

    private void updateConservationRealtime(){
        Intent sendMessage=new Intent("Conservation");
        LocalBroadcastManager.getInstance(this).sendBroadcast(sendMessage);
    }

}