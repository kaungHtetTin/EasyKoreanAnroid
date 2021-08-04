package com.calamus.easykorean.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.calamus.easykorean.CommentActivity;
import com.calamus.easykorean.MyYouTubeVideoActivity;
import com.calamus.easykorean.R;
import com.calamus.easykorean.app.AppHandler;
import com.calamus.easykorean.models.NotiModel;
import java.util.ArrayList;
import de.hdodenhof.circleimageview.CircleImageView;
import me.myatminsoe.mdetect.MDetect;

public class NotiAdapter extends RecyclerView.Adapter<NotiAdapter.Holder> {

    private final Activity c;
    private final ArrayList<NotiModel> data;
    private final LayoutInflater mInflater;
    @SuppressLint("SimpleDateFormat")

    public NotiAdapter(Activity c, ArrayList<NotiModel> data) {
        this.data = data;
        this.c = c;
        this.mInflater = LayoutInflater.from(c);
        MDetect.INSTANCE.init(c);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public NotiAdapter.Holder onCreateViewHolder( ViewGroup parent, int viewType) {

        View view = mInflater.inflate(R.layout.item_notification, parent, false);
        return new Holder(view);

    }


    @Override
    public void onBindViewHolder(final NotiAdapter.Holder holder, final int i) {

        final NotiModel model=data.get(i);
        String name=getColoredSpanned(AppHandler.setMyanmar(model.getWriterName()),"#000000");
        String subContent;
        if(model.getPostBody().length()>26){
            subContent=model.getPostBody().substring(0,25);
        }else {
            subContent=model.getPostBody();
        }

        String content=getColoredSpanned(model.getAction()+"\n"+subContent,"#777777");
        holder.tv_noti.setText(Html.fromHtml(name+" "+content));

        long time=Long.parseLong(model.getTime());
        holder.tv_time.setText(AppHandler.formatTime(time));

        if(model.getSeen().equals("1")){
            holder.rLayout.setBackgroundColor(Color.WHITE);
        }else if(model.getSeen().equals("2")){
            holder.rLayout.setBackgroundColor(Color.parseColor("#FDF7E5"));
        }else {
            holder.rLayout.setBackgroundColor(Color.parseColor("#E7F3FF"));
        }

        if(model.getColor().equals("2")){
            holder.rLayout.setBackgroundColor(Color.parseColor("#FDF7E5"));
        }

        if(!model.getWriterImage().equals(""))AppHandler.setPhotoFromRealUrl(holder.iv,model.getWriterImage());
        holder.iv_blueMark.setVisibility(View.GONE);
        if(model.getIsVip().equals("1"))holder.iv_blueMark.setVisibility(View.VISIBLE);

        holder.iv_notiIcon.setBackgroundResource(AppHandler.setNotificationIcon(Integer.parseInt(model.getColor())));
    }

    public class Holder extends RecyclerView.ViewHolder {

        CircleImageView iv;
        ImageView iv_blueMark,iv_notiIcon;
        TextView tv_noti;
        TextView tv_time;
        RelativeLayout rLayout;
        public Holder(View view) {
            super(view);
            iv=view.findViewById(R.id.iv_noti);
            tv_noti=view.findViewById(R.id.tv_orderNoti);
            tv_time=view.findViewById(R.id.tv_notiTime);
            rLayout=view.findViewById(R.id.orderNoti_layout);
            iv_blueMark=view.findViewById(R.id.iv_blueMark);
            iv_notiIcon=view.findViewById(R.id.iv_notiAction);
            view.setOnClickListener(v -> {
                final NotiModel model=data.get(getAbsoluteAdapterPosition());

                if(model.getHas_video().equals("1")){
                    rLayout.setBackgroundColor(Color.WHITE);
                    String videoId=model.getPostImage().substring(model.getPostImage().indexOf("vi/")+3);
                    videoId=videoId.substring(0,videoId.length()-6);
                    Intent intent=new Intent(c, MyYouTubeVideoActivity.class);
                    intent.putExtra("videoTitle","");
                    intent.putExtra("videoId",videoId);
                    intent.putExtra("time",Long.parseLong(model.getPostId()));
                    intent.putExtra("cmtTime",model.getTime());
                    c.startActivity(intent);

                }else {
                    rLayout.setBackgroundColor(Color.WHITE);
                    Intent intent=new Intent(c, CommentActivity.class);
                    intent.putExtra("postId",model.getPostId());
                    intent.putExtra("time",model.getTime());
                    c.startActivity(intent);
                }

            });


        }

    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    private String getColoredSpanned(String text,String color){
        return "<font color="+color+">"+text+"</font>";
    }

}