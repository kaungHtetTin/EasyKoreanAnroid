package com.calamus.easykorean.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.calamus.easykorean.R;
import com.calamus.easykorean.SongDetailActivity;
import com.calamus.easykorean.app.AppHandler;
import com.calamus.easykorean.controller.LikeController;
import com.calamus.easykorean.models.SongOnlineModel;
import com.google.android.gms.ads.MobileAds;
import java.util.ArrayList;
import static com.calamus.easykorean.app.AppHandler.reactFormat;
import static com.calamus.easykorean.app.AppHandler.setPhotoFromRealUrl;


public class SongOnlineAdapter extends RecyclerView.Adapter<SongOnlineAdapter.Holder> {

    private final Activity c;
    private final ArrayList<SongOnlineModel> data;
    private final ArrayList<SongOnlineModel> dataPopular;
    private final LayoutInflater mInflater;
    SharedPreferences sharedPreferences;
    String currentUserId;

    @SuppressLint("SimpleDateFormat")

    public SongOnlineAdapter(Activity c, ArrayList<SongOnlineModel> data,ArrayList<SongOnlineModel> dataPopular) {
        this.data = data;
        this.dataPopular=dataPopular;
        this.c = c;
        this.mInflater = LayoutInflater.from(c);
        sharedPreferences=c.getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        currentUserId=sharedPreferences.getString("phone","000");

        MobileAds.initialize(c, initializationStatus -> {});
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public SongOnlineAdapter.Holder onCreateViewHolder( ViewGroup parent, int viewType) {

        if(viewType==0){
            View view = mInflater.inflate(R.layout.item_song_popular_container, parent, false);
            return new PopularHolder(view);
        }

        View view = mInflater.inflate(R.layout.item_song_online, parent, false);
        return new Holder(view);

    }

    @Override
    public void onBindViewHolder(final SongOnlineAdapter.Holder holder, final int i) {

      if(i==0){

          PopularHolder popularHolder=(PopularHolder)holder;
          SongPopularAdapter adapter=new SongPopularAdapter(c,dataPopular);
          LinearLayoutManager lm = new LinearLayoutManager(c, LinearLayoutManager.HORIZONTAL, false);
          popularHolder.recyclerView.setLayoutManager(lm);
          popularHolder.recyclerView.setItemAnimator(new DefaultItemAnimator());
          popularHolder.recyclerView.setAdapter(adapter);
          adapter.notifyDataSetChanged();


      }else{
          SongOnlineModel model=data.get(i);

          holder.tv_title.setText(model.getTitle());
          holder.tv_artist.setText(model.getArtist());
          holder.tv_drama.setText(model.getDrama());
          String imageUrl="https://www.calamuseducation.com/uploads/songs/image/"+model.getUrl()+".png";
          setPhotoFromRealUrl(holder.iv_songImage,imageUrl);
          int reactCount=Integer.parseInt(model.getLikeCount());
          if(reactCount==0)holder.tv_reactCount.setText("");
          else holder.tv_reactCount.setText(reactFormat(reactCount));
          holder.iv_downloaded.setVisibility(View.GONE);


          int downloads=Integer.parseInt(model.getDownloadCount());
          holder.tv_downloadCount.setText(AppHandler.downloadFormat(downloads));

          holder.iv_react.setBackgroundResource(R.drawable.ic_normal_react);
          if(model.getIsLike().equals("1")){
              holder.iv_react.setBackgroundResource(R.drawable.ic_react_love);
          }

          holder.cardReact.setOnClickListener(view -> {

              if(model.getIsLike().equals("1")){
                  holder.iv_react.setBackgroundResource(R.drawable.ic_normal_react);
                  int rectCount=Integer.parseInt(model.getLikeCount());
                  rectCount--;

                  if(rectCount>0)holder.tv_reactCount.setText(reactFormat(rectCount));
                  else holder.tv_reactCount.setText("");

                  model.setIsLike("0");
                  model.setLikeCount(rectCount+"");

              }else{
                  holder.iv_react.setBackgroundResource(R.drawable.ic_react_love);
                  int rectCount=Integer.parseInt(model.getLikeCount());
                  rectCount++;
                  holder.tv_reactCount.setText(reactFormat(rectCount));
                  model.setIsLike("1");
                  model.setLikeCount(rectCount+"");
              }

              LikeController.likeTheSong(currentUserId,model.getId());

          });

      }

    }


    public class Holder extends RecyclerView.ViewHolder {

        ImageView iv_songImage,iv_react,iv_downloaded;
        TextView tv_title,tv_artist,tv_reactCount,tv_downloadCount,tv_drama;
        CardView cardReact;

        public Holder(View v) {
            super(v);

            iv_songImage=v.findViewById(R.id.songImage);
            iv_react=v.findViewById(R.id.iv_react);
            tv_title=v.findViewById(R.id.tv_title);
            tv_artist=v.findViewById(R.id.tv_artist);
            tv_reactCount=v.findViewById(R.id.tv_reactCount);
            tv_downloadCount=v.findViewById(R.id.tv_downloadCount);
            cardReact=v.findViewById(R.id.card_react);
            tv_drama=v.findViewById(R.id.tv_drama);
            iv_downloaded=v.findViewById(R.id.iv_downloadCheck);

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


    public class PopularHolder extends Holder {

        RecyclerView recyclerView;
        public PopularHolder(@NonNull View itemView) {
            super(itemView);
            recyclerView=itemView.findViewById(R.id.recycler_song_popular);
        }
    }

}