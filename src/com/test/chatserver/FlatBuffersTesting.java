package com.test.chatserver;

import com.google.flatbuffers.FlatBufferBuilder;

import Schema.*;
public class FlatBuffersTesting {
    
    public static void main (String[] args) {
        FlatBufferBuilder builder = new FlatBufferBuilder(1024);
        
        Message.startMessage(builder);
        Message.addDataType(builder, Data.Auth);
    }
}
