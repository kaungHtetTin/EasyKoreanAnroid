package com.calamus.easykorean;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;



public class PhotoActivity extends AppCompatActivity {
    ImageView iv,iv_more;
    String image;
    ImageButton ibt;

    Bitmap imageBitmap=null;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);
        image=getIntent().getExtras().getString("image");

        setUpView();
    }

    private void setUpView(){
        iv=findViewById(R.id.iv_photo);
        ibt=findViewById(R.id.ibt_back);
        iv_more=findViewById(R.id.iv_more);

        if(!image.isEmpty()){
            Glide.with(this)
                    .asBitmap()
                    .load(image)
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            imageBitmap=resource;
                            iv.setImageBitmap(resource);
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                        }
                    });

        }else {
            iv.setBackgroundResource(R.drawable.ic_baseline_account_circle_24);
        }


        ibt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        iv_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMyMenu(v);
            }
        });
    }


    private void  showMyMenu(View v){
        PopupMenu popup=new PopupMenu(this,v);
        popup.getMenuInflater().inflate(R.menu.photo_menu,popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            int id=item.getItemId();
            if(id==R.id.save_photo){
                if(isPermissionGranted()){
                    if(imageBitmap!=null) {

                        try {
                            saveImage(imageBitmap,"Easy_English_"+System.currentTimeMillis());
                        } catch (IOException e) {
                            Log.e("photoSave ",e.toString());
                        }
                    }else{
                        Toast.makeText(getApplicationContext(),"Can't save.Please try again.",Toast.LENGTH_SHORT).show();
                    }
                }else{
                    takePermission();
                }

            }

            return true;
        });
        popup.show();
    }

    private boolean isPermissionGranted(){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return  true;
        }else{
            int  writeExternalStorage= ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            return  writeExternalStorage== PackageManager.PERMISSION_GRANTED;
        }

    }

    private void takePermission(){
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},101);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length>0){
            if(requestCode==101){
                boolean writeExternalStorage=grantResults[0]==PackageManager.PERMISSION_GRANTED;
                if(writeExternalStorage){
                    if(imageBitmap!=null) {
                        try {
                            saveImage(imageBitmap,"Easy_English_"+System.currentTimeMillis());
                        } catch (IOException e) {
                            Log.e("photoSave ",e.toString());
                        }
                    }else{
                        Toast.makeText(getApplicationContext(),"Can't save.Please try again.",Toast.LENGTH_SHORT).show();
                    }
                }else {
                    takePermission();
                }
            }
        }
    }



    private void saveImage(Bitmap bitmap, @NonNull String name) throws IOException {
        OutputStream fos;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            ContentResolver resolver = getContentResolver();
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, name + ".jpg");
            contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg");
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);

            Uri imageUri = resolver.insert(MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY), contentValues);

            Log.e("photoUri ",imageUri.toString());
            try {
                ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(imageUri, "w",null);
                fos = new FileOutputStream(pfd.getFileDescriptor());
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                Objects.requireNonNull(fos).close();
                pfd.close();
            }catch (Exception e){
                Log.e("photoErr ",e.toString());
                Toast.makeText(getApplicationContext(),"Can't save.Please try again.",Toast.LENGTH_SHORT).show();
            }

        } else {
            String imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
            File image = new File(imagesDir, name + ".jpg");
            fos = new FileOutputStream(image);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            Objects.requireNonNull(fos).close();
        }

        Toast.makeText(getApplicationContext(),"Saved",Toast.LENGTH_SHORT).show();
    }
}
