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
 * appropriate type of Message data.
 * 
 * @see Schema
 * 
 * @author jalbatross (Joey Albano)
 *
 */

//TODO: Enforce maximum size of flatbuffer credential for the codec,

public class FlatBuffersCodec {
    
    public static final int SERIALIZED_CRED_LEN = 128;
    public static final int DEFAULT_SIZE = 1024;
    
    /**
     * Converts a pair of user credentials (username and password) to a 
     * serialized FlatBuffer ByteBuf object with Data type Credentials.
     * 
     * For further reference, refer to schema.fbs in Schema.
     * 
     * @param name    A name string
     * @param pw      A char sequence for password, preferably char[]
     * 
     * @see           Schema
     * @return        Serialized FlatBuffer Message with data type Credentials
     */
    static public ByteBuffer credentialsToByteBuffer(String name, CharSequence pw) {
        FlatBufferBuilder fbb = new FlatBufferBuilder(DEFAULT_SIZE);
        
        //Create credential
        int cred = Credentials.createCredentials(fbb, 
                fbb.createString(name),
                fbb.createString(pw));
                
        Message.startMessage(fbb);
        Message.addDataType(fbb, Data.Credentials);
        Message.addData(fbb, cred);
        
        int finishedMsg = Message.endMessage(fbb);
        fbb.finish(finishedMsg);
        
        return fbb.dataBuffer();
    }
    
    /**
     * Wraps a boolean into a serialized FlatBuffer ByteBuf with Data type
     * Auth. Intended to be used to verify user logins, authorizations, etc.
     * by a server.
     * 
     * @param verified      True if verified, false otherwise
     * @return              Serialized FlatBuffer Message as a ByteBuf, Data type
     *                      Auth
     *                      
     * @see Schema
     */
    static public ByteBuffer authToByteBuffer(boolean verified) {
        FlatBufferBuilder fbb = new FlatBufferBuilder(DEFAULT_SIZE);
        
        int auth = Auth.createAuth(fbb, verified);
        
        Message.startMessage(fbb);
        Message.addDataType(fbb, Data.Auth);
        Message.addData(fbb, auth);
        
        int finishedMsg = Message.endMessage(fbb);
        fbb.finish(finishedMsg);
        
        return fbb.dataBuffer();
    }
    
    /**
     * Serializes a TimeChatMessage to a ByteBuffer using FlatBuffers.
     * The serialized data is a Message conforming to the schema provided
     * in schema.fbs which can be found in the Schema package. 
     * 
     * Its data can be dereferenced by reading the Message object and 
     * casting its data as a Chat object.
     * 
     * @see Schema
     * 
     * @param chatMsg       A TimeChatMessage
     * @return              Serialized FlatBuffer as ByteBuf with Data type
     *                      Chat
     */
    static public ByteBuffer chatToByteBuffer(TimeChatMessage chatMsg){
        FlatBufferBuilder fbb = new FlatBufferBuilder(DEFAULT_SIZE);
        
        int chat = Chat.createChat(fbb, 
                chatMsg.getTime(), 
                fbb.createString(chatMsg.getAuthor()), 
                fbb.createString(chatMsg.getMsg()));
        
        Message.startMessage(fbb);
        Message.addDataType(fbb, Data.Chat);
        Message.addData(fbb, chat);
        
        int finishedMsg = Message.endMessage(fbb);
        fbb.finish(finishedMsg);
        
        return fbb.dataBuffer();
        
    }
    
    /**
     * Serializes a vector of strings to a ByteBuffer using FlatBuffers.
     * Intended to be used to transmit lists of server lobbies between server
     * and client. The serialized data is a Message conforming to the schema
     * in schema.fbs which can be found in the Schema package.
     * 
     * Its data can be dereferenced by using the getRootAsMessage function
     * from the Message library on the ByteBuffer returned by this
     * and casting the return of the Message.data() function to 
     * Lobbies.
     * 
     * 
     * @param lobbies   A vector of Strings that should be a list of server lobbies
     * @return          Serialized FlatBuffer as ByteBuf with Data type
     *                  Lobbies
     * 
     * @see Schema
     */
    static public ByteBuffer lobbiesToByteBuffer(String[] lobbies) {
        FlatBufferBuilder fbb = new FlatBufferBuilder(DEFAULT_SIZE);
        
        int[] lobbiesOffsets = new int[lobbies.length];
        for (int i = 0; i < lobbies.length; i ++) {
            lobbiesOffsets[i] = fbb.createString(lobbies[i]);
        }
        int listVect = Lobbies.createListVector(fbb, lobbiesOffsets);
        int lobbiesFinal = Lobbies.createLobbies(fbb, listVect);
        
        Message.startMessage(fbb);
        Message.addDataType(fbb, Data.Lobbies);
        Message.addData(fbb, lobbiesFinal);
        
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
