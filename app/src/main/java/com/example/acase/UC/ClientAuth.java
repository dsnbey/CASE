package com.example.acase.UC;

import static android.content.ContentValues.TAG;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.acase.Common;
import com.google.common.hash.Hashing;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.nio.charset.StandardCharsets;

/**
 * Controls Authentication & encryption. Talks to Firebase RTDB.
 */
public class ClientAuth {

    private static boolean credentialStatus = false;

    public static boolean authenticateUser(String username, String password) {

        Common.ref.child("Login").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild(username)) {
                    if (snapshot.child(username).getValue(String.class).equals(password)) {
                        credentialStatus = true;

                        hash(username, password);

                        retrieveAPIKey();
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "onCancelled: " + error.getMessage());
            }
        });


        return credentialStatus;
    }

    private static void hash(String username, String password) {
        String in = username + password + "hashed";
        String sha256hex = Hashing.sha256()
                .hashString(in, StandardCharsets.UTF_8)
                .toString();

        Log.d(TAG, "hash: " + sha256hex);
        Common.id = sha256hex;

    }

    private static void retrieveAPIKey() {
        Common.ref.child("Keys").child(Common.id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Common.EMBEDDING_MODEL_NAME = snapshot.child("EMBEDDING_MODEL_NAME").getValue(String.class);
                Common.openaiApiKey = snapshot.child("openaiApiKey").getValue(String.class);
                Common.MODEL = snapshot.child("MODEL").getValue(String.class);
                Common.pineconeBaseUrlQuery = snapshot.child("pineconeBaseUrlQuery").getValue(String.class);
                Common.pineconeBaseUrlUpsert = snapshot.child("pineconeBaseUrlUpsert").getValue(String.class);
                Common.pineconeApiKey = snapshot.child("pineconeKey").getValue(String.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "onCancelled: key not retrieved " );
            }
        });
    }
}
