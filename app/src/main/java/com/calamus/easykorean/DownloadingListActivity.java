package com.calamus.easykorean;

import static java.lang.Thread.sleep;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.calamus.easykorean.adapters.DownloadAdapter;
import com.calamus.easykorean.service.DownloaderService;
import java.util.concurrent.Executor;

public class DownloadingListActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    DownloadAdapter adapter;
    Executor postExecutor;
    boolean onBack=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_downloading_list);
        postExecutor= ContextCompat.getMainExecutor(this);
        getSupportActionBar().hide();
        setUpView();
        setUpCustomAppBar();

    }

    @Override
    protected void onResume() {
        onBack=false;
        super.onResume();
    }




    private void setUpView(){
        recyclerView=findViewById(R.id.recyclerView);
        LinearLayoutManager lm=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(lm);
        adapter=new DownloadAdapter(this, DownloaderService.downloaderLists);
        recyclerView.setAdapter(adapter);
        updateProgress();
    }

    private void setUpCustomAppBar(){
        TextView tv=findViewById(R.id.tv_appbar);
        ImageView iv=findViewById(R.id.iv_back);
        tv.setText("Downloads");
        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        onBack=true;
        super.onBackPressed();
    }


    private void updateProgress(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (DownloaderService.downloaderLists.size()>0&&!onBack){

                    postExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            adapter.notifyDataSetChanged();
                        }
                    });

                    try {
                        sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                postExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        }).start();

    }
}