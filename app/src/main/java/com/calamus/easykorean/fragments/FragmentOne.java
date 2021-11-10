package com.calamus.easykorean.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.calamus.easykorean.R;
import com.calamus.easykorean.adapters.MainAdapter;
import com.calamus.easykorean.models.CategoryModel;
import com.calamus.easykorean.models.CourseModel;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;

public class FragmentOne extends Fragment {
    private View v;

    private final ArrayList<Object> courseList = new ArrayList<>();
    SharedPreferences sharedPreferences;
    String functionJson,firstFormJson,enrollJson;
    MainAdapter adapter;


    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_one, container, false);

        sharedPreferences=getActivity().getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        functionJson=sharedPreferences.getString("functionForm",null);
        firstFormJson=sharedPreferences.getString("firstForm",null);
        enrollJson=sharedPreferences.getString("enrollProgress",null);

        setupViews();
        courseList.add(0,functionJson);
        setCourse();

        return v;
    }

    private void setupViews() {
        RecyclerView recycler;


        recycler = v.findViewById(R.id.recycler_frag_one);


        GridLayoutManager gm = new GridLayoutManager(getActivity(), 1){
            @Override
            public boolean canScrollVertically() {
                return true;
            }
        };
        recycler.setLayoutManager(gm);
      //  recycler.addItemDecoration(new SpacingItemDecoration(2, XUtils.toPx(Objects.requireNonNull(getActivity()), 2), true));
        recycler.setItemAnimator(new DefaultItemAnimator());


        adapter = new MainAdapter(getActivity(), courseList);
        recycler.setAdapter(adapter);

    }


    private void setCourse(){
        JSONArray progressArr=null;
        try {
            JSONArray ja=new JSONArray(firstFormJson);
            if(enrollJson!=null){
                Log.e("enrollJson: ",enrollJson);
                progressArr=new JSONArray(enrollJson);
            }
            for(int i=0;i<ja.length();i++){
                JSONObject jo=ja.getJSONObject(i);
                String id=jo.getString("id");
                String title=jo.getString("title");
                String subject=jo.getString("sub");
                int progress=0;
                if(progressArr!=null){
                    for(int j=0;j<progressArr.length();j++){
                        JSONObject jo2=progressArr.getJSONObject(j);
                        String course_id=jo2.getString("course_id");
                        int learned=Integer.parseInt(jo2.getString("learned"));
                        int total=Integer.parseInt(jo2.getString("total"));
                        if(course_id.equals(id)){
                            progress=(learned*100)/total;
                        }
                    }
                }
                courseList.add(new CourseModel(id,title,subject,progress));
            }

        }catch (Exception e){
            Log.e("FisrtJson : ",e.toString());
        }
    }

}
