package com.example.acase.UC;

import com.example.acase.Model.ChatCompletionRequest;
import com.example.acase.Model.ChatCompletionResponse;
import com.example.acase.Model.EmbeddingRequest;
import com.example.acase.Model.EmbeddingResponse;
import com.example.acase.Model.TranscriptionResponse;

import io.reactivex.Single;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/**
 * Rest api for OpenAi endpoints
 */
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

    @Multipart
    @POST("v1/audio/transcriptions")
    Call<TranscriptionResponse> transcribeAudio(
            @Header("Authorization") String token,
            @Part MultipartBody.Part file,
            @Part("model") RequestBody model
    );

}
