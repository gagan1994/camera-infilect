package com.infilect.taskcamera.network;

import okhttp3.ResponseBody;
import retrofit2.Call;

public class FileUploader {
    public static Call<ResponseBody> uploadFile(String fileName, ProgressRequestBody file){
        return ApiClient.getClient().create(UploadInterface.class)
                .uploadFile("media",fileName,file);
    }
}
