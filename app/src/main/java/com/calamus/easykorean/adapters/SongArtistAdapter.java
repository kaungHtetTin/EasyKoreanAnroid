package com.calamus.easykorean.adapters;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.calamus.easykorean.AlbumSongActivity;
import com.calamus.easykorean.R;
import com.calamus.easykorean.models.SongArtistModel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import static com.calamus.easykorean.app.AppHandler.setPhotoFromRealUrl;

public class SongArtistAdapter extends RecyclerView.Adapter<SongArtistAdapter.Holder> {

    private final Activity c;
    private final ArrayList<SongArtistModel> data;
    private final LayoutInflater mInflater;
    @SuppressLint("SimpleDateFormat")
    SimpleDateFormat sdf= new SimpleDateFormat("MMMdd ,yyyy HH:mm");

    public SongArtistAdapter(Activity c, ArrayList<SongArtistModel> data) {
        this.data = data;
        this.c = c;
        this.mInflater = LayoutInflater.from(c);


    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public SongArtistAdapter.Holder onCreateViewHolder( ViewGroup parent, int viewType) {

        View view = mInflater.inflate(R.layout.item_song_artist, parent, false);
        return new Holder(view);

    }

    @Override
    public void onBindViewHolder(final SongArtistAdapter.Holder holder, final int i) {
        setPhotoFromRealUrl(holder.iv,data.get(i).getUrl());
        holder.tv.setText(data.get(i).getArtist());
    }


    public class Holder extends RecyclerView.ViewHolder {

        ImageView iv;
        TextView tv;
        public Holder(View v) {
            super(v);
            iv=v.findViewById(R.id.iv_songArtist);
            tv=v.findViewById(R.id.tv_artistName);
            v.setOnClickListener(v1 -> {
                Intent intent=new Intent(c, AlbumSongActivity.class);
                intent.putExtra("artist",data.get(getAbsoluteAdapterPosition()).getArtist());
                c.startActivity(intent);
            });

        }

    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }
}