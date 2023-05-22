package com.example.acase.Model;

public class TranscriptionRequest {
    private String model;
    private String file;

    public TranscriptionRequest(String model, String file) {
        this.model = model;
        this.file = file;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }
}
