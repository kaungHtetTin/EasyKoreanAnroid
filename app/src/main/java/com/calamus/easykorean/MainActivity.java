package com.calamus.easykorean;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.calamus.easykorean.app.AppHandler;
import com.calamus.easykorean.app.Config;
import com.calamus.easykorean.app.MyDialog;
import com.calamus.easykorean.app.NotificationUtils;
import com.calamus.easykorean.fragments.FragmentFive;
import com.calamus.easykorean.fragments.FragmentFour;
import com.calamus.easykorean.fragments.FragmentOne;
import com.calamus.easykorean.fragments.FragmentThree;
import com.calamus.easykorean.fragments.FragmentTwo;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.messaging.FirebaseMessaging;
import java.util.Objects;

import static com.calamus.easykorean.app.AppHandler.makeActiveNow;
import static com.calamus.easykorean.app.AppHandler.makeOffline;

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
    String currentUserId;
    BroadcastReceiver mRegistrationBroadcastReceiver;
    RelativeLayout mainLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainLayout=findViewById(R.id.activity_main);
        share = getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        currentUserId=share.getString("phone",null);
        FirebaseMessaging.getInstance().subscribeToTopic("koreaUsers");
        FirebaseMessaging.getInstance().subscribeToTopic("easyKorea");

        setUpView();
        makeActiveNow(Long.parseLong(currentUserId)+"");
        String goSomeWhere= Objects.requireNonNull(getIntent().getExtras()).getString("message",null);
        switch (goSomeWhere) {
            case "splash":
                String version = share.getString("version", "");
                if (!version.equals("2.17")) confirmUpdate(); //check the version in generaldata.php

                break;
            case "login":
                Toast.makeText(this, "Welcome!", Toast.LENGTH_SHORT).show();
                break;
            case "1":
            case "postFeed":
                Intent intent = new Intent(MainActivity.this, NotiListActivity.class);
                startActivity(intent);
                break;

            default: {
                Intent intent1 = new Intent(MainActivity.this, ClassRoomActivity.class);
                intent1.putExtra("action",goSomeWhere);
                startActivity(intent1);
                break;
            }
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
    }

    private void setUpView(){
        fragmentOne =new FragmentOne();
        fragmentTwo=new FragmentTwo();
        fragmentThree=new FragmentThree();
        fragmentFour=new FragmentFour("public",currentUserId);
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
                        Objects.requireNonNull(getSupportActionBar()).setTitle("Lessons");
                        pagerPosition=true;
                        isHome=true;
                        break;
                    case 1:
                        bnv.getMenu().findItem(R.id.frag_two).setChecked(true);
                        Objects.requireNonNull(getSupportActionBar()).setTitle("Notes");
                        pagerPosition=true;
                        isHome=false;

                        break;
                    case 2:
                        bnv.getMenu().findItem(R.id.frag_three).setChecked(true);
                        Objects.requireNonNull(getSupportActionBar()).setTitle("Videos");
                        pagerPosition=true;
                        isHome=false;

                        break;

                    case 3:
                        bnv.getMenu().findItem(R.id.frag_four).setChecked(true);
                        Objects.requireNonNull(getSupportActionBar()).setTitle("Calamus");
                        pagerPosition=false;
                        isHome=false;

                        break;
                    case 4:
                        bnv.getMenu().findItem(R.id.frag_five).setChecked(true);
                        Objects.requireNonNull(getSupportActionBar()).setTitle("Menu");
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
                    return new FragmentFour("public",currentUserId);
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


    @Override
    public void onBackPressed() {
        SplashScreenActivity.mListener=null;
        try{
            if(FragmentFour.canExit()||pagerPosition){
                if(isHome){
                    super.onBackPressed();
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

}