package com.chk.getupdatetest;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.chk.getupdatetest.services.ApiService;
import com.chk.getupdatetest.services.DownloadProgressResponseBody;
import com.chk.getupdatetest.services.LoggingInterceptor;
import com.chk.getupdatetest.services.OnProgressCallback;
import com.tencent.tinker.lib.tinker.TinkerInstaller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {

    String ip_self = "10.0.2.2";
    String ip_my_phone = "10.42.0.1";
    String SELECTED_IP = ip_self;

    @BindView(R.id.applyNewCode)
    Button applyNewCode;

    @BindView(R.id.showResult)
    Button showResult;

    @BindView(R.id.progressId)
    ProgressBar mProgress;

    private OnProgressCallback onProgressCallback = new OnProgressCallback() {
        @Override
        public void onStart() {

        }

        @Override
        public void onProgress(float progress) {
            mProgress.setProgress((int)(progress*100));
            Log.i("MainActivity","progress:"+progress);
        }

        @Override
        public void onFinish() {
            Toast.makeText(MainActivity.this, "下载完成", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
    }

    @OnClick({R.id.applyNewCode,R.id.showResult})
    void onClick(View view) {
        switch (view.getId()) {
            case R.id.applyNewCode:
                applyNewCode();
                break;
            case R.id.showResult:
                showResult();
                break;
        }
    }

    void applyNewCode() {
//        String patchPatch = Environment.getExternalStorageDirectory().getAbsolutePath()+"/patch_signed_7zip.apk";
//        TinkerInstaller.onReceiveUpgradePatch(this, patchPatch);
        executeDownload(1);
    }

    void showResult() {
        Toast.makeText(this, "this is the code before the patch", Toast.LENGTH_SHORT).show();
    }

    void executeDownload(int version) {

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new LoggingInterceptor())
                .addInterceptor(new Interceptor() {
                    @Override
                    public okhttp3.Response intercept(Chain chain) throws IOException {
                        okhttp3.Response response = chain.proceed(chain.request());
                        DownloadProgressResponseBody downloadProgressResponseBody = new DownloadProgressResponseBody(response.body(),onProgressCallback);

                        return response.newBuilder().body(downloadProgressResponseBody).build();
                    }
                })
                .build();

        new Retrofit.Builder()
                .baseUrl("http://"+ SELECTED_IP +":8080/")
                .client(client)
                .build()
                .create(ApiService.class)
                .getPatch(version)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        try {
                            InputStream is = response.body().byteStream();
                            FileOutputStream fos = new FileOutputStream(new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/DownloadFile"));
                            byte[] buffers = new byte[2048];
                            while (-1 != is.read(buffers)) {
                                fos.write(buffers);
                            }
                            fos.flush();
                            fos.close();
                            is.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.i("MainActivity","IO问题");
                        }
                        Log.i("MainActivity","write end");
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        t.printStackTrace();
                    }
                });
    }
}
