package com.calamus.easykorean;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.calamus.easykorean.adapters.AppAdapter;
import com.calamus.easykorean.app.MyHttp;
import com.calamus.easykorean.app.Routing;
import com.calamus.easykorean.models.AppModel;
import com.google.android.material.snackbar.Snackbar;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.Executor;
import static com.calamus.easykorean.app.AppHandler.setMyanmar;


public class UpdateActivity extends AppCompatActivity {
    TextView tv,tv2;
    String available,link;
    Button bt2;
    ImageView iv_apkpure;
    ViewGroup main;
    ProgressBar pb_loading;
    final String checkOnline="Please check your internet connection";
    final String noUpdate="No update version is available now";

    SharedPreferences sharedPreferences;
    boolean isVip;
    Executor postExecutor;
    private AppAdapter adapter;
    private final ArrayList<AppModel> appList = new ArrayList<>();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);
        sharedPreferences=getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        isVip=sharedPreferences.getBoolean("isVIP",false);
        postExecutor= ContextCompat.getMainExecutor(this);
        setUpView();
        setUpCustomAppBar();
        Objects.requireNonNull(getSupportActionBar()).hide();
        getUpdateDataFromHostinger();


    }

    private void setUpCustomAppBar(){
        TextView tv=findViewById(R.id.tv_appbar);
        ImageView iv=findViewById(R.id.iv_back);
        tv.setText("Update");
        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void setUpView(){
        tv=findViewById(R.id.tv_update);
        tv2=findViewById(R.id.tv_status);
        bt2=findViewById(R.id.get_playStore);
        iv_apkpure=findViewById(R.id.get_apk_pure);
        main= findViewById(R.id.layout_update);

        pb_loading=findViewById(R.id.pb_loading);
        RecyclerView recycler = findViewById(R.id.recyclerProduct);


        if(!isOnline())tv.setText(checkOnline);
        bt2.setOnClickListener(v -> {
            if(isOnline()){
                if(available!=null){
                    if(available.equals("true")){
                        goPlayStore();

                    }else{
                        setSnackBar(noUpdate);
                    }
                }else{
                    setSnackBar(checkOnline);
                }
            }else{
                setSnackBar(checkOnline);
            }
        });

        iv_apkpure.setOnClickListener(v -> {

            if(isOnline()){
                if(available!=null){
                    if(available.equals("true")){
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                        startActivity(browserIntent);

                    }else{
                        setSnackBar(noUpdate);
                    }
                }else{
                    setSnackBar(checkOnline);
                }
            }else{
                setSnackBar(checkOnline);
            }
        });


        LinearLayoutManager lm = new LinearLayoutManager(this);
        recycler.setLayoutManager(lm);
        recycler.setItemAnimator(new DefaultItemAnimator());

        adapter = new AppAdapter(this, appList);
        recycler.setAdapter(adapter);

    }

    private void setSnackBar(String s){
        final  Snackbar sb=Snackbar.make(main,s,Snackbar.LENGTH_INDEFINITE);
        sb.setAction("OK", v -> sb.dismiss()).setActionTextColor(Color.WHITE)
                .show();
    }

    private void getUpdateDataFromHostinger(){
        pb_loading.setVisibility(View.VISIBLE);
        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.POST, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                   postExecutor.execute(() -> {
                       getUpdateData(response);
                       pb_loading.setVisibility(View.GONE);
                   });

                }
                @Override
                public void onError(String msg) {}
            }).url(Routing.APP_UPDATE);
            myHttp.runTask();
        }).start();

    }

    private void getUpdateData(String response){
        pb_loading.setVisibility(View.GONE);
        try {
            JSONObject jo=new JSONObject(response);
            String status=jo.getString("status");

            available=jo.getString("available");
            tv.setText(setMyanmar(status));
            link=jo.getString("link");

            JSONArray jsonArray=jo.getJSONArray("apps");

            for(int i=0;i<jsonArray.length();i++){
                JSONObject joApp=jsonArray.getJSONObject(i);
                String name=joApp.getString("name");
                String description=joApp.getString("des");
                String link=joApp.getString("link");
                String thumb=joApp.getString("thumb");
                appList.add(new AppModel(name,description,thumb,link));

            }

            adapter.notifyDataSetChanged();

        }catch (Exception ignored){}
    }

    public void goPlayStore(){
        startActivity(new Intent(Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id="+getPackageName())));
    }

    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) Objects.requireNonNull(this).getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = Objects.requireNonNull(cm).getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }


}
