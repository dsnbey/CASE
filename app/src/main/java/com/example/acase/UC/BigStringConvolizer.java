package com.example.acase.UC;

import static android.content.ContentValues.TAG;

import android.util.Log;

import com.example.acase.Common;
import com.example.acase.Model.Chat;
import com.google.firebase.database.DatabaseReference;

import java.io.IOException;

public class BigStringConvolizer {

    private final int CONVOLIZE_CONSTANT = 1000;
    private static volatile BigStringConvolizer instance;
    private SendChatService sendChatService;
    private PineconeService pineconeService;

    public static synchronized BigStringConvolizer getInstance() {
        if (instance == null) {
            instance = new BigStringConvolizer();
        }
        return instance;
    }

    private BigStringConvolizer() {
        sendChatService = new SendChatService();
        pineconeService = PineconeService.getPineconeService();
    }

    public void sendFile(String s) {
        int i = 0;
        int charLeft = s.length();
        while (charLeft > CONVOLIZE_CONSTANT) {
            String batched = s.substring(i, i + CONVOLIZE_CONSTANT);
            Chat chat = new Chat(0, batched, false, true);


            DatabaseReference ref = Common.ref.child("Conversations").child(Common.id).push();
            ref.setValue(chat);

            try {
                pineconeService.sendChatToPinecone(chat, ref.getKey());
            } catch (IOException e) {
                e.printStackTrace();
            }
            charLeft -= CONVOLIZE_CONSTANT;
            i+=CONVOLIZE_CONSTANT;
        }
        String batched = s.substring(i);
        Chat chat = new Chat(0, batched, false, true);


        DatabaseReference ref = Common.ref.child("Conversations").child(Common.id).push();
        ref.setValue(chat);

        try {
            pineconeService.sendChatToPinecone(chat, ref.getKey());
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "sendFile: ");


    }
}
