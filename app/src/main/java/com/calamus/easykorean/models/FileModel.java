package com.calamus.easykorean.models;

import java.io.File;

public class FileModel {
    File file;
    int selectedState;

    public FileModel(File file, int selectedState) {
        this.file = file;
        this.selectedState = selectedState;
    }

    public File getFile() {
        return file;
    }

    public int getSelectedState() {
        return selectedState;
    }

    public void setSelectedState(int selectedState) {
        this.selectedState = selectedState;
    }
}
