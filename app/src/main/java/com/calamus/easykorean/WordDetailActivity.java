package com.calamus.easykorean;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.calamus.easykorean.app.AppHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class WordDetailActivity extends AppCompatActivity {

    ImageView iv;
    TextView tv_date,tv_main,tv_detail;
    String wordOfTheDay;
    boolean isVIP;
    SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_detail);
        sharedPreferences=getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        isVIP=sharedPreferences.getBoolean("isVIP",false);

        wordOfTheDay=getIntent().getExtras().getString("word","");

        setUpView();
        Objects.requireNonNull(getSupportActionBar()).hide();
        setUpCustomAppBar();

    }

    private void setUpCustomAppBar(){

        TextView tv=findViewById(R.id.tv_appbar);
        ImageView iv=findViewById(R.id.iv_back);
        tv.setText("Detail");
        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    private void setUpView(){
        iv=findViewById(R.id.iv_word_of_the_day);
        tv_date=findViewById(R.id.tv_date);
        tv_main=findViewById(R.id.tv_main);
        tv_detail=findViewById(R.id.tv_detail);


        SimpleDateFormat sdf= new SimpleDateFormat("MMMdd,yyyy HH:mm");
        Date resultDate=new Date(System.currentTimeMillis());
        tv_date.setText(sdf.format(resultDate));

        try{
            JSONArray ja=new JSONArray(wordOfTheDay);
            JSONObject jo=ja.getJSONObject(0);
            String english=jo.getString("korea");
            String myanmar=jo.getString("myanmar");
            String speech=jo.getString("speech");
            String example=jo.getString("example");
            String thumb=jo.getString("thumb");
            tv_main.setText(english+" ( "+speech+" )\n"+ AppHandler.setMyanmar(myanmar));
            AppHandler.setPhotoFromRealUrl(iv,thumb);
            tv_detail.setText(example);

        }catch (Exception e){
            tv_detail.setText("Word of the day is not available now for a while");
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {

        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}