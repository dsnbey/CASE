package com.example.acase.UC;

import com.example.acase.Model.ChatCompletionRequest;
import com.example.acase.Model.ChatCompletionResponse;
import com.example.acase.Model.EmbeddingRequest;
import com.example.acase.Model.EmbeddingResponse;

import io.reactivex.Single;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

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
}
