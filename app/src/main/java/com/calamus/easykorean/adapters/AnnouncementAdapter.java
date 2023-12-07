package com.calamus.easykorean.adapters;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.calamus.easykorean.R;
import com.calamus.easykorean.models.AnounceModel;

import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;


public class AnnouncementAdapter extends RecyclerView.Adapter<AnnouncementAdapter.Holder>{

    private final Activity c;
    private final ArrayList<AnounceModel> data;
    private final LayoutInflater mInflater;
    SharedPreferences sharedPreferences;
    String currentUserId;


    public AnnouncementAdapter(Activity c, ArrayList<AnounceModel> data) {
        this.data = data;
        this.c = c;
        this.mInflater = LayoutInflater.from(c);
        sharedPreferences=c.getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        currentUserId=sharedPreferences.getString("phone",null);

    }


    @Override
    public int getItemCount() {
        return data.size();
    }

    @NotNull
    @Override
    public AnnouncementAdapter.Holder onCreateViewHolder(@NotNull ViewGroup parent, int p2) {
        View view = mInflater.inflate(R.layout.item_anounce, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NotNull final AnnouncementAdapter.Holder holder, final int i) {
        try {
            AnounceModel model=data.get(i);
            holder.wv.loadUrl(model.getlinkAounce()+"/"+currentUserId);

        } catch (Exception e) {
            Toast.makeText(c, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    public class Holder extends RecyclerView.ViewHolder {

        WebView wv;
        CardView cardView;
        public Holder(final View view) {
            super(view);
            wv=view.findViewById(R.id.wv_anounce);
            cardView=view.findViewById(R.id.card_View);
            WebSettings settings = wv.getSettings();
            settings.setJavaScriptEnabled(true);
            settings.setCacheMode(WebSettings.LOAD_DEFAULT);

            wv.setWebViewClient(new WebViewClient(){
                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    cardView.setVisibility(View.VISIBLE);
                }
            });
        }
    }



    @Override
    public int getItemViewType(int position) {
        return position;
    }

}