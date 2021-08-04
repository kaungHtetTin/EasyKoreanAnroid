package com.calamus.easykorean.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.RecyclerView;
import com.calamus.easykorean.PlayerActivity;
import com.calamus.easykorean.R;
import com.calamus.easykorean.app.MyDialog;
import com.calamus.easykorean.models.SongModel;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class SongLocalAdapter extends RecyclerView.Adapter<SongLocalAdapter.Holder> {

    private final Activity c;
    private final ArrayList<SongModel> data;
    private final LayoutInflater mInflater;
    @SuppressLint("SimpleDateFormat")
    SimpleDateFormat sdf= new SimpleDateFormat("MMMdd ,yyyy HH:mm");

    public SongLocalAdapter(Activity c, ArrayList<SongModel> data) {
        this.data = data;
        this.c = c;
        this.mInflater = LayoutInflater.from(c);


    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public SongLocalAdapter.Holder onCreateViewHolder( ViewGroup parent, int viewType) {

        View view = mInflater.inflate(R.layout.item_song_local, parent, false);
        return new Holder(view);

    }

    @Override
    public void onBindViewHolder(final SongLocalAdapter.Holder holder, final int i) {
        String tempName=data.get(i).getTitle();
        String songFileName=tempName.substring(0,tempName.length()-4);
        holder.tv_file_name.setText(songFileName);


        holder.iv_menuMore.setOnClickListener(view -> {
            PopupMenu popupMenu=new PopupMenu(c,view);
            popupMenu.getMenuInflater().inflate(R.menu.popup,popupMenu.getMenu());
            popupMenu.show();
            popupMenu.setOnMenuItemClickListener(menuItem -> {
                switch (menuItem.getItemId()) {
                    case R.id.delete:

                        deleteSong(data.get(i).getUri(), i);
                        break;
                }
                return true;
            });
        });

    }


    public class Holder extends RecyclerView.ViewHolder {

        ImageView album_art,iv_menuMore;
        TextView tv_file_name;
        public Holder(View v) {
            super(v);

            album_art=itemView.findViewById(R.id.music_img);
            tv_file_name=itemView.findViewById(R.id.tv_songName);
            iv_menuMore=itemView.findViewById(R.id.iv_menuMore);
            v.setOnClickListener(v1 -> {
                Intent intent=new Intent(c, PlayerActivity.class);
                intent.putExtra("position",getAbsoluteAdapterPosition());
                c.startActivity(intent);
            });

        }

    }


    @Override
    public int getItemViewType(int position) {
        return position;
    }

    private void deleteSong(Uri uri, int position){

        MyDialog myDialog=new MyDialog(c, "Delete!", "Do you really want to delete this video", () -> {
            File songFile=new File(uri.getPath());
            boolean isDeleted=songFile.delete();
            if(isDeleted){
                data.remove(position);
                notifyDataSetChanged();
                Toast.makeText(c,"Deleted",Toast.LENGTH_SHORT).show();
            }
        });
        myDialog.showMyDialog();


    }


}