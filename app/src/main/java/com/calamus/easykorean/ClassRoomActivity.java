package com.calamus.easykorean;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.calamus.easykorean.fragments.ChatOne;
import com.calamus.easykorean.fragments.ChatThree;
import com.calamus.easykorean.fragments.ChatTwo;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.Objects;

public class ClassRoomActivity extends AppCompatActivity {

    ChatOne fragmentOne;
    ChatTwo fragmentTwo;
    ChatThree fragmentThree;
    BottomNavigationView bnv;
    private ViewPager viewPager;
    public static boolean isConservationFrag=true;
    boolean isVip;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_room);
        sharedPreferences=getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        isVip=sharedPreferences.getBoolean("isVIP",false);
        MobileAds.initialize(this, initializationStatus -> {});


        setUpView();
        AdView adView = findViewById(R.id.adview);
        if(!isVip){
            adView.setVisibility(View.VISIBLE);
            AdRequest request=new AdRequest.Builder().build();
            adView.loadAd(request);
        }

        String go=getIntent().getExtras().getString("action",null);
        if(go.equals("3")){
            viewPager.setCurrentItem(1);
        }else if(go.equals("4")){
            viewPager.setCurrentItem(2);
        }

    }

    private void setUpView() {
        fragmentOne =new ChatOne();
        fragmentTwo=new ChatTwo();
        fragmentThree=new ChatThree();
        viewPager=findViewById(R.id.view_pager_chat);
        bnv=findViewById(R.id.bot_nav_view_chat);

        ViewPagerAdapter vAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(vAdapter);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener(){

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position){
                    case 0:
                        bnv.getMenu().findItem(R.id.nav_one).setChecked(true);
                        Objects.requireNonNull(getSupportActionBar()).setTitle("ChatRoom");
                        isConservationFrag=true;
                        break;
                    case 1:
                        bnv.getMenu().findItem(R.id.nav_two).setChecked(true);
                        Objects.requireNonNull(getSupportActionBar()).setTitle("Requests");
                        isConservationFrag=false;
                        break;
                    case 2:
                        bnv.getMenu().findItem(R.id.nav_three).setChecked(true);
                        Objects.requireNonNull(getSupportActionBar()).setTitle("Classmates");
                        isConservationFrag=false;
                        break;

                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        bnv.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                int itemId = item.getItemId();
                if (itemId == R.id.nav_one) {
                    viewPager.setCurrentItem(0);
                } else if (itemId == R.id.nav_two) {
                    viewPager.setCurrentItem(1);
                } else if (itemId == R.id.nav_three) {
                    viewPager.setCurrentItem(2);
                }

                return true;
            }
        });
    }

    public class ViewPagerAdapter extends FragmentStatePagerAdapter {

        public ViewPagerAdapter(FragmentManager fm){
            super(fm,BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0:
                    return new ChatOne();
                case 1:
                    return new ChatTwo();
                case 2:
                    return new ChatThree();

            }
            return null;
        }

        @Override
        public int getCount() {
            return 3;
        }
    }


}