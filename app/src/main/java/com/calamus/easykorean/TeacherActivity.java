package com.calamus.easykorean;


import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.calamus.easykorean.adapters.ChatAdapter;
import com.calamus.easykorean.app.AppHandler;
import com.calamus.easykorean.app.Routing;
import com.calamus.easykorean.models.ChatModel;
import com.calamus.easykorean.models.ConservationModel;
import com.calamus.easykorean.app.MyHttp;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import static com.calamus.easykorean.ChattingActivity.fImage;


public class TeacherActivity extends AppCompatActivity {

    ImageButton ibtSend;
    EditText et_chatInput;
    RecyclerView recyclerView;
    private ChatAdapter adapter;
    private final ArrayList<ChatModel> ChatList = new ArrayList<>();

    String myId;
    SharedPreferences sharedPreferences;
    private DatabaseReference db,dbc,dbn;
    SwipeRefreshLayout swipe;

    String myImage,myName,my_token;

    Uri imageUri;
    private final int galleryPick=1;
    String downloadImageUrl,hasImage="";
    private StorageReference chatImageRef;
    ImageView iv_msg,iv_cancel,iv_insert_photo;

    public static String isChatting="";
    LinearLayoutManager lm;
    Executor postExecutor;
    boolean isVip;
    String team;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher);

        sharedPreferences=getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        myId=Long.parseLong(sharedPreferences.getString("phone",null))+"";
        myImage=sharedPreferences.getString("imageUrl",null);
        myName=sharedPreferences.getString("Username",null);
        my_token=sharedPreferences.getString("token","");
        isVip=sharedPreferences.getBoolean("isVIP",false);
        team=getIntent().getExtras().getString("team");


        fImage=getIntent().getExtras().getString("imageUrl");
        chatImageRef= FirebaseStorage.getInstance().getReference().child("Chat Images");
        db= FirebaseDatabase.getInstance().getReference().child(Routing.MAJOR).child(team).child("ChatRoom");
        dbc=FirebaseDatabase.getInstance().getReference().child(Routing.MAJOR).child(team).child("Conservation");
        dbn=FirebaseDatabase.getInstance().getReference().child(Routing.MAJOR).child("notification");

        dbc.child(myId).child("seen").setValue(0);

        if(team.equals("Developer")){
            dbn.child(myId).child("developer_message").removeValue();
            SplashScreenActivity.DEVELOPER_MESSAGE=false;
        }else{
            dbn.child(myId).child("teacher_message").removeValue();
            SplashScreenActivity.TEACHER_MESSAGE=false;
        }


        setUpView();
        setUpActionBar();

        fetchMessage();
        postExecutor = ContextCompat.getMainExecutor(this);


    }

    @Override
    protected void onDestroy() {
        isChatting="";
        super.onDestroy();
    }

    private void setUpActionBar(){
        TextView tv_name,tv_status;
        ImageView iv_profile,iv_back,iv_more;
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater inflator = (LayoutInflater) this .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflator.inflate(R.layout.my_action_bar_chat, null);

        tv_name=v.findViewById(R.id.tv_username2);
        tv_status=v.findViewById(R.id.tv_status);
        iv_back=v.findViewById(R.id.iv_back);
        iv_profile=v.findViewById(R.id.iv_profile);
        iv_more=v.findViewById(R.id.iv_menuMore);

        tv_name.setText(team);

        tv_status.setText(Html.fromHtml("<small><font color=\"#00ff00\">" +"Active now"+ "</font></small>"));

        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();
            }
        });

        iv_more.setVisibility(View.INVISIBLE);


        AppHandler.setPhotoFromRealUrl(iv_profile,fImage);

        actionBar.setCustomView(v);

    }

    private void setUpView(){
        recyclerView=findViewById(R.id.recyclerChat);
        et_chatInput=findViewById(R.id.et_chat_input);
        ibtSend=findViewById(R.id.chat_send_bt);
        swipe=findViewById(R.id.swipe);
        iv_msg=findViewById(R.id.iv_msg);
        iv_cancel=findViewById(R.id.iv_cancel);
        iv_insert_photo=findViewById(R.id.iv_insert_photo);
        lm = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(lm);

        recyclerView.setItemAnimator(new DefaultItemAnimator());
        adapter = new ChatAdapter(this,ChatList);

        recyclerView.setAdapter(adapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        ibtSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msgBody=et_chatInput.getText().toString();
                Long time = System.currentTimeMillis();
                if(!TextUtils.isEmpty(msgBody)) {
                    if(hasImage.equals(""))sendMessage(msgBody, time);
                }
                if(!hasImage.equals("")){
                    sendingImage(msgBody,time);
                    ChatList.add(new ChatModel(myId,msgBody,time,1,imageUri.toString()));
                }
                et_chatInput.setText("");
                iv_msg.setImageBitmap(null);
                iv_msg.setVisibility(View.GONE);
                iv_cancel.setVisibility(View.GONE);
                hasImage="";
            }
        });

        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                ChatList.clear();
                fetchMessage();
            }
        });

        iv_insert_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isPermissionGranted()){
                    pickImageFromGallery();
                }else {
                    takePermission();
                }
            }
        });


        iv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iv_msg.setImageBitmap(null);
                iv_msg.setVisibility(View.GONE);
                iv_cancel.setVisibility(View.GONE);
                hasImage="";

            }
        });


        et_chatInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recyclerView.smoothScrollToPosition(ChatList.size()-1);
            }
        });

    }

    private void fetchMessage(){
        swipe.setRefreshing(true);

        db.child(myId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try{
                    ChatList.clear();

                    for (DataSnapshot dss: snapshot.getChildren()){
                        String senderId=(String)dss.child("senderId").getValue();
                        String msg=(String)dss.child("msg").getValue();
                        Long time= (Long) dss.child("time").getValue();
                        String imageUrl=(String)dss.child("imageUrl").getValue();
                        long seen=(Long)dss.child("seen").getValue();
                        ChatModel chatModel=new ChatModel(senderId,msg,time,(int)seen,imageUrl);

                        ChatList.add(chatModel);


                    }
                    adapter.notifyItemInserted(ChatList.size());
                    recyclerView.smoothScrollToPosition(ChatList.size());
                    swipe.setRefreshing(false);

                }catch (Exception e){
                    swipe.setRefreshing(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                swipe.setRefreshing(false);
            }
        });

    }


    private void sendingImage(String msg,long time){
        StorageReference filePath=chatImageRef.child(time+"");

        final UploadTask uploadTask=filePath.putFile(imageUri);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                String message=e.toString();
                Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                Task<Uri> urlTask=uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()){
                            throw task.getException();
                        }

                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if(task.isSuccessful()){
                            filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
                            {
                                @Override
                                public void onSuccess(Uri downloadUrl)
                                {
                                    downloadImageUrl=downloadUrl.toString();
                                    saveMessageOnFirebase( msg,time,downloadImageUrl);
                                }
                            });

                        }
                    }
                });
            }
        });
    }

    //this method is invoked when sending a message with photo
    private void saveMessageOnFirebase(String msg,long time,String imageUrl) {
        String msgNull="";
        if(msg.equals(""))msgNull=myName+" sent a photo";
        else msgNull=msg;
        db.child(myId).child(time+"").setValue(new ChatModel(myId,msg,time,1,imageUrl));
        dbc.child(myId).setValue(new ConservationModel(myId,myName,myImage,msgNull,time+"",myId,my_token,0));
        // if(!activeNow[0])sendNotification(myName,msgNull,fri_token,web_link);
        dbc.child(myId).child("list").setValue(1-time);
        dbc.child(myId).child(team).setValue(1);
        PushNotiToAdmin(msg);
    }

    //this method is invoked for message without image;
    private void sendMessage(String msg,long time){

        db.child(myId).child(time+"").setValue(new ChatModel(myId,msg,time,1,""));
        dbc.child(myId).setValue(new ConservationModel(myId,myName,myImage,msg,time+"",myId,my_token,0));
        //  if(!activeNow[0])sendNotification(myName,msg,fri_token,web_link);
        dbc.child(myId).child("list").setValue(1-time);
        dbc.child(myId).child(team).setValue(1);
        PushNotiToAdmin(msg);
    }

    public void PushNotiToAdmin(String message){

        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.POST, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {}
                @Override
                public void onError(String msg) {}
            }).url(Routing.PUSH_NOTIFICATION_TOPIC)
                    .field("title",myName)
                    .field("message",team+" : "+message)
                    .field("to",Routing.ADMIN_TOPIC);
            myHttp.runTask();
        }).start();
    }

    private boolean isPermissionGranted(){
        int  readExternalStorage;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            readExternalStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES);
        }else{
            readExternalStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        return  readExternalStorage==PackageManager.PERMISSION_GRANTED;
    }

    private void takePermission(){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_MEDIA_IMAGES},101);
        }else{
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},101);
        }

    }

    private void pickImageFromGallery(){
        mGetContent.launch("image/*");
    }

    ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri uri) {
                    // Handle the returned Uri
                    imageUri=uri;
                    iv_msg.setVisibility(View.VISIBLE);
                    iv_msg.setImageURI(imageUri);
                    iv_cancel.setVisibility(View.VISIBLE);
                    hasImage=imageUri.toString();
                }
            });



    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {

            finish();
        }
        return super.onOptionsItemSelected(item);
    }

}