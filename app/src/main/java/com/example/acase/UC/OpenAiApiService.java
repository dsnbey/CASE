package com.example.acase.UC;

import com.example.acase.Model.ChatCompletionRequest;
import com.example.acase.Model.ChatCompletionResponse;
import com.example.acase.Model.EmbeddingRequest;
import com.example.acase.Model.EmbeddingResponse;
import com.example.acase.Model.TranscriptionRequest;

import java.io.File;

import io.reactivex.Single;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface OpenAiApiService {
    @Headers({
            "Content-Type: application/json"
    })
    @POST("v1/chat/completions")
    Single<ChatCompletionResponse> getChatCompletions(
            @Header("Authorization") String authorization,
            @Body ChatCompletionRequest request
    );

    @Headers({
            "Content-Type: application/json"
    })
    @POST("v1/embeddings")
    Single<EmbeddingResponse> getEmbeddings(
            @Header("Authorization") String authorization,
            @Body EmbeddingRequest request);

    @Headers("Content-Type: multipart/form-data")
    @POST("v1/audio/transcriptions")
    Single<ResponseBody> transcribeAudio(
            @Header("Authorization") String authorization,
            @Header("Content-Type") String contentType,
            @Body TranscriptionRequest request
    );
}
