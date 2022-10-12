package com.calamus.easykorean.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.calamus.easykorean.R;
import com.calamus.easykorean.models.FinishCourseModel;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class CourseFinishAdapter extends RecyclerView.Adapter<CourseFinishAdapter.Holder> {

    ArrayList<FinishCourseModel> data;
    Activity c;
    private final LayoutInflater mInflater;
    public CourseFinishAdapter(ArrayList<FinishCourseModel> data, Activity c) {
        this.data = data;
        this.c = c;
        this.mInflater = LayoutInflater.from(c);
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view=mInflater.inflate(R.layout.item_course_finish,parent,false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull Holder holder, int position) {
        holder.tv.setText(data.get(position).getTitle());
    }

    @Override
    public int getItemCount() {
        return data.size();
    }


    public class Holder extends RecyclerView.ViewHolder{

        TextView tv;
        public Holder(@NonNull @NotNull View itemView) {
            super(itemView);
            tv=itemView.findViewById(R.id.tv);
        }
    }
}
