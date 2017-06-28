package com.test.chatserver;

import Schema.Credentials;
import com.google.flatbuffers.FlatBufferBuilder;

public class FlatBufferTesting {

    public static void main(String[] args) {
        FlatBufferBuilder builder = new FlatBufferBuilder(1024);
        
        int username = builder.createString("joseph");
        int password = builder.createString("none");
        
        Credentials.startCredentials(builder);
        Credentials.addUsername(builder, username);
        Credentials.addPassword(builder, password);
        
        int credential = Credentials.endCredentials(builder);
        builder.finish(credential);
        
        byte[] myBuf = builder.sizedByteArray();
        
     // This must be called after `finish()`.
     //   java.nio.ByteBuffer buf = builder.dataBuffer();
        
//Reading
        
        java.nio.ByteBuffer theBuf = java.nio.ByteBuffer.wrap(myBuf);
        // Get an accessor to the root object inside the buffer.
        Credentials cred = Credentials.getRootAsCredentials(theBuf);
        
        String theName = cred.username();
        String thePass = cred.password();
        
        System.out.println("username: " + theName +
                "\npassword: " + thePass);
    }
}
