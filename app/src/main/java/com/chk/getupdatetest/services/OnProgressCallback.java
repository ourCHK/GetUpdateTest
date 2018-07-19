package com.chk.getupdatetest.services;

/**
 * Created by CHK on 18-7-13.
 */
public interface OnProgressCallback {

    void onStart();
    void onProgress(float progress);
    void onFinish();
}
