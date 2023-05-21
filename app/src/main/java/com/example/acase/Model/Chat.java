package com.example.acase.Model;

public class Chat {

    private int sender; // 1 for API, 0 for user
    private String message;


    public Chat( int sender, String message, boolean isVoice) {
        this.sender = sender;
        this.message = message;


    }

    public Chat() {

    }

    public int getSender() {
        return sender;
    }

    public String getMessage() {
        return message;
    }

}
