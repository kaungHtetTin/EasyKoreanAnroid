package com.calamus.easykorean.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.calamus.easykorean.R;
import com.calamus.easykorean.SongDetailActivity;
import com.calamus.easykorean.models.SongOnlineModel;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import static com.calamus.easykorean.app.AppHandler.reactFormat;
import static com.calamus.easykorean.app.AppHandler.setPhotoFromRealUrl;

public class SongPopularAdapter extends RecyclerView.Adapter<SongPopularAdapter.Holder> {

    private final Activity c;
    private final ArrayList<SongOnlineModel> data;
    private final LayoutInflater mInflater;
    SharedPreferences sharedPreferences;
    String currentUserId;
    boolean isVip;
    Executor postExecute;

    public SongPopularAdapter(Activity c, ArrayList<SongOnlineModel> data) {
        this.data = data;
        this.c = c;
        this.mInflater = LayoutInflater.from(c);
        postExecute= ContextCompat.getMainExecutor(c);
        sharedPreferences=c.getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        currentUserId=sharedPreferences.getString("phone","000");
        isVip=sharedPreferences.getBoolean("isVIP",false);

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public SongPopularAdapter.Holder onCreateViewHolder( ViewGroup parent, int viewType) {

        View view = mInflater.inflate(R.layout.item_song_popular, parent, false);
        return new Holder(view);

    }

    @Override
    public void onBindViewHolder(final SongPopularAdapter.Holder holder, final int i) {
        SongOnlineModel model=data.get(i);
        holder.tv_title.setText(model.getTitle());
        String imageUrl="https://www.calamuseducation.com/uploads/songs/image/"+model.getUrl()+".png";
        setPhotoFromRealUrl(holder.iv_song,imageUrl);
        int reactCount=Integer.parseInt(model.getLikeCount());
        if(reactCount==0)holder.tv_reactCount.setText("");
        else holder.tv_reactCount.setText(reactFormat(reactCount));

        holder.iv_react.setBackgroundResource(R.drawable.ic_normal_react);
        if(model.getIsLike().equals("1")){
            holder.iv_react.setBackgroundResource(R.drawable.ic_react_love);
        }
    }


    public class Holder extends RecyclerView.ViewHolder {
        ImageView iv_song,iv_react;
        TextView tv_title;
        TextView tv_reactCount;
        CardView cardReact;

        public Holder(View v) {
            super(v);
            iv_song=v.findViewById(R.id.songImage);
            iv_react=v.findViewById(R.id.iv_react);
            tv_title=v.findViewById(R.id.tv_title);
            cardReact=v.findViewById(R.id.card_react);
            tv_reactCount=v.findViewById(R.id.tv_reactCount);

            v.setOnClickListener(view -> {
                SongOnlineModel model=data.get(getAbsoluteAdapterPosition());
                Intent intent=new Intent(c, SongDetailActivity.class);
                intent.putExtra("id",model.getId());
                intent.putExtra("title",model.getTitle());
                intent.putExtra("artist",model.getArtist());
                intent.putExtra("likeCount",model.getLikeCount());
                intent.putExtra("commentCount",model.getCommentCount());
                intent.putExtra("downloadCount",model.getDownloadCount());
                intent.putExtra("url",model.getUrl());
                intent.putExtra("isLike",model.getIsLike().equals("1"));
                intent.putExtra("userId",currentUserId);
                c.startActivity(intent);
            });
        }

    }


    @Override
    public int getItemViewType(int position) {
        return position;
    }

}