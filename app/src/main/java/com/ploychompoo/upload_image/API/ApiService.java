package com.ploychompoo.upload_image.API;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiService {
    @Multipart
    @POST("upload")
    Call<String> upload(@Part MultipartBody.Part file, @Part("description") RequestBody description);
}
