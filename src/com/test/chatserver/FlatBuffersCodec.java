package com.test.chatserver;

import Schema.*;

import java.nio.ByteBuffer;

import com.google.flatbuffers.FlatBufferBuilder;

/**
 * Class to convert between FlatBuffers and Messages.
 * 
 * Conforms to Schema provided in the Schema package.
 * 
 * Takes ByteBufs or byte arrays and converts them to
 * credentials,
 * 
 * @author jalbatross (Joey Albano)
 *
 */

//TODO: Enforce maximum size of flatbuffer credential for the codec,

public class FlatBuffersCodec {
    
    public static final int SERIALIZED_CRED_LEN = 128;
    public static final int DEFAULT_SIZE = 1024;
    
    static public ByteBuffer credentialsToByteBuffer(String name, String pw) {
        FlatBufferBuilder fbb = new FlatBufferBuilder(DEFAULT_SIZE);
        
        Message.startMessage(fbb);
        Message.addDataType(fbb, Data.Credentials);
        Message.addData(fbb, Credentials.createCredentials(fbb,
                fbb.createString(name), fbb.createString(pw)));
        
        int finishedMsg = Message.endMessage(fbb);
        fbb.finish(finishedMsg);
        
        return fbb.dataBuffer();
    }
    
    static public ByteBuffer authToByteBuffer(boolean verified) {
        FlatBufferBuilder fbb = new FlatBufferBuilder(DEFAULT_SIZE);
        
        Message.startMessage(fbb);
        Message.addDataType(fbb, Data.Auth);
        Message.addData(fbb, Auth.createAuth(fbb, verified));
        
        int finishedMsg = Message.endMessage(fbb);
        fbb.finish(finishedMsg);
        
        return fbb.dataBuffer();
    }
    
    static public ByteBuffer chatToByteBuffer(TimeChatMessage chatMsg){
        FlatBufferBuilder fbb = new FlatBufferBuilder(DEFAULT_SIZE);
        
        Message.startMessage(fbb);
        Message.addDataType(fbb, Data.Chat);
        Message.addData(fbb, Chat.createChat(fbb, 
                chatMsg.getTime(), 
                fbb.createString(chatMsg.getAuthor()), 
                fbb.createString(chatMsg.getMsg())));
        
        int finishedMsg = Message.endMessage(fbb);
        fbb.finish(finishedMsg);
        
        return fbb.dataBuffer();
        
    }
    
    static public ByteBuffer lobbiesToByteBuffer(String[] lobbies) {
        FlatBufferBuilder fbb = new FlatBufferBuilder(DEFAULT_SIZE);
        
        Message.startMessage(fbb);
        Message.addDataType(fbb, Data.Lobbies);
        
        int[] lobs = new int[lobbies.length];
        for (int i = 0; i < lobbies.length; i ++) {
            lobs[i] = fbb.createString(lobbies[i]);
        }
        
        int lobList = Lobbies.createListVector(fbb, lobs);
        Message.addData(fbb, Lobbies.createLobbies(fbb, lobList));
        
        int finishedMsg = Message.endMessage(fbb);
        fbb.finish(finishedMsg);
        
        return fbb.dataBuffer();
    }
    
    /**
     * Creates a serialized Credentials Flatbuffer as a ByteBuffer. 
     * Strings should be UTF-8 encoded to conform to Flatbuffer requirements.
     *  
     * @param name A username ranging from 1-12 UTF-8 characters
     * @param pw   A password ranging from 1-32 UTF-8 characters
     * @return  Serialized Flatbuffer as ByteBuffer
     * 
     * @see package Schema 
     */
    /*
    static private ByteBuffer credentialsToByteBuffer(String name, String pw) {
        
        FlatBufferBuilder builder = new FlatBufferBuilder(SERIALIZED_CRED_LEN);   
        credentialToFlatBuff(builder, name, pw);
        
        return builder.dataBuffer();
    }*/
    
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
