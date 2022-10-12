package com.calamus.easykorean.adapters;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.calamus.easykorean.ChattingActivity;
import com.calamus.easykorean.MyDiscussionActivity;
import com.calamus.easykorean.PhotoActivity;
import com.calamus.easykorean.R;
import com.calamus.easykorean.app.AppHandler;
import com.calamus.easykorean.models.ChatModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import me.myatminsoe.mdetect.MDetect;

import static com.calamus.easykorean.app.AppHandler.setMyanmar;


public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.Holder> {

    private final Activity c;
    private final ArrayList<ChatModel> data;
    private final LayoutInflater mInflater;
    final SharedPreferences sharedPreferences;
    final String myId;
    @SuppressLint("SimpleDateFormat")
    final
    SimpleDateFormat sdf= new SimpleDateFormat("MMMdd ,yyyy HH:mm");
    public ChatAdapter(Activity c, ArrayList<ChatModel> data){
        this.data=data;
        this.c=c;
        this.mInflater=LayoutInflater.from(c);
        sharedPreferences=c.getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        myId=Long.parseLong(sharedPreferences.getString("phone",null))+"";
        MDetect.INSTANCE.init(c);
    }


    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=mInflater.inflate(R.layout.item_message,parent,false);

        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        try{


            final ChatModel model=data.get(position);
            holder.tv_msgBody.setVisibility(View.GONE);

            if(!model.getMsg().equals("")){
                holder.tv_msgBody.setText(setMyanmar(model.getMsg()));
                holder.tv_msgBody.setVisibility(View.VISIBLE);
            }


            Date resultDate=new Date(model.getTime());
            holder.tv_time.setText(sdf.format(resultDate));
            holder.tv_time.setVisibility(View.GONE);
            holder.iv_seen.setVisibility(View.INVISIBLE);
            holder.iv_msg.setVisibility(View.GONE);
            holder.iv_card.setVisibility(View.GONE);
            Log.e("MyId : ",myId);
            Log.e("senderId : ",model.getSenderId());
            if(position!=data.size()-1)holder.tv_receive.setVisibility(View.GONE);
            if(myId.equals(model.getSenderId())){

                holder.iv_msg_profile.setVisibility(View.INVISIBLE);
                holder.msgContainer.setGravity(Gravity.RIGHT);
                holder.bubbleLayout.setBackgroundResource(R.drawable.my_msg);
                holder.bubbleLayout.setGravity(Gravity.RIGHT);
                holder.tv_msgBody.setTextColor(Color.WHITE);

                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)holder.iv_card.getLayoutParams();
                // params.addRule(RelativeLayout.ALIGN_PARENT_END);
                params.addRule(RelativeLayout.START_OF, R.id.iv_seen);
                holder.iv_card.setLayoutParams(params);

                if(model.getSeen()==1){
                    holder.iv_seen.setVisibility(View.VISIBLE);
                    holder.tv_receive.setVisibility(View.GONE);

                }else{
                    holder.iv_seen.setVisibility(View.INVISIBLE);

                    ChatModel model1=data.get(position+1);
                    if(model.getSeen()!=model1.getSeen()&&model1.getSenderId().equals(myId))holder.tv_receive.setVisibility(View.VISIBLE);
                    if(position==data.size()-1)holder.tv_receive.setVisibility(View.VISIBLE);
                }

            }else{
                holder.msgContainer.setGravity(Gravity.LEFT);
                holder.bubbleLayout.setBackgroundResource(R.drawable.other_msg);
                holder.bubbleLayout.setGravity(Gravity.LEFT);
                holder.tv_receive.setVisibility(View.GONE);

                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)holder.iv_card.getLayoutParams();
                params.addRule(RelativeLayout.ALIGN_PARENT_START);
                holder.iv_card.setLayoutParams(params);

                holder.tv_msgBody.setTextColor(Color.parseColor("#403F3F"));

                if(position>=1){
                    ChatModel model1=data.get(position-1);
                    if(!model.getSenderId().equals(model1.getSenderId())){
                        AppHandler.setPhotoFromRealUrl(holder.iv_msg_profile, ChattingActivity.fImage);
                        holder.iv_msg_profile.setVisibility(View.VISIBLE);
                    }else {
                        holder.iv_msg_profile.setVisibility(View.INVISIBLE);
                    }

                    if(model.getTime()==model1.getTime()){
                        data.remove(position);
                        notifyDataSetChanged();
                    }

                }else {
                    holder.iv_msg_profile.setVisibility(View.INVISIBLE);
                }

            }

            if(position>1){
                ChatModel model1=data.get(position-1);
                if(!model.getSenderId().equals(model1.getSenderId())&&model.getTime()-model1.getTime()>300000){
                    holder.tv_time.setVisibility(View.VISIBLE);
                }
                if(model.getTime()-model1.getTime()>60*30000){
                    holder.tv_time.setVisibility(View.VISIBLE);
                }
            }


            if(!model.getImageUrl().equals("")){
                AppHandler.setPhotoFromRealUrl(holder.iv_msg,model.getImageUrl());
                holder.iv_msg.setVisibility(View.VISIBLE);
                holder.iv_card.setVisibility(View.VISIBLE);


            }else{
                holder.iv_msg.setVisibility(View.GONE);
                holder.iv_card.setVisibility(View.GONE);
            }


        }catch (Exception e){

        }


    }

    public class Holder extends RecyclerView.ViewHolder{
        final RelativeLayout msgContainer;
        final LinearLayout bubbleLayout;
        final LinearLayout tv_receive;
        final TextView tv_msgBody;
        final TextView tv_time;
        final CardView iv_card;
        final ImageView iv_msg_profile;
        final ImageView iv_seen;
        final ImageView iv_msg;

        public Holder(View view){
            super(view);
            msgContainer=view.findViewById(R.id.bubble_layout_part);
            bubbleLayout=view.findViewById(R.id.bubble_layout);
            tv_msgBody=view.findViewById(R.id.message_body);
            tv_time=view.findViewById(R.id.msg_time);
            iv_msg_profile=view.findViewById(R.id.iv_message_profile);
            iv_seen=view.findViewById(R.id.iv_seen);
            tv_receive=view.findViewById(R.id.tv_received);
            iv_msg=view.findViewById(R.id.iv_main);
            iv_card=view.findViewById(R.id.iv_card);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    tv_time.setVisibility(View.VISIBLE);
                }
            });

            iv_msg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ChatModel model=data.get(getAbsoluteAdapterPosition());
                    Intent intent=new Intent(c, PhotoActivity.class);
                    intent.putExtra("image",model.getImageUrl());
                    intent.putExtra("des","");
                    c.startActivity(intent);
                }
            });

            iv_msg_profile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ChatModel model=data.get(getAbsoluteAdapterPosition());
                    Intent intent=new Intent(c, MyDiscussionActivity.class);
                    intent.putExtra("userId",model.getSenderId()); // give fri Id
                    intent.putExtra("userName","Discussion");
                    c.startActivity(intent);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }



}
