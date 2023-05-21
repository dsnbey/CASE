package com.example.acase.Model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class EmbeddingResponse {
    @SerializedName("object")
    private String object;

    @SerializedName("data")
    private List<EmbeddingData> data;

    @SerializedName("model")
    private String model;

    @SerializedName("usage")
    private Usage usage;

    public String getObject() {
        return object;
    }

    public List<EmbeddingData> getData() {
        return data;
    }

    public String getModel() {
        return model;
    }

    public Usage getUsage() {
        return usage;
    }
}
