package com.calamus.easykorean;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.calamus.easykorean.adapters.ChatAdapter;
import com.calamus.easykorean.app.MyHttp;
import com.calamus.easykorean.app.Routing;
import com.calamus.easykorean.controller.NotificationController;
import com.calamus.easykorean.models.ChatModel;
import com.calamus.easykorean.models.ConservationModel;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.Executor;
import me.myatminsoe.mdetect.MDetect;
import static android.content.ContentValues.TAG;
import static com.calamus.easykorean.app.AppHandler.changeFont;
import static com.calamus.easykorean.app.AppHandler.setMyanmar;


public class ChattingActivity extends AppCompatActivity {


    ImageButton ibtSend;
    EditText et_chatInput;
    RecyclerView recyclerView;
    private ChatAdapter adapter;
    private final ArrayList<ChatModel> ChatList = new ArrayList<>();
    private final ArrayList<ChatModel> newMsgOnServer=new ArrayList<>();

    String myId,FId,fName;
    SharedPreferences sharedPreferences;
    private DatabaseReference db,dbc;
    SwipeRefreshLayout swipe;
    public static String fImage;
    SQLiteDatabase dbLite;
    String dbdir,myImage,myName,fri_token,my_token;
    final String dbName="conservation.db";
    String dbPath;
    int counting;

    Uri imageUri;
    private final int galleryPick=1;
    String downloadImageUrl,hasImage="";
    private StorageReference chatImageRef;
    ImageView iv_msg,iv_cancel,iv_insert_photo;
    final boolean [] activeNow={false};

    public static String isChatting="";
    LinearLayoutManager lm;
    Executor postExecutor;
    FirebaseDatabase firebaseDatabase;

