package com.calamus.easykorean.adapters;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.calamus.easykorean.LikeListActivity;
import com.calamus.easykorean.MyDiscussionActivity;
import com.calamus.easykorean.PhotoActivity;
import com.calamus.easykorean.R;
import com.calamus.easykorean.app.AppHandler;
import com.calamus.easykorean.app.Routing;
import com.calamus.easykorean.controller.LikeController;
import com.calamus.easykorean.controller.MyCommentController;
import com.calamus.easykorean.controller.NotificationController;
import com.calamus.easykorean.models.CommentModel;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import me.myatminsoe.mdetect.MDetect;
import static com.calamus.easykorean.app.AppHandler.formatTime;
import static com.calamus.easykorean.app.AppHandler.reactFormat;
import static com.calamus.easykorean.app.AppHandler.setMyanmar;
import static com.calamus.easykorean.app.AppHandler.setPhotoFromRealUrl;


public class VideoCommentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Activity c;
    private final ArrayList<Object> data;
    private final LayoutInflater mInflater;

    SharedPreferences sharedPreferences;
    String imagePath,checkTime,currentUserId,userName;
    private Callback callback;

    public VideoCommentAdapter(Activity c, ArrayList<Object> data,String checkTime,Callback callback) {
        this.data = data;
        this.c = c;
        this.checkTime=checkTime;
        this.mInflater = LayoutInflater.from(c);
        this.callback=callback;
        MDetect.INSTANCE.init(c);
        sharedPreferences=c.getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        imagePath=sharedPreferences.getString("imageUrl",null);
        currentUserId=sharedPreferences.getString("phone",null);
        userName=sharedPreferences.getString("Username",null);

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {

        View view = mInflater.inflate(R.layout.item_comment, parent, false);
        return new CommentHolder(view);
    }

    @Override
    public void onBindViewHolder(@NotNull final RecyclerView.ViewHolder holder, final int i) {

        try{
            final CommentModel cModel=(CommentModel)data.get(i);
            CommentHolder commentHolder=(CommentHolder)holder;

            if(cModel.getTime().equals(checkTime)){
                commentHolder.Rlayout.setBackgroundColor(Color.parseColor("#E7F3FF"));
            }


            long time=Long.parseLong(cModel.getTime());

            commentHolder.tv_time.setText(formatTime(time));

            commentHolder.tv_name.setText(setMyanmar(cModel.getName()));

            if(cModel.getComment().length()>100){
                String sub=cModel.getComment().substring(0,100);
                commentHolder.tv_comment.setText(setMyanmar(sub));
                commentHolder.tv_readMore.setVisibility(View.VISIBLE);
            }else{
                commentHolder.tv_comment.setText(setMyanmar(cModel.getComment()));
                commentHolder.tv_readMore.setVisibility(View.GONE);
            }

            commentHolder.tv_readMore.setOnClickListener(v -> {
                commentHolder.tv_comment.setText(setMyanmar(cModel.getComment()));
                commentHolder.tv_readMore.setVisibility(View.GONE);
            });



            commentHolder.iv_comment.setVisibility(View.GONE);

            if(!cModel.getCommentImage().equals("")){

                commentHolder.iv_comment.setVisibility(View.VISIBLE);
                setPhotoFromRealUrl(commentHolder.iv_comment,cModel.getCommentImage());
            }


            if(!cModel.getImageUrl().equals("")) AppHandler.setPhotoFromRealUrl(commentHolder.iv,cModel.getImageUrl());
            commentHolder.iv_blueMark.setVisibility(View.GONE);
            if(cModel.getIsVip().equals("1")){
                commentHolder.iv_blueMark.setVisibility(View.VISIBLE);

            }
            commentHolder.callback=callback;


            commentHolder.iv_react.setBackgroundResource(R.drawable.ic_normal_react);
            if(cModel.getIsLiked().equals("1")){
                commentHolder.iv_react.setBackgroundResource(R.drawable.ic_react_love);
            }


            commentHolder.iv_react.setOnClickListener(v -> {

                LikeController.likeTheComment(cModel.getPostId(),currentUserId,cModel.getTime());

                if(cModel.getIsLiked().equals("1")){
                    commentHolder.iv_react.setBackgroundResource(R.drawable.ic_normal_react);
                    int rectCount=Integer.parseInt(cModel.getLikes());
                    rectCount--;
                    if(rectCount>0){
                        commentHolder.tv_reactCount.setText(reactFormat(rectCount));
                        commentHolder.cardView_reactCount.setVisibility(View.VISIBLE);
                    }else {
                        commentHolder.cardView_reactCount.setVisibility(View.GONE);
                    }


                    cModel.setIsLiked("0");
                    cModel.setLikes(rectCount+"");

                }else{
                    commentHolder.iv_react.setBackgroundResource(R.drawable.ic_react_love);

                    int rectCount=Integer.parseInt(cModel.getLikes());
                    rectCount++;
                    commentHolder.tv_reactCount.setText(reactFormat(rectCount));
                    commentHolder.cardView_reactCount.setVisibility(View.VISIBLE);

                    if(!cModel.getWriterId().equals(currentUserId)){
                        NotificationController notificationController=new NotificationController(c);
                        notificationController.sendNotification(userName+" reacted your comment.",cModel.getWriterToken(),"Easy Korean","1");
                    }

                    cModel.setIsLiked("1");
                    cModel.setLikes(rectCount+"");
                }


            });



            if (!cModel.getLikes().equals("0")){
                commentHolder.tv_reactCount.setText(reactFormat(Integer.parseInt(cModel.getLikes())));
                commentHolder.cardView_reactCount.setVisibility(View.VISIBLE);
            }else{
                commentHolder.cardView_reactCount.setVisibility(View.GONE);
            }


        }catch (Exception e){
            Toast.makeText(c,e.toString(),Toast.LENGTH_SHORT).show();
        }

    }


    public class CommentHolder extends RecyclerView.ViewHolder{
        ImageView iv,iv_blueMark,iv_reply,iv_react,iv_comment;
        TextView tv_name,tv_comment,tv_time,tv_readMore,tv_reactCount;
        CardView cardView_reactCount;
        RelativeLayout Rlayout;
        protected Callback callback;

        public CommentHolder(@NonNull View itemView) {
            super(itemView);
            iv=itemView.findViewById(R.id.iv_small_profile);
            tv_name=itemView.findViewById(R.id.tv_cmt1);
            tv_comment=itemView.findViewById(R.id.tv_cmt2);
            tv_time=itemView.findViewById(R.id.tv_cmt3);
            Rlayout=itemView.findViewById(R.id.bubble_layout_part);
            iv_blueMark=itemView.findViewById(R.id.iv_blueMark);
            iv_reply=itemView.findViewById(R.id.iv_reply);
            tv_readMore=itemView.findViewById(R.id.tv_readmore);

            iv_comment=itemView.findViewById(R.id.iv_main);
            iv_react=itemView.findViewById(R.id.iv_react);
            tv_reactCount=itemView.findViewById(R.id.tv_reactCount);
            cardView_reactCount=itemView.findViewById(R.id.card_reactCount);

            iv_comment.setClipToOutline(true);

            iv_reply.setOnClickListener(v -> {
                final CommentModel cModel=(CommentModel)data.get(getAbsoluteAdapterPosition());
                if (callback != null) {
                    callback.onReplyClick(cModel.getName(),cModel.getWriterId(),cModel.getWriterToken());
                }
            });

            iv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final CommentModel cModel=(CommentModel)data.get(getAbsoluteAdapterPosition());
                    Intent intent=new Intent(c, MyDiscussionActivity.class);
                    intent.putExtra("userId",cModel.getWriterId());
                    intent.putExtra("userName",cModel.getWriterId());
                    c.startActivity(intent);
                }
            });

            tv_name.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final CommentModel cModel=(CommentModel)data.get(getAbsoluteAdapterPosition());
                    Intent intent=new Intent(c, MyDiscussionActivity.class);
                    intent.putExtra("userId",cModel.getWriterId());
                    intent.putExtra("userName",cModel.getWriterId());
                    c.startActivity(intent);
                }
            });


            itemView.setOnLongClickListener(v -> {
                final CommentModel cModel=(CommentModel)data.get(getAbsoluteAdapterPosition());
                showMenu(v,cModel,getAbsoluteAdapterPosition());
                return false;
            });

            cardView_reactCount.setOnClickListener(view -> {
                final CommentModel cModel=(CommentModel)data.get(getAbsoluteAdapterPosition());
                Intent intent=new Intent(c, LikeListActivity.class);
                intent.putExtra("contentId",cModel.getTime());
                intent.putExtra("fetch", Routing.FETCH_COMMENT_LIKE);
                c.startActivity(intent);
            });

            iv_comment.setOnClickListener(v -> {
                CommentModel model=(CommentModel)data.get(getAbsoluteAdapterPosition());
                Intent intent=new Intent(c, PhotoActivity.class);
                intent.putExtra("image",model.getCommentImage());
                intent.putExtra("des",model.getComment());
                c.startActivity(intent);
            });



        }
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public  interface Callback {
        void onReplyClick(String name, String id, String token_to);

    }

    private void showMenu(View v,CommentModel model,int position){
        PopupMenu popup=new PopupMenu(c,v);
        if(("0"+model.getWriterId()).equals(currentUserId)||model.getWriterId().equals(currentUserId)){
            popup.getMenuInflater().inflate(R.menu.private_comment,popup.getMenu());
        }else {
            popup.getMenuInflater().inflate(R.menu.public_comment,popup.getMenu());
        }
        popup.setOnMenuItemClickListener(item -> {
            int id=item.getItemId();
            if(id==R.id.delete_comment){
                MyCommentController commentController=new MyCommentController("","",c);
                commentController.deleteComment(model.getPostId(),model.getTime());
                data.remove(position);
                notifyDataSetChanged();
            }else if(id==R.id.reply_comment){
                if (callback != null) {
                    callback.onReplyClick(model.getName(),model.getWriterId(),model.getWriterToken());
                }
            }else if(id==R.id.copy_comment){
                ClipboardManager clipboard = (ClipboardManager) c.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("calamus", model.getComment());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(c,"Copy to Clipboard",Toast.LENGTH_SHORT).show();
            }
            return true;
        });
        popup.show();
    }
}