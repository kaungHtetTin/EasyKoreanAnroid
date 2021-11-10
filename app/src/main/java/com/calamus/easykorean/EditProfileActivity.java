package com.calamus.easykorean;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.calamus.easykorean.app.AppHandler;
import com.calamus.easykorean.app.MyHttp;
import com.calamus.easykorean.app.RealPathUtil;
import com.calamus.easykorean.app.Routing;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;
import java.util.concurrent.Executor;
import me.myatminsoe.mdetect.MDetect;

public class EditProfileActivity extends AppCompatActivity {

    EditText et_name,et_email,et_education,et_work,et_region;
    ImageView iv;
    Button bt;
    ProgressBar pb;
    private final int galleryPick=1;
    private final int storageRequestCode=123;
    Uri imageUri;
    String ivName="",imagePath="",userPhone,userName,userEmail,G_gender,born,month,day,bd_d,bd_m,bd_y,userWork,userEducation,userRegion;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    Executor postExecutor;
    TextView tv_genderEdit,tv_myGender;

    private final ArrayList<Integer> year=new ArrayList<Integer>();
    private final ArrayList<Integer> days=new ArrayList<Integer>();

    TextView tv_bird_edit,tv_myBirthday;
    RelativeLayout bd_layout;
    Button bt_cancel,bt_done;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        sharedPreferences=getSharedPreferences("GeneralData", Context.MODE_PRIVATE);
        editor=sharedPreferences.edit();
        MDetect.INSTANCE.init(this);
        postExecutor = ContextCompat.getMainExecutor(this);
        userPhone=sharedPreferences.getString("phone",null);

