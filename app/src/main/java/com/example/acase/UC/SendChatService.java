package com.example.acase.UC;

import static android.content.ContentValues.TAG;

import android.util.Log;

import com.example.acase.Common;
import com.example.acase.Model.Chat;
import com.example.acase.Model.ChatMessage;
import com.google.firebase.database.DatabaseReference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;

public class SendChatService {

    private PineconeService pineconeService;
    private AIResponseService aiResponseService;

    private final int MAX_CHARACTERS_ALLOWED = 15000;

    public SendChatService() {
        aiResponseService = new AIResponseService();
        pineconeService = PineconeService.getPineconeService();
    }

    public boolean sendChat(Chat chat) {
        AtomicBoolean stats = new AtomicBoolean(false);
        boolean validation = validateChat(chat);
        if (validation) {
            Log.d(TAG, "sendChat: validated");
            String ref = sendChatToFirebase(chat);
            pineconeService.fetchMemory(chat)
                    .subscribe(new SingleObserver<List<ChatMessage>>() {
                                                            @Override
                                                            public void onSubscribe(Disposable d) {

                                                            }

                                                            @Override
                                                            public void onSuccess(List<ChatMessage> messageList) {
                                                                if (!messageList.isEmpty()) {
                                                                    for (ChatMessage mes: messageList) {
                                                                        Log.d(TAG, "sendChat: " + mes.getContent());
                                                                    }
                                                                }
                                                                boolean status = aiResponseService.getResponse(chat, messageList);
                                                                if (status) {
                                                                    try {
                                                                        pineconeService.sendChatToPinecone(chat, ref);
                                                                        stats.set(true);
                                                                    } catch (IOException e) {
                                                                        Log.d(TAG, "sendChatService: sendChatToPinecone is problematic " + e.getMessage());
                                                                    }
                                                                    Log.d(TAG, "sendChat: response received");

                                                                }
                                                            }

                                                            @Override
                                                            public void onError(Throwable e) {
                                                                Log.d(TAG, "onError: ");
                                                                boolean status = aiResponseService.getResponse(chat, new ArrayList<>());
                                                                if (status) {
                                                                    try {
                                                                        pineconeService.sendChatToPinecone(chat, ref);
                                                                        stats.set(true);
                                                                    } catch (IOException ex) {
                                                                        Log.d(TAG, "sendChatService: sendChatToPinecone is problematic " + ex.getMessage());
                                                                    }
                                                                    Log.d(TAG, "sendChat: response received");

                                                                }
                                                            }
                                                        }

            );
        }
        return stats.get();
    }




    private boolean validateChat(Chat chat) {
        return chat.getMessage().length() < MAX_CHARACTERS_ALLOWED;
    }

    private String sendChatToFirebase(Chat chat) {
        DatabaseReference ref = Common.ref.child("Conversations").child(Common.id).push();
        ref.setValue(chat);
        return ref.getKey();
    }

}

