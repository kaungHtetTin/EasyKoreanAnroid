package com.calamus.easykorean.adapters;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.RecyclerView;
import com.calamus.easykorean.DetailActivity;
import com.calamus.easykorean.MyYouTubeVideoActivity;
import com.calamus.easykorean.R;
import com.calamus.easykorean.WebSiteActivity;
import com.calamus.easykorean.app.AppHandler;
import com.calamus.easykorean.app.MyDialog;
import com.calamus.easykorean.app.Routing;
import com.calamus.easykorean.models.LessonModel;

import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import me.myatminsoe.mdetect.MDetect;

import static com.calamus.easykorean.LessonActivity.eCode;
import static com.calamus.easykorean.LessonActivity.fragmentId;
import static com.calamus.easykorean.LessonActivity.picLink;
import static com.calamus.easykorean.LessonActivity.level;
import static com.calamus.easykorean.app.AppHandler.setMyanmar;


public class LessonAdapter extends RecyclerView.Adapter<LessonAdapter.Holder>
        implements Filterable {

    private final Activity c;
    private final ArrayList<LessonModel> data;
    private final LayoutInflater mInflater;
    private XFilter xFilter;
    SharedPreferences share;
    SharedPreferences.Editor editor;
    String currentUserId;
    SharedPreferences sharedPreferences;


    public LessonAdapter(Activity c, ArrayList<LessonModel> data) {
        this.data = data;
        this.c = c;
        this.mInflater = LayoutInflater.from(c);
        sharedPreferences=c.getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        currentUserId=sharedPreferences.getString("phone","901");
        MDetect.INSTANCE.init(c);

    }


    @Override
    public int getItemCount() {
        return data.size();
    }

    @NotNull
    @Override
    public LessonAdapter.Holder onCreateViewHolder(@NotNull ViewGroup parent, int p2) {
        View view = mInflater.inflate(R.layout.list_item_lesson, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NotNull final LessonAdapter.Holder holder, final int i) {
        try {

            LessonModel model = data.get(i);
            holder.tvTitle.setText(setMyanmar(model.getTitle()));
            boolean alreadyLearnerd=share.getBoolean(model.getCate()+model.getTitle(),false);

            if(alreadyLearnerd)holder.tvLearned.setVisibility(View.VISIBLE);

            if(model.isVideo()){
                holder.ivVideoCircle.setVisibility(View.VISIBLE);
                AppHandler.setPhotoFromRealUrl(holder.iv,"https://img.youtube.com/vi/"+model.getLink()+"/0.jpg");
            }else {
                holder.ivVideoCircle.setVisibility(View.INVISIBLE);
                AppHandler.setPhotoFromRealUrl(holder.iv,picLink);
            }

            holder.ivVipMark.setVisibility(View.GONE);
            if(model.isVip())holder.ivVipMark.setVisibility(View.VISIBLE);


        } catch (Exception e) {
            Toast.makeText(c, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    public class Holder extends RecyclerView.ViewHolder {
        TextView tvTitle,tvLearned;
        ImageView iv,ivVipMark,ivVideoCircle;


        public Holder(final View view) {
            super(view);

            tvTitle = view.findViewById(R.id.lessonItemTv21);
            iv = view.findViewById(R.id.lessonItemIv1);
            ivVipMark=view.findViewById(R.id.iv_vipMark);
            tvLearned=view.findViewById(R.id.tv_learned);
            ivVideoCircle=view.findViewById(R.id.iv_video_circle);
            tvTitle.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            tvTitle.setMarqueeRepeatLimit(-1);
            tvTitle.setSingleLine(true);
            tvTitle.setSelected(true);

            iv.setClipToOutline(true);

            share=c.getSharedPreferences("GeneralData",Context.MODE_PRIVATE);
            editor=share.edit();

            view.setOnClickListener(p1 -> {
                LessonModel model = data.get(getAbsoluteAdapterPosition());

                Bundle bundle = new Bundle();
                boolean isMember=share.getBoolean(level,false);

                if(fragmentId==1){
                    if(!model.isVip()||isMember){

                        go(model,bundle);

                    }else{
                        showVIPRegistrationDialog();
                    }
                }else{
                    boolean isVip=share.getBoolean("isVIP",false);
                    if(!model.isVip()||isVip){
                        go(model,bundle);
                    }else{
                        showVIPRegistrationDialog();
                    }
                }

            });

        }
    }


    private void go(LessonModel model,Bundle bundle){
        boolean alreadyLearnerd=share.getBoolean(model.getCate()+model.getTitle(),false);
        int eValue=share.getInt(eCode,0);
        if(!alreadyLearnerd){
            eValue++;
            editor.putBoolean(model.getCate()+model.getTitle(),true);
            editor.putInt(eCode,eValue);
            editor.apply();
        }

        if(eCode.equals("z"))AppHandler.recordAClick(currentUserId,"Kdrama");
        AppHandler.recordAClick(currentUserId,model.getCate());

        if(model.isVideo()){

            final  Intent i3=new Intent(c, MyYouTubeVideoActivity.class);
            i3.putExtra("videoId",model.getLink());
            i3.putExtra("videoTitle",model.getTitle());
            i3.putExtra("time",model.getTime());
            c.startActivity(i3);


        }else {
            final Intent i = new Intent(c, DetailActivity.class);
            i.putExtra("link",model.getLink());
            i.putExtra("title",model.getTitle());
            i.putExtras(bundle);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            c.startActivity(i);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public Filter getFilter() {
        if (xFilter == null)
            xFilter = new XFilter(data);
        return xFilter;
    }


    public class XFilter extends Filter {

        private final ArrayList<LessonModel> o_data;
        private final ArrayList<LessonModel> f_data;

        private XFilter(ArrayList<LessonModel> o_data) {
            super();
            this.o_data = new ArrayList<LessonModel>(o_data);
            this.f_data = new ArrayList<>();
        }

        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            f_data.clear();

            final FilterResults results = new FilterResults();

            if (charSequence.length() == 0) {
                f_data.addAll(o_data);

            } else {
                final String filterPattern = charSequence.toString().toLowerCase().trim();
                for (int i = 0; i < o_data.size(); i++) {
                    String item = o_data.get(i).getTitle();

                    if (item.toLowerCase().contains(filterPattern)) {
                        f_data.add(o_data.get(i));
                    }
                }
            }
            results.count = f_data.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            data.clear();
            data.addAll(f_data);

            notifyDataSetChanged();
        }
    }


    private  void showVIPRegistrationDialog(){
        String title="VIP Registration";
        String msg="Your need to register as a VIP to continue the game.\n\nDo you want to contact the Calamus Education for VIP Registration";
        MyDialog myDialog=new MyDialog(c, title, msg, () -> {
            Intent intent=new Intent(c, WebSiteActivity.class);
            intent.putExtra("link", Routing.PAYMENT);
            c.startActivity(intent);
        });
        myDialog.showMyDialog();
    }

}