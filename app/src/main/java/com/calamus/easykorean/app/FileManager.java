package com.calamus.easykorean.app;


import static android.media.MediaMetadataRetriever.METADATA_KEY_DURATION;

import android.app.Activity;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.calamus.easykorean.models.FileModel;
import com.calamus.easykorean.models.FolderModel;
import com.calamus.easykorean.models.SavedVideoModel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.Executor;

public class FileManager extends Thread {

    Activity c;
    Executor postExecutor;


    public FileManager(Activity c){
        this.c=c;
        postExecutor= ContextCompat.getMainExecutor(c);
    }

    public void move(ArrayList<FileModel> fileLists,ArrayList<Integer> selectedIndex,String destPath,FileMovingListener mListener){
        new Thread(new Runnable() {
            @Override
            public void run() {
                int fileCount=selectedIndex.size();
                int progress;
                int taskCount;

                for(int i=0;i<selectedIndex.size();i++){
                    taskCount=i+1;
                    File sourceFile=fileLists.get(selectedIndex.get(i)).getFile();
                    if(!sourceFile.exists()) continue;
                    fileMover(sourceFile,destPath,mListener);
                    progress=(taskCount*100)/fileCount;
                    int finalProgress = progress;
                    postExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            mListener.onProgress(finalProgress);
                        }
                    });
                }

                postExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        mListener.onCompleted();
                    }
                });
            }
        }).start();
    }

    private void fileMover(File sourceFile,String destPath,FileMovingListener mListener){
        String filename=sourceFile.getName();
        String sourcePath=sourceFile.getAbsolutePath();
        if(sourceFile.isDirectory()){
            if(sourcePath.equals(destPath)) return;
            File file=new File(destPath,filename);
            if(!file.exists()){
                if(file.mkdirs()){
                    String currentDestPath=file.getAbsolutePath();
                    File [] files=sourceFile.listFiles();
                    if(files!=null){
                        if(files.length>0){
                            for(File f :files){
                                fileMover(f,currentDestPath,mListener);
                            }
                        }else{
                            if(sourceFile.delete())return;
                        }
                    }else{
                        if(sourceFile.delete())return;
                    }
                    sourceFile.delete();
                }
            }
        }else{
            try {

                OutputStream os=new FileOutputStream(destPath+"/"+filename);
                byte[] buffer=new byte[1024];
                int length;
                InputStream is=new FileInputStream(sourceFile);
                while ((length=is.read(buffer))>0){
                    os.write(buffer,0,length);
                }
                is.close();
                os.flush();
                os.close();

                sourceFile.delete();

            }catch (Exception e){
                postExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        mListener.onFail(filename);
                    }
                });
                Log.e("FileMoving Err ",e.toString());
            }
        }
    }

    public void searchVideo(String searchingFilename, File root,OnFileSearching onFileSearching){
        new Thread(new Runnable() {
            @Override
            public void run() {
                onFileSearching.onSearching();
                searcher(searchingFilename,root,onFileSearching);

            }
        }).start();
    }

    private void searcher(String searchingFilename, File root,OnFileSearching onFileSearching){
        File[] files =root.listFiles();
        if(files==null){
            onFileSearching.onFailure();
            return;
        }
        for(File file : files){

            if(file.isDirectory()){
                Log.e("folderDir Search ",file.getName());
                searcher(searchingFilename,file,onFileSearching);
            }else{
                Log.e("fileName search ",file.getName());
                if(file.getName().equals(searchingFilename)){
                    onFileSearching.onSuccess(file);
                    return;
                }
            }
        }
    }

    public interface  OnFileSearching{
        void onSearching();
        void onSuccess(File file);
        void onFailure();
    }

    public void deleteFiles(ArrayList<FileModel> fileLists,ArrayList<Integer> selectedIndex,FileDeletingListener mListener){
        new Thread(new Runnable() {
            @Override
            public void run() {
                int fileCount=selectedIndex.size();
                int progress;
                int taskCount;
                for(int i=0;i<fileCount;i++) {
                    taskCount=i+1;
                    File sourceFile = fileLists.get(selectedIndex.get(i)).getFile();
                    deleteFile(sourceFile);

                    progress=(taskCount/fileCount)*100;
                    int finalProgress = progress;
                    postExecutor.execute(new Runnable() {
                        @Override
                        public void run() {

                            mListener.onProgress(finalProgress);
                        }
                    });
                }
                postExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        mListener.onCompleted();
                    }
                });
            }
        }).start();
    }

    private void deleteFile(File sourceFile){

        if(sourceFile.exists()) {
            File [] files=sourceFile.listFiles();
            if(files!=null){
                if(files.length>0){
                    for(File file :files){
                        deleteFile(file);
                    }
                }else{
                    if(sourceFile.delete())return;
                }
            }else{
                if(sourceFile.delete())return;
            }
            sourceFile.delete();
        }
    }

    public void loadDirectory(File root,OnFileLoading onFileLoading){
        new Thread(new Runnable() {
            @Override
            public void run() {
                File[] files =root.listFiles();
                ArrayList<FileModel> dirLists=new ArrayList<>();
                if(files==null){
                    onFileLoading.onLoaded(dirLists);
                    return;
                }
                for(File file : files){
                    if(file.isDirectory()){
                        Log.e("folderDir ",file.getName());
                        dirLists.add(new FolderModel(file,0));
                    }
                }
                postExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        onFileLoading.onLoaded(dirLists);
                    }
                });
            }
        }).start();
    }

    public void loadFiles(File rootDirectory,OnFileLoading onFileLoading){
        new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<FileModel> fileLists=new ArrayList<>();
                File[] files =rootDirectory.listFiles();

                if(files==null){
                    onFileLoading.onLoaded(fileLists);
                    return;
                }
                Arrays.sort(files, new Comparator<File>() {
                    public int compare(File f1, File f2) {
                        return Long.compare(f1.lastModified(), f2.lastModified());
                    }
                });

                for (File file : files) {
                    try {
                        Uri uri = Uri.fromFile(file);
                        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                        retriever.setDataSource(c, uri);

                        String duration = retriever.extractMetadata(METADATA_KEY_DURATION);
                        if (duration != null) {
                            fileLists.add(new SavedVideoModel(file, 0, uri, file.getName(), Integer.parseInt(duration), 400, null));
                        }
                    } catch (Exception e) {
                        fileLists.add(0, new FolderModel(file, 0));
                    }
                }
                postExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        onFileLoading.onLoaded(fileLists);
                    }
                });
            }
        }).start();

    }

    public void createNewFolder(File rootDirectory,String folderName,OnFolderCreating mListener){
        File file=new File(rootDirectory.getAbsolutePath(),folderName);
        if(!file.exists()){
            if(file.mkdirs()){
                mListener.onCreated(file);
            }else{
                mListener.onFail("Folder creating fail");
            }
        }else{
            mListener.onFail("Folder has already exited");
        }
    }

    // Interface

    public interface OnFolderCreating{
        void onCreated(File file);
        void onFail(String error);
    }

    public interface  OnFileLoading{
        void onLoaded(ArrayList<FileModel> files);
    }

    public interface FileMovingListener {
        void onFail(String msg);
        void onProgress(int progress);
        void onCompleted();
    }

    public interface FileDeletingListener{
        void onProgress(int progress);
        void onCompleted();
    }

}

