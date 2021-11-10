package com.calamus.easykorean;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.MenuItem;
import com.calamus.easykorean.adapters.SavedVideoAdapter;
import com.calamus.easykorean.models.SavedVideoModel;
import java.io.File;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.Executor;
import static android.media.MediaMetadataRetriever.METADATA_KEY_DURATION;

public class SavedVideoActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ArrayList<SavedVideoModel> videoLists =new ArrayList<>();
    SavedVideoAdapter adapter;
    Executor postExecutor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_video);

        setUpView();
        postExecutor = ContextCompat.getMainExecutor(this);
        setTitle("Downloads");
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    private void setUpView(){
        recyclerView=findViewById(R.id.recycler);
        adapter=new SavedVideoAdapter(videoLists,this);
        LinearLayoutManager lm=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(lm);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

        new VideoLoader().start();
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();

        }
        return super.onOptionsItemSelected(item);
    }




    class VideoLoader extends Thread{
        @Override
        public void run() {
            super.run();

            File directory = getExternalFilesDir(Environment.DIRECTORY_MOVIES);
            File[] videoFiles =directory.listFiles();

            try {
                for (File videoFile : videoFiles) {

                    Uri uri=Uri.fromFile(videoFile);
                    MediaMetadataRetriever retriever=new MediaMetadataRetriever();
                    retriever.setDataSource(SavedVideoActivity.this,uri);

                    String duration=retriever.extractMetadata(METADATA_KEY_DURATION);
                    postExecutor.execute(() -> {
                        if(duration!=null){
                            videoLists.add(new SavedVideoModel(uri,videoFile.getName(),Integer.parseInt(duration),400,null));
                            adapter.notifyDataSetChanged();
                        }
                    });
                }
            }catch(Exception ignored){

            }

        }
    }
}