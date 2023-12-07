package com.calamus.easykorean.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.calamus.easykorean.SplashScreenActivity;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.calamus.easykorean.ChattingActivity;
import com.calamus.easykorean.R;
import com.calamus.easykorean.SearchingActivity;
import com.calamus.easykorean.TeacherActivity;
import com.calamus.easykorean.app.AppHandler;
import com.calamus.easykorean.models.ConservationModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Objects;

import static com.calamus.easykorean.app.AppHandler.setMyanmar;

import org.jetbrains.annotations.NotNull;


public class ConservationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Activity c;
    private final ArrayList<ConservationModel> data;
    private final LayoutInflater mInflater;
    final SharedPreferences sharedPreferences;
    @SuppressLint("SimpleDateFormat")
    final
    SimpleDateFormat sdf= new SimpleDateFormat("MMMdd ,yyyy HH:mm");
    final String myId;
    final SQLiteDatabase dbLite;
    final String dbName="conservation.db";
    final String dbPath;
    final String dbdir;

    public ConservationAdapter(Activity c,ArrayList<ConservationModel> data){
        this.data=data;
        this.c=c;
        this.mInflater=LayoutInflater.from(c);
        sharedPreferences=c.getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        myId=Long.parseLong(sharedPreferences.getString("phone",null))+"";

        dbdir= Objects.requireNonNull(c.getFilesDir()).getPath()+"/databases/";
        dbPath=dbdir+dbName;
        dbLite=SQLiteDatabase.openDatabase(dbPath,null,SQLiteDatabase.OPEN_READWRITE);
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType==1){
            View view=mInflater.inflate(R.layout.item_search_bar,parent,false);
            return new SearchBoxHolder(view);
        }else if(viewType==2){
            View view=mInflater.inflate(R.layout.item_title_chat,parent,false);
            return new TitleHolder(view);
        }else if(viewType==3){
            View view=mInflater.inflate(R.layout.item_question,parent,false);
            return new QuestionHolder(view);
        }else {
            View view=mInflater.inflate(R.layout.item_conservation,parent,false);
            return new Holder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(position==0){
            return 1;
        }else if(position==1 || position==3){
            return 2;
        }else if(position==2){
            return 3;
        }else{
            return 4;
        }

    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
//        try{

        final ConservationModel model=data.get(position);

        if(position==0){

        }else if(position==1 || position==3){
            TitleHolder holder=(TitleHolder) viewHolder;
            holder.tv.setText(model.getTitle());

        }else if(position==2){
            QuestionHolder holder=(QuestionHolder) viewHolder;
            if(SplashScreenActivity.TEACHER_MESSAGE)holder.iv_teacher_noti.setVisibility(View.VISIBLE);
            else holder.iv_teacher_noti.setVisibility(View.GONE);

            if(SplashScreenActivity.DEVELOPER_MESSAGE)holder.iv_developer_noti.setVisibility(View.VISIBLE);
            else holder.iv_developer_noti.setVisibility(View.GONE);

        }else if(position>3){
            Holder holder=(Holder) viewHolder;
            holder.tv_name.setText(setMyanmar(model.getUserName()));

            if(model.getTime()!=null){
                holder.tv_time.setText(AppHandler.formatTime(Long.parseLong(model.getTime())));
            }
            AppHandler.setPhotoFromRealUrl(model.getImageUrl(),holder.iv,holder.container);

            if(model.getSeen()==1){
                holder.tv_msg.setText((Html.fromHtml(getBoldFont(setMyanmar(msgFormat(model.getMessage()))))));
                holder.cardView.setBackgroundColor(c.getResources().getColor(R.color.notiBlue));
            }else{
                if(model.getSenderId().equals(myId))
                    holder.tv_msg.setText("You: "+setMyanmar(msgFormat(model.getMessage())));
                else holder.tv_msg.setText (setMyanmar(msgFormat(model.getMessage())));
                holder.cardView.setBackgroundColor(c.getResources().getColor(R.color.appBar));
            }
        }

//        }catch (Exception e){
//
//        }

    }

    private String getBoldFont(String msg){
        int nightModeFlags =
                c.getResources().getConfiguration().uiMode &
                        Configuration.UI_MODE_NIGHT_MASK;
        if(nightModeFlags==Configuration.UI_MODE_NIGHT_YES)   return "<font color=\"#ffffff\"><b>" +msg+ "</b></font>";
        else  return "<font color=\"#333333\"><b>" +msg+ "</b></font>";

    }

    public class Holder extends RecyclerView.ViewHolder{

        final ImageView iv;
        final TextView tv_name;
        final TextView tv_msg;
        final TextView tv_time;
        final ShimmerFrameLayout container;
        final CardView cardView;

        public Holder(View view){
            super(view);
            container=view.findViewById(R.id.shimmer_view_container);
            iv=view.findViewById(R.id.iv_profile);
            tv_name=view.findViewById(R.id.tv_name);
            tv_time=view.findViewById(R.id.tv_time);
            tv_msg=view.findViewById(R.id.tv_msg);
            cardView=view.findViewById(R.id.conservation_card);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ConservationModel model=data.get(getAbsoluteAdapterPosition());
                    if(getAbsoluteAdapterPosition()>3) {
                        Intent intent=new Intent(c, ChattingActivity.class);
                        intent.putExtra("fId",model.getUserId());
                        intent.putExtra("fImage",model.getImageUrl());
                        intent.putExtra("fName",model.getUserName());
                        intent.putExtra("token",model.getToken());
                        c.startActivity(intent);
                    }
                }
            });

            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    ConservationModel model=data.get(getAbsoluteAdapterPosition());
                    if(getAbsoluteAdapterPosition()!=0)confirmDeleteMessage(model.getUserId(),getAbsoluteAdapterPosition());
                    return false;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }



    private String msgFormat(String msg){
        if(msg.length()>50){
            return msg.substring(0,50)+"...";
        }else{
            return  msg;
        }
    }

    private void confirmDeleteMessage(String FId,int position){
        final AlertDialog ad = new AlertDialog.Builder(c).create();
        ad.setTitle("Delete Confirmation");
        ad.setIcon(R.mipmap.kommmainicon);
        ad.setMessage("Do you really want to delete?");
        ad.setButton(AlertDialog.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                //clearAllMessages();
                dbLite.delete("Chats", "(senderId="+myId+" AND chatRoom="+FId+myId+") OR senderId="+FId, null);
                dbLite.delete("Conservations","fri_id="+FId,null);
                Toast.makeText(c,"Deleted",Toast.LENGTH_SHORT).show();
                data.remove(position);
                notifyDataSetChanged();

            }
        });

        ad.setButton(AlertDialog.BUTTON_NEGATIVE, "No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                ad.dismiss();
            }
        });
        ad.show();
    }

    public class TitleHolder extends RecyclerView.ViewHolder{

        TextView tv;
        public TitleHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            tv=itemView.findViewById(R.id.tv_item_title);
        }
    }

    public class SearchBoxHolder extends RecyclerView.ViewHolder{

        public SearchBoxHolder(@NonNull View itemView) {
            super(itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    c.startActivity(new Intent(c, SearchingActivity.class));
                }
            });
        }
    }

    public class QuestionHolder extends RecyclerView.ViewHolder{

        ImageView iv_teacher,iv_developer,iv_developer_noti,iv_teacher_noti;
        RelativeLayout layoutDeveloper,layoutTeacher;


        public QuestionHolder(@NonNull View itemView) {
            super(itemView);

            iv_teacher=itemView.findViewById(R.id.iv_teacher);
            iv_developer=itemView.findViewById(R.id.iv_developer);
            layoutDeveloper=itemView.findViewById(R.id.layout_developer);
            layoutTeacher=itemView.findViewById(R.id.layout_teacher);
            iv_developer_noti=itemView.findViewById(R.id.noti_red_mark_developer);
            iv_teacher_noti=itemView.findViewById(R.id.noti_red_mark_teacher);

            AppHandler.setPhotoFromRealUrl(iv_teacher,"file:///android_asset/teacher.png");
            AppHandler.setPhotoFromRealUrl(iv_developer,"file:///android_asset/developer.png");

            layoutTeacher.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent=new Intent(c, TeacherActivity.class);
                    intent.putExtra("team","Teacher");
                    intent.putExtra("imageUrl","file:///android_asset/teacher.png");
                    iv_teacher_noti.setVisibility(View.GONE);
                    c.startActivity(intent);
                }
            });

            layoutDeveloper.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent=new Intent(c, TeacherActivity.class);
                    intent.putExtra("team","Developer");
                    intent.putExtra("imageUrl","file:///android_asset/developer.png");
                    iv_teacher_noti.setVisibility(View.GONE);
                    c.startActivity(intent);
                }
            });

        }
    }

}
