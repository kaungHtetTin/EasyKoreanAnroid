package com.calamus.easykorean.adapters;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
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
import java.util.ArrayList;

public class SongLocalAdapter extends RecyclerView.Adapter<SongLocalAdapter.Holder> {

    private final Activity c;
    private final ArrayList<SongModel> data;
    private final LayoutInflater mInflater;

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
        String tempName=data.get(holder.getAbsoluteAdapterPosition()).getTitle();
        String songFileName=tempName.substring(0,tempName.length()-4);
        String songName=songFileName.substring(0,songFileName.indexOf("("));
        String artistName=songFileName.substring(songFileName.indexOf("(")+1,songFileName.length()-1);
        holder.tv_file_name.setText(songName);

        artistName=artistName.replace(".","");
        artistName=artistName.replace("_"," ");
        holder.tv_artist_name.setText(artistName);


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

        ImageView music_img,iv_menuMore;
        TextView tv_file_name,tv_artist_name;
        public Holder(View v) {
            super(v);

            music_img=v.findViewById(R.id.music_img);
            tv_file_name=v.findViewById(R.id.tv_songName);
            tv_artist_name=v.findViewById(R.id.tv_artistName);
            iv_menuMore=v.findViewById(R.id.iv_menuMore);

            music_img.setClipToOutline(true);

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent(c, PlayerActivity.class);
                    intent.putExtra("position",getAbsoluteAdapterPosition());
                    c.startActivity(intent);
                }
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