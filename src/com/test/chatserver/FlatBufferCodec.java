package com.test.chatserver;

import Schema.Credentials;

import java.nio.ByteBuffer;

import com.google.flatbuffers.FlatBufferBuilder;

/**
 * Class to convert between FlatBuffers and Credentials.
 * 
 * Intended to be used for user authentication for a simple chat server,
 * or any other type of simple application.
 * 
 * Sending serialized data as opposed to plaintext acts as a reduction of
 * vulnerability during transmission.
 * 
 * @author jalbatross (Joey Albano)
 *
 */

public class FlatBufferCodec {
    public static final int SERIALIZED_CRED_LEN = 1024;
    /**
     * Creates a serialized Credentials Flatbuffer as a ByteBuffer of
     * length 128 bytes. Strings should be UTF-8 encoded to conform to
     * Flatbuffer requirements. 
     * @param name A username ranging from 1-12 UTF-8 characters
     * @param pw   A password ranging from 1-32 UTF-8 characters
     * @return 128 byte serialized Flatbuffer as ByteBuffer
     * 
     * @see package Schema 
     */
    static public ByteBuffer credentialsToByteBuffer(String name, String pw) {
        
        FlatBufferBuilder builder = new FlatBufferBuilder(SERIALIZED_CRED_LEN);   
        credentialToFlatBuff(builder, name, pw);
        
        return builder.dataBuffer();
    }
    
    /**
     * Creates a serialized Credentials Flatbuffer as a byte array of
     * length 128 bytes. Strings should be UTF-8 encoded to conform to
     * Flatbuffer requirements. 
     * @param name A username ranging from 1-12 UTF-8 characters
     * @param pw   A password ranging from 1-32 UTF-8 characters
     * @return 128 byte serialized Flatbuffer as byte[]
     * 
     * @see package Schema 
     */
    static public byte[] credentialsToByteArr(String name, String pw) {
        
        FlatBufferBuilder builder = new FlatBufferBuilder(SERIALIZED_CRED_LEN);
        credentialToFlatBuff(builder, name, pw);
        System.out.println("size: " + builder.sizedByteArray().length);
        return builder.sizedByteArray();
    }
    
    /**
     * Helper method to create a Credential Flatbuffer provided a builder,
     * username and password string. 
     * @param builder A FlatBufferBuilder
     * @param name    A username
     * @param pw      A password
     */
    private static void credentialToFlatBuff(FlatBufferBuilder builder, String name, String pw) {
        
        int user = builder.createString(name);
        int pass = builder.createString(pw);
        
        Credentials.startCredentials(builder);
        Credentials.addUsername(builder, user);
        Credentials.addPassword(builder, pass);
        
        int credential = Credentials.endCredentials(builder);
        builder.finish(credential);
    }
    
    /**
     * Return Credentials object from byte array
     * @param bytes   Serialized Credentials FlatBuffer
     * @return        Credentials
     */
    public static Credentials byteArrToCredentials(byte[] bytes) {
        ByteBuffer theBuf = java.nio.ByteBuffer.wrap(bytes);
        // Get an accessor to the root object inside the buffer.
        return Credentials.getRootAsCredentials(theBuf); 
    }
    
    /**
     * Return Credentials object from ByteBuffer
     * @param buf     Serialized Credentials FlatBuffer
     * @return        Credentials
     */
    public static Credentials byteBufToCredentials(ByteBuffer buf) {
        return Credentials.getRootAsCredentials(buf);
    }
}
