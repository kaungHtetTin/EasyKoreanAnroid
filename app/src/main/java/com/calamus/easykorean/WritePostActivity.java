package com.calamus.easykorean;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.calamus.easykorean.app.MyDialog;
import com.calamus.easykorean.app.MyHttp;
import com.calamus.easykorean.app.Routing;
import com.hbisoft.pickit.PickiT;
import com.hbisoft.pickit.PickiTCallbacks;
import com.calamus.easykorean.adapters.NewFeedAdapter;
import com.calamus.easykorean.app.AppHandler;
import com.calamus.easykorean.models.NewfeedModel;

import java.util.Objects;
import java.util.concurrent.Executor;

import me.myatminsoe.mdetect.MDetect;

import static com.calamus.easykorean.app.AppHandler.setMyanmar;


public class WritePostActivity extends AppCompatActivity implements PickiTCallbacks {

    ImageView iv_profile,iv_post,iv_cancel;
    TextView tv_post_writer;
    SharedPreferences sharedPreferences;
    String imageProfile,phone,userName,imagePath="",postText,oldPostId,newPostId;
    EditText et_post;
    CardView cardView;
    ProgressBar pb;
    boolean action;
    String cloudImageUrl,ivName="";
    PickiT pickiT;

    Executor postExecutor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_post);
        sharedPreferences=getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        imageProfile=sharedPreferences.getString("imageUrl",null);
        userName=sharedPreferences.getString("Username",null);
        phone=sharedPreferences.getString("phone",null);
        postExecutor = ContextCompat.getMainExecutor(this);
        MDetect.INSTANCE.init(Objects.requireNonNull(this));
        pickiT = new PickiT(this, this, this);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        setTitle("Create Post");
        cloudImageUrl=getIntent().getExtras().getString("postImage","");
        action=getIntent().getExtras().getBoolean("action",true);
        postText=getIntent().getExtras().getString("postBody","");
        oldPostId=getIntent().getExtras().getString("postId","");


        setUpView();

    }





    private void setUpView(){
        iv_profile=findViewById(R.id.iv_profile);
        tv_post_writer=findViewById(R.id.tv_post_writer);
        et_post=findViewById(R.id.et_post);
        iv_post=findViewById(R.id.iv_post);
        cardView=findViewById(R.id.card_writer);
        iv_cancel=findViewById(R.id.iv_cancel);
        pb=findViewById(R.id.pb_loading);
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
            setTitle("Edit");
        }


        cardView.setOnClickListener(v -> {
            if(isPermissionGranted()){
                pickImageFromGallery();
            }else {
                takePermission();
            }
        });

        iv_profile.setOnClickListener(v -> {
            Intent intent=new Intent(WritePostActivity.this, PhotoActivity.class);
            intent.putExtra("image",imageProfile);
            intent.putExtra("des","");
            startActivity(intent);
        });

        iv_cancel.setOnClickListener(v -> {
            iv_post.setImageBitmap(null);
            iv_post.setVisibility(View.GONE);
            iv_cancel.setVisibility(View.GONE);
            imagePath="";
            cloudImageUrl="";
        });

    }

    private boolean isPermissionGranted(){

        int  readExternalStorage=ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE);
        return  readExternalStorage==PackageManager.PERMISSION_GRANTED;

    }


    private void takePermission(){
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},101);
    }

    private void pickImageFromGallery(){
        mGetContent.launch("image/*");
    }

    ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri uri) {
                    // Handle the returned Uri
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            if(requestCode==102){
                if(data!=null){
                    Uri uri=data.getData();
                    iv_post.setVisibility(View.VISIBLE);
                    iv_cancel.setVisibility(View.VISIBLE);
                    iv_post.setImageURI(uri);
                    if(uri!=null){
                        pickiT.getPath(data.getData(), Build.VERSION.SDK_INT);
                    }
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length>0){
            if(requestCode==101){
                boolean readExternalStorage=grantResults[0]==PackageManager.PERMISSION_GRANTED;
                if(readExternalStorage){
                    pickImageFromGallery();
                }else {
                    takePermission();
                }
            }
        }
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
                if(action)
                    uploadPost(phone,AppHandler.changeUnicode(et_post.getText().toString()));
                else
                    editPost(oldPostId,AppHandler.changeUnicode(et_post.getText().toString()));
            }else {
                pb.setVisibility(View.INVISIBLE);
                Toast.makeText(getApplicationContext(),"Discuss something about this post",Toast.LENGTH_SHORT).show();
            }

        }

        return super.onOptionsItemSelected(item);
    }


    private void uploadPost(String learner_id,String body){
        newPostId=System.currentTimeMillis()+"";
        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.POST, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    postExecutor.execute(() -> {
                        pb.setVisibility(View.INVISIBLE);
                        NewfeedModel model;

                        if(!ivName.equals("")){
                            model = new NewfeedModel(userName,phone,"",imageProfile,newPostId,body,"0","0","https://www.calamuseducation.com/uploads/posts/"+ivName,"0","0","0","0");
                        }else {
                            model = new NewfeedModel(userName,phone,"",imageProfile,newPostId,body,"0","0","","0","0","0","0");
                        }
                        NewFeedAdapter.data.add(1,model);
                        finish();
                    });
                }
                @Override
                public void onError(String msg) {
                    postExecutor.execute(() -> {
                        pb.setVisibility(View.INVISIBLE);
                        Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show();
                    });
                }
            }).url(Routing.ADD_POST)
                    .field("post_id",newPostId)
                    .field("learner_id",learner_id)
                    .field("body",body)
                    .field("major","korea")
                    .field("hasVideo","0");
            if(!userName.isEmpty())myHttp .file("myfile",imagePath);
            myHttp.runTask();
        }).start();

    }

    private void editPost(String post_id,String body){

        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.POST, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    postExecutor.execute(() -> {
                        pb.setVisibility(View.INVISIBLE);
                        finish();
                        NewfeedModel model;
                        if(!ivName.equals("")){
                            model = new NewfeedModel(userName,phone,"",imageProfile,post_id,body,"0","0","https://www.calamuseducation.com/uploads/posts/"+ivName,"0","0","0","0");
                        }else {
                            model =  new NewfeedModel(userName,phone,"",imageProfile,post_id,body,"0","0", cloudImageUrl,"0","0","0","0");
                        }
                        NewFeedAdapter.data.add(1,model);
                        finish();
                    });
                }
                @Override
                public void onError(String msg) {
                    postExecutor.execute(() -> {
                        pb.setVisibility(View.INVISIBLE);
                        Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show();
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

    @Override
    public void onBackPressed() {
        showDiscardDialog();
    }

    private void showDiscardDialog(){
        MyDialog myDialog=new MyDialog(this, "Discard!", "Do you really want to discard?", () -> {
            finish();
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