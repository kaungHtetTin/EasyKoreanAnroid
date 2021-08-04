package com.calamus.easykorean.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
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

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class FragmentOne extends Fragment {
    private View v;

    private final ArrayList<Object> categoryList = new ArrayList<>();
    SharedPreferences sharedPreferences;
    String functionJson,firstFormJson;
    MainAdapter adapter;


    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_one, container, false);

        sharedPreferences=getActivity().getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        functionJson=sharedPreferences.getString("functionForm",null);
        firstFormJson=sharedPreferences.getString("firstForm",null);

        setupViews();
        categoryList.add(0,functionJson);
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


        adapter = new MainAdapter(getActivity(), categoryList);
        recycler.setAdapter(adapter);

    }


    private void setCourse(){

        try {
            JSONArray ja=new JSONArray(firstFormJson);
            for(int i=0;i<ja.length();i++){
                JSONObject jo=ja.getJSONObject(i);
                String level=jo.getString("level");
                String enroll=jo.getString("enroll");
                String sub=jo.getString("sub");
                String eCode=jo.getString("eCode");
                categoryList.add(new CategoryModel(level,enroll,sub,eCode));
            }

        }catch (Exception e){}
    }

}
