package com.calamus.easykorean;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.calamus.easykorean.app.Config;
import com.calamus.easykorean.app.MyDialog;
import com.calamus.easykorean.app.NotificationUtils;
import com.calamus.easykorean.app.Routing;
import com.calamus.easykorean.fragments.FragmentFive;
import com.calamus.easykorean.fragments.FragmentFour;
import com.calamus.easykorean.fragments.FragmentOne;
import com.calamus.easykorean.fragments.FragmentThree;
import com.calamus.easykorean.fragments.FragmentTwo;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.messaging.FirebaseMessaging;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import static com.calamus.easykorean.app.AppHandler.makeActiveNow;
import static com.calamus.easykorean.app.AppHandler.makeOffline;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private ViewPager viewPager;
    BottomNavigationView bnv;
    FragmentOne fragmentOne;
    FragmentTwo fragmentTwo;
    FragmentThree fragmentThree;
    FragmentFour fragmentFour;
    FragmentFive fragmentFive;
    SharedPreferences share;
    public static boolean pagerPosition,isHome=true;
    String currentUserId,studyTime,inAppAds;
    BroadcastReceiver mRegistrationBroadcastReceiver;
    RelativeLayout mainLayout;
    ExecutorService myExecutor;
    Executor postExecutor;
    boolean isVip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainLayout=findViewById(R.id.activity_main);
        share = getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        currentUserId=share.getString("phone",null);
        studyTime=share.getString("studyTime",null);
        inAppAds=share.getString("inappads",null);
        isVip=share.getBoolean("isVIP",false);
        myExecutor= Executors.newFixedThreadPool(1);
        postExecutor= ContextCompat.getMainExecutor(this);
        FirebaseMessaging.getInstance().subscribeToTopic(Routing.subscribeToTopic);

        Objects.requireNonNull(getSupportActionBar()).hide();
        setUpView();
        makeActiveNow(Long.parseLong(currentUserId)+"");
        //getRecommendation(currentUserId);

        String goSomeWhere= getIntent().getStringExtra("message");
        String payLoadStr=getIntent().getStringExtra("payload");
        if(payLoadStr==null) payLoadStr="null value";
        if(goSomeWhere==null)goSomeWhere="null value";

        switch (goSomeWhere) {
            case "splash":
                String version = share.getString("version", "");
                if (!version.equals("3.3.33")) confirmUpdate(); //check the version in generaldata.php
                break;
            case "login":
                Toast.makeText(this, "Welcome!", Toast.LENGTH_SHORT).show();
                break;
            case "new_lesson":
                try {
                    JSONObject jo = new JSONObject(payLoadStr);
                    Intent intent=new Intent(getApplicationContext(), DayListActivity.class);
                    intent.putExtra("course_id",jo.getString("course_id"));
                    intent.putExtra("course_title",jo.getString("course_title"));
                    intent.putExtra("theme_color",jo.getString("theme_color"));
                    startActivity(intent);
                }catch (Exception e){
                    Log.e("Lesson Noti Error",e.toString());
                }

                break;
            case "cloud_message":
                break;
            case "1":
            case "postFeed":
                Intent intent = new Intent(MainActivity.this, NotiListActivity.class);
                startActivity(intent);
                break;

            case "downloadVideo" :
            case "downloadSong" :
            case "downloadingLists":
                startActivity(new Intent(MainActivity.this,DownloadingListActivity.class));
                break;

            default:
                Intent intent1 = new Intent(MainActivity.this, ClassRoomActivity.class);
                intent1.putExtra("action", goSomeWhere);
                startActivity(intent1);
                break;

        }

        // new push notification is received
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if (intent.getAction().equals(Config.REGISTRATION_COMPLETE)) {
                    FirebaseMessaging.getInstance().subscribeToTopic(Config.TOPIC_GLOBAL);
                } else if (intent.getAction().equals("MessageArrived")) {
                    // new push notification is received
                    String message = intent.getStringExtra("message");
                    String sender = intent.getStringExtra("sender");
                    final Snackbar sb=Snackbar.make(mainLayout,sender+" : "+message,Snackbar.LENGTH_INDEFINITE);
                    sb.setAction("View", v ->{
                                Intent intent1 = new Intent(MainActivity.this, ClassRoomActivity.class);
                                intent1.putExtra("action","");
                                startActivity(intent1);
                            }
                    ).setActionTextColor(Color.WHITE).show();
                }
            }
        };
        bnv.setItemIconTintList(null);
        if(inAppAds!=null){
            if(inAppAds.equals("on")){
                if(!goSomeWhere.equals("login")&&!isVip){
                    startActivity(new Intent(MainActivity.this,MyAdActivity.class));
                }
            }
        }

        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                SplashScreenActivity.mListener=null;
                try{
                    if(FragmentFour.canExit()||pagerPosition){
                        if(isHome){
                            finish();
                            makeOffline(Long.parseLong(currentUserId)+"");
                        }else{
                            viewPager.setCurrentItem(0);
                        }
                    }else {
                        FragmentFour.goToFirst();
                    }

                }catch (Exception e){
                    comfirm_exist();
                }
            }
        });
        postNotificationPermission();
    }


    private void setUpView(){
        fragmentOne =new FragmentOne();
        fragmentTwo=new FragmentTwo();
        fragmentThree=new FragmentThree();
        fragmentFour=new FragmentFour();
        fragmentFive=new FragmentFive();

        viewPager=findViewById(R.id.view_pager);
        ViewPagerAdapter vAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(vAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener(){

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                switch (position){
                    case 0:
                        bnv.getMenu().findItem(R.id.frag_one).setChecked(true);
                        pagerPosition=true;
                        isHome=true;
                        break;
                    case 1:
                        bnv.getMenu().findItem(R.id.frag_two).setChecked(true);
                        pagerPosition=true;
                        isHome=false;
                        break;

                    case 2:
                        bnv.getMenu().findItem(R.id.frag_three).setChecked(true);
                        pagerPosition=true;
                        isHome=false;
                        break;

                    case 3:
                        bnv.getMenu().findItem(R.id.frag_four).setChecked(true);
                        pagerPosition=false;
                        isHome=false;
                        break;
                    case 4:
                        bnv.getMenu().findItem(R.id.frag_five).setChecked(true);
                        pagerPosition=true;
                        isHome=false;
                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });

        bnv=findViewById(R.id.bot_nav_view);
        bnv.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()){
                case R.id.frag_one:
                    viewPager.setCurrentItem(0);
                    break;

                case R.id.frag_two:
                    viewPager.setCurrentItem(1);
                    break;

                case R.id.frag_three:
                    viewPager.setCurrentItem(2);
                    break;

                case R.id.frag_four:
                    viewPager.setCurrentItem(3);
                    break;

                case R.id.frag_five:
                    viewPager.setCurrentItem(4);
                    break;
            }
            return true;
        });

    }

    public void postNotificationPermission(){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            int readExternalStorage = ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS);
            if(!(readExternalStorage== PackageManager.PERMISSION_GRANTED)){
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.POST_NOTIFICATIONS},101);
            }
        }
    }

    public class ViewPagerAdapter extends FragmentStatePagerAdapter {

        private ViewPagerAdapter(FragmentManager fm){
            super(fm,BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0:
                    return new FragmentOne();
                case 1:
                    return new FragmentTwo();
                case 2:
                    return new FragmentThree();
                case 3:
                    return new FragmentFour();
                case 4:
                    return new FragmentFive();
            }
            return null;
        }

        @Override
        public int getCount() {
            return 5;
        }
    }


    public void comfirm_exist(){

        MyDialog myDialog=new MyDialog(this, "Exit Confirmation", "Do you really want to exit?", () ->{
            finish();
            makeOffline(Long.parseLong(currentUserId)+"");

        });
        myDialog.showMyDialog();
    }

    public void  confirmUpdate(){

        MyDialog myDialog=new MyDialog(this, "Update Version!", "Do you want to update the app now?", () -> {
            Intent intent=new Intent(MainActivity.this,UpdateActivity.class);
            startActivity(intent);
        });
        myDialog.showMyDialog();
    }

    @Override
    protected void onResume() {

        super.onResume();
        // register GCM registration complete receiver
        //  LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
        //    new IntentFilter(Config.REGISTRATION_COMPLETE));

        // register new push message receiver
        // by doing this, the activity will be notified each time a new message arrives
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter("MessageArrived"));

        // clear the notification area when the app is opened

        NotificationUtils.clearNotifications(getApplicationContext());
    }

//    private void getRecommendation(String Id){
//        myExecutor.execute(()->{
//            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.GET, new MyHttp.Response() {
//                @Override
//                public void onResponse(String response) {
//                    Log.e("recommend : ",response);
//                    postExecutor.execute(()->{
//                        try{
//                            JSONObject jo=new JSONObject(response);
//                            new RecommendationDialog(MainActivity.this,jo.getString("msg")).showDialog();
//                        }catch (Exception e){
//                            if(studyTime==null){
//                                 StudyTimeSetter studyTimeSetter=new StudyTimeSetter(MainActivity.this);
//                                 studyTimeSetter.showTimePicker();
//                            }
//                        }
//
//                    });
//                }
//                @Override
//                public void onError(String msg) {}
//            }).url("https://www.calamuseducation.com/calamus-guide/korea/calculate.php?userid="+Id);
//            myHttp.runTask();
//        });
//    }

}