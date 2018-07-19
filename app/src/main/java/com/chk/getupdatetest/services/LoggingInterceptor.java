package com.chk.getupdatetest.services;

import android.util.Log;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Connection;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.http.Body;

/**
 * Created by CHK on 18-7-12.
 */
public class LoggingInterceptor implements Interceptor{


    private String TAG = LoggingInterceptor.class.getSimpleName();

    public int NONE = 1<<0;
    public int HEADERS = 1<<1;
    public int BODY = 1<<2;
    public int level;

    public void setLevel(int level) {
        this.level = level;
        this.level = BODY;
    }


    @Override
    public Response intercept(Chain chain) throws IOException {


        Request request = chain.request();
        RequestBody requestBody = request.body();

        if (level == NONE)
            return chain.proceed(request);

        boolean hasRequestBody = request.body() != null;

        Connection connection = chain.connection();
        Protocol protocol = connection != null ? connection.protocol() : Protocol.HTTP_1_1;
        String requestMessage = "--> "+request.method()+" "+request.url()+" "+protocol;
        if (hasRequestBody) {
            requestMessage += " ("+requestBody.contentLength()+"-byte body)";
            Log.i(TAG,requestMessage);
            Log.i(TAG,"content_type:"+requestBody.contentType());
        }

        Headers headers = request.headers();
        for (int i=0; i<headers.size(); i++) {
            String name = headers.name(i);
            if (!("Content-Type").equalsIgnoreCase(name) && !("Content-length").equalsIgnoreCase(name)) {
                Log.i(TAG,name+":"+headers.value(i));
            }
        }
        Log.i(TAG,"-->RequestEnd");

        long startNs = System.nanoTime();
        Response response = chain.proceed(request);
        long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);

        ResponseBody responseBody = response.body();
        long contentLength = responseBody.contentLength();
        String bodySize = contentLength != -1 ? contentLength+"-byte" : "unknown-length";
        Log.i(TAG,"<-- "+response.code()+" "+response.message()+" " + response.request().url()+" " +
                "("+tookMs+"ms, contentLength:"+contentLength);
        Headers headersResponse = response.headers();
        for (int i=0; i<headers.size(); i++) {
            if (i==0)
                Log.i(TAG,"ResponseHeaders:");
            String name = headers.name(i);
            Log.i(TAG,name+":"+headers.value(i));
        }
        Log.i(TAG,"<--ResponseEnd");

        return response;
    }



}
