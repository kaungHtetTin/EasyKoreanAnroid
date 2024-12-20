package com.calamus.easykorean;


import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.calamus.easykorean.adapters.SaveAdapter;
import com.calamus.easykorean.models.SaveModel;
import java.util.ArrayList;
import java.util.Objects;

public class SavePostActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    SaveAdapter adapter;
    ArrayList<SaveModel> saveList=new ArrayList<>();
    String dbdir;
    String dbPath;
    SQLiteDatabase db;

    boolean isVip;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_post);

        setUpView();
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

        adapter = new SaveAdapter(this, saveList);
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
        String query="SELECT*FROM SavePost;";
        Cursor cursor=db.rawQuery(query,null);
        if(cursor.moveToFirst()){

            for(int i=0;i<cursor.getCount();i++){

                String post_id=cursor.getString(1);
                String post_body=cursor.getString(2);
                String post_image=cursor.getString(3);
                String owner_name=cursor.getString(4);
                String owner_image=cursor.getString(5);
                String isVideo=cursor.getString(6);
                saveList.add(new SaveModel(post_id,post_body,post_image,owner_name,owner_image,isVideo));
                cursor.moveToNext();

            }

            adapter.notifyDataSetChanged();
        }

    }
}