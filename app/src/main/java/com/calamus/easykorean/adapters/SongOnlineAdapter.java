package com.calamus.easykorean.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.calamus.easykorean.R;
import com.calamus.easykorean.SongDetailActivity;
import com.calamus.easykorean.app.AppHandler;
import com.calamus.easykorean.controller.LikeController;
import com.calamus.easykorean.models.AdModel;
import com.calamus.easykorean.models.SongOnlineModel;

import java.util.ArrayList;

import static com.calamus.easykorean.app.AppHandler.myAdClick;
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

        MobileAds.initialize(c,new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {}
        });
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
        }else if(viewType==1){
            View view = mInflater.inflate(R.layout.item_song_online, parent, false);
            return new Holder(view);
        }else{
            View view = mInflater.inflate(R.layout.item_appads, parent, false);
            return new AdHolder(view);
        }



    }

    @Override
    public int getItemViewType(int position) {
        if(position==0){
            return 0;
        }else if(data.get(position) instanceof SongOnlineModel){
            return  1;

        }else{
            return 2;
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int i) {

        if(i==0){

            PopularHolder popularHolder=(PopularHolder)holder;
            SongPopularAdapter adapter=new SongPopularAdapter(c,dataPopular);
            LinearLayoutManager lm = new LinearLayoutManager(c, LinearLayoutManager.HORIZONTAL, false);
            popularHolder.recyclerView.setLayoutManager(lm);
            popularHolder.recyclerView.setItemAnimator(new DefaultItemAnimator());
            popularHolder.recyclerView.setAdapter(adapter);
            adapter.notifyDataSetChanged();


        }else{
            if(data.get(i) instanceof SongOnlineModel){
                SongOnlineModel model=(SongOnlineModel) data.get(i);
                Holder songHolder=(Holder)holder;
                songHolder.tv_title.setText(model.getTitle());
                songHolder.tv_artist.setText(model.getArtist());
                songHolder.tv_drama.setText(model.getDrama());
                String imageUrl="https://www.calamuseducation.com/uploads/songs/image/"+model.getUrl()+".png";
                setPhotoFromRealUrl(songHolder.iv_songImage,imageUrl);
                int reactCount=Integer.parseInt(model.getLikeCount());
                if(reactCount==0)songHolder.tv_reactCount.setText("");
                else songHolder.tv_reactCount.setText(reactFormat(reactCount));
                songHolder.iv_downloaded.setVisibility(View.GONE);


                int downloads=Integer.parseInt(model.getDownloadCount());
                songHolder.tv_downloadCount.setText(AppHandler.downloadFormat(downloads));

                songHolder.iv_react.setBackgroundResource(R.drawable.ic_normal_react);
                if(model.getIsLike().equals("1")){
                    songHolder.iv_react.setBackgroundResource(R.drawable.ic_react_love);
                }

                songHolder.cardReact.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if(model.getIsLike().equals("1")){
                            songHolder.iv_react.setBackgroundResource(R.drawable.ic_normal_react);
                            int rectCount=Integer.parseInt(model.getLikeCount());
                            rectCount--;

                            if(rectCount>0)songHolder.tv_reactCount.setText(reactFormat(rectCount));
                            else songHolder.tv_reactCount.setText("");

                            model.setIsLike("0");
                            model.setLikeCount(rectCount+"");

                        }else{
                            songHolder.iv_react.setBackgroundResource(R.drawable.ic_react_love);
                            int rectCount=Integer.parseInt(model.getLikeCount());
                            rectCount++;
                            songHolder.tv_reactCount.setText(reactFormat(rectCount));
                            model.setIsLike("1");
                            model.setLikeCount(rectCount+"");
                        }

                        LikeController.likeTheSong(currentUserId,model.getId());

                    }
                });

            }else if(data.get(i) instanceof AdModel){
                AdHolder adHolder=(AdHolder)holder;
                AdModel model=(AdModel)data.get(i);
                adHolder.tvAppName.setText(model.getAppName());
                adHolder.tvAppDes.setText(model.getAppDes());
                setPhotoFromRealUrl(adHolder.ivCover,model.getAppCover());
                setPhotoFromRealUrl(adHolder.ivIcon,model.getAppIcon());

                if(!model.getType().equals("")){
                    adHolder.btAppInstall.setVisibility(View.VISIBLE);
                    adHolder.btAppInstall.setText(model.getType());
                }else {
                    adHolder.btAppInstall.setVisibility(View.GONE);
                }

                adHolder.btAppInstall.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        c.startActivity(new Intent(Intent.ACTION_VIEW,
                                Uri.parse(model.getLink())));
                        myAdClick(model.getId());
                    }
                });
            }
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

    public class AdHolder extends RecyclerView.ViewHolder{

        ImageView ivCover,ivIcon;
        TextView tvAppName,tvAppDes;
        Button btAppInstall;
        public AdHolder(View view){
            super(view);
            ivCover=view.findViewById(R.id.iv_appCover);
            ivIcon=view.findViewById(R.id.iv_appIcon);
            tvAppName=view.findViewById(R.id.tv_appName);
            tvAppDes=view.findViewById(R.id.tv_appDes);
            btAppInstall=view.findViewById(R.id.bt_appInstall);
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