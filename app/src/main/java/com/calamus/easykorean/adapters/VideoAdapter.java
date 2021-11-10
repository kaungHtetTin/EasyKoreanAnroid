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
import com.calamus.easykorean.MyYouTubeVideoActivity;
import com.calamus.easykorean.R;
import com.calamus.easykorean.WebSiteActivity;
import com.calamus.easykorean.app.AppHandler;
import com.calamus.easykorean.app.MyDialog;
import com.calamus.easykorean.app.MyHttp;
import com.calamus.easykorean.app.Routing;
import com.calamus.easykorean.models.VideoModel;
import com.calamus.easykorean.service.DownloaderService;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import org.jetbrains.annotations.NotNull;
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

    public VideoAdapter(Activity c, ArrayList<VideoModel> data) {
        this.data = data;
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
            holder.tvTitle.setText(setMyanmar(model.getCategory()));


            if(model.isLearned()){
                holder.tv_watch.setText("Watched");
                holder.tv_watch.setTextColor(Color.GRAY);
            }
            else {
                holder.tv_watch.setText("New");
                holder.tv_watch.setTextColor(Color.RED);
            }

            Picasso.get()
                    .load("https://img.youtube.com/vi/"+model.getVideoId()+"/0.jpg")
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

        } catch (Exception ignored) {

        }
    }


    public class Holder extends RecyclerView.ViewHolder {
        TextView tvTime, tvTitle,tv_watch;
        ImageView iv;
        ShimmerFrameLayout container;
        ImageView ibt_download;
        public Holder(final View view) {
            super(view);
            tvTime = view.findViewById(R.id.lessonItemTv11);
            tvTitle = view.findViewById(R.id.lessonItemTv21);
            iv= view.findViewById(R.id.lessonItemIv1);
            container=view.findViewById(R.id.shimmer_view_container);
            ibt_download=view.findViewById(R.id.ib_download);
            tv_watch=view.findViewById(R.id.tv_watch);
            tvTitle.setSelected(true);

            view.setOnClickListener(p1 -> {

                final VideoModel model = data.get(getAbsoluteAdapterPosition());
                AppHandler.recordAClick(currentUserId,model.getCategory());
                final Intent i=new Intent(c, MyYouTubeVideoActivity.class);
                i.putExtra("videoTitle",model.getVideoTitle());
                i.putExtra("videoId",model.getVideoId());
                i.putExtra("time",model.getTime());
                model.setLearned(true);
                tv_watch.setTextColor(Color.GRAY);
                tv_watch.setText("Watched");
                c.startActivity(i);

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
                        downloadPreparation(model.getTime()+"",model.getVideoTitle());
                    }else{
                        //showVIPRegistrationDialog();
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

        private void downloadPreparation(String postId,String title){
            new Thread(() -> {
                MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.GET, new MyHttp.Response() {
                    @Override
                    public void onResponse(String response) {
                        postExecutor.execute(() -> {
                            Toast.makeText(c,"Start Downloading",Toast.LENGTH_SHORT).show();
                            Intent intent=new Intent(c, DownloaderService.class);
                            String checkTitle=title.replace("/"," ");
                            intent.putExtra("dir",c.getExternalFilesDir(Environment.DIRECTORY_MOVIES).getPath());
                            intent.putExtra("filename",checkTitle+".mp4");
                            intent.putExtra("downloadUrl",response);
                            c.startService(intent);

                        });
                    }
                    @Override
                    public void onError(String msg) {
                        postExecutor.execute(() -> {
                            Toast.makeText(c,"Cannot Download",Toast.LENGTH_SHORT).show();
                        });
                    }
                }).url(Routing.GET_VIDEO_URL+postId);
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


}

