package com.calamus.easykorean.adapters;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.RecyclerView;

import com.calamus.easykorean.MyDiscussionActivity;
import com.calamus.easykorean.R;
import com.calamus.easykorean.models.LikeListModel;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import static com.calamus.easykorean.app.AppHandler.setMyanmar;
import static com.calamus.easykorean.app.AppHandler.setPhotoFromRealUrl;


public class LikeListAdapter extends RecyclerView.Adapter<LikeListAdapter.Holder>{

    private final Activity c;
    private final ArrayList<LikeListModel> data;
    private final LayoutInflater mInflater;

    public LikeListAdapter(Activity c, ArrayList<LikeListModel> data) {
        this.data = data;
        this.c = c;
        this.mInflater = LayoutInflater.from(c);

    }


    @Override
    public int getItemCount() {
        return data.size();
    }

    @NotNull
    @Override
    public LikeListAdapter.Holder onCreateViewHolder(@NotNull ViewGroup parent, int p2) {
        View view = mInflater.inflate(R.layout.item_like_list, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NotNull final LikeListAdapter.Holder holder, final int i) {
        try {
            LikeListModel model=data.get(i);
            holder.tv_name.setText(setMyanmar(model.getUserName()));
            holder.iv_isVip.setVisibility(View.GONE);
            if(model.getIsVip().equals("1"))holder.iv_isVip.setVisibility(View.VISIBLE);

            setPhotoFromRealUrl(holder.iv_profile,model.getImageUrl());



        } catch (Exception e) {
            Toast.makeText(c, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    public class Holder extends RecyclerView.ViewHolder {

        ImageView iv_profile,iv_isVip;
        TextView tv_name;

        public Holder(final View view) {
            super(view);
            iv_profile=view.findViewById(R.id.iv_profile);
            iv_isVip=view.findViewById(R.id.iv_blueMark);
            tv_name=view.findViewById(R.id.tv_name);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LikeListModel model=data.get(getAbsoluteAdapterPosition());
                    Intent intent=new Intent(c, MyDiscussionActivity.class);
                    intent.putExtra("userId",model.getUserId());
                    intent.putExtra("userName",model.getUserName());
                    c.startActivity(intent);
                }
            });
        }
    }



    @Override
    public int getItemViewType(int position) {
        return position;
    }

}