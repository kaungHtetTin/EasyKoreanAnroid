package com.calamus.easykorean.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.calamus.easykorean.R;
import com.calamus.easykorean.SongDetailActivity;
import com.calamus.easykorean.app.AppHandler;
import com.calamus.easykorean.controller.LikeController;
import com.calamus.easykorean.models.SongOnlineModel;

import java.util.ArrayList;

import static com.calamus.easykorean.app.AppHandler.reactFormat;
import static com.calamus.easykorean.app.AppHandler.setPhotoFromRealUrl;


public class SongOnlineAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Activity c;
    private final ArrayList<Object> data;
    private final ArrayList<SongOnlineModel> dataPopular;
    private final LayoutInflater mInflater;
    SharedPreferences sharedPreferences;
    String currentUserId;

    @SuppressLint("SimpleDateFormat")

    public SongOnlineAdapter(Activity c, ArrayList<Object> data,ArrayList<SongOnlineModel> dataPopular) {
        this.data = data;
        this.dataPopular=dataPopular;
        this.c = c;
        this.mInflater = LayoutInflater.from(c);
        sharedPreferences=c.getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        currentUserId=sharedPreferences.getString("phone","000");

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder( ViewGroup parent, int viewType) {

        if(viewType==0){
            View view = mInflater.inflate(R.layout.item_song_popular_container, parent, false);
            return new PopularHolder(view);
        }else{
            View view = mInflater.inflate(R.layout.item_song_online, parent, false);
            return new Holder(view);
        }

    }

    @Override
    public int getItemViewType(int position) {
        Object o = data.get(position);
        if(o instanceof SongOnlineModel){
            return 1;
        }else{
            return 0;
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int i) {
        Object o = data.get(i);
        if(! (o instanceof  SongOnlineModel)){
            PopularHolder popularHolder=(PopularHolder)holder;
            SongPopularAdapter adapter=new SongPopularAdapter(c,dataPopular);
            LinearLayoutManager lm = new LinearLayoutManager(c, LinearLayoutManager.HORIZONTAL, false);
            popularHolder.recyclerView.setLayoutManager(lm);
            popularHolder.recyclerView.setItemAnimator(new DefaultItemAnimator());
            popularHolder.recyclerView.setAdapter(adapter);
            adapter.notifyDataSetChanged();

        }else{
            SongOnlineModel model=(SongOnlineModel) data.get(i);
            Holder songHolder=(Holder)holder;
            songHolder.tv_title.setText(model.getTitle());
            songHolder.tv_description.setText(model.getArtist()+" "+model.getDrama());

            songHolder.tv_title.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            songHolder.tv_title.setMarqueeRepeatLimit(-1);
            songHolder.tv_title.setSingleLine(true);
            songHolder.tv_title.setSelected(true);

            songHolder.tv_description.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            songHolder.tv_description.setMarqueeRepeatLimit(-1);
            songHolder.tv_description.setSingleLine(true);
            songHolder.tv_description.setSelected(true);

            String imageUrl="https://www.calamuseducation.com/uploads/songs/image/"+model.getUrl()+".png";
            setPhotoFromRealUrl(songHolder.iv_songImage,imageUrl);
            int reactCount=Integer.parseInt(model.getLikeCount());
            if(reactCount==0)songHolder.tv_reactCount.setText("");
            else songHolder.tv_reactCount.setText(reactFormat(reactCount));


            int downloads=Integer.parseInt(model.getDownloadCount());
            songHolder.tv_downloadCount.setText(AppHandler.reactFormat(downloads));

            songHolder.tv_reactCount.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_song_normal_react, 0, 0, 0);
            if(model.getIsLike().equals("1")){
                songHolder.tv_reactCount.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_song_love_react, 0, 0, 0);
            }

            songHolder.tv_reactCount.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if(model.getIsLike().equals("1")){
                        songHolder.tv_reactCount.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_song_normal_react, 0, 0, 0);
                        int rectCount=Integer.parseInt(model.getLikeCount());
                        rectCount--;

                        if(rectCount>0)songHolder.tv_reactCount.setText(reactFormat(rectCount));
                        else songHolder.tv_reactCount.setText("");

                        model.setIsLike("0");
                        model.setLikeCount(rectCount+"");

                    }else{
                        songHolder.tv_reactCount.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_song_love_react, 0, 0, 0);
                        int rectCount=Integer.parseInt(model.getLikeCount());
                        rectCount++;
                        songHolder.tv_reactCount.setText(reactFormat(rectCount));
                        model.setIsLike("1");
                        model.setLikeCount(rectCount+"");
                    }

                    LikeController.likeTheSong(currentUserId,model.getId());

                }
            });

        }

    }

    public class Holder extends RecyclerView.ViewHolder {

        ImageView iv_songImage;
        TextView tv_title,tv_description,tv_reactCount,tv_downloadCount;

        public Holder(View v) {
            super(v);

            iv_songImage=v.findViewById(R.id.songImage);
            tv_title=v.findViewById(R.id.tv_info_header);
            tv_description=v.findViewById(R.id.tv_description);
            tv_reactCount=v.findViewById(R.id.tv_react);
            tv_downloadCount=v.findViewById(R.id.tv_downloadCount);


            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SongOnlineModel model=(SongOnlineModel) data.get(getAbsoluteAdapterPosition());
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
                }
            });
        }
    }

    public class PopularHolder extends Holder {

        RecyclerView recyclerView;
        public PopularHolder(@NonNull View itemView) {
            super(itemView);
            recyclerView=itemView.findViewById(R.id.recycler_song_popular);
        }
    }

}