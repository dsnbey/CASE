package com.example.acase.Model;

public class Chat {

    private int sender; // 1 for API, 0 for user
    private String message;
    private boolean deepMemory;


    public Chat( int sender, String message, boolean isVoice, boolean deepMemory) {
        this.sender = sender;
        this.message = message;
        this.deepMemory = deepMemory;


    }

    public Chat() {

    }

    public int getSender() {
        return sender;
    }

    public String getMessage() {
        return message;
    }
    public void setDeepMemory(boolean b) {deepMemory = b;}

    public boolean isDeepMemory() {
        return deepMemory;
    }
}
