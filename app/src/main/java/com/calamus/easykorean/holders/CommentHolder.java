package com.calamus.easykorean.holders;

import static com.calamus.easykorean.app.AppHandler.formatTime;
import static com.calamus.easykorean.app.AppHandler.reactFormat;
import static com.calamus.easykorean.app.AppHandler.setMyanmar;
import static com.calamus.easykorean.app.AppHandler.setPhotoFromRealUrl;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.calamus.easykorean.MyDiscussionActivity;
import com.calamus.easykorean.PhotoActivity;
import com.calamus.easykorean.R;
import com.calamus.easykorean.app.AppHandler;
import com.calamus.easykorean.app.Routing;
import com.calamus.easykorean.controller.LikeController;
import com.calamus.easykorean.controller.MyCommentController;
import com.calamus.easykorean.controller.NotificationController;
import com.calamus.easykorean.models.CommentModel;

public class CommentHolder extends RecyclerView.ViewHolder{

    ImageView iv,iv_blueMark,iv_comment;

    TextView tv_name,tv_comment,tv_time,tv_readingMore,tv_reactCount,tv_reply;
    RelativeLayout Rlayout;
    Activity c;

    String currentUserId,currentUsername,postOwnerId,checkTime;
    public CallBack callBack;

    public CommentHolder(@NonNull View itemView, Activity c) {
        super(itemView);
        this.c=c;
        iv=itemView.findViewById(R.id.iv_small_profile);
        tv_name=itemView.findViewById(R.id.tv_username2);
        tv_comment=itemView.findViewById(R.id.tv_comment);
        tv_time=itemView.findViewById(R.id.tv_time);
        Rlayout=itemView.findViewById(R.id.bubble_layout_part);
        iv_blueMark=itemView.findViewById(R.id.iv_blueMark);
        tv_reply=itemView.findViewById(R.id.tv_reply);
        tv_readingMore=itemView.findViewById(R.id.tv_readmore);
        iv_comment=itemView.findViewById(R.id.iv_main);
        tv_reactCount=itemView.findViewById(R.id.tv_react);


        iv_comment.setClipToOutline(true);

    }

    public void setCommentModel(CommentModel model,String currentUserId,String currentUsername,String postOwnerId,String checkTime){
        this.currentUserId=currentUserId;
        this.currentUsername=currentUsername;
        this.postOwnerId=postOwnerId;
        this.checkTime=checkTime;
        setUIEvent(model);
        bindData(model);


    }

