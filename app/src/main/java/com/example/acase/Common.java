package com.example.acase;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Stores keys, locations & constants. I have provided here for testing purposes.
 * It also fetches them securely from Firebase. In production, the keys should be nulled beforehand.
 */
public class Common {

    public static final DatabaseReference ref = FirebaseDatabase.getInstance("https://case-31371-default-rtdb.europe-west1.firebasedatabase.app/").getReference();
    public static String id = "3ea1a93ec3326d5dade8a9a5d303cf80c4ed9f334df3a24e3efaaf74d5fff1f1";
    public static String openaiApiKey = "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";
    public static String MODEL = "gpt-3.5-turbo";
    public static String pineconeApiKey = "0d6b6c99-ea18-4725-b036-a503901925cb";
    public static String pineconeBaseUrlUpsert = "https://case-1bfe2b5.svc.asia-northeast1-gcp.pinecone.io/vectors/upsert/";
    public static String pineconeBaseUrlQuery = "https://case-1bfe2b5.svc.asia-northeast1-gcp.pinecone.io/query/";
    public static String EMBEDDING_MODEL_NAME = "text-embedding-ada-002";

}
