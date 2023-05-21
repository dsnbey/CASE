package com.example.acase.Model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class EmbeddingData {
    @SerializedName("object")
    private String object;

    @SerializedName("embedding")
    private List<Float> embedding;

    @SerializedName("index")
    private int index;

    public String getObject() {
        return object;
    }

    public List<Float> getEmbedding() {
        return embedding;
    }

    public int getIndex() {
        return index;
    }
}