package com.calamus.easykorean.adapters;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.calamus.easykorean.GammingActivity;
import com.calamus.easykorean.LessonActivity;
import com.calamus.easykorean.R;
import com.calamus.easykorean.SaveWordActivity;
import com.calamus.easykorean.SongListActivity;
import com.calamus.easykorean.app.AppHandler;
import com.calamus.easykorean.models.CategoryModel;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class MainListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Activity c;
    private final ArrayList<Object> data;
    private final LayoutInflater mInflater;
    String dbdir;
    String dbPath;
    SQLiteDatabase db;
    String currentUserId;
    SharedPreferences sharedPreferences;

    public MainListAdapter(Activity c, ArrayList<Object> data) {
        this.data = data;
        this.c = c;
        this.mInflater = LayoutInflater.from(c);
        sharedPreferences=c.getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        currentUserId=sharedPreferences.getString("phone","901");
        dbdir=c.getFilesDir().getPath()+"/databases/";
        dbPath=dbdir+"post.db";
        db=SQLiteDatabase.openDatabase(dbPath,null,SQLiteDatabase.OPEN_READWRITE);

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int p2) {
        if(p2==0){
            View view=mInflater.inflate(R.layout.item_wordday,parent,false);
            return new DayHolder(view);
        }else if(p2==1){
            View view = mInflater.inflate(R.layout.item_music, parent, false);
            return new ItemHolder(view);
        }else {
            View view = mInflater.inflate(R.layout.item_main, parent, false);
            return new Holder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NotNull RecyclerView.ViewHolder holder, final int i) {
        if(i==0){

            DayHolder dayHolder=(DayHolder)holder;
            String wordOfTheDayJson=(String)data.get(0);
            TextToSpeech tts;
            tts=new TextToSpeech(c, status -> {
                if(status==0){
                    dayHolder.tv_main.setEnabled(true);
                }
            });
            setWordOfTheDay(dayHolder.tv_main,dayHolder.tv_example,dayHolder.iv_word,wordOfTheDayJson,tts,dayHolder.tv_save);
            SimpleDateFormat sdf= new SimpleDateFormat("MMMdd,yyyy HH:mm");
            Date resultDate=new Date(System.currentTimeMillis());
            dayHolder.tv_date.setText(sdf.format(resultDate));

        }else if(i==1){
                ItemHolder itemHolder=(ItemHolder)holder;
        }else{
            try {

                CategoryModel model=(CategoryModel)data.get(i);
                Holder lessonHolder=(Holder)holder;

                AppHandler.setPhotoFromRealUrl(lessonHolder.iv,model.getPic());
                lessonHolder.tv.setText(model.getCate());

            } catch (Exception e) {
                // Toast.makeText(c, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

    }

    public class Holder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView iv;
        TextView tv;
        public Holder(View view) {
            super(view);

            iv=view.findViewById(R.id.mainItemIv);
            tv=view.findViewById(R.id.tv_category);
            cardView=view.findViewById(R.id.card_View);
            view.setOnClickListener(p1 -> go(getAbsoluteAdapterPosition()));
        }
    }

    public class ItemHolder extends RecyclerView.ViewHolder {

        public ItemHolder(View view) {
            super(view);
            view.setOnClickListener(p1 -> {
                AppHandler.recordAClick(currentUserId,"song");
                Intent intent=new Intent(c, SongListActivity.class);
                c.startActivity(intent);
            });
        }
    }

    //This class is for word of the day.
    //this is located at index 0.

    public class DayHolder extends RecyclerView.ViewHolder{

        TextView tv_date,tv_main, tv_example;
        ImageView iv_word;
        TextView tv_save,tv_savedList;
        CardView cardView;

        public DayHolder(@NonNull View v) {
            super(v);
            cardView=v.findViewById(R.id.card_View);
            tv_date=v.findViewById(R.id.tv_date);
            tv_example=v.findViewById(R.id.tv_example);
            tv_main=v.findViewById(R.id.tv_main);
            iv_word=v.findViewById(R.id.iv_word_of_the_day);
            tv_save=v.findViewById(R.id.tv_save);
            tv_savedList=v.findViewById(R.id.tv_saveList);

            tv_savedList.setOnClickListener(v1 -> {
                Intent intent =new Intent(c, SaveWordActivity.class);
                c.startActivity(intent);
            });

        }
    }

    private void saveWord(String wordJSON){
        ContentValues cv=new ContentValues();
        cv.put("wordJSON",wordJSON);
        cv.put("time",System.currentTimeMillis()+"");
        db.insert("SaveWord",null,cv);
        Toast.makeText(c,"Saved",Toast.LENGTH_SHORT).show();
    }


    //this method set the word of the day from the sever
    @SuppressLint("SetTextI18n")
    private void setWordOfTheDay(TextView tv_main, TextView tv_example, ImageView iv, String response, TextToSpeech tts, TextView tv_save){
        try{
            JSONArray ja=new JSONArray(response);
            JSONObject jo=ja.getJSONObject(0);
            String english=jo.getString("korea");
            String myanmar=jo.getString("myanmar");
            String speech=jo.getString("speech");
            String example=jo.getString("example");
            String thumb=jo.getString("thumb");
            tv_main.setText(english+"     ( "+speech+" )     "+AppHandler.setMyanmar(myanmar));
            tv_example.setText(AppHandler.setMyanmar(example));

            AppHandler.setPhotoFromRealUrl(iv,thumb);

            tv_main.setOnClickListener(v -> tts.speak(english,TextToSpeech.QUEUE_FLUSH,null,null));

            tv_save.setOnClickListener(v -> saveWord(response));

        }catch (Exception e){
            tv_example.setText("Word of the day is not available now for a while");
        }
    }


    @Override
    public int getItemViewType(int position) {
        return position;
    }



    private void go(int d){

        if(d==2){
            Intent intent=new Intent(c, GammingActivity.class);
            c.startActivity(intent);
        }else{
            Intent i = new Intent(c, LessonActivity.class);
            CategoryModel model=(CategoryModel)data.get(d);
            i.putExtra("cate", model.getCate());
            i.putExtra("picLink",model.getPic());
            i.putExtra("setCate",model.getCate());
            i.putExtra("eCode","hnin");
            i.putExtra("level","vip");
            i.putExtra("fragment",2);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            c.startActivity(i);
        }
    }

}
