package com.calamus.easykorean;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import me.myatminsoe.mdetect.MDetect;
import static com.calamus.easykorean.app.AppHandler.setMyanmar;


public class PhotoActivity extends AppCompatActivity {
    TextView tv;
    ImageView iv;
    String des,image;
    ImageButton ibt;
    LinearLayout cBar;
    ScrollView sv;
    boolean b=false;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        des=getIntent().getExtras().getString("des");
        image=getIntent().getExtras().getString("image");
        MDetect.INSTANCE.init(this);
        setUpView();
    }

    private void setUpView(){
        tv=findViewById(R.id.tv_photo);
        iv=findViewById(R.id.iv_photo);
        ibt=findViewById(R.id.ibt_back);
        cBar=findViewById(R.id.c_bar);
        sv=findViewById(R.id.id_scroll);

        if(des.equals("")){
            tv.setVisibility(View.GONE);
            iv.setEnabled(false);
            sv.setVisibility(View.GONE);
        }
        tv.setText(setMyanmar(des));
        if(!image.isEmpty()){
            Picasso.get()
                    .load(image)
                    .centerInside()
                    .fit()
                    .error(R.drawable.ic_baseline_account_circle_24)
                    .into(iv, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(Exception e) {

                        }
                    });
        }else {
            iv.setBackgroundResource(R.drawable.ic_baseline_account_circle_24);
        }

        iv.setOnClickListener(v -> {
            if(!b){
                tv.setVisibility(View.INVISIBLE);
                cBar.setVisibility(View.INVISIBLE);
                sv.setVisibility(View.INVISIBLE);
                b=true;
            }else{
                tv.setVisibility(View.VISIBLE);
                cBar.setVisibility(View.VISIBLE);
                sv.setVisibility(View.VISIBLE);
                b=false;
            }
        });

        ibt.setOnClickListener(v -> {
            finish();

        });
    }
}
