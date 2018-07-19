package com.chk.getupdatetest.services;

import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;

/**
 * Created by CHK on 18-7-13.
 */
public class UploadProgressRequestBody extends RequestBody{


    RequestBody mRequestBody;
    BufferedSink mBufferedSink;

    long uploadedByte;

    OnProgressCallback mOnProgressCallback;

    public UploadProgressRequestBody(RequestBody requestBody, OnProgressCallback progressCallback) {
        this.mRequestBody = requestBody;
        mOnProgressCallback = progressCallback;
    }

    @Override
    public long contentLength() throws IOException {
        return mRequestBody.contentLength();
    }

    @Nullable
    @Override
    public MediaType contentType() {
        return mRequestBody.contentType();
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        if (mBufferedSink == null)
            mBufferedSink = Okio.buffer(wrapSink(sink));
        mRequestBody.writeTo(mBufferedSink);
        mBufferedSink.flush();
    }



    private Sink wrapSink(Sink sink) {

        return new ForwardingSink(sink) {
            @Override
            public void write(Buffer source, long byteCount) throws IOException {
                super.write(source, byteCount);
                if (!(contentLength() <= 0)) {
                    uploadedByte += byteCount;
                    mOnProgressCallback.onProgress(uploadedByte/((float)contentLength()));
                    Log.i("ProgressRequestBody","progress:"+(uploadedByte/((float)contentLength())));
                }
            }
        };
    }

}
