package com.calamus.easykorean;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.calamus.easykorean.app.AppHandler;
import com.calamus.easykorean.app.Routing;
import com.calamus.easykorean.app.MyHttp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.concurrent.Executor;

public class CourseRatingActivity extends AppCompatActivity {

    //main layout
    ProgressBar pb;
    NestedScrollView layoutRating;
    TextView tv_about,tv_see_reviews;

    //Course
    ImageView iv_course_image;
    TextView tv_course_title,tv_course_description,tv_day,tv_course_rating;
    ConstraintLayout courseLayout;

    //add review
    RelativeLayout layout_add_review;
    RatingBar postRatingBar;
    EditText et_post;
    Button bt_post;


    //my review
    RelativeLayout layout_my_review;
    ImageView iv_profile;
    TextView tv_username,tv_my_review,tv_review_date;
    RatingBar my_rating_bar;
    ImageView iv_more;

    //rating and review
    TextView tv_main_rating,tv_total_reviewer;
    RatingBar rating_bar_main;
    ProgressBar pb1,pb2,pb3,pb4,pb5;

    SharedPreferences sharedPreferences;
    String userId,userName,imageUrl,course_title;
    String courseId;
    Executor postExecutor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_rating);
        sharedPreferences=getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        userId=sharedPreferences.getString("phone",null);
        userName=sharedPreferences.getString("Username",null);
        imageUrl=sharedPreferences.getString("imageUrl",null);
        courseId=getIntent().getExtras().getString("course_id");
        postExecutor= ContextCompat.getMainExecutor(this);
        getSupportActionBar().hide();

        setUpView();
    }

    private void setUpView(){
        //main layout
        pb=findViewById(R.id.pb);
        layoutRating=findViewById(R.id.layout_rating);
        tv_about=findViewById(R.id.tv_about);
        tv_see_reviews=findViewById(R.id.tv_see_reviews);
        //course
        iv_course_image=findViewById(R.id.iv_course_cover);
        tv_course_title=findViewById(R.id.tv_course_title);
        tv_course_description=findViewById(R.id.tv_course_description);
        tv_day=findViewById(R.id.tv_day);
        tv_course_rating=findViewById(R.id.tv_course_rating);
        courseLayout=findViewById(R.id.layout_course_item);
        //add review
        layout_add_review=findViewById(R.id.layout_add_review);
        postRatingBar=findViewById(R.id.post_rating_bar);
        et_post=findViewById(R.id.et_post);
        bt_post=findViewById(R.id.bt_post);

        //my review;
        layout_my_review=findViewById(R.id.layout_my_review);
        tv_username=findViewById(R.id.tv_username);
        tv_my_review=findViewById(R.id.tv_review);
        my_rating_bar=findViewById(R.id.rating_bar);
        tv_review_date=findViewById(R.id.tv_date);
        iv_profile=findViewById(R.id.iv_profile);
        iv_more=findViewById(R.id.iv_more);

        //rating and reviews
        tv_main_rating=findViewById(R.id.tv_main_rating);
        tv_total_reviewer=findViewById(R.id.tv_total_reviewer);
        rating_bar_main=findViewById(R.id.rating_bar_main);
        pb1=findViewById(R.id.pb1);
        pb2=findViewById(R.id.pb2);
        pb3=findViewById(R.id.pb3);
        pb4=findViewById(R.id.pb4);
        pb5=findViewById(R.id.pb5);

        fetchReview();

        bt_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                float starNum=postRatingBar.getRating();
                String review=et_post.getText().toString();
                if(TextUtils.isEmpty(et_post.getText().toString())){
                    review="";
                }
                if(starNum!=0){
                    postAReview(starNum,review);
                }else{
                    Toast.makeText(getApplicationContext(),"Please Rate this course", Toast.LENGTH_SHORT).show();
                }
            }
        });

        tv_see_reviews.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(CourseRatingActivity.this,ReviewsActivity.class);
                intent.putExtra("course_id",courseId);
                intent.putExtra("course_title",course_title);
                startActivity(intent);
            }
        });
    }

    private void fetchReview(){
        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.GET, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    postExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            setResult(response);
                        }
                    });
                }
                @Override
                public void onError(String msg) {

                    Log.e("CourseRating Err : ",msg);
                }
            }).url(Routing.GET_COURSE_RATING+"?user_id="+userId+"&course_id="+courseId);
            myHttp.runTask();
        }).start();
    }

    private void setResult(String response){
        pb.setVisibility(View.GONE);
        layoutRating.setVisibility(View.VISIBLE);

        try {
            // about course
            JSONObject joMain=new JSONObject(response);
            JSONObject joCourse=joMain.getJSONObject("course");
            setUpCourseDetail(joCourse);

            //myReview
            boolean rated=joMain.getBoolean("rated");
            if(rated){
                layout_add_review.setVisibility(View.GONE);
                layout_my_review.setVisibility(View.VISIBLE);
                JSONObject joMyReview=joMain.getJSONObject("my_review");
                setMyReview(joMyReview);

            }else{
                layout_my_review.setVisibility(View.GONE);
                layout_add_review.setVisibility(View.VISIBLE);
            }

            // rating and reviews
            JSONArray jaRating=joMain.getJSONArray("rating");
            setRating(jaRating);


        }catch (Exception e){}

        Log.e("Course Review ",response);
    }

    private void setRating(JSONArray jaRating) throws JSONException{
        int total_star=0,total_rating=0;
        int star1=0,star2=0,star3=0,star4=0,star5=0;
        for(int i=0;i<jaRating.length();i++){
            JSONObject joRating=jaRating.getJSONObject(i);
            float star=(float) joRating.getDouble("star");
            int rating=joRating.getInt("total_rating");

            if(star==1) star1=rating;
            if(star==2) star2=rating;
            if(star==3) star3=rating;
            if(star==4) star4=rating;
            if(star==5) star5=rating;

            double temp=star*rating;

            total_star+=temp;
            total_rating+=rating;
        }

        if(total_rating!=0){
            DecimalFormat decimalFormat = new DecimalFormat("0.0");
            float rating=(float) total_star/total_rating;
            String mainRating=decimalFormat.format(rating);
            tv_course_rating.setText(mainRating);
            tv_main_rating.setText(mainRating);
            rating_bar_main.setRating(rating);

        }else{
            tv_course_rating.setText("0.0");
        }

        int oneStarPercent=(int) (star1*100)/total_rating;
        int twoStarPercent=(int) (star2*100)/total_rating;
        int threeStarPercent=(int) (star3*100)/total_rating;
        int fourStarPercent=(int) (star4*100)/total_rating;
        int fiveStarPercent=(int) (star5*100)/total_rating;
        tv_total_reviewer.setText(total_rating+"");

        pb1.setProgress(oneStarPercent);
        pb2.setProgress(twoStarPercent);
        pb3.setProgress(threeStarPercent);
        pb4.setProgress(fourStarPercent);
        pb5.setProgress(fiveStarPercent);
    }


    private void setMyReview(JSONObject jo) throws  JSONException{
        int star=jo.getInt("star");
        String review=jo.getString("review");
        long time=jo.getLong("time");

        my_rating_bar.setRating(star);
        if(review.equals("")){
            tv_my_review.setVisibility(View.GONE);
        }else{
            tv_my_review.setText(review);
        }
        tv_review_date.setText(AppHandler.formatTime(time));
        tv_username.setText(userName);
        AppHandler.setPhotoFromRealUrl(iv_profile,imageUrl);

        iv_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu=new PopupMenu(CourseRatingActivity.this,view);
                popupMenu.getMenuInflater().inflate(R.menu.popup,popupMenu.getMenu());
                popupMenu.show();
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()){
                            case R.id.delete:

                                deleteReview();
                                break;
                        }
                        return true;
                    }
                });
            }
        });

    }

    private void setUpCourseDetail (JSONObject joCourse) throws JSONException {
        course_title=joCourse.getString("title");
        String imageUrl=joCourse.getString("cover_url");
        String description=joCourse.getString("description");
        String details=joCourse.getString("details");
        int duration=joCourse.getInt("duration");
        String background_color=joCourse.getString("background_color");

        tv_course_description.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        tv_course_description.setMarqueeRepeatLimit(-1);
        tv_course_description.setSingleLine(true);
        tv_course_description.setSelected(true);

        tv_course_title.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        tv_course_title.setMarqueeRepeatLimit(-1);
        tv_course_title.setSingleLine(true);
        tv_course_title.setSelected(true);

        tv_course_title.setText(course_title);
        tv_day.setText(duration+" Days");
        tv_day.setVisibility(View.VISIBLE);
        AppHandler.setPhotoFromRealUrl(iv_course_image,imageUrl);
        tv_course_description.setText(description);
        courseLayout.setBackgroundColor(Color.parseColor(background_color));
        tv_about.setText(details);

    }

    private void postAReview(float rating,String review){
        pb.setVisibility(View.VISIBLE);
        bt_post.setEnabled(false);
        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.POST, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    Log.e("Rating ",response);
                    fetchReview();
                }
                @Override
                public void onError(String msg) {
                    bt_post.setEnabled(true);
                    Log.e("CourseRating Err : ",msg);
                }
            }).url(Routing.RATING)
                    .field("user_id",userId)
                    .field("course_id",courseId)
                    .field("star",rating+"")
                    .field("review",review);
            myHttp.runTask();
        }).start();
    }

    private void deleteReview(){
        bt_post.setEnabled(true);
        pb.setVisibility(View.VISIBLE);
        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.POST, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    Log.e("Del Rating ",response);
                    fetchReview();
                }
                @Override
                public void onError(String msg) {

                    Log.e("Del Rating Err : ",msg);
                }
            }).url(Routing.DELETE_RATING)
                    .field("user_id",userId)
                    .field("course_id",courseId);
            myHttp.runTask();
        }).start();
    }

}