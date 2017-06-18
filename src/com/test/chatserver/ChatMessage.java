package com.test.chatserver;

public class ChatMessage {
    private String time = "time";
    private String author = "you";
    private String message =  "msg";
    
    public ChatMessage(String time, String auth, String msg) {
        this.time = time;
        author = auth;
        message = msg;
    };
    
    public ChatMessage() { };

}
