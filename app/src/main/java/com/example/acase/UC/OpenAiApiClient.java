package com.example.acase.UC;

import static android.content.ContentValues.TAG;

import android.media.MediaRecorder;
import android.util.Log;

import com.example.acase.Common;

import com.example.acase.Model.ChatCompletionRequest;
import com.example.acase.Model.ChatCompletionResponse;
import com.example.acase.Model.ChatMessage;
import com.example.acase.Model.EmbeddingData;
import com.example.acase.Model.EmbeddingRequest;
import com.example.acase.Model.TranscriptionRequest;


import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableSingleObserver;

import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class OpenAiApiClient {
    private static final String BASE_URL = "https://api.openai.com/";

    private static final int TIMEOUT_SECONDS = 60;

    private OpenAiApiService apiService;


    private static volatile OpenAiApiClient instance;


    public static synchronized OpenAiApiClient getInstance() {
        if (instance == null) {
            instance = new OpenAiApiClient();
        }
        return instance;
    }

    private OpenAiApiClient() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createAsync())
                .build();

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .build();

        retrofit = retrofit.newBuilder().client(client).build();

        apiService = retrofit.create(OpenAiApiService.class);
    }

    // extract model and apikey to common. refactor the usage.
    public Single<ChatCompletionResponse> getChatCompletions(List<ChatMessage> recentConversations, String userMessage) {
        ChatCompletionRequest request = new ChatCompletionRequest();
        request.setModel(Common.MODEL);

        // Combine recent conversations and user message
        List<ChatMessage> messages = new ArrayList<>(recentConversations);
        ChatMessage userMessageObj = new ChatMessage();
        userMessageObj.setRole("user");
        userMessageObj.setContent(userMessage);
        messages.add(userMessageObj);
        request.setMessages(messages);
        Log.d(TAG, "getChatCompletions: " + request.getMessages().get(request.getMessages().size() -1).getContent());

        String authorizationHeader = "Bearer " + Common.openaiApiKey;

        return apiService.getChatCompletions(authorizationHeader, request);
    }

    public Single<Float[]> vectorizeContent(String content) {
        EmbeddingRequest request = new EmbeddingRequest(content, Common.EMBEDDING_MODEL_NAME);

        return apiService.getEmbeddings("Bearer " + Common.openaiApiKey, request)
                .timeout(60000, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(embeddingResponse -> {
                    if (embeddingResponse != null && embeddingResponse.getData() != null && !embeddingResponse.getData().isEmpty()) {
                        EmbeddingData embeddingData = embeddingResponse.getData().get(0);
                        List<Float> embedding = embeddingData.getEmbedding();
                        Float[] embeddingArray = new Float[embedding.size()];
                        embedding.toArray(embeddingArray);
                        Log.d(TAG, "onSuccess: vectorize content");
                        return embeddingArray;
                    } else {
                        throw new RuntimeException("Invalid response or empty data");
                    }
                })
                .doOnError(throwable -> Log.d(TAG, "onError: ", throwable));
    }

    public Single<String> transcribeAudio(String apiKey, String filePath, String model) {


        TranscriptionRequest request = new TranscriptionRequest(model, filePath);
        Log.d(TAG, "transcribeAudio: " + filePath);

        return apiService.transcribeAudio("Bearer " + apiKey, "multipart/form-data" ,request)
                .map(responseBody -> {
                    // Extract the required data from the response body
                    String responseString = responseBody.string();
                    Log.d(TAG, "transcribeAudio: " + responseString);
                    // Parse the JSON response and extract the "text" field
                    // Here, you can use a JSON parsing library like Gson or Jackson
                    // For simplicity, let's assume the response is in the expected format
                    return responseString;
                });

    }
}
