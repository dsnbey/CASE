package com.example.acase.UC;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.acase.Common;
import com.example.acase.Model.Chat;
import com.example.acase.Model.ChatMessage;
import com.example.acase.Model.Match;
import com.example.acase.Model.Metadata;
import com.example.acase.Model.RequestModelPinecone;
import com.example.acase.Model.ResponseModelPinecone;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;


public class PineconeService {

    private static volatile PineconeService instance;
    private OpenAiApiClient openAiApiClient;

    private PineconeApiService pineconeApiService;


    public static synchronized PineconeService getPineconeService() {
        if (instance == null) {
            instance = new PineconeService();
        }
        return instance;
    }

    private PineconeService() {

        openAiApiClient = OpenAiApiClient.getInstance();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Common.pineconeBaseUrlQuery)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        pineconeApiService = retrofit.create(PineconeApiService.class);
    }

    @SuppressLint("CheckResult")
    public Single<List<ChatMessage>> fetchMemory(Chat chat) {
        return openAiApiClient.vectorizeContent(chat.getMessage())
                .subscribeOn(Schedulers.io())
                .flatMap(floats -> {
                    RequestModelPinecone requestModel = new RequestModelPinecone();
                    requestModel.setIncludeValues(true);
                    requestModel.setIncludeMetadata(true);
                    requestModel.setTopK(10);
                    requestModel.setVector(floats);

                    return pineconeApiService.sendData(Common.pineconeBaseUrlQuery, Common.pineconeApiKey, requestModel)
                            .subscribeOn(Schedulers.io());
                })
                .flatMap(response -> {
                    List<Match> matches = response.getMatches();

                    List<String> refList = new ArrayList<>();
                    for (Match match : matches) {
                        refList.add(match.getId());
                    }

                    List<ChatMessage> messageList = new ArrayList<>();

                    List<Single<Chat>> singles = refList.stream()
                            .map(ref -> Single.create(emitter -> {
                                Log.d(TAG, "fetchMemory: " + ref);
                                Common.ref.child("Conversations").child(Common.id).child(ref).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        Chat chatFB = snapshot.getValue(Chat.class);
                                        Log.d(TAG, "onDataChange: " + chatFB.getMessage());
                                        emitter.onSuccess(chatFB);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        emitter.onError(error.toException());
                                    }
                                });
                            }).cast(Chat.class))
                            .collect(Collectors.toList());

                    return Single.zip(singles, objects -> {
                        for (Object object : objects) {
                            try {
                                Chat chatFB = (Chat) object;
                                String role = (chatFB.getSender() == 0) ? "user" : "assistant";
                                messageList.add(new ChatMessage(role, chatFB.getMessage()));
                            } catch (Exception e) {
                                Log.e(TAG, "Error retrieving chat: " + e.getMessage());
                            }
                        }
                        return messageList;
                    });
                })
                .observeOn(AndroidSchedulers.mainThread());
    }


    public void sendChatToPinecone(Chat chat, String ref) throws IOException {

        openAiApiClient.vectorizeContent(chat.getMessage())
                .subscribeOn(Schedulers.io())
                .flatMap(embeddingArray -> {



                    OkHttpClient client = new OkHttpClient();
                    MediaType mediaType = MediaType.parse("application/json");

                    JSONObject jsonBody = new JSONObject();
                    JSONArray vectorsArray = new JSONArray();
                    JSONObject vectorObject = new JSONObject();
                    vectorObject.put("id", ref);
                    vectorObject.put("values", new JSONArray(embeddingArray));
                    JSONObject metadataObject = new JSONObject();
                    metadataObject.put("sender", chat.getSender());
                    vectorObject.put("metadata", metadataObject);
                    vectorsArray.put(vectorObject);
                    jsonBody.put("vectors", vectorsArray);

                    RequestBody requestBody = RequestBody.create(mediaType, jsonBody.toString());

                    Request request = new Request.Builder()
                            .url("https://case-1bfe2b5.svc.asia-northeast1-gcp.pinecone.io/vectors/upsert")
                            .addHeader("accept", "application/json")
                            .addHeader("content-type", "application/json")
                            .addHeader("Api-Key", Common.pineconeApiKey)
                            .post(requestBody)
                            .build();

                    return Single.create(emitter -> {
                        client.newCall(request).enqueue(new Callback() {


                            @Override
                            public void onFailure(Call call, IOException e) {
                                emitter.onError(e);
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {

                                emitter.onSuccess(response);
                            }
                        });
                    });
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {

                }, throwable -> {
                    String errorMessage = throwable.getMessage();
                    if (errorMessage == null) {
                        errorMessage = "Unknown error occurred";
                    }

                });


    }
}
