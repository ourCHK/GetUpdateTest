package com.chk.getupdatetest.services;

import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

/**
 * Created by CHK on 18-7-18.
 */
public interface ApiService {

    @Headers("Accept-Encoding:identity")
    @GET("FileUploadAndDownloadTest/GetPatchServlet")
    Call<ResponseBody> getPatch(@Query("version") int version);

}
