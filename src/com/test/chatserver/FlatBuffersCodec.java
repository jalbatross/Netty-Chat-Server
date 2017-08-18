package com.test.chatserver;

import Schema.*;
import game.ServerGame;

import java.nio.ByteBuffer;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;

/**
 * Class to convert between FlatBuffers and Messages.
 * 
 * Conforms to Schema provided in the Schema package.
 * 
 * Takes ByteBufs or byte arrays and converts them to
 * appropriate type of Message data and vice versa
 * 
 * @see {@link Schema}
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
     * Wraps a boolean and a String ticket into a serialized FlatBuffer ByteBuf 
     * with Data type Auth. Intended to be used to verify user logins and 
     * authorization by a server.
     * @param verified      True if verified, false otherwise
     * @param ticket        A ticket String
     * 
     * @return              Serialized FlatBuffer Message as a ByteBuf, Data type
     *                      Auth
     *                      
     * @see Schema
     */
    static public ByteBuffer authToByteBuffer(boolean verified, String ticket) {
        FlatBufferBuilder fbb = new FlatBufferBuilder(DEFAULT_SIZE);
        
        int auth = Auth.createAuth(fbb, verified, fbb.createString(ticket));
        
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
     * Intended to be used to transmit lists of Strings between server
     * and client. The serialized data is a Message conforming to the schema
     * in schema.fbs which can be found in the Schema package.
     * 
     * Its data can be dereferenced by using the getRootAsMessage function
     * from the Message library on the ByteBuffer returned by this
     * and casting the return of the Message.data() function to 
     * Lobbies.
     * 
     * @param type      The type of data that contents refers to, i.e lobbies or
     *                  usernames
     *                  
     * @param contents  A vector of Strings 
     *                  
     * @return          Serialized FlatBuffer as ByteBuf with Data type
     *                  Lobbies
     * 
     * @see Schema
     */
    static public ByteBuffer listToByteBuffer(String type, String[] contents) {
        FlatBufferBuilder fbb = new FlatBufferBuilder(DEFAULT_SIZE);
        
        int[] contentsOffsets = new int[contents.length];
        for (int i = 0; i < contents.length; i ++) {
            contentsOffsets[i] = fbb.createString(contents[i]);
        }
        int contentsVect = List.createContentsVector(fbb, contentsOffsets);
        int typeVect = fbb.createString(type);
        
        int listFinal = List.createList(fbb, typeVect, contentsVect);
        
        Message.startMessage(fbb);
        Message.addDataType(fbb, Data.List);
        Message.addData(fbb, listFinal);
        
        int finishedMsg = Message.endMessage(fbb);
        fbb.finish(finishedMsg);
        
        return fbb.dataBuffer();
    }
    
    static public ByteBuffer gameToByteBuffer(ServerGame game) throws Exception {
        FlatBufferBuilder fbb = new FlatBufferBuilder(DEFAULT_SIZE);
        
        byte gameType = '\0';
        int gameData;
        int[] playerNamesData = new int[game.players().size()];
        int playerNamesVect = 0 ;
        short bestOf = 0;
        boolean completed = false;
        
        //Get game type
        switch (game.type()) {
            case RPS:
                gameType = Schema.GameType.RPS;
                break;
            case COUP:
                gameType = Schema.GameType.COUP;
                break;
            default:
                throw new Exception("Invalid game type for FlatBuffers game");
                
        }
        
        //Get gamestate
        gameData = fbb.createByteVector(game.gameState());
        
        //Create player names offset
        for (int i = 0; i < game.players().size(); i++ ) {
            playerNamesData[i] = fbb.createString(game.players().get(i));
        }
        playerNamesVect = Game.createPlayersVector(fbb, playerNamesData);
        
        //Get bestof data
        bestOf = game.bestOf();
        
        //Get gameover
        completed = game.gameOver();
        
        int fbGame = Game.createGame(fbb, gameType, gameData, playerNamesVect, bestOf, completed);
        
        Message.startMessage(fbb);
        Message.addDataType(fbb, Data.Game);
        Message.addData(fbb, fbGame);
        
        int finishedMsg = Message.endMessage(fbb);
        fbb.finish(finishedMsg);
        
        return fbb.dataBuffer();
    }
    
    /**
     * Deserialize FlatBuffers byteBuf into one of Table data types
     * defined in schema.fbs as a new instance of the type T.
     * 
     * @see Schema
     * 
     * @param buf     Serialized FlatBuffers
     * @param type    Type of object to return
     * @return        Deserialized object of type T
     * 
     * @throws Exception    Illegal Access Exception if type is wrong
     */
    @SuppressWarnings("unchecked")
    public static <T extends Table> T byteBufToData(ByteBuffer buf, Class<T> type) 
            throws Exception {
        Message msg = Message.getRootAsMessage(buf);
        return (T) msg.data(type.newInstance());
    }
    
}
