package com.calamus.easykorean.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.calamus.easykorean.FlashCardActivity;
import com.calamus.easykorean.R;
import com.calamus.easykorean.models.DeckModel;

import java.util.ArrayList;

public class DeckAdapter extends RecyclerView.Adapter<DeckAdapter.Holder> {

    private final Activity c;
    private final ArrayList<DeckModel> data;
    private final LayoutInflater mInflater;
    final SharedPreferences sharedPreferences;
    final String currentUserName;

    public DeckAdapter(Activity c, ArrayList<DeckModel> data) {
        this.data = data;
        this.c = c;
        this.mInflater = LayoutInflater.from(c);
        sharedPreferences = c.getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        currentUserName = sharedPreferences.getString("userName", null);
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_deck, parent, false);

        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        try {

            final DeckModel model = data.get(position);
            holder.tv_name.setText(model.getTitle());
            holder.tv_recall.setText("Recall " + model.getRecallWord());
            holder.tv_new.setText("New " + model.getNewWord());
            holder.tv_progress.setText(model.getMasteredWord() + " mastered / " + model.getTotalWord() + " total");
            holder.tv_learned.setText(model.getLearnedWord() + " learned");

            holder.progressBar.setProgress(model.getProgress());

        } catch (Exception e) {

        }

    }

    public class Holder extends RecyclerView.ViewHolder {
        TextView tv_name, tv_new, tv_recall, tv_progress, tv_learned;
        ProgressBar progressBar;

        public Holder(View view) {
            super(view);
            tv_name = view.findViewById(R.id.tv_name);
            tv_new = view.findViewById(R.id.tv_new);
            tv_recall = view.findViewById(R.id.tv_recall);
            tv_progress = view.findViewById(R.id.tv_progress);
            progressBar = view.findViewById(R.id.progressBar);
            tv_learned = view.findViewById(R.id.tv_learned);

            view.setOnClickListener(v -> {
                DeckModel model = data.get(getAbsoluteAdapterPosition());
                Intent intent = new Intent(c, FlashCardActivity.class);
                intent.putExtra("deck_id", model.getId());
                c.startActivity(intent);
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