    ValueEventListener valueEventListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_chatting);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        sharedPreferences=getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        myId=Long.parseLong(sharedPreferences.getString("phone",null))+"";
        myImage=sharedPreferences.getString("imageUrl",null);
        myName=sharedPreferences.getString("Username",null);
        my_token=sharedPreferences.getString("token","");


        FId=getIntent().getExtras().getString("fId");
        fImage=getIntent().getExtras().getString("fImage");
        fName=getIntent().getExtras().getString("fName");
        fri_token=getIntent().getExtras().getString("token");
        isChatting=FId;

        MDetect.INSTANCE.init(this);
        chatImageRef= FirebaseStorage.getInstance().getReference().child("Chat Images");
        firebaseDatabase=FirebaseDatabase.getInstance();

        db= firebaseDatabase.getReference().child(Routing.MAJOR).child("ChatRoom");
        dbc=firebaseDatabase.getReference().child(Routing.MAJOR).child("Conservation");

        dbdir= Objects.requireNonNull(getFilesDir()).getPath()+"/databases/";
        dbPath=dbdir+dbName;
        dbLite=SQLiteDatabase.openDatabase(dbPath,null,SQLiteDatabase.OPEN_READWRITE);

        setUpView();

        setTitle((Html.fromHtml("<small><font>" +setMyanmar(fName)+ "</font></small>")));
        status(FId);

        checkSeen();



        fetchMessage();
        postExecutor = ContextCompat.getMainExecutor(this);

        if(!isOnline()){
            fetchMessageFromLocal();
            swipe.setRefreshing(false);
        }

    }

    @Override
    protected void onDestroy() {
        isChatting="";
        super.onDestroy();
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


        ibtSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msgBody=changeFont(et_chatInput.getText().toString());
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
                counting=counting+20;
                loadMoreMessage(counting);
            }
        });

        iv_insert_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        menu.add("Profile");
        menu.add("Clear all messages");
        menu.add("Block");

        return super.onCreateOptionsMenu(menu);
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            setSeenOnLocalConservation();
            finish();
        }else{
            String s = item.getTitle().toString();
            switch (s) {

                case "Profile":
                    Intent intent=new Intent(ChattingActivity.this,MyDiscussionActivity.class);
                    intent.putExtra("userId",FId); // give fri Id
                    intent.putExtra("userName",fName);
                    startActivity(intent);
                    break;
                case "Clear all messages":
                    confirmDeleteMessage();
                    break;
                case "Block":
                    blockUser();
                    break;

            }
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean isConservationExist(){
        String query="SELECT*FROM Conservations WHERE fri_id="+FId+" and my_id="+myId;
        Cursor cursor=dbLite.rawQuery(query,null);


        return cursor.getCount()>0;
    }

    private void makeConservation(String msgBody, String senderId, long time, String token){
        if(isConservationExist()){
            ContentValues cv=new ContentValues();
            cv.put("msg_body",msgBody);
            cv.put("fri_name",setMyanmar(fName));
            cv.put("time",time+"");
            cv.put("senderId",senderId);
            cv.put("token",token);
            cv.put("seen", 2);
            dbLite.update("Conservations",cv,"fri_id="+FId+" and my_id="+myId,null);
        }else{
            ContentValues cv=new ContentValues();
            cv.put("fri_id",FId);
            cv.put("fri_name",setMyanmar(fName));
            cv.put("fri_image",fImage);
            cv.put("msg_body",msgBody);
            cv.put("time",time+"");
            cv.put("senderId",senderId);
            cv.put("token",token);
            cv.put("seen", 2);
            cv.put("my_id",myId);
            dbLite.insert("Conservations",null,cv);

        }
    }


    private void fetchMessage(){
        swipe.setRefreshing(true);
         ArrayList<ChatModel> tempChatList=new ArrayList<>();
        valueEventListener=new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try{
                    tempChatList.clear();
                    ChatList.clear();
                    newMsgOnServer.clear();
                    for (DataSnapshot dss: snapshot.getChildren()){
                        String senderId=(String)dss.child("senderId").getValue();
                        String msg=(String)dss.child("msg").getValue();
                        Long time= (Long) dss.child("time").getValue();
                        String imageUrl=(String)dss.child("imageUrl").getValue();

                        if(activeNow[0]){
                            tempChatList.add(new ChatModel(senderId,msg,time,2,imageUrl));
                            newMsgOnServer.add(new ChatModel(senderId,msg,time,2,imageUrl));
                        }
                        else{
                            tempChatList.add(new ChatModel(senderId,msg,time,1,imageUrl));
                            newMsgOnServer.add(new ChatModel(senderId,msg,time,1,imageUrl));
                        }

                        makeConservation(msg,senderId,time,fri_token);
                        status(FId);

                    }

                    if(tempChatList.size()<10){
                        fetchMessageFromLocal();
                    }
                    ChatList.addAll(tempChatList);
                    adapter.notifyDataSetChanged();
                    recyclerView.smoothScrollToPosition(ChatList.size()-1);

                    swipe.setRefreshing(false);

                }catch (Exception e){
                    fetchMessageFromLocal();
                    Log.e("366 : ",e.toString());
                    swipe.setRefreshing(false);
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                swipe.setRefreshing(false);
                Log.e("374 : ",error.toString());
                fetchMessageFromLocal();
            }
        };

        db.child(myId+FId).addValueEventListener(valueEventListener);

    }



    private void addToChat(String senderId, String msg, String time, String imageUrl,int seen){

        ContentValues cv=new ContentValues();
        cv.put("chatRoom",FId+myId);
        cv.put("senderId",senderId);
        cv.put("msg",msg);
        cv.put("time",time);
        cv.put("seen",seen);
        cv.put("imageUrl",imageUrl);
        dbLite.insert("Chats",null,cv);
        Log.e("CV : ",cv.toString());
    }

    private boolean saveNewMessageOnLocal(){
        for(int i=0;i<newMsgOnServer.size();i++){
            long time=newMsgOnServer.get(i).getTime();
            addToChat(newMsgOnServer.get(i).getSenderId(),newMsgOnServer.get(i).getMsg(),time+"",newMsgOnServer.get(i).getImageUrl(),newMsgOnServer.get(i).getSeen());
        }

        return  true;
    }

    private void deleteMessageFromFIrebase(String chatRoomId) {

       if( saveNewMessageOnLocal()){
           Query applesQuery = db.child(chatRoomId);

           applesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
               @Override
               public void onDataChange(DataSnapshot dataSnapshot) {
                   for (DataSnapshot appleSnapshot: dataSnapshot.getChildren()) {
                       appleSnapshot.getRef().removeValue();

                   }
               }

               @Override
               public void onCancelled(DatabaseError databaseError) {
                   Log.e(TAG, "onCancelled", databaseError.toException());
               }
           });
       }
    }



    private void fetchMessageFromLocal(){
        swipe.setRefreshing(false);
        counting=20;
     //   String query="SELECT*FROM Chats WHERE (senderId="+myId+" AND chatRoom like '%"+FId+myId+"%') OR senderId="+FId+ " ORDER BY time DESC LIMIT 20";
        String query="SELECT*FROM Chats WHERE  chatRoom like '%"+FId+myId+"%' ORDER BY time DESC LIMIT 20";

        Cursor cursor=dbLite.rawQuery(query,null);
        if(cursor.moveToLast()){

            for(int i=0;i<cursor.getCount();i++){
                //int id=cursor.getInt(0);
                String senderId=cursor.getString(2);
                String msgBody=cursor.getString(3);
                String time=cursor.getString(4);
                String imageUrl=cursor.getString(5);
                int seen=cursor.getInt(6);
                ChatList.add(new ChatModel(senderId,msgBody,Long.parseLong(time),seen,imageUrl));

              //  Log.e("dbMsg: ", id+" : "+senderId+" : "+msgBody+" : "+time+" : "+imageUrl+" : "+seen);
                cursor.moveToPrevious();

            }

            adapter.notifyDataSetChanged();
            recyclerView.smoothScrollToPosition(ChatList.size()-1);

        }
        cursor.close();
    }

    private void loadMoreMessage(int count){
        swipe.setRefreshing(false);
        long firstIndexTime=getMsgTimeAtIndexZero();
        int itemPost=0;
       // String query="SELECT*FROM Chats WHERE (senderId="+myId+" AND chatRoom like '%"+FId+myId+"%') OR senderId="+FId+ " ORDER BY time DESC LIMIT "+count;
        String query="SELECT*FROM Chats WHERE chatRoom like '%"+FId+myId+"%' ORDER BY time DESC LIMIT "+count;

        Cursor cursor=dbLite.rawQuery(query,null);
        if(cursor.moveToLast()){

            for(int i=0;i<cursor.getCount();i++){

                String senderId=cursor.getString(2);
                String msgBody=cursor.getString(3);
                String time=cursor.getString(4);
                long timeStamp=Long.parseLong(time);
                String imageUrl=cursor.getString(5);
                int seen=cursor.getInt(6);

                if(firstIndexTime==0){
                    ChatList.add(new ChatModel(senderId,msgBody,timeStamp,seen,imageUrl));
                    adapter.notifyDataSetChanged();
                }

                if(timeStamp<firstIndexTime){
                    ChatList.add(itemPost++,new ChatModel(senderId,msgBody,timeStamp,seen,imageUrl));
                    adapter.notifyItemInserted(itemPost);
                }
                cursor.moveToPrevious();

            }

            lm.scrollToPositionWithOffset(19,0);


        }
        cursor.close();
    }

    private long getMsgTimeAtIndexZero(){
        if (ChatList.size()!=0){
            ChatModel model=ChatList.get(0);
            return model.getTime();
        }else {
            return 0;
        }

    }


    private long getLastMessageTime(){
        long a=0;
        String query="SELECT*FROM Chats  WHERE (senderId="+myId+" AND chatRoom="+FId+myId+") OR senderId="+FId+" ORDER BY time DESC LIMIT 1;";
        //  String query="SELECT*FROM Chats    ORDER BY time DESC LIMIT 1;";
        Cursor cursor=dbLite.rawQuery(query,null);
        if(cursor.moveToFirst()){

            for(int i=0;i<cursor.getCount();i++){

                String time=cursor.getString(4);

                a=Long.parseLong(time);

                cursor.moveToNext();

            }
        }
        return a;

    }


    private void status(String fId){

        DatabaseReference dbA=FirebaseDatabase.getInstance().getReference().child("korea").child("Active").child(fId);

        dbA.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child("active").exists()){
                    boolean active = (Boolean) snapshot.child("active").getValue();

                    long time=(Long)snapshot.child("time").getValue();
                    if(active) {
                        Objects.requireNonNull(getSupportActionBar()).setSubtitle((Html.fromHtml("<small><font color=\"#00ff00\">" +"Active now"+ "</font></small>")));
                        activeNow[0]=true;
                    }
                    else {
                        activeNow[0]=false;
                        Objects.requireNonNull(getSupportActionBar()).setSubtitle((Html.fromHtml("<small><font>" +calculateMin(time)+ "</font></small>")));
                    }

                }else{
                    Objects.requireNonNull(getSupportActionBar()).setSubtitle((Html.fromHtml("<small><font>" +"Offline"+ "</font></small>")));
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });


    }

    private String calculateMin(long time){
        long currentTime=System.currentTimeMillis();
        long timeDifference=currentTime-time;
        long s=1000;
        long min=s*60;
        long hour=min*60;
        long day=hour*24;
        if(timeDifference<min)return "active "+timeDifference/s+" s ago";
        else if(timeDifference>min&&timeDifference<hour) return "active "+timeDifference/min+" min ago";
        else if(timeDifference>hour&&timeDifference<day) return "active "+timeDifference/hour+ " h ago";
        else if(timeDifference>day) return "active "+timeDifference/day +" d ago";
        else if(timeDifference>day*30)return "Offline";
        else return null;
    }

    private void checkSeen(){

        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.child(FId+myId).exists()){
                    setSeenOnLocal();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

    }



    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) Objects.requireNonNull(this).getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = Objects.requireNonNull(cm).getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }



    private void clearAllMessages(){
        dbLite.delete("Chats", "(senderId="+myId+" AND chatRoom="+FId+") OR senderId="+FId, null);
        makeConservation("No message",myId,System.currentTimeMillis(),fri_token);
        ChatList.clear();
        adapter.notifyDataSetChanged();

    }

    private void openGallery() {

        Intent galleryIntent=new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,galleryPick);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==galleryPick && resultCode==RESULT_OK && data!=null){
            imageUri=data.getData();
            iv_msg.setVisibility(View.VISIBLE);
            iv_msg.setImageURI(imageUri);
            iv_cancel.setVisibility(View.VISIBLE);
            hasImage=imageUri.toString();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        int storageRequestCode = 123;
        if(requestCode== storageRequestCode){
            if(grantResults.length==2 && grantResults[0]== PackageManager.PERMISSION_GRANTED && grantResults[1]==PackageManager.PERMISSION_GRANTED){


            }else{
                if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE) ||   ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) ){
                    Toast.makeText(this,"Storage permission is required.",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(this,"Allow storage permission in your phone setting.",Toast.LENGTH_SHORT).show();
                }
            }
        }
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
        String msgNull;
        if(msg.equals(""))msgNull=myName+" sent a photo";
        else msgNull=msg;
        db.child(myId+FId).child(time+"").setValue(new ChatModel(myId,msg,time,1,imageUrl));
        db.child(FId+myId).child(time+"").setValue(new ChatModel(myId,msg,time,1,imageUrl));
        dbc.child(FId).child(myId).setValue(new ConservationModel(myId,myName,myImage,msgNull,time+"",myId,my_token,1));

        NotificationController notificationController=new NotificationController(this);
        notificationController.sendNotification(msg,fri_token,myName,"2");


    }

    //this method is invoked for message without image;
    private void sendMessage(String msg,long time){

        db.child(myId+FId).child(time+"").setValue(new ChatModel(myId,msg,time,1,""));
        db.child(FId+myId).child(time+"").setValue(new ChatModel(myId,msg,time,1,""));
        dbc.child(FId).child(myId).setValue(new ConservationModel(myId,myName,myImage,msg,time+"",myId,my_token,1));

        NotificationController notificationController=new NotificationController(this);
        notificationController.sendNotification(msg,fri_token,myName,"2");


    }

    private void blockUser(){
        friendShip(FId,myId, Routing.UN_FRIEND);
        dbc.child(FId).child(myId).setValue(new ConservationModel(myId,myName,myImage,"block5241",System.currentTimeMillis()+"",myId,"",1));
        ContentValues cv=new ContentValues();
        cv.put("msg_body","You block "+ fName);
        cv.put("fri_name","Easy Korean User");
        cv.put("time",System.currentTimeMillis()+"");
        cv.put("senderId",myId);
        cv.put("token","");
        cv.put("seen", 2);
        cv.put("fri_id","5241");
        dbLite.update("Conservations",cv,"fri_id="+FId+" and my_id="+myId,null);
        finish();
    }

    private void friendShip(String userId,String currentUserId,String url){
        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.POST, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    postExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            Log.e("Res: ",response);
                        }
                    });
                }
                @Override
                public void onError(String msg) {
                    Log.e("Err: ", msg);
                }
            }).url(url)
                    .field("my_id",currentUserId)
                    .field("other_id",userId)
                    .field("major",Routing.MAJOR);
            myHttp.runTask();
        }).start();
    }

    private void confirmDeleteMessage(){
        final AlertDialog ad = new AlertDialog.Builder(ChattingActivity.this).create();
        ad.setTitle("Delete Confirmation");
        ad.setIcon(R.mipmap.kommmainicon);
        ad.setMessage("Do you really want to delete?");
        ad.setButton(AlertDialog.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                clearAllMessages();
            }
        });

        ad.setButton(AlertDialog.BUTTON_NEGATIVE, "No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                ad.dismiss();
            }
        });
        ad.show();
    }

    //this method set seen on chatting table
    private void setSeenOnLocal(){
        ContentValues cv=new ContentValues();
        cv.put("seen",2);
        dbLite.update("Chats",cv,"chatRoom like '%"+FId+myId+"%'",null);

    }

    //this method set seen on conservation table;
    private void setSeenOnLocalConservation(){
        ContentValues cv=new ContentValues();
        cv.put("seen",2);
        dbLite.update("Conservations",cv,"fri_id="+FId,null);
    }

    @Override
    public void onBackPressed() {
        isChatting="";
        valueEventListener=null;
        deleteMessageFromFIrebase(myId+FId);
        super.onBackPressed();
        setSeenOnLocalConservation();

    }

}