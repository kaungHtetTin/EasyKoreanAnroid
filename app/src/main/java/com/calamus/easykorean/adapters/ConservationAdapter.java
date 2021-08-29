package com.calamus.easykorean.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.calamus.easykorean.ChattingActivity;
import com.calamus.easykorean.R;
import com.calamus.easykorean.TeacherActivity;
import com.calamus.easykorean.app.AppHandler;
import com.calamus.easykorean.models.ConservationModel;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import me.myatminsoe.mdetect.MDetect;

import static com.calamus.easykorean.app.AppHandler.setMyanmar;

public class ConservationAdapter extends RecyclerView.Adapter<ConservationAdapter.Holder> {

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
        MDetect.INSTANCE.init(c);

        dbdir= Objects.requireNonNull(c.getFilesDir()).getPath()+"/databases/";
        dbPath=dbdir+dbName;
        dbLite=SQLiteDatabase.openDatabase(dbPath,null,SQLiteDatabase.OPEN_READWRITE);
    }


    @NonNull
    @Override
    public ConservationAdapter.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=mInflater.inflate(R.layout.item_conservation,parent,false);

        return new Holder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ConservationAdapter.Holder holder, int position) {
        try{

            final ConservationModel model=data.get(position);
            holder.tv_name.setText(setMyanmar(model.getUserName()));

            if(model.getTime()!=null){
                Date resultDate=new Date(Long.parseLong(model.getTime()));
                holder.tv_time.setText(sdf.format(resultDate));
            }
            AppHandler.setPhotoFromRealUrl(model.getImageUrl(),holder.iv,holder.container);

            if(model.getSeen()==1){
                holder.tv_msg.setText((Html.fromHtml("<font color=\"#000000\"><b>" +setMyanmar(msgFormat(model.getMessage()))+ "</b></font>")));
                holder.cardView.setBackgroundColor(Color.parseColor("#E7F3FF"));
            }else{
                if(model.getSenderId().equals(myId))
                    holder.tv_msg.setText("You: "+setMyanmar(msgFormat(model.getMessage())));
                else holder.tv_msg.setText (setMyanmar(msgFormat(model.getMessage())));
                holder.cardView.setBackgroundColor(Color.WHITE);
            }




        }catch (Exception e){

        }

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
                    if(getAbsoluteAdapterPosition()==0){
                        Intent intent=new Intent(c, TeacherActivity.class);
                        intent.putExtra("team","Teacher");
                        intent.putExtra("imageUrl",model.getImageUrl());
                        c.startActivity(intent);
                    }else if(getAbsoluteAdapterPosition()==1){
                        Intent intent=new Intent(c, TeacherActivity.class);
                        intent.putExtra("team","Developer");
                        intent.putExtra("imageUrl",model.getImageUrl());
                        c.startActivity(intent);
                    } else {

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

    @Override
    public int getItemViewType(int position) {
        return position;
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

}
