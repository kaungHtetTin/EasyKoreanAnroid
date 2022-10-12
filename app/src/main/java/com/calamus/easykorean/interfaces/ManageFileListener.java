package com.calamus.easykorean.interfaces;

public interface ManageFileListener {
    void onStart(String msg);
    void onFail(String msg);
    void onSuccess(String msg);
    void onCompleted();
}
