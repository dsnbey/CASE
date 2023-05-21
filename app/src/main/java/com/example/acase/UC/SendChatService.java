package com.example.acase.UC;

import static android.content.ContentValues.TAG;

import android.util.Log;

import com.example.acase.Common;
import com.example.acase.Model.Chat;
import com.example.acase.Model.ChatMessage;
import com.google.firebase.database.DatabaseReference;

import java.io.IOException;
import java.util.List;

public class SendChatService {

    private PineconeService pineconeService;
    private AIResponseService aiResponseService;

    private final int MAX_CHARACTERS_ALLOWED = 15000;

    public SendChatService() {
        aiResponseService = new AIResponseService();
        pineconeService = PineconeService.getPineconeService();
    }

    public boolean sendChat(Chat chat) {
        boolean validation = validateChat(chat);
        if (validation) {
            Log.d(TAG, "sendChat: validated");
            String ref = sendChatToFirebase(chat);
            List<ChatMessage> memory = pineconeService.fetchMemory(chat);
            boolean status = aiResponseService.getResponse(chat, memory);
            if (status) {
                try {
                    pineconeService.sendChatToPinecone(chat, ref);
                } catch (IOException e) {
                    Log.d(TAG, "sendChatService: sendChatToPinecone is problematic " + e.getMessage());
                }
                Log.d(TAG, "sendChat: response received");
                return true;
            }
        }
        return false;
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

