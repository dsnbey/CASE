package com.example.acase.UC;

import com.example.acase.Model.RequestModelPinecone;
import com.example.acase.Model.ResponseModelPinecone;

import io.reactivex.Single;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Url;

public interface PineconeApiService {
    @Headers({
            "accept: application/json",
            "content-Type: application/json"
    })
    @POST
    Single<ResponseModelPinecone> sendData(@Url String url, @Header("Api-Key") String apiKey, @Body RequestModelPinecone requestModel);
}
