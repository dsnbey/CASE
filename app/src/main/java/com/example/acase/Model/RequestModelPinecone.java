package com.example.acase.Model;

import java.util.List;

public class RequestModelPinecone {
    private Float[] vector;

    private int topK;


    private boolean includeMetadata;

    private boolean includeValues;


    public RequestModelPinecone() {
    }

    public Float[] getVector() {
        return vector;
    }

    public void setVector(Float[]vector) {
        this.vector = vector;
    }

    public int getTopK() {
        return topK;
    }

    public void setTopK(int topK) {
        this.topK = topK;
    }

    public boolean isIncludeMetadata() {
        return includeMetadata;
    }

    public void setIncludeMetadata(boolean includeMetadata) {
        this.includeMetadata = includeMetadata;
    }

    public boolean isIncludeValues() {
        return includeValues;
    }

    public void setIncludeValues(boolean includeValues) {
        this.includeValues = includeValues;
    }

}