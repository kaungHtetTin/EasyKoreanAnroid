package com.calamus.easykorean.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.calamus.easykorean.R;
import com.calamus.easykorean.app.AppHandler;
import com.calamus.easykorean.models.AppModel;
import java.util.ArrayList;
import me.myatminsoe.mdetect.MDetect;

public class AppAdapter extends RecyclerView.Adapter<AppAdapter.Holder> {

    private final Activity c;
    private final ArrayList<AppModel> data;
    private final LayoutInflater mInflater;
    final SharedPreferences sharedPreferences;
    final String currentUserName;
    public AppAdapter(Activity c, ArrayList<AppModel> data){
        this.data=data;
        this.c=c;
        this.mInflater= LayoutInflater.from(c);
        sharedPreferences=c.getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        currentUserName=sharedPreferences.getString("userName",null);
        MDetect.INSTANCE.init(c);
    }


    @NonNull
    @Override
    public AppAdapter.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=mInflater.inflate(R.layout.item_app,parent,false);

        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppAdapter.Holder holder, int position) {
        try{

            final AppModel model=data.get(position);

            holder.tvName.setText(model.getName());
            holder.tvDes.setText(model.getDescription());

            AppHandler.setPhotoFromRealUrl(holder.iv,model.getThumbLink());

        }catch (Exception ignored){

        }


    }

    public class Holder extends RecyclerView.ViewHolder{

        final ImageView iv;
        final TextView tvName;
        final TextView tvDes;

        public Holder(View view){
            super(view);
            iv=view.findViewById(R.id.iv_app);
            tvName=view.findViewById(R.id.tv_name);
            tvDes=view.findViewById(R.id.tv_des);
            view.setOnClickListener(v -> {
                AppModel model=data.get(getAbsoluteAdapterPosition());
                c.startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse(model.getAppLink())));
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