    private void bindData(CommentModel cModel){
        if(cModel.getTime().equals(checkTime)){
            Rlayout.setBackgroundResource(R.drawable.bg_notification);
        }

        tv_readingMore.setVisibility(View.GONE);

        int normalProfileSize = c.getResources().getDimensionPixelSize(R.dimen.comment_profile);
        int normalProfileMargin=c.getResources().getDimensionPixelSize(R.dimen.comment_profile_margin);
        RelativeLayout.LayoutParams layoutParams;
        if(!cModel.getParentId().equals("0")){

            layoutParams = new RelativeLayout.LayoutParams(
                    80,
                    80);
            layoutParams.setMargins( 140 , 4 , 4 , 4 ) ;
        }else{
            layoutParams = new RelativeLayout.LayoutParams(
                    normalProfileSize,
                    normalProfileSize);
            layoutParams.setMargins( normalProfileMargin , normalProfileMargin , normalProfileMargin , normalProfileMargin ) ;
        }
        iv.setLayoutParams(layoutParams);

        long time=Long.parseLong(cModel.getTime());
        tv_time.setText(formatTime(time));

        tv_name.setText(setMyanmar(cModel.getName()));

        if(cModel.getComment().length()>100){
            String sub=cModel.getComment().substring(0,100);
            tv_comment.setText(setMyanmar(sub));
            tv_readingMore.setVisibility(View.VISIBLE);
        }else{
            tv_comment.setText(setMyanmar(cModel.getComment()));
            tv_readingMore.setVisibility(View.GONE);
        }
        tv_readingMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_comment.setText(setMyanmar(cModel.getComment()));
                tv_readingMore.setVisibility(View.GONE);
            }
        });


        if(!cModel.getImageUrl().equals("")) AppHandler.setPhotoFromRealUrl(iv,cModel.getImageUrl());iv_blueMark.setVisibility(View.GONE);
        if(cModel.getIsVip().equals("1")){
            iv_blueMark.setVisibility(View.VISIBLE);

        }


        tv_reactCount.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_comment_normal_react, 0, 0);
        if(cModel.getIsLiked().equals("1")){
            tv_reactCount.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_song_love_react, 0, 0);
        }

        iv_comment.setVisibility(View.GONE);

        if(!cModel.getCommentImage().equals("")){
            iv_comment.setVisibility(View.VISIBLE);
            setPhotoFromRealUrl(iv_comment,cModel.getCommentImage());
        }


        tv_reactCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(cModel.getIsLiked().equals("1")){
                    tv_reactCount.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_comment_normal_react, 0, 0);
                    int rectCount=Integer.parseInt(cModel.getLikes());
                    rectCount--;
                    if(rectCount>0){
                        tv_reactCount.setText(reactFormat(rectCount));
                    }else {
                        tv_reactCount.setText("");
                    }


                    cModel.setIsLiked("0");
                    cModel.setLikes(rectCount+"");

                }else{
                    tv_reactCount.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_song_love_react, 0, 0);

                    int rectCount=Integer.parseInt(cModel.getLikes());
                    rectCount++;
                    tv_reactCount.setText(reactFormat(rectCount));

                    if(!cModel.getWriterId().equals(currentUserId)){
                        NotificationController notificationController=new NotificationController(c);
                        notificationController.sendNotification(currentUsername+" reacted your comment.",cModel.getWriterToken(),Routing.APP_NAME,"1");
                    }

                    cModel.setIsLiked("1");
                    cModel.setLikes(rectCount+"");
                }

                LikeController.likeTheComment(cModel.getPostId(),currentUserId,cModel.getTime());

            }
        });


        if (!cModel.getLikes().equals("0")){
            tv_reactCount.setText(reactFormat(Integer.parseInt(cModel.getLikes())));

        }else{
            tv_reactCount.setText("");
        }

    }

    private void setUIEvent(CommentModel model){
        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(c, MyDiscussionActivity.class);
                intent.putExtra("userId",model.getWriterId());
                intent.putExtra("userName",model.getWriterId());
                c.startActivity(intent);
            }
        });

        tv_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(c, MyDiscussionActivity.class);
                intent.putExtra("userId",model.getWriterId());
                intent.putExtra("userName",model.getWriterId());
                c.startActivity(intent);
            }
        });


        tv_reply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callBack != null) {
                    callBack.onReply();
                }
            }
        });

        itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showMenu(v,model,getAbsoluteAdapterPosition(),currentUserId,postOwnerId);
                return false;
            }
        });

        iv_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(c, PhotoActivity.class);
                intent.putExtra("image",model.getCommentImage());
                intent.putExtra("des",model.getComment());
                c.startActivity(intent);
            }
        });

//        tv_reactCount.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent=new Intent(c, LikeListActivity.class);
//                intent.putExtra("contentId",model.getTime());
//                intent.putExtra("fetch", Routing.FETCH_COMMENT_LIKE);
//                c.startActivity(intent);
//            }
//        });
    }

    private void showMenu(View v,CommentModel model,int position,String currentUserId,String postOwnerId){
        PopupMenu popup=new PopupMenu(c,v);

        if(("0"+model.getWriterId()).equals(currentUserId)|| model.getWriterId().equals(currentUserId)||("0"+postOwnerId).equals(currentUserId)||postOwnerId.equals(currentUserId)){
            popup.getMenuInflater().inflate(R.menu.private_comment,popup.getMenu());
        }else {
            popup.getMenuInflater().inflate(R.menu.public_comment,popup.getMenu());
        }
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @SuppressLint("Recycle")
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id=item.getItemId();
                if(id==R.id.delete_comment){
                    MyCommentController commentController=new MyCommentController("","",c);
                    commentController.deleteComment(model.getPostId(),model.getTime());
                    if (callBack != null) {
                        callBack.onCommentDelete(position);
                    }
                }else if(id==R.id.reply_comment){
                    if (callBack != null) {
                        callBack.onReply();
                    }
                }else if(id==R.id.copy_comment){
                    ClipboardManager clipboard = (ClipboardManager) c.getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("calamus", model.getComment());
                    clipboard.setPrimaryClip(clip);
                }
                return true;
            }
        });
        popup.show();
    }

    public void setCallBack(CallBack callBack) {
        this.callBack = callBack;
    }

    public interface  CallBack{
        void onCommentDelete(int position);
        void onReply();
    }

}
