package com.example.acase.UC;

import static android.content.ContentValues.TAG;

import android.util.Log;

import com.example.acase.Common;

import com.example.acase.Model.ChatCompletionRequest;
import com.example.acase.Model.ChatCompletionResponse;
import com.example.acase.Model.ChatMessage;
import com.example.acase.Model.EmbeddingData;
import com.example.acase.Model.EmbeddingRequest;
import com.example.acase.Model.TranscriptionResponse;
import com.example.acase.UI.ChatActivity;


import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;

import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Responsible for interactions with openai api in general. Handles concurrent operations.
 */
public class OpenAiApiClient {
    private static final String BASE_URL = "https://api.openai.com/";

    private static final int TIMEOUT_SECONDS = 60;

    private OpenAiApiService apiService;
    private ChatActivity ref;


    private static volatile OpenAiApiClient instance;


    /**
     * Singleton constructor.
     * @return
     */
    public static synchronized OpenAiApiClient getInstance() {
        if (instance == null) {
            instance = new OpenAiApiClient();
        }
        return instance;
    }

    public static synchronized OpenAiApiClient getInstancePassRef(ChatActivity ref) {
        if (instance == null) {
            instance = new OpenAiApiClient(ref);
        }
        return instance;
    }

    private OpenAiApiClient(ChatActivity ref) {
        this.ref = ref;
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


    /**
     *
     * Talks to the Rest Api (OpenAiApiService).
     */
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

    /**
     * Vectorizes content asynchronously. Uses Embeddings API.
     */
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

    /**
     * Uses Whisper-1 to convert Speech to text. NOT END-USER TESTED: See ChatActivity
     * @param filePath
     * @return
     */
    public CompletableFuture<Void> transcribeAudio(String filePath) {
        // Create the file part
        File audioFile = new File(filePath);
        RequestBody fileRequestBody = RequestBody.create(MediaType.parse("multipart/form-data"), audioFile);
        MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", audioFile.getName(), fileRequestBody);

        // Create the model part
        RequestBody modelRequestBody = RequestBody.create(MediaType.parse("multipart/form-data"), "whisper-1");

        CompletableFuture<Void> future = new CompletableFuture<>();

        // Make the API call asynchronously
        apiService.transcribeAudio("Bearer " + Common.openaiApiKey, filePart, modelRequestBody)
                .enqueue(new Callback<TranscriptionResponse>() {
                    @Override
                    public void onResponse(Call<TranscriptionResponse> call, Response<TranscriptionResponse> response) {
                        if (response.isSuccessful()) {
                            TranscriptionResponse transcription = response.body();
                            if (transcription != null) {
                                Log.d(TAG, "onResponse: " + transcription.getText());
                                ref.getB().edtChat.setText(transcription.getText());
                            } else {
                                Log.d(TAG, "onResponse: transcription is null");
                            }
                        } else {
                            Log.d(TAG, "Error: " + response.code() + " " + response.message());
                        }
                        future.complete(null); // Complete the CompletableFuture
                    }

                    @Override
                    public void onFailure(Call<TranscriptionResponse> call, Throwable t) {
                        t.printStackTrace();
                        future.completeExceptionally(t); // Complete the CompletableFuture exceptionally
                    }
                });

        return future;
    }
}