        setTitle("Edit Profile");
        setUpView();
        getData();

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }


    private void setUpView(){
        et_name=findViewById(R.id.et_name);
        et_email=findViewById(R.id.et_email);
        iv=findViewById(R.id.iv_profile);
        bt=findViewById(R.id.bt_save);
        bd_layout=findViewById(R.id.bd_layout);
        bt_cancel=findViewById(R.id.bt_cancel);
        bt_done=findViewById(R.id.bt_done);
        pb=findViewById(R.id.pb_loading);
        pb.setVisibility(View.INVISIBLE);
        tv_bird_edit=findViewById(R.id.tv_birthday_edit);
        tv_myBirthday=findViewById(R.id.tv_myBirthday);
        tv_genderEdit=findViewById(R.id.tv_gender_edit);
        tv_myGender=findViewById(R.id.tv_myGener);
        et_education=findViewById(R.id.et_education);
        et_work=findViewById(R.id.et_work);
        et_region=findViewById(R.id.et_region);


        Spinner sp_birth_year = findViewById(R.id.sp_birth_year);
        Spinner sp_birth_month = findViewById(R.id.sp_birth_month);
        Spinner sp_birth_day=findViewById(R.id.sp_birth_day);
        Spinner sp_gender = findViewById(R.id.sp_gender);


        setYear();
        ArrayAdapter<Integer> yearAdapter=new ArrayAdapter<Integer>(this,android.R.layout.simple_dropdown_item_1line,year);
        sp_birth_year.setAdapter(yearAdapter);
        sp_birth_year.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                born= year.get(position) +"";
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ArrayAdapter<String> monthAdapter=new ArrayAdapter<String>(this,android.R.layout.simple_dropdown_item_1line,months);
        sp_birth_month.setAdapter(monthAdapter);
        sp_birth_month.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                month=months[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        ArrayAdapter<Integer> dayAdapter=new ArrayAdapter<Integer>(this,android.R.layout.simple_dropdown_item_1line,days);
        sp_birth_day.setAdapter(dayAdapter);
        sp_birth_day.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                day= days.get(position) +"";
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        ArrayAdapter<String> genderAdapter=new ArrayAdapter<String>(this,android.R.layout.simple_dropdown_item_1line,gender);
        sp_gender.setAdapter(genderAdapter);
        sp_gender.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                G_gender=gender[position];
                sp_gender.setVisibility(View.GONE);
                tv_myGender.setVisibility(View.VISIBLE);
                tv_myGender.setText(G_gender);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                sp_gender.setVisibility(View.GONE);
                tv_myGender.setVisibility(View.VISIBLE);
            }
        });


        tv_bird_edit.setOnClickListener(v -> {
            tv_myBirthday.setVisibility(View.GONE);
            bd_layout.setVisibility(View.VISIBLE);
        });

        bt_done.setOnClickListener(v -> {
            tv_myBirthday.setVisibility(View.VISIBLE);
            bd_layout.setVisibility(View.GONE);
            bd_y=born;
            bd_m=month;
            bd_d=day;
            tv_myBirthday.setText(day+" "+month+" "+born);
        });

        bt_cancel.setOnClickListener(v -> {
            born=bd_y;
            month=bd_m;
            day=bd_d;
            tv_myBirthday.setText(day+" "+month+" "+born);
            tv_myBirthday.setVisibility(View.VISIBLE);
            bd_layout.setVisibility(View.GONE);
        });

        tv_genderEdit.setOnClickListener(v -> {
            tv_myGender.setVisibility(View.GONE);
            sp_gender.setVisibility(View.VISIBLE);
        });


        iv.setOnClickListener(v -> {
            if(isPermissionGranted())openGallery();
            else takePermission();
        });

        bt.setOnClickListener(v -> {
            pb.setVisibility(View.VISIBLE);
            userName=et_name.getText().toString();
            userEmail=et_email.getText().toString();
            userWork=et_work.getText().toString();
            userEducation=et_education.getText().toString();
            userRegion=et_region.getText().toString();
            if(userName.isEmpty()){
                pb.setVisibility(View.INVISIBLE);
                Toast.makeText(EditProfileActivity.this,"Please enter your name",Toast.LENGTH_SHORT).show();
            }

            if(!userName.isEmpty()){
                saveData();
            }

        });


    }

    private void setYear(){
        int currentYear= Calendar.getInstance().get(Calendar.YEAR);
        for(int i=currentYear-10;i>1973;i--){
            year.add(i);
        }

        for(int i=1;i<32;i++){
            days.add(i);
        }
    }

    private void takePermission(){
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},storageRequestCode);
    }

    private  boolean isPermissionGranted(){
        int readExternalStorage=ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE);
        return readExternalStorage==PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();

        }
        return super.onOptionsItemSelected(item);
    }


    private void openGallery() {
        mGetContent.launch("image/*");
    }

    ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri uri) {
                    // Handle the returned Uri
                    imageUri=uri;
                    iv.setImageURI(imageUri);
                    imagePath= RealPathUtil.getRealPath(EditProfileActivity.this,imageUri);
                    ivName=imagePath.substring(imagePath.lastIndexOf("/")+1);
                }
            });

    private void getData(){

        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.GET, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    postExecutor.execute(() -> {
                        pb.setVisibility(View.INVISIBLE);
                        doAsGetResult(response);
                    });
                }
                @Override
                public void onError(String msg) {
                    postExecutor.execute(() -> {
                        pb.setVisibility(View.VISIBLE);
                        Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show();
                    });
                }
            }).url(Routing.GET_PROFILE+"/"+userPhone);
            myHttp.runTask();
        }).start();


    }


    private void saveData(){

        new Thread(() -> {
            MyHttp myHttp=new MyHttp(MyHttp.RequesMethod.POST, new MyHttp.Response() {
                @Override
                public void onResponse(String response) {
                    postExecutor.execute(() -> {
                        pb.setVisibility(View.INVISIBLE);
                        doAsResult(response);
                    });
                }
                @Override
                public void onError(String msg) {
                    postExecutor.execute(() -> {
                        pb.setVisibility(View.VISIBLE);
                        Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show();
                    });
                }
            }).url(Routing.EDIT_PROFILE)
                    .field("phone",userPhone)
                    .field("name", AppHandler.changeUnicode(userName))
                    .field("email",userEmail)
                    .field("bd_day",bd_d)
                    .field("bd_month",bd_m)
                    .field("bd_year",bd_y)
                    .field("work",userWork)
                    .field("education",userEducation)
                    .field("region",userRegion)
                    .field("gender",G_gender);
            if(!ivName.isEmpty())myHttp.file("myfile",imagePath);
            myHttp.runTask();
        }).start();


    }

    private void doAsGetResult(String response){
        try {
            JSONObject jsonObject=new JSONObject(response);
            String name=jsonObject.getString("learner_name");
            String email=jsonObject.getString("learner_email");
            String imageUrl=jsonObject.getString("learner_image");
            String gender=jsonObject.getString("gender");
            String bd_day=jsonObject.getString("bd_day");
            String bd_month=jsonObject.getString("bd_month");
            String bd_year=jsonObject.getString("bd_year");
            String education=jsonObject.getString("education");
            String work=jsonObject.getString("work");
            String region=jsonObject.getString("region");

            bd_d=bd_day;
            bd_m=bd_month;
            bd_y=bd_year;
            G_gender=gender;
            tv_myGender.setText(gender);
            if(!bd_day.equals("")&&!bd_month.equals("")&&!bd_year.equals(""))tv_myBirthday.setText(bd_day+" "+bd_month+" "+bd_year);
            et_name.setText(AppHandler.setMyanmar(name));
            et_email.setText(email);
            AppHandler.setPhotoFromRealUrl(iv,imageUrl);
            et_education.setText(AppHandler.setMyanmar(education));
            et_work.setText(AppHandler.setMyanmar(work));
            et_region.setText(AppHandler.setMyanmar(region));

        }catch (Exception e){
            Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_SHORT).show();
        }
    }


    private void doAsResult(String response){

        try {
            JSONObject jsonObject=new JSONObject(response);
            String name=jsonObject.getString("learner_name");
            String email=jsonObject.getString("learner_email");
            String imageUrl=jsonObject.getString("learner_image");
            editor.putString("Username",name);
            editor.putString("userEmail",email);
            if(!imageUrl.equals("")){
                editor.putString("imageUrl",imageUrl);
                AppHandler.setPhotoFromRealUrl(iv,imageUrl);
            }
            editor.apply();
            Toast.makeText(getApplicationContext(),"All changes are saved",Toast.LENGTH_SHORT).show();
            finish();

        }catch (Exception e){
            Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_SHORT).show();
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==storageRequestCode){
            if(grantResults.length==1 && grantResults[0]== PackageManager.PERMISSION_GRANTED){
                openGallery();
            }else{
                takePermission();
            }
        }
    }



    final String[] gender={
            "Male",
            "Female"
    };


    final String [] months={
            "Jan",
            "Feb",
            "March",
            "April",
            "May",
            "June",
            "July",
            "Aug",
            "Sept",
            "Oct",
            "Nov",
            "Dec"
    };
}