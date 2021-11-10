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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.calamus.easykorean.R;
import com.calamus.easykorean.adapters.MainListAdapter;
import com.calamus.easykorean.models.CategoryModel;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import me.myatminsoe.mdetect.MDetect;


public class FragmentTwo extends Fragment {

    View v;
    private final ArrayList<Object> categoryList = new ArrayList<>();

    RecyclerView recycler;
    SharedPreferences share;
    MainListAdapter adapter;
    String categoryJson,wordOfTheDayJson;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v=inflater.inflate(R.layout.fragment_two,container,false);

        share= requireActivity().getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        categoryJson=share.getString("secondForm",null);
        wordOfTheDayJson=share.getString("wordOfTheDay",null);
        MDetect.INSTANCE.init(getActivity());
        setUpView();

        return v;
    }

    private void setUpView(){

        recycler = v.findViewById(R.id.recycler_frag_two);
        GridLayoutManager gm = new GridLayoutManager(getActivity(), 2){};
        recycler.setLayoutManager(gm);
        gm.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if(position<=1){
                    return  2;
                }else{
                    return 1;
                }
            }
        });

        adapter = new MainListAdapter(getActivity(), categoryList);
        recycler.setAdapter(adapter);
        categoryList.clear();
        categoryList.add(0,wordOfTheDayJson);
        categoryList.add(1,"Lyric Songs");
        setCategoryList();
    }

    private void setCategoryList(){
        try {
            JSONArray ja=new JSONArray(categoryJson);
            for(int i=0;i<ja.length();i++){
                JSONObject jo=ja.getJSONObject(i);
                String cate=jo.getString("category");
                String cate_id=jo.getString("category_id");
                String pic=jo.getString("pic");
                categoryList.add(new CategoryModel(cate,cate_id,pic,""));
            }
            adapter.notifyDataSetChanged();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
