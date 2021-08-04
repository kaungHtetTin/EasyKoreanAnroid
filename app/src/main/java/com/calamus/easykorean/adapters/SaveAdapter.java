package com.calamus.easykorean.adapters;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.RecyclerView;

import com.calamus.easykorean.CommentActivity;
import com.calamus.easykorean.MyYouTubeVideoActivity;
import com.calamus.easykorean.R;
import com.calamus.easykorean.app.AppHandler;
import com.calamus.easykorean.app.SQLiteHandler;
import com.calamus.easykorean.models.SaveModel;

import java.util.ArrayList;

import static com.calamus.easykorean.app.AppHandler.setMyanmar;

public class SaveAdapter extends RecyclerView.Adapter<SaveAdapter.Holder> {

    private final Activity c;
    private final ArrayList<SaveModel> data;
    private final LayoutInflater mInflater;
    @SuppressLint("SimpleDateFormat")


    public SaveAdapter(Activity c, ArrayList<SaveModel> data) {
        this.data = data;
        this.c = c;
        this.mInflater = LayoutInflater.from(c);

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public SaveAdapter.Holder onCreateViewHolder( ViewGroup parent, int viewType) {

        View view = mInflater.inflate(R.layout.item_save, parent, false);
        return new Holder(view);

    }

    @Override
    public void onBindViewHolder(final SaveAdapter.Holder holder, final int i) {
        SaveModel saveModel=data.get(i);

        if(saveModel.getPost_body().length()>90){
            String sub=saveModel.getPost_body().substring(0,90);
            holder.tv_body.setText(sub+"...");
        }else {
            holder.tv_body.setText(setMyanmar(saveModel.getPost_body()));
        }

        holder.tv_postOwner.setText(setMyanmar(saveModel.getOwner_name()));

        if(!saveModel.getPost_image().equals("")){
            AppHandler.setPhotoFromRealUrl(holder.iv_save,saveModel.getPost_image());
        }else if(!saveModel.getOwner_image().equals("")){
            AppHandler.setPhotoFromRealUrl(holder.iv_save,saveModel.getOwner_image());
        }
    }


    public class Holder extends RecyclerView.ViewHolder {
        TextView tv_body,tv_postOwner;
        ImageView iv_save;
        ImageButton iv_more;
        public Holder(View view) {
            super(view);
            iv_save=view.findViewById(R.id.iv_save);
            tv_body=view.findViewById(R.id.tv_body);
            tv_postOwner=view.findViewById(R.id.tv_postOwner);
            iv_more=view.findViewById(R.id.bt_iv_more);

            iv_save.setClipToOutline(true);

            view.setOnClickListener(v -> {
                SaveModel model=data.get(getAbsoluteAdapterPosition());
                go(model);

            });

            iv_more.setOnClickListener(v -> {
                SaveModel model=data.get(getAbsoluteAdapterPosition());
                showMenu(v,model,getAbsoluteAdapterPosition());
            });

        }

    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    private void go(SaveModel model){
        if(model.getIsVideo().equals("1")){
            String videoId=model.getPost_image().substring(model.getPost_image().indexOf("vi/")+3);
            videoId=videoId.substring(0,videoId.length()-6);
            Intent intent=new Intent(c, MyYouTubeVideoActivity.class);
            intent.putExtra("videoTitle","");
            intent.putExtra("videoId",videoId);
            intent.putExtra("time",Long.parseLong(model.getPost_id()));
            c.startActivity(intent);

        }else {
            Intent intent=new Intent(c, CommentActivity.class);
            intent.putExtra("postId",model.getPost_id());
            intent.putExtra("time","");//for comment seen
            c.startActivity(intent);
        }
    }

    private void showMenu(View v, SaveModel model,int position){
        PopupMenu popup=new PopupMenu(c,v);

        popup.getMenuInflater().inflate(R.menu.remove_save,popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            int id=item.getItemId();
            if(id==R.id.remove){
                String dbdir=c.getFilesDir().getPath()+"/databases/";
                String dbPath=dbdir+"post.db";
                SQLiteHandler.deleteRowRromTable(dbPath,"SavePost","post_id",model.getPost_id());
                Toast.makeText(c,"Removed",Toast.LENGTH_SHORT).show();
                data.remove(position);
                notifyDataSetChanged();
            }

            return true;
        });
        popup.show();
    }

}