package com.calamus.easykorean.adapters;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.calamus.easykorean.R;
import com.calamus.easykorean.app.AppHandler;
import com.calamus.easykorean.app.SQLiteHandler;
import com.calamus.easykorean.models.SaveWordModel;
import org.json.JSONArray;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class SaveWordAdapter extends RecyclerView.Adapter<SaveWordAdapter.Holder> {

    private final Activity c;
    private final ArrayList<SaveWordModel> data;
    private final LayoutInflater mInflater;
    @SuppressLint("SimpleDateFormat")
    SimpleDateFormat sdf= new SimpleDateFormat("MMMdd ,yyyy HH:mm");

    public SaveWordAdapter(Activity c, ArrayList<SaveWordModel> data) {
        this.data = data;
        this.c = c;
        this.mInflater = LayoutInflater.from(c);


    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public SaveWordAdapter.Holder onCreateViewHolder( ViewGroup parent, int viewType) {

        View view = mInflater.inflate(R.layout.item_wordday, parent, false);
        return new Holder(view);

    }

    @Override
    public void onBindViewHolder(final SaveWordAdapter.Holder holder, final int i) {
        SaveWordModel model=data.get(i);

        String json=model.getJson();
        setWordOfTheDay(holder.tv_main,holder.tv_example,holder.iv_word,json);
        holder.tv_save.setText("Remove");
        holder.tv_savedList.setVisibility(View.INVISIBLE);

        long time=Long.parseLong(model.getTime());
        Date resultDate=new Date(time);
        holder.tv_date.setText(sdf.format(resultDate));

        holder.tv_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveWordModel model=data.get(i);
                showMenu(v,model,i);
            }
        });
    }


    public class Holder extends RecyclerView.ViewHolder {
        TextView tv_date,tv_main, tv_example;
        ImageView iv_word;
        TextView tv_save,tv_savedList;
        CardView cardView;

        public Holder(View v) {
            super(v);

            cardView=v.findViewById(R.id.card_View);
            tv_date=v.findViewById(R.id.tv_date);
            tv_example=v.findViewById(R.id.tv_example);
            tv_main=v.findViewById(R.id.tv_main);
            iv_word=v.findViewById(R.id.iv_word_of_the_day);
            tv_save=v.findViewById(R.id.tv_save);
            tv_savedList=v.findViewById(R.id.tv_saveList);

            v.setOnClickListener(v1 -> {

            });

        }

    }



    private void setWordOfTheDay(TextView tv_main, TextView tv_example, ImageView iv, String response){
        try{
            JSONArray ja=new JSONArray(response);
            JSONObject jo=ja.getJSONObject(0);
            String korea=jo.getString("korea");
            String myanmar=jo.getString("myanmar");
            String speech=jo.getString("speech");
            String example=jo.getString("example");
            String thumb=jo.getString("thumb");
            tv_main.setText(korea+"     ( "+speech+" )     "+ AppHandler.setMyanmar(myanmar));
            tv_example.setText(AppHandler.setMyanmar(example));

            AppHandler.setPhotoFromRealUrl(iv,thumb);



        }catch (Exception e){
            tv_example.setText("Word of the day is not available now for a while");
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    private void showMenu(View v, SaveWordModel model, int position){
        PopupMenu popup=new PopupMenu(c,v);

        popup.getMenuInflater().inflate(R.menu.remove_save,popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            int id=item.getItemId();
            if(id==R.id.remove){
                String dbdir=c.getFilesDir().getPath()+"/databases/";
                String dbPath=dbdir+"post.db";
                SQLiteHandler.deleteRowRromTable(dbPath,"SaveWord","id",model.getId()+"");
                Toast.makeText(c,"Removed",Toast.LENGTH_SHORT).show();
                data.remove(position);
                notifyDataSetChanged();
            }

            return true;
        });
        popup.show();
    }

}