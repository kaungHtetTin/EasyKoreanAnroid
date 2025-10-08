package com.calamus.easykorean.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.animation.AnimationUtils;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.calamus.easykorean.R;
import com.calamus.easykorean.adapters.LectureNoteAdapter;
import com.calamus.easykorean.models.LectureNoteModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class LectureNoteMenu {
    Activity c;
    String lectureNoteJSON;
    public LectureNoteMenu(Activity c, String lectureNoteJSON){
        this.c = c;
        this.lectureNoteJSON = lectureNoteJSON;
    }

    public void show(){
        RecyclerView recyclerView;
        View v = c.getLayoutInflater().inflate(R.layout.custom_note_dialog, null);
        v.setAnimation(AnimationUtils.loadAnimation(c, R.anim.transit_up));
        recyclerView = v.findViewById(R.id.recycler);

        ArrayList<LectureNoteModel> notes = new ArrayList<>();
        LectureNoteAdapter adapter = new LectureNoteAdapter(c, notes);

        final LinearLayoutManager lm = new LinearLayoutManager(c);
        recyclerView.setLayoutManager(lm);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

        try {
            JSONArray ja = new JSONArray(lectureNoteJSON);
            for(int i =0;i<ja.length();i++){
                JSONObject jo = ja.getJSONObject(i);
                String time = jo.getString("time");
                String note = jo.getString("note");
                notes.add(new LectureNoteModel(time,note));
            }
            adapter.notifyDataSetChanged();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setView(v);
        final AlertDialog ad = builder.create();
        ad.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        ad.show();
    }
}
