package com.calamus.easykorean.app;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MyImagePicker extends AppCompatActivity {
    private AppCompatActivity c;
    public Callback callback;
    ActivityResultLauncher<PickVisualMediaRequest> pickMedia;
    ActivityResultLauncher<String> mGetContent;
    MyImagePicker myImagePicker;
    public MyImagePicker(AppCompatActivity c){
        this.c = c;
        myImagePicker = this;
        pickMedia = c.registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            // Callback is invoked after the user selects a media item or closes the
            // photo picker.
            this.callback.onResult(uri);
        });
        mGetContent = c.registerForActivityResult(new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri uri) {
                        // Handle the returned Uri
                        myImagePicker.callback.onResult(uri);
                    }
                });
    }
    public MyImagePicker(AppCompatActivity c, Callback callback){
        this.c = c;
        this.callback = callback;
        myImagePicker = this;
        pickMedia = c.registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                    // Callback is invoked after the user selects a media item or closes the
                    // photo picker.
                    this.callback.onResult(uri);
                });
        mGetContent = c.registerForActivityResult(new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri uri) {
                        // Handle the returned Uri
                        myImagePicker.callback.onResult(uri);
                    }
                });
    }

    public void pick(Callback callback){
        this.callback = callback;
        if(isPermissionGranted()){
            pickImageFromGallery();
        }else {
            takePermission();
        }
    }

    private boolean isPermissionGranted(){
        int  readExternalStorage;
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.TIRAMISU) {
            readExternalStorage = ContextCompat.checkSelfPermission(c, Manifest.permission.READ_EXTERNAL_STORAGE);
            return  readExternalStorage== PackageManager.PERMISSION_GRANTED;
        }else{
            return true;
        }
    }

    private void takePermission(){
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(c,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},101);
        }
    }

    private void pickImageFromGallery(){
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.TIRAMISU) {
            mGetContent.launch("image/*");
        }else{
            pickMedia.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
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

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public interface Callback{
        void onResult(Uri uri);
    }
}
