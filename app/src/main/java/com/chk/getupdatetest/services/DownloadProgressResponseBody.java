package com.chk.getupdatetest.services;

import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

/**
 * Created by CHK on 18-7-18.
 */
public class DownloadProgressResponseBody extends ResponseBody {

    ResponseBody responseBody;
    OnProgressCallback onProgressCallback;

    long downloadedBytes;
    BufferedSource bufferedSource;

    public DownloadProgressResponseBody(ResponseBody responseBody,OnProgressCallback onProgressCallback) {
        this.responseBody = responseBody;
        this.onProgressCallback = onProgressCallback;
    }

    @Nullable
    @Override
    public MediaType contentType() {
        return responseBody.contentType();
    }

    @Override
    public long contentLength() {
        return responseBody.contentLength();
    }

    @Override
    public BufferedSource source() {
        if (bufferedSource == null) {
            bufferedSource = Okio.buffer(wrapSource(responseBody.source()));
        }
        return bufferedSource;
    }

    private ForwardingSource wrapSource(Source source) {
        return new ForwardingSource(source) {
            @Override
            public long read(Buffer sink, long byteCount) throws IOException {

                long bytesRead = super.read(sink,byteCount);
                downloadedBytes += (bytesRead == -1 ? 0 : bytesRead);
                Log.i("DownloadingProgress:","downloadedBytes:"+downloadedBytes+" length"+contentLength()+" byteread:"+bytesRead);
                onProgressCallback.onProgress(downloadedBytes/((float)contentLength()));
                if (bytesRead == -1) {
                    Log.i("DownloadingProgress","download end");
                    onProgressCallback.onFinish();
                }
                return bytesRead;
            }
        };
    }



}
