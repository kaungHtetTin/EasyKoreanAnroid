package com.calamus.easykorean.fragments;

import static com.calamus.easykorean.SplashScreenActivity.MESSAGE_ARRIVE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.calamus.easykorean.ClassRoomActivity;
import com.calamus.easykorean.R;
import com.calamus.easykorean.adapters.MainListAdapter;
import com.calamus.easykorean.app.Routing;
import com.calamus.easykorean.dialogs.MenuDialog;
import com.calamus.easykorean.models.ExtraCourseModel;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;

public class FragmentTwo extends Fragment {

    View v;
    private final ArrayList<Object> categoryList = new ArrayList<>();

    RecyclerView recycler;
    SharedPreferences share;
    MainListAdapter adapter;
    String categoryJson,wordOfTheDayJson,kDramaJson,music;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v=inflater.inflate(R.layout.fragment_two,container,false);

        share= requireActivity().getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        categoryJson=share.getString("additionalLessons",null);
        wordOfTheDayJson=share.getString("wordOfTheDay",null);
        kDramaJson=share.getString("kDramas","");
        music=share.getString("music",null);
        setUpView();
        setAppBar();

        return v;
    }

    private void setUpView(){

        recycler = v.findViewById(R.id.recycler_frag_two);
        GridLayoutManager gm = new GridLayoutManager(getActivity(), 3){};
        recycler.setLayoutManager(gm);
        gm.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                Object o=categoryList.get(position);
                if(o instanceof String || o instanceof WordOfTheDay || o instanceof LyricsSong || o instanceof Game){
                    return 3;
                }else {
                    return 1;
                }
            }
        });

        adapter = new MainListAdapter(getActivity(), categoryList);
        recycler.setAdapter(adapter);
        categoryList.clear();
        categoryList.add(0,"Word Of The Day");
        categoryList.add(1,new WordOfTheDay(wordOfTheDayJson));

        if(music!=null){
            if(music.equals("on")){
                categoryList.add("Improve Listening Skill");
                categoryList.add(new LyricsSong());
            }
        }
        categoryList.add("Small Game");
        categoryList.add(new Game());

        categoryList.add("Additional Lessons");
        setCategoryList();
        categoryList.add("Learning With K drama");
        setDramaLesson();

    }

    private void setAppBar(){
        RelativeLayout toolBarContent=v.findViewById(R.id.toolbarContent);
        CollapsingToolbarLayout toolbarLayout=v.findViewById(R.id.ctb);
        ImageView iv_messenger=v.findViewById(R.id.iv_messenger);
        ImageView iv_menu=v.findViewById(R.id.iv_menu);

        ImageView iv_noti_red_mark=v.findViewById(R.id.noti_red_mark);
        if(MESSAGE_ARRIVE)iv_noti_red_mark.setVisibility(View.VISIBLE);
        else iv_noti_red_mark.setVisibility(View.GONE);

        toolbarLayout.setTitle(Routing.APP_NAME);
        toolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppBar);
        toolbarLayout.setCollapsedTitleTextAppearance(R.style.CollapsedAppBar);

        iv_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MenuDialog(requireActivity()).initDialog();
            }
        });

        iv_messenger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getActivity(), ClassRoomActivity.class);
                intent.putExtra("action","");
                iv_noti_red_mark.setVisibility(View.GONE);
                startActivity(intent);
            }
        });

        AppBarLayout mAppBarLayout =v.findViewById(R.id.app_bar_layout);
        mAppBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {

            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {

                Log.e("scrollRange ",verticalOffset+"");

                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                    toolBarContent.setVisibility(View.VISIBLE);
                }
                if (verticalOffset<-64) {
                    isShow = true;
                    toolBarContent.setVisibility(View.GONE);
                } else if (isShow) {
                    isShow = false;
                    toolBarContent.setVisibility(View.VISIBLE);
                }
            }
        });
    }


    private void setCategoryList(){
        try {
            JSONArray ja=new JSONArray(categoryJson);
            for(int i=0;i<ja.length();i++){
                JSONObject jo=ja.getJSONObject(i);
                String title=jo.getString("category");
                String id=jo.getString("category_id");
                String pic=jo.getString("image_url");
                categoryList.add(new ExtraCourseModel(title,id,pic));
            }
            adapter.notifyDataSetChanged();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setDramaLesson(){
        try {
            JSONArray ja=new JSONArray(kDramaJson);
            for(int a=0;a<ja.length();a++){
                JSONObject jo2=ja.getJSONObject(a);
                String title=jo2.getString("title");
                String image_url=jo2.getString("image_url");
                String drama_id=jo2.getString("drama_id");
                categoryList.add(new ExtraCourseModel(title,drama_id,image_url));

            }
        }catch (Exception e){}
    }

    public class WordOfTheDay{
        String wordOfTheDayJson;

        public WordOfTheDay(String wordOfTheDayJson) {
            this.wordOfTheDayJson = wordOfTheDayJson;
        }

        public String getWordOfTheDayJson() {
            return wordOfTheDayJson;
        }
    }
    public class LyricsSong{}

    public class Game{}

}
