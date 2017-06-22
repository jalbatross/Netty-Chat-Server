package com.test.chatserver;

/**
 * A class intended to be used to transmit chat messages between clients
 * and servers.
 * 
 * Each chat message contains an author and a message.
 * 
 * @author jalbatross (Joey Albano)
 *
 */

public class ChatMessage {
    protected String author;
    protected String message;
    
    public ChatMessage(String auth, String msg) {

        author = auth;
        message = msg;
    };
       
    public String getAuthor() { return author;}
    public String getMsg() {return message;}

}
