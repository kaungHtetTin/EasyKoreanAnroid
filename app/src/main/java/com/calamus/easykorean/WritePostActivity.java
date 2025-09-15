package com.calamus.easykorean;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.calamus.easykorean.app.MyImagePicker;
import com.hbisoft.pickit.PickiT;
import com.hbisoft.pickit.PickiTCallbacks;
import com.calamus.easykorean.adapters.NewFeedAdapter;
import com.calamus.easykorean.app.AppHandler;
import com.calamus.easykorean.app.MyDialog;
import com.calamus.easykorean.app.Routing;
import com.calamus.easykorean.models.NewfeedModel;
import com.calamus.easykorean.app.MyHttp;
import java.util.Objects;
import java.util.concurrent.Executor;
import static com.calamus.easykorean.app.AppHandler.setMyanmar;


public class WritePostActivity extends AppCompatActivity implements PickiTCallbacks {

    ImageView iv_profile,iv_post,iv_cancel;
    TextView tv_post_writer;
    SharedPreferences sharedPreferences;
    String imageProfile,phone,userName,imagePath="",postText,oldPostId;
    EditText et_post;
    CardView cardView;
    ProgressBar pb;
    boolean action;
    String cloudImageUrl,ivName="";
    PickiT pickiT;
    boolean isVip;
    Executor postExecutor;
    TextView tv_appbar,tv_post;
    ImageView iv_back;
    MyImagePicker myImagePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_post);
        sharedPreferences=getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        imageProfile=sharedPreferences.getString("imageUrl",null);
        userName=sharedPreferences.getString("Username",null);
        phone=sharedPreferences.getString("phone",null);
        isVip=sharedPreferences.getBoolean("isVIP",false);
        postExecutor = ContextCompat.getMainExecutor(this);
        pickiT = new PickiT(this, this, this);

        cloudImageUrl=getIntent().getExtras().getString("postImage","");
        action=getIntent().getExtras().getBoolean("action",true);
        postText=getIntent().getExtras().getString("postBody","");
        oldPostId=getIntent().getExtras().getString("postId","");

        Objects.requireNonNull(getSupportActionBar()).hide();
        setUpMyActionBar();
        setUpView();

        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                showDiscardDialog();
            }
        });

        myImagePicker = new MyImagePicker(this);

    }

    private void setUpMyActionBar(){
        tv_appbar=findViewById(R.id.tv_appbar);
        iv_back=findViewById(R.id.iv_back);

        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDiscardDialog();
            }
        });
    }

    private void setUpView(){
        iv_profile=findViewById(R.id.iv_profile);
        tv_post_writer=findViewById(R.id.tv_post_writer);
        et_post=findViewById(R.id.et_post);
        iv_post=findViewById(R.id.iv_post);
        cardView=findViewById(R.id.card_writer);
        iv_cancel=findViewById(R.id.iv_cancel);
        pb=findViewById(R.id.pb_loading);
        tv_post=findViewById(R.id.tv_post);
        pb.setVisibility(View.INVISIBLE);

        tv_post_writer.setText(setMyanmar(userName));

        if(imageProfile!=null) AppHandler.setPhotoFromRealUrl(iv_profile,imageProfile);

        if(!action){
            et_post.setText(setMyanmar(postText));
            if(!cloudImageUrl.equals("")){
                AppHandler.setPhotoFromRealUrl(iv_post,cloudImageUrl);
                iv_post.setVisibility(View.VISIBLE);
                iv_cancel.setVisibility(View.VISIBLE);
            }
            tv_appbar.setText("Edit");
        }else{
            tv_appbar.setText("Create Post");
        }


        tv_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pb.setVisibility(View.VISIBLE);
                if(!TextUtils.isEmpty(et_post.getText().toString())||!imagePath.equals("")||!cloudImageUrl.equals("")){
                    if(action) {
                        Intent intent=new Intent();
                        intent.putExtra("body",et_post.getText().toString());
                        intent.putExtra("imagePath",imagePath);
                        setResult(Activity.RESULT_OK,intent);
                        finish();
                    }
                    else
                        editPost(oldPostId,et_post.getText().toString());
                }else {
                    pb.setVisibility(View.INVISIBLE);
                    Toast.makeText(getApplicationContext(),"Discuss something about this post",Toast.LENGTH_SHORT).show();
                }
            }
        });

        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myImagePicker.pick(new MyImagePicker.Callback() {
                    @Override
                    public void onResult(Uri uri) {
                        iv_post.setVisibility(View.VISIBLE);
                        iv_cancel.setVisibility(View.VISIBLE);
                        iv_post.setImageURI(uri);
                        if(uri!=null){
                            pickiT.getPath(uri, Build.VERSION.SDK_INT);
                        }else{
                            iv_post.setImageBitmap(null);
                            iv_post.setVisibility(View.GONE);
                            iv_cancel.setVisibility(View.GONE);
                            imagePath="";
                            cloudImageUrl="";
                        }
                    }
                });
            }
        });

        iv_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(WritePostActivity.this, PhotoActivity.class);
                intent.putExtra("image",imageProfile);
                intent.putExtra("des","");
                startActivity(intent);
            }
        });

        iv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iv_post.setImageBitmap(null);
                iv_post.setVisibility(View.GONE);
                iv_cancel.setVisibility(View.GONE);
                imagePath="";
                cloudImageUrl="";
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("POST")
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            showDiscardDialog();
        }else if (item.getTitle().equals("POST")){

            pb.setVisibility(View.VISIBLE);
            if(!TextUtils.isEmpty(et_post.getText().toString())||!imagePath.equals("")||!cloudImageUrl.equals("")){
                if(action){
                    Intent intent=new Intent();
                    intent.putExtra("body",et_post.getText().toString());
                    intent.putExtra("imagePath",imagePath);
                    setResult(Activity.RESULT_OK,intent);
                    finish();
                }
                else
                    editPost(oldPostId,et_post.getText().toString());
            }else {
                pb.setVisibility(View.INVISIBLE);
                Toast.makeText(getApplicationContext(),"Discuss something about this post",Toast.LENGTH_SHORT).show();
            }

        }

        return super.onOptionsItemSelected(item);
    }


    private void editPost(String post_id,String body){

        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.POST, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    postExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            pb.setVisibility(View.INVISIBLE);
                            finish();
                            NewfeedModel model;
                            if(!ivName.equals("")){
                                model = new NewfeedModel(userName,phone,"",imageProfile,post_id,body,"0","0","https://www.calamuseducation.com/uploads/posts/"+ivName,"0","0","0","0",0,0);
                            }else {
                                model =  new NewfeedModel(userName,phone,"",imageProfile,post_id,body,"0","0", cloudImageUrl,"0","0","0","0",0,0);
                            }
                            NewFeedAdapter.data.add(1,model);
                            finish();
                        }
                    });
                }
                @Override
                public void onError(String msg) {
                    postExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            pb.setVisibility(View.INVISIBLE);
                            Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }).url(Routing.EDIT_POST)
                    .field("post_id",post_id)
                    .field("body",body)
                    .field("image",cloudImageUrl);
            if(!userName.isEmpty())myHttp .file("myfile",imagePath);
            myHttp.runTask();
        }).start();

    }

    private void showDiscardDialog(){
        MyDialog myDialog=new MyDialog(this, "Discard!", "Do you really want to discard?", new MyDialog.ConfirmClick() {
            @Override
            public void onConfirmClick() {
                finish();
            }
        });
        myDialog.showMyDialog();
    }

    @Override
    public void PickiTonUriReturned() {

    }

    @Override
    public void PickiTonStartListener() {

    }

    @Override
    public void PickiTonProgressUpdate(int progress) {

    }

    @Override
    public void PickiTonCompleteListener(String path, boolean wasDriveFile, boolean wasUnknownProvider, boolean wasSuccessful, String Reason) {
        imagePath=path;
        ivName=imagePath.substring(imagePath.lastIndexOf("/")+1);
    }
}