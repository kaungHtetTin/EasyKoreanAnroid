package com.calamus.easykorean.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
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
import com.calamus.easykorean.WordDetailActivity;
import com.calamus.easykorean.app.AppHandler;
import com.calamus.easykorean.fragments.FragmentTwo;
import com.calamus.easykorean.models.ExtraCourseModel;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Calendar;

public class MainListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Activity c;
    private final ArrayList<Object> data;
    private final LayoutInflater mInflater;
    String dbdir;
    String dbPath;
    SQLiteDatabase db;
    String currentUserId;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    String wordSavingCheck;

    public MainListAdapter(Activity c, ArrayList<Object> data) {
        this.data = data;
        this.c = c;
        this.mInflater = LayoutInflater.from(c);
        sharedPreferences=c.getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        editor=sharedPreferences.edit();
        wordSavingCheck=sharedPreferences.getString("wordSavingChecker","check");
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
            View view=mInflater.inflate(R.layout.item_title,parent,false);
            return new TitleHolder(view);
        }else if(p2==1){
            View view=mInflater.inflate(R.layout.item_wordday,parent,false);
            return new DayHolder(view);
        }else if(p2==2){
            View view = mInflater.inflate(R.layout.item_music, parent, false);
            return new ItemHolder(view);
        }else if(p2==3){
            View view = mInflater.inflate(R.layout.item_game, parent, false);
            return new GameHolder(view);
        }else {
            View view = mInflater.inflate(R.layout.item_main, parent, false);
            return new Holder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {

        if(data.get(position) instanceof  String){
            return 0;
        }else if(data.get(position) instanceof FragmentTwo.WordOfTheDay){
            return 1;
        }else if(data.get(position) instanceof FragmentTwo.LyricsSong){
            return 2;
        }else if(data.get(position) instanceof FragmentTwo.Game){
            return 3;
        }else {
            return 4;
        }
    }

    @Override
    public void onBindViewHolder(@NotNull RecyclerView.ViewHolder holder, final int i) {
        if(data.get(i) instanceof  String){
            String title=(String)data.get(i);
            TitleHolder titleHolder=(TitleHolder)holder;
            titleHolder.tv.setText(title);
        }else if(data.get(i) instanceof FragmentTwo.WordOfTheDay){

            DayHolder dayHolder=(DayHolder)holder;
            FragmentTwo.WordOfTheDay wDay=(FragmentTwo.WordOfTheDay)data.get(i);
            String wordOfTheDayJson=wDay.getWordOfTheDayJson();
            setWordOfTheDay(dayHolder.tv_main,dayHolder.iv_word,wordOfTheDayJson);

        }else if(data.get(i) instanceof FragmentTwo.LyricsSong){

        }else if(data.get(i) instanceof FragmentTwo.Game){

        }else{
            try {

                ExtraCourseModel model=(ExtraCourseModel) data.get(i);
                Holder lessonHolder=(Holder)holder;
                lessonHolder.tv.setText(model.getTitle());
                AppHandler.setPhotoFromRealUrl(lessonHolder.iv,model.getImage_url());

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

            iv=view.findViewById(R.id.iv_book);
            tv=view.findViewById(R.id.tv_category);
            cardView=view.findViewById(R.id.card_View);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(c, LessonActivity.class);
                    ExtraCourseModel model=(ExtraCourseModel) data.get(getAbsoluteAdapterPosition());

                    i.putExtra("category_id", model.getId());
                    i.putExtra("category_title",model.getTitle());
                    i.putExtra("course_title","Additional Lessons");
                    i.putExtra("level","vip");
                    i.putExtra("fragment",2);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    c.startActivity(i);
                }
            });
        }
    }

    public class ItemHolder extends RecyclerView.ViewHolder {
        ImageView iv;

        public ItemHolder(View view) {
            super(view);
            iv=view.findViewById(R.id.iv);

            AppHandler.setPhotoFromRealUrl(iv,"file:///android_asset/music.png");

            view.setOnClickListener(p1 -> {
                AppHandler.recordAClick(currentUserId,"song");
                Intent intent=new Intent(c, SongListActivity.class);
                c.startActivity(intent);
            });
        }
    }

    public class GameHolder extends RecyclerView.ViewHolder{

        public GameHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent(c,GammingActivity.class);
                    c.startActivity(intent);
                }
            });
        }
    }


    public class DayHolder extends RecyclerView.ViewHolder{

        TextView tv_main,tv_detail,tv_save_word;
        ImageView iv_word,iv_save;


        public DayHolder(@NonNull View v) {
            super(v);
            iv_save=v.findViewById(R.id.iv_save);
            tv_main=v.findViewById(R.id.tv_main);
            iv_word=v.findViewById(R.id.iv_word_of_the_day);
            tv_detail=v.findViewById(R.id.tv_detail);
            tv_save_word=v.findViewById(R.id.tv_save_word);

            if(wordSavingCheck.equals(wordSavingChecker())){
                iv_save.setImageResource(R.drawable.ic_react_love);
            }

            iv_save.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(wordSavingCheck.equals(wordSavingChecker())){
                        Toast.makeText(c,"Already saved",Toast.LENGTH_SHORT).show();
                    }else{
                        FragmentTwo.WordOfTheDay mDay=(FragmentTwo.WordOfTheDay)data.get(getAbsoluteAdapterPosition());
                        saveWord(mDay.getWordOfTheDayJson());
                        iv_save.setImageResource(R.drawable.ic_react_love);
                    }

                }
            });
            iv_word.setOnClickListener(v1 -> {
                Intent intent =new Intent(c, SaveWordActivity.class);
                c.startActivity(intent);
            });

            tv_detail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FragmentTwo.WordOfTheDay mDay=(FragmentTwo.WordOfTheDay)data.get(getAbsoluteAdapterPosition());
                    Intent intent=new Intent(c, WordDetailActivity.class);
                    intent.putExtra("word",mDay.getWordOfTheDayJson());
                    c.startActivity(intent);
                }
            });
        }

        private String wordSavingChecker(){
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            int month=calendar.get(Calendar.MONTH);
            int day=calendar.get((Calendar.DAY_OF_MONTH));

            return "checker"+month+day;
        }

        private void saveWord(String wordJSON){
            ContentValues cv=new ContentValues();
            cv.put("wordJSON",wordJSON);
            cv.put("time",System.currentTimeMillis()+"");
            db.insert("SaveWord",null,cv);

            editor.putString("wordSavingChecker",wordSavingChecker());
            editor.apply();

            Toast.makeText(c,"Saved",Toast.LENGTH_SHORT).show();

        }
    }




    //this method set the word of the day from the sever
    @SuppressLint("SetTextI18n")
    private void setWordOfTheDay(TextView tv_main, ImageView iv, String response){
        try{
            JSONArray ja=new JSONArray(response);
            JSONObject jo=ja.getJSONObject(0);
            String english=jo.getString("korea");
            String myanmar=jo.getString("myanmar");
            String speech=jo.getString("speech");
            String example=jo.getString("example");
            String thumb=jo.getString("thumb");
            tv_main.setText(english+"\n( "+speech+" )\n"+AppHandler.setMyanmar(myanmar));

            AppHandler.setPhotoFromRealUrl(iv,thumb);


        }catch (Exception e){

        }
    }


    public class TitleHolder extends RecyclerView.ViewHolder{

        TextView tv;
        public TitleHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            tv=itemView.findViewById(R.id.tv_item_title);
        }
    }


}
