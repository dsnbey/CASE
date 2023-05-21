package com.example.acase.Model;

import com.google.gson.annotations.SerializedName;

public class EmbeddingRequest {
    @SerializedName("input")
    private String input;

    @SerializedName("model")
    private String model;

    public EmbeddingRequest(String input, String model) {
        this.input = input;
        this.model = model;
    }
}
