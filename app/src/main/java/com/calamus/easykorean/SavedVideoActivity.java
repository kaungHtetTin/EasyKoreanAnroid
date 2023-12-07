package com.calamus.easykorean;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.calamus.easykorean.adapters.SavedVideoAdapter;
import com.calamus.easykorean.app.FileManager;
import com.calamus.easykorean.models.FileModel;
import com.calamus.easykorean.models.FolderModel;
import java.io.File;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.Executor;


public class SavedVideoActivity extends AppCompatActivity {


    RecyclerView recyclerView;
    LinearLayout layoutFileManager;
    RelativeLayout layoutProgress;
    TextView tv_move,tv_delete,tv_currentDir;
    ProgressBar pb_info;
    TextView tv_infoHeader,tv_progress;
    ArrayList<FileModel> fileLists =new ArrayList<>();
    ArrayList<FileModel> dirLists=new ArrayList<>();
    ArrayList<Integer> selectedIndex=new ArrayList<>();
    ArrayList<String> destFolderNames=new ArrayList<>();
    SavedVideoAdapter adapter,dirAdapter;
    Executor postExecutor;
    FloatingActionButton fab;
    File rootDirectory;
    String rootPath;
    boolean selecting=false;
    FolderModel destFolder;
    FileManager fileManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_video);
        rootPath=getIntent().getExtras().getString("rootPath");
        rootDirectory= new File(rootPath);
        postExecutor = ContextCompat.getMainExecutor(this);
        fileManager=new FileManager(this);

        Objects.requireNonNull(getSupportActionBar()).hide();
        setUpView();
        setUpCustomAppBar();

        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                back();
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        loadFile();
    }

    private void back(){
        if(selecting){
            selecting=false;
            setSelectingState(0);
            layoutFileManager.setVisibility(View.GONE);
            fab.setVisibility(View.VISIBLE);
        }else{
             finish();
        }
    }


    private void setUpCustomAppBar(){
        TextView tv=findViewById(R.id.tv_appbar);
        ImageView iv=findViewById(R.id.iv_back);
        tv.setText("Downloads");
        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                back();
            }
        });
    }

    private void setUpView(){
        recyclerView=findViewById(R.id.recycler);
        fab=findViewById(R.id.fab);
        layoutFileManager=findViewById(R.id.layoutFileManager);
        tv_delete=findViewById(R.id.tv_delete);
        tv_move=findViewById(R.id.tv_move);
        layoutProgress=findViewById(R.id.progressLayout);
        tv_infoHeader=findViewById(R.id.tv_infoHeader);
        tv_progress=findViewById(R.id.tv_progress);
        pb_info=findViewById(R.id.pb_info);
        tv_currentDir=findViewById(R.id.tv_currentDir);

        adapter=new SavedVideoAdapter(fileLists, this, false,new SavedVideoAdapter.CallBack() {
            @Override
            public void onLongClick(int position) {
                selecting=true;
                setSelectingState(1);
                fab.setVisibility(View.GONE);
                layoutFileManager.setVisibility(View.VISIBLE);
            }

            @Override
            public void onSelected(int position) {
                selected(position);
            }

            @Override
            public void onDirectoryChosen(FolderModel model) {

            }
        });
        LinearLayoutManager lm=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(lm);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCreateNewFolderDialog();
            }
        });



        tv_move.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(selectedIndex.size()>0){
                    showDialogForDirectoryChoosing();
                }else{
                    Toast.makeText(getApplicationContext(),"Select files for moving",Toast.LENGTH_SHORT).show();
                }
            }
        });

        tv_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(selectedIndex.size()>0){
                    deleteFiles();
                }else{
                    Toast.makeText(getApplicationContext(),"Select files for delete",Toast.LENGTH_SHORT).show();
                }
            }
        });

        setCurrentDir();

    }

    private void setCurrentDir(){
        int temp=rootPath.indexOf("Movies");
        tv_currentDir.setText("Downloads"+rootPath.substring(temp+6));
    }

    private void showCreateNewFolderDialog() {
        EditText et_folderName;
        TextView tv_cancel,tv_create;

        View v=getLayoutInflater().inflate(R.layout.dialog_create_new_folder,null);
        v.setAnimation(AnimationUtils.loadAnimation(this,R.anim.transit_up));
        et_folderName=v.findViewById(R.id.et_folderName);
        tv_cancel=v.findViewById(R.id.tv_cancel);
        tv_create=v.findViewById(R.id.tv_create);

        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setView(v);
        final AlertDialog ad=builder.create();
        ad.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        ad.show();

        tv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ad.dismiss();
            }
        });

        tv_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!TextUtils.isEmpty(et_folderName.getText().toString())){
                    createNewFolder(et_folderName.getText().toString());
                    ad.dismiss();
                }else{
                    Toast.makeText(getApplicationContext(),"Please enter new folder name",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void showDialogForDirectoryChoosing(){
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this,R.style.SheetDialog);
        bottomSheetDialog.setContentView(R.layout.dialog_directory_chooser);
        RecyclerView recyclerViewDir=bottomSheetDialog.findViewById(R.id.recyclerViewDirectory);
        TextView tv_move_here=bottomSheetDialog.findViewById(R.id.tv_move_here);
        TextView tv_path=bottomSheetDialog.findViewById(R.id.tv_path);
        ImageView iv_goParent=bottomSheetDialog.findViewById(R.id.iv_goParent);
        String rootPath=getExternalFilesDir(Environment.DIRECTORY_MOVIES).getAbsolutePath();


        dirAdapter=new SavedVideoAdapter(dirLists, this, true, new SavedVideoAdapter.CallBack() {
            @Override
            public void onLongClick(int position) {

            }

            @Override
            public void onSelected(int position) {

            }

            @Override
            public void onDirectoryChosen(FolderModel model) {
                destFolder=model;
                String destPath =model.getFile().getAbsolutePath();
                loadDirectory(destPath);
                destFolderNames.add(model.getFile().getName());
                tv_path.setText(navigateCurrentFolder());

            }
        });

        LinearLayoutManager lm=new LinearLayoutManager(this);
        recyclerViewDir.setLayoutManager(lm);
        recyclerViewDir.setAdapter(dirAdapter);

        tv_move_here.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
                moveFiles(rootPath+navigateCurrentFolder());
            }
        });

        iv_goParent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(destFolderNames.size()>0){
                    destFolderNames.remove(destFolderNames.size()-1);
                    tv_path.setText(navigateCurrentFolder());
                }
                loadDirectory(rootPath+navigateCurrentFolder());
            }
        });

        loadDirectory(rootPath);
        tv_path.setText(navigateCurrentFolder());
        bottomSheetDialog.show();

    }


    private void setSelectingState(int state){
        for(int i=0;i<fileLists.size();i++){
            fileLists.get(i).setSelectedState(state);
        }
        adapter.notifyDataSetChanged();
    }

    private void selected(int position){
        FileModel model=fileLists.get(position);
        if(model.getSelectedState()==1){
            model.setSelectedState(2); //selected
            selectedIndex.add(position);
            Log.e("select ",model.getFile().getName());
        }else if(model.getSelectedState()==2){
            model.setSelectedState(1); // unselected
            for(int i=0;i<selectedIndex.size();i++){
                if(selectedIndex.get(i)==position){
                    selectedIndex.remove(i);
                    break;
                }
            }

            Log.e("unselect ",model.getFile().getName());
        }

        adapter.notifyItemChanged(position);
    }

    private String navigateCurrentFolder(){
        StringBuilder result= new StringBuilder("/");
        for(int i=0;i<destFolderNames.size();i++){
            result.append(destFolderNames.get(i)).append("/");
        }
        return result.toString();
    }



    // file manager methods /////////////////////////////////////////////////////////////////


    private void loadFile(){
        fileManager.loadFiles(rootDirectory, new FileManager.OnFileLoading() {
            @Override
            public void onLoaded(ArrayList<FileModel> files) {
                fileLists.clear();
                fileLists.addAll(files);
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void loadDirectory(String path){
        fileManager.loadDirectory(new File(path), new FileManager.OnFileLoading() {
            @Override
            public void onLoaded(ArrayList<FileModel> files) {
                dirLists.clear();
                dirLists.addAll(files);
                dirAdapter.notifyDataSetChanged();
            }
        });
    }

    private void moveFiles(String destPath){
        layoutProgress.setVisibility(View.VISIBLE);
        tv_infoHeader.setText("Copying");
        fileManager.move(fileLists, selectedIndex, destPath, new FileManager.FileMovingListener() {

            @Override
            public void onFail(String msg) {
                Log.e("Fail Moving ",msg);
            }

            @Override
            public void onProgress(int progress) {
                tv_progress.setText(progress+"%");
                pb_info.setProgress(progress);
            }

            @Override
            public void onCompleted() {
                loadFile();
                selectedIndex.clear();
                layoutProgress.setVisibility(View.GONE);
                back();
                Log.e("Completed Moving ","All completed");
            }
        });
    }

    private void deleteFiles(){
        layoutProgress.setVisibility(View.VISIBLE);
        tv_infoHeader.setText("Deleting");
        fileManager.deleteFiles(fileLists, selectedIndex, new FileManager.FileDeletingListener() {
            @Override
            public void onProgress(int progress) {
                tv_progress.setText(progress+"%");
                pb_info.setProgress(progress);
            }

            @Override
            public void onCompleted() {
                selectedIndex.clear();
                loadFile();
                layoutProgress.setVisibility(View.GONE);
                back();
            }
        });
    }

    private void createNewFolder(String folderName){

        fileManager.createNewFolder(rootDirectory, folderName, new FileManager.OnFolderCreating() {
            @Override
            public void onCreated(File file) {
                fileLists.add(0,new FolderModel(file,0));
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFail(String error) {

            }
        });
    }
}