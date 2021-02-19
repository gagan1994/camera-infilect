package com.infilect.taskcamera.network;

import com.google.gson.JsonElement;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface UploadInterface {

    @POST("o")
    Call<ResponseBody> uploadFile(@Query("uploadType") String uploadType,
                                  @Query("name") String name,
                                  @Body RequestBody files);

}