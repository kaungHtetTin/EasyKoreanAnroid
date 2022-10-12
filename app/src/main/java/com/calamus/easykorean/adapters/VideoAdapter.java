package com.calamus.easykorean.adapters;

import android.app.*;
import android.content.*;
import android.graphics.Color;
import android.os.Environment;
import android.util.Log;
import android.view.*;
import android.widget.*;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.calamus.easykorean.R;
import com.calamus.easykorean.VimeoPlayerActivity;
import com.calamus.easykorean.WebSiteActivity;
import com.calamus.easykorean.app.MyDialog;
import com.calamus.easykorean.app.MyHttp;
import com.calamus.easykorean.app.Routing;
import com.calamus.easykorean.models.VideoModel;
import com.calamus.easykorean.service.DownloaderService;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
import java.util.concurrent.Executor;

import me.myatminsoe.mdetect.MDetect;
import static com.calamus.easykorean.app.AppHandler.setMyanmar;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.Holder> implements Filterable {

    private final Activity c;
    private final ArrayList<VideoModel> data;
    private final LayoutInflater mInflater;
    private XFilter xFilter;
    private SharedPreferences sharedPreferences;
    boolean isVip;
    String currentUserId;
    Executor postExecutor;
    CallBack callBack;

    public VideoAdapter(Activity c, ArrayList<VideoModel> data,CallBack callBack) {
        this.data = data;
        this.callBack=callBack;
        this.c = c;
        this.mInflater = LayoutInflater.from(c);
        sharedPreferences=c.getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        currentUserId=sharedPreferences.getString("phone","901");
        isVip=sharedPreferences.getBoolean("isVIP",false);
        postExecutor= ContextCompat.getMainExecutor(c);
        MDetect.INSTANCE.init(c);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @NotNull
    @Override
    public VideoAdapter.Holder onCreateViewHolder(@NotNull ViewGroup parent, int p2) {
        View view = mInflater.inflate(R.layout.item_video_grid, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NotNull final  VideoAdapter.Holder holder, final int i) {
        try {

            VideoModel model = data.get(i);
            holder.tvTitle.setText(setMyanmar(model.getVideoTitle()));
            holder.tv_lesson_category.setText(model.getCategory());
            if(model.getTime()!=0){

                if(model.isLearned()){
                    holder.tv_watch.setText("Watched");
                    holder.tv_watch.setTextColor(Color.GRAY);
                }
                else {
                    holder.tv_watch.setText("New");
                    holder.tv_watch.setTextColor(Color.RED);
                }

                holder.iv.setVisibility(View.VISIBLE);
                Picasso.get()
                        .load(model.getThumbnail())
                        .centerInside()
                        .fit()
                        .error(R.drawable.ic_feather)
                        .into(holder.iv, new Callback() {
                            @Override
                            public void onSuccess() {
                                holder.container.stopShimmer();
                                holder.container.hideShimmer();
                            }

                            @Override
                            public void onError(Exception e) {
                                holder.container.stopShimmer();
                                holder.container.hideShimmer();
                            }
                        });

                holder.tv_lesson_category.setBackgroundColor(c.getResources().getColor(R.color.appBar));
                holder.tvTitle.setBackgroundColor(c.getResources().getColor(R.color.appBar));
                holder.ibt_download.setVisibility(View.VISIBLE);
                holder.tv_watch.setVisibility(View.VISIBLE);
                holder.iv_circle.setVisibility(View.VISIBLE);
            }else{
                holder.container.showShimmer(true);
                holder.tv_lesson_category.setBackgroundResource(R.drawable.bg_shimmer_content);
                holder.tvTitle.setBackgroundResource(R.drawable.bg_shimmer_content);
                holder.ibt_download.setVisibility(View.GONE);
                holder.iv.setImageResource(R.drawable.bg_shimmer_content);
                holder.tv_watch.setVisibility(View.GONE);
                holder.iv_circle.setVisibility(View.GONE);

            }


        } catch (Exception ignored) {

        }
    }


    public class Holder extends RecyclerView.ViewHolder {
        TextView tvTime, tvTitle,tv_watch,tv_lesson_category;
        ImageView iv,iv_circle;
        ShimmerFrameLayout container;
        ImageView ibt_download;

        public Holder(final View view) {
            super(view);
            tvTime = view.findViewById(R.id.lessonItemTv11);
            tvTitle = view.findViewById(R.id.tv_lesson_title);
            iv= view.findViewById(R.id.lessonItemIv1);
            iv_circle=view.findViewById(R.id.iv_video_circle);
            container=view.findViewById(R.id.shimmer_view_container);
            ibt_download=view.findViewById(R.id.ib_download);
            tv_watch=view.findViewById(R.id.tv_watch);
            tv_lesson_category=view.findViewById(R.id.tv_lesson_category);
            tvTitle.setSelected(true);

            view.setOnClickListener(p1 -> {

                final VideoModel model = data.get(getAbsoluteAdapterPosition());
                Log.e("videoJSON ",convertArrayToJSON());
                if(model.getTime()!=0){
                    final Intent i=new Intent(c, VimeoPlayerActivity.class);
                    i.putExtra("videoTitle",model.getVideoTitle());
                    i.putExtra("videoId",model.getVideoId());
                    i.putExtra("time",model.getTime());
                    i.putExtra("videoChannel",true);
                    i.putExtra("lessonJSON",convertArrayToJSON());
                    model.setLearned(true);
                    tv_watch.setTextColor(Color.GRAY);
                    tv_watch.setText("Watched");
                    c.startActivity(i);
                }

            });

            ibt_download.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final VideoModel model = data.get(getAbsoluteAdapterPosition());
                    boolean isMember=sharedPreferences.getBoolean(model.getCategory(),false);
                    Log.e("Category : ",model.getCategory());
                    // Log.e("isMenber : ",isMember+"");
                    if(isVip||isMember){
                        Toast.makeText(c,"Preparation for download",Toast.LENGTH_SHORT).show();
                        downloadPreparation(model.getTime()+"",model.getVideoTitle(),model.getCategory());
                    }else{
                        showVIPRegistrationDialog();
                    }
                }
            });
        }

        private  void showVIPRegistrationDialog(){
            String title="VIP Registration";
            String msg="Your need to register as a VIP to download the video.\n\nDo you want to contact the Calamus Education for VIP Registration";
            MyDialog myDialog=new MyDialog(c, title, msg, () -> {
                Intent intent=new Intent(c, WebSiteActivity.class);
                intent.putExtra("link", Routing.PAYMENT);
                c.startActivity(intent);
            });
            myDialog.showMyDialog();
        }

        private void downloadPreparation(String postId,String title,String lessonCategory){
            new Thread(() -> {
                MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.GET, new MyHttp.Response() {
                    @Override
                    public void onResponse(String response) {
                        postExecutor.execute(() -> {

                            Log.e("downloadUrl ",response);
                            Intent intent=new Intent(c, DownloaderService.class);
                            String checkTitle=title.replace("/"," ");
                            intent.putExtra("dir",c.getExternalFilesDir(Environment.DIRECTORY_MOVIES).getPath()+"/"+lessonCategory);
                            intent.putExtra("filename",checkTitle+".mp4");
                            intent.putExtra("downloadUrl",response);
                            intent.putExtra("intentMessage","downloadLists");
                            c.startService(intent);
                            callBack.onDownloadClick();

                        });
                    }
                    @Override
                    public void onError(String msg) {
                        postExecutor.execute(() -> {
                            Toast.makeText(c,"Cannot Download",Toast.LENGTH_SHORT).show();
                        });
                    }
                }).url(Routing.GET_VIDEO_URL+"?postId="+postId);
                myHttp.runTask();
            }).start();
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

        private final ArrayList<VideoModel> o_data;
        private final ArrayList<VideoModel> f_data;

        private XFilter(ArrayList<VideoModel> o_data) {
            super();
            this.o_data = new ArrayList<VideoModel>(o_data);
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
                    String item = o_data.get(i).getVideoTitle();

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

    private String convertArrayToJSON(){
        try{
            JSONArray ja=new JSONArray();
            for(int i=0;i<data.size();i++){
                VideoModel model=data.get(i);
                JSONObject jo=new JSONObject();
                jo.put("id","");
                jo.put("link",model.getVideoId());
                jo.put("title",model.getVideoTitle());
                jo.put("title_mini","");
                jo.put("image_url","");
                jo.put("thumbnail",model.getThumbnail());
                jo.put("isVideo","1");
                jo.put("isVip","0");
                if(model.isLearned()) jo.put("learned","1");
                else jo.put("learned","0");
                jo.put("date",model.getTime());
                jo.put("duration",model.getDuration());
                jo.put("category",model.getCategory());
                ja.put(jo);

            }

            return ja.toString();
        }catch (Exception e){
            return null;
        }
    }

    public interface CallBack{
        void onDownloadClick();
    }
}

