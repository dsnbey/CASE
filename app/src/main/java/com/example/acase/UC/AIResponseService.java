package com.example.acase.UC;

import static android.content.ContentValues.TAG;

import android.util.Log;

import com.example.acase.Common;
import com.example.acase.Model.Chat;
import com.example.acase.Model.ChatCompletionResponse;
import com.example.acase.Model.ChatMessage;
import com.google.firebase.database.DatabaseReference;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


/**
 * Controls the response of openai Api's
 */
public class AIResponseService {

    private OpenAiApiClient openAiApiClient;
    private PineconeService pineconeService;

    public AIResponseService() {
        openAiApiClient = OpenAiApiClient.getInstance();
        pineconeService = PineconeService.getPineconeService();
    }

    /**
     * Retrieves response asynchronously. Sends the message to Pinecone + Firebase. Uses OpenAiApiClient.
     */
    public boolean getResponse(Chat chat, List<ChatMessage> memory) {
        final boolean[] status = {false};
        Single<ChatCompletionResponse> response =  openAiApiClient.getChatCompletions(memory, chat.getMessage());

        response.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .timeout(60000, TimeUnit.MILLISECONDS)
                .subscribe(new SingleObserver<ChatCompletionResponse>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(ChatCompletionResponse chatCompletionResponse) {
                            String responseMessage = chatCompletionResponse.getChoices().get(0).getMessage().getContent();
                            Chat responseChat = new Chat(1,responseMessage, false , false);
                            responseChat.setDeepMemory(false);
                            DatabaseReference ref = Common.ref.child("Conversations").child(Common.id).push();
                            ref.setValue(responseChat);
                        try {
                            pineconeService.sendChatToPinecone(responseChat, ref.getKey());
                        } catch (IOException e) {
                            Log.d(TAG, "onSuccess: sendChatToPinecone is problematic " + e.getMessage());
                        }
                        status[0] = true;
                        Log.d(TAG, "onSuccess: ");

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "onError: here " + e.getMessage());
                    }
                });

        return status[0];
    }
}
