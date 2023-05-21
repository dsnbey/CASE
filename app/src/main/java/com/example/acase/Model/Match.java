package com.example.acase.Model;

import java.util.List;

public class Match {

    private String id;

    private double score;


    private float[] values;

    private Metadata metadata;

    public Match() {

    }

    public Match(String id, double score, float[] values, Metadata metadata) {
        this.id = id;
        this.score = score;
        this.values = values;
        this.metadata = metadata;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public float[] getValues() {
        return values;
    }

    public void setValues(float [] values) {
        this.values = values;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }
}
