package com.example.acase.UC;

import static android.content.ContentValues.TAG;

import android.util.Log;

import com.example.acase.Common;
import com.example.acase.Model.Chat;
import com.example.acase.Model.ChatMessage;


import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import java.util.Arrays;
import java.util.List;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class PineconeService {



    private static volatile PineconeService instance;
    private OpenAiApiClient openAiApiClient;


    public static synchronized PineconeService getPineconeService() {
        if (instance == null) {
            instance = new PineconeService();
        }
        return instance;
    }

    private PineconeService() {
        openAiApiClient = OpenAiApiClient.getInstance();
    }

    public List<ChatMessage> fetchMemory(Chat chat) {
        return new ArrayList<>();
    }

    public void sendChatToPinecone(Chat chat, String ref) throws IOException {

        openAiApiClient.vectorizeContent(chat.getMessage())
                .subscribeOn(Schedulers.io())
                .flatMap(embeddingArray -> {
                    Log.d(TAG, "sendChatToPinecone: PCS" + Arrays.toString(embeddingArray));


                    OkHttpClient client = new OkHttpClient();
                    MediaType mediaType = MediaType.parse("application/json");

                    JSONObject jsonBody = new JSONObject();
                    JSONArray vectorsArray = new JSONArray();
                    JSONObject vectorObject = new JSONObject();
                    vectorObject.put("id", ref);
                    vectorObject.put("values", new JSONArray(embeddingArray));
                    JSONObject metadataObject = new JSONObject();
                    metadataObject.put("newKey", "New Value");
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
                                Log.d(TAG, "onResponse: ");
                                emitter.onSuccess(response);
                            }
                        });
                    });
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    Log.d(TAG, "sendChatToPinecone: response received yey: " + response.toString());
                    // Handle the response here
                }, throwable -> {
                    String errorMessage = throwable.getMessage();
                    if (errorMessage == null) {
                        errorMessage = "Unknown error occurred";
                    }
                    Log.d(TAG, "sendChatToPinecone: error" + errorMessage);
                });


    }
}
