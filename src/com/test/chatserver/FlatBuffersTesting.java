package com.test.chatserver;

import java.nio.ByteBuffer;

import com.google.flatbuffers.FlatBufferBuilder;

import Schema.*;
public class FlatBuffersTesting {
    
    public static final int CHAT = 0;
    public static final int CREDENTIAL = 1;
    public static final int AUTH = 2;
    public static final int LOBBIES = 3;
    
    public static void main (String[] args) {
        FlatBufferBuilder builder = new FlatBufferBuilder(1024);
        
        int user = builder.createString("wahoo");
        int pw = builder.createString("Jones");
        
        int cred = Credentials.createCredentials(builder, user, pw);
        
        Message.startMessage(builder);
        Message.addDataType(builder, Data.Credentials);
        Message.addData(builder, cred);
        
        int msg = Message.endMessage(builder);
        builder.finish(msg);
        
        ByteBuffer buf = builder.dataBuffer();
        
        Message decoded = Message.getRootAsMessage(buf);
        
        switch (decoded.dataType()) {
            case CHAT:
                System.out.println("got chat msg");
                break;
            case CREDENTIAL:
                System.out.println("got credentials");
                break;
            case AUTH:
                System.out.println("got auth");
                break;
            case LOBBIES:
                System.out.println("got lobbies");
                break;
            default:
                System.out.println("oh no");
                break;
        }
        
    }
}
