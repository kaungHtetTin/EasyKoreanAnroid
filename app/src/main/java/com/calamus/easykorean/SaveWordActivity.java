package com.calamus.easykorean;


import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.calamus.easykorean.adapters.SaveWordAdapter;
import com.calamus.easykorean.models.SaveWordModel;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import java.util.ArrayList;
import java.util.Objects;

public class SaveWordActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    SaveWordAdapter adapter;
    ArrayList<SaveWordModel> saveList=new ArrayList<>();
    String dbdir;
    String dbPath;
    SQLiteDatabase db;

    boolean isVip;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_post);

        sharedPreferences=getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        isVip=sharedPreferences.getBoolean("isVIP",false);

        setUpView();

        MobileAds.initialize(this, initializationStatus -> {});

        AdView adView = findViewById(R.id.adview);
        if(!isVip){
            adView.setVisibility(View.VISIBLE);
            AdRequest request=new AdRequest.Builder().build();
            adView.loadAd(request);

        }

        setUpCustomAppBar();
        Objects.requireNonNull(getSupportActionBar()).hide();

    }
    private void setUpCustomAppBar(){

        TextView tv=findViewById(R.id.tv_appbar);
        ImageView iv=findViewById(R.id.iv_back);
        tv.setText("Saved");
        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    private void setUpView(){
        recyclerView=findViewById(R.id.recycler_save);

        adapter = new SaveWordAdapter(this, saveList);
        GridLayoutManager gm = new GridLayoutManager(this, 1){};
        recyclerView.setLayoutManager(gm);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

        dbdir=getFilesDir().getPath()+"/databases/";
        dbPath=dbdir+"post.db";
        db=SQLiteDatabase.openDatabase(dbPath,null,SQLiteDatabase.OPEN_READWRITE);
        fetchFromDatabase();
    }


    public void fetchFromDatabase(){
        saveList.clear();
        String query="SELECT*FROM SaveWord;";
        Cursor cursor=db.rawQuery(query,null);
        if(cursor.moveToFirst()){

            for(int i=0;i<cursor.getCount();i++){

                int id=cursor.getInt(0);
                String wordJSON=cursor.getString(1);
                String time=cursor.getString(2);
                saveList.add(new SaveWordModel(id,wordJSON,time));
                cursor.moveToNext();
            }

            adapter.notifyDataSetChanged();
        }
        cursor.close();

    }
}