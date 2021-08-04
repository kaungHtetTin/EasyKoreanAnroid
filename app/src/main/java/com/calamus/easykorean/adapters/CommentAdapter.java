package com.calamus.easykorean.adapters;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.MenuItem;
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
import com.calamus.easykorean.PhotoActivity;
import com.calamus.easykorean.R;
import com.calamus.easykorean.app.AppHandler;
import com.calamus.easykorean.app.Routing;
import com.calamus.easykorean.controller.LikeController;
import com.calamus.easykorean.controller.MyCommentController;
import com.calamus.easykorean.controller.NotificationController;
import com.calamus.easykorean.models.CommentModel;
import com.calamus.easykorean.models.NewfeedModel;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import me.myatminsoe.mdetect.MDetect;
import static com.calamus.easykorean.app.AppHandler.commentFormat;
import static com.calamus.easykorean.app.AppHandler.formatTime;
import static com.calamus.easykorean.app.AppHandler.reactFormat;
import static com.calamus.easykorean.app.AppHandler.setMyanmar;
import static com.calamus.easykorean.app.AppHandler.setPhotoFromRealUrl;


public class CommentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Activity c;
    private final ArrayList<Object> data;
    private final LayoutInflater mInflater;
    SharedPreferences sharedPreferences;
    String imagePath,checkTime,currentUserId,postOwnerId,userName;
    private Callback callback;

    public CommentAdapter(Activity c, ArrayList<Object> data,String checkTime,Callback callback) {
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
        if(viewType==0){
            View view = mInflater.inflate(R.layout.item_single_post, parent, false);
            return new Holder(view);
        }else{
            View view = mInflater.inflate(R.layout.item_comment, parent, false);
            return new CommentHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(@NotNull final RecyclerView.ViewHolder holder, final int i) {


        if(i==0){
            try {
                final NewfeedModel model = (NewfeedModel) data.get(i);
                postOwnerId=model.getUserId();
                Holder postHolder=(Holder)holder;
                long time=Long.parseLong(model.getPostId());
                postHolder.tv_time.setText(formatTime(time));
                postHolder.tv_userName.setText(setMyanmar(model.getUserName()));
                postHolder.tv_body.setText(setMyanmar(model.getPostBody()));
                if(!model.getUserImage().equals("")) AppHandler.setPhotoFromRealUrl(postHolder.iv_profile,model.getUserImage());
                postHolder.iv_blueMark.setVisibility(View.GONE);

                if(!model.getPostImage().equals("")){
                    postHolder.iv_post.setVisibility(View.VISIBLE);
                    AppHandler.setPhotoFromRealUrl(postHolder.iv_post,model.getPostImage());

                }

                if(model.getIsVip().equals("1"))postHolder.iv_blueMark.setVisibility(View.VISIBLE);

                if(model.getPostBody().length()>200){
                    String sub=model.getPostBody().substring(0,200);
                    postHolder.tv_body.setText(sub);
                    postHolder.tv_read_more.setVisibility(View.VISIBLE);
                }

                postHolder.tv_read_more.setOnClickListener(v -> {
                    postHolder.tv_body.setText(model.getPostBody());
                    postHolder.tv_read_more.setVisibility(View.INVISIBLE);
                });

                int commentNumber=Integer.parseInt(model.getPostComments());
                postHolder.tvCmt.setText(commentFormat(commentNumber));

                postHolder.iv_react.setBackgroundResource(R.drawable.ic_normal_react);
                if(model.getIsLike().equals("1")){
                    postHolder.iv_react.setBackgroundResource(R.drawable.ic_react_love);
                }

                postHolder.iv_react.setOnClickListener(v -> {

                    if(model.getIsLike().equals("1")){
                        postHolder.iv_react.setBackgroundResource(R.drawable.ic_normal_react);
                        int rectCount=Integer.parseInt(model.getPostLikes());
                        rectCount--;
                        if(rectCount>0){
                            postHolder.tvReactCount.setText(reactFormat(rectCount));
                            postHolder.card_reactCount.setVisibility(View.VISIBLE);
                        }else {
                            postHolder.card_reactCount.setVisibility(View.GONE);
                        }

                        model.setIsLike("0");
                        model.setPostLikes(rectCount+"");

                    }else{
                        postHolder.iv_react.setBackgroundResource(R.drawable.ic_react_love);
                        int rectCount=Integer.parseInt(model.getPostLikes());
                        rectCount++;
                        postHolder.tvReactCount.setText(reactFormat(rectCount));
                        postHolder.card_reactCount.setVisibility(View.VISIBLE);

                        if(!model.getUserId().equals(currentUserId)){
                            NotificationController notificationController=new NotificationController(c);
                            notificationController.sendNotification(userName+" reacted your post.",model.getUserToken());
                        }

                        model.setIsLike("1");
                        model.setPostLikes(rectCount+"");
                    }

                    LikeController.likeThePost(currentUserId,model.getPostId());

                });

                postHolder.card_reactCount.setVisibility(View.GONE);
                if (!model.getPostLikes().equals("0")){
                    postHolder.tvReactCount.setText(reactFormat(Integer.parseInt(model.getPostLikes())));
                    postHolder.card_reactCount.setVisibility(View.VISIBLE);
                }else{
                    postHolder.card_reactCount.setVisibility(View.GONE);
                }

            } catch (Exception e) {
                Toast.makeText(c, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }else {

            try{
                final CommentModel cModel=(CommentModel)data.get(i);
                CommentHolder commentHolder=(CommentHolder)holder;

                if(cModel.getTime().equals(checkTime)){
                    commentHolder.Rlayout.setBackgroundColor(Color.parseColor("#E7F3FF"));
                }

                commentHolder.tv_readingMore.setVisibility(View.GONE);
                commentHolder.cardView_reactCount.setVisibility(View.GONE);

                long time=Long.parseLong(cModel.getTime());
                commentHolder.tv_time.setText(formatTime(time));

                commentHolder.tv_name.setText(setMyanmar(cModel.getName()));

                if(cModel.getComment().length()>100){
                    String sub=cModel.getComment().substring(0,100);
                    commentHolder.tv_comment.setText(setMyanmar(sub));
                    commentHolder.tv_readingMore.setVisibility(View.VISIBLE);
                }else{
                    commentHolder.tv_comment.setText(setMyanmar(cModel.getComment()));
                    commentHolder.tv_readingMore.setVisibility(View.GONE);
                }

                commentHolder.tv_readingMore.setOnClickListener(v -> {
                    commentHolder.tv_comment.setText(setMyanmar(cModel.getComment()));
                    commentHolder.tv_readingMore.setVisibility(View.GONE);
                });


                commentHolder.iv_comment.setVisibility(View.GONE);

                if(!cModel.getCommentImage().equals("")){

                    commentHolder.iv_comment.setVisibility(View.VISIBLE);
                    setPhotoFromRealUrl(commentHolder.iv_comment,cModel.getCommentImage());
                }

                if(!cModel.getImageUrl().equals(""))AppHandler.setPhotoFromRealUrl(commentHolder.iv,cModel.getImageUrl());
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
                            notificationController.sendNotification(userName+" reacted your comment.",cModel.getWriterToken());
                        }

                        cModel.setIsLiked("1");
                        cModel.setLikes(rectCount+"");
                    }

                    LikeController.likeTheComment(cModel.getPostId(),currentUserId,cModel.getTime());

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

    }


    public class Holder extends RecyclerView.ViewHolder {
        TextView tv_userName,tv_time,tv_body,tvCmt,tv_read_more,tvReactCount;
        ImageView iv_profile,iv_post,iv_blueMark,iv_react;
        public CardView card_reactCount;


        public Holder(View view) {
            super(view);
            tv_time = view.findViewById(R.id.newfeedtime);
            tvCmt=view.findViewById(R.id.nf_cmt_tv);
            tv_read_more=view.findViewById(R.id.tv_readmore);
            iv_profile = view.findViewById(R.id.newfeediv);
            tv_userName=view.findViewById(R.id.newfeedtitle);
            tv_body=view.findViewById(R.id.tv_body);
            iv_post=view.findViewById(R.id.newfeedImage);
            iv_blueMark=view.findViewById(R.id.iv_blueMark);
            iv_react=view.findViewById(R.id.iv_react);
            tvReactCount=view.findViewById(R.id.tv_reactCount);
            card_reactCount=view.findViewById(R.id.card_reactCount);

            iv_post.setOnClickListener(v -> {
                NewfeedModel model = (NewfeedModel) data.get(getAbsoluteAdapterPosition());
                Intent intent=new Intent(c, PhotoActivity.class);
                intent.putExtra("image",model.getPostImage());
                intent.putExtra("des",model.getPostBody());
                c.startActivity(intent);
            });


            card_reactCount.setOnClickListener(view1 -> {
                NewfeedModel model = (NewfeedModel) data.get(getAbsoluteAdapterPosition());
                Intent intent=new Intent(c, LikeListActivity.class);
                intent.putExtra("contentId",model.getPostId());
                intent.putExtra("fetch", Routing.FETCH_POST_LIKE);
                c.startActivity(intent);
            });
        }
    }

    public class CommentHolder extends RecyclerView.ViewHolder{
        ImageView iv,iv_blueMark,iv_reply,iv_react,iv_comment;
        TextView tv_name,tv_comment,tv_time,tv_readingMore,tv_reactCount;
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
            tv_readingMore=itemView.findViewById(R.id.tv_readmore);

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

            itemView.setOnLongClickListener(v -> {
                final CommentModel cModel=(CommentModel)data.get(getAbsoluteAdapterPosition());
                showMenu(v,cModel,getAdapterPosition(),currentUserId,postOwnerId);
                return false;
            });

            cardView_reactCount.setOnClickListener(view -> {
                final CommentModel cModel=(CommentModel)data.get(getAbsoluteAdapterPosition());
                Intent intent=new Intent(c, LikeListActivity.class);
                intent.putExtra("contentId",cModel.getTime());
                intent.putExtra("fetch",Routing.FETCH_COMMENT_LIKE);
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

    private void showMenu(View v,CommentModel model,int position,String currentUserId,String postOwnerId){
        PopupMenu popup=new PopupMenu(c,v);
        if(("0"+model.getWriterId()).equals(currentUserId)||model.getWriterId().equals(currentUserId)||("0"+postOwnerId).equals(currentUserId)||postOwnerId.equals(currentUserId)){
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
            }
            return true;
        });
        popup.show();
    }
}