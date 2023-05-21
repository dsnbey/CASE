package com.example.acase.Model;


/**
 * Will be used by Openai API - to match JSON
 */
public class ChatMessage {
    private String role;
    private String content;



    public ChatMessage() {
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}