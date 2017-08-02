package Tests;

import java.nio.ByteBuffer;

import com.google.flatbuffers.FlatBufferBuilder;
import com.test.chatserver.FlatBuffersCodec;

import Schema.*;

/**
 * Testing class for FlatBuffers schema and FlatBuffersCodec
 * 
 * @author jalbatross (Joey Albano)
 *
 */
public class FlatBuffersTesting {
    
    public static void main (String[] args) {
        FlatBufferBuilder builder = new FlatBufferBuilder(1024);
        
        int user = builder.createString("wahoo");
        int pw = builder.createString("Jones");
        
        //make credential
        int cred = Credentials.createCredentials(builder, user, pw);
        
        //make auth
        int verif = Auth.createAuth(builder, false, user);
        
        //make chat
        int chatMsg = Chat.createChat(builder, 6969, 
                builder.createString("joe"), builder.createString("BLOGGS"));
        
        //make lobbies
        int[] lobbies = new int[4];

        lobbies[0] = builder.createString("Tasma");
        lobbies[1] = builder.createString("Ceres");
        lobbies[2] = builder.createString("Larc");
        lobbies[3] = builder.createString("Gevergreen");
        int contentsList = List.createContentsVector(builder, lobbies);
        int listVec = List.createList(builder, builder.createString("lobbies"), contentsList);
        
        Message.startMessage(builder);
        Message.addDataType(builder, Data.Credentials);
        Message.addData(builder, cred);
        
        int msg = Message.endMessage(builder);
        builder.finish(msg);
        
        ByteBuffer buf = builder.dataBuffer();
        
        Message decoded = Message.getRootAsMessage(buf);
        
        Message[] messages = new Message[5];
        messages[0] = decoded;
        
        Message.startMessage(builder);
        Message.addDataType(builder, Data.Auth);
        Message.addData(builder, verif);
        int finAuth = Message.endMessage(builder);
        builder.finish(finAuth);
        buf = builder.dataBuffer();
        Message authMsg = Message.getRootAsMessage(buf);
        messages[1] = authMsg;
        
        Message.startMessage(builder);
        Message.addDataType(builder, Data.Chat);
        Message.addData(builder, chatMsg);
        int finChat = Message.endMessage(builder);
        builder.finish(finChat);
        buf = builder.dataBuffer();
        Message chatFin = Message.getRootAsMessage(buf);
        messages[2] = chatFin;
        
        Message.startMessage(builder);
        Message.addDataType(builder, Data.List);
        Message.addData(builder, listVec);
        int lobbyMsg = Message.endMessage(builder);
        builder.finish(lobbyMsg);
        buf = builder.dataBuffer();
        Message lobbyFin = Message.getRootAsMessage(buf);
        messages[3] = lobbyFin;
        
        messages[4] = authMsg;
        
      //expect: cred , auth, chat, list, auth
        for (int i = 0; i < messages.length; i ++) {
            switch (messages[i].dataType()) {
                case Type.Chat:
                    System.out.println("got chat msg");
                    Chat chat = (Chat)messages[i].data(new Chat());
                    System.out.println("author: " + chat.author() + "\n"
                            + "msg: " + chat.message());
                    break;
                case Type.Credentials:
                    System.out.println("got credentials");
                    Credentials creds = (Credentials)messages[i].data(new Credentials());
                    System.out.println("username: " + creds.username() + "\n"
                            + "pass: " + creds.password());
                    break;
                case Type.Auth:
                    System.out.println("got auth");
                    Auth auth = (Auth)messages[i].data(new Auth());
                    if (auth.verified()){
                        System.out.println("verified");
                    }
                    else {
                        System.out.println("not verified");
                    }
                    System.out.println("Auth Ticket: " + auth.ticket());
                    break;
                case Type.List:
                    System.out.println("got lobbies");
                    List stuff = (List)messages[i].data(new List());
                    System.out.println("type of contents: " + stuff.type());
                    System.out.println("num of elements in contents: " + stuff.contentsLength());
                    
                    for (int j = 0; j < stuff.contentsLength(); j++) {
                        System.out.println("lobby " + j + ": " + stuff.contents(j));
                    }
                    break;
                default:
                    System.out.println("oh no");
                    break;
            }
            System.out.println("---------");
        }
        
        //Testing FlatBuffersCodecs
        String[] dudes = {"eli","ralph","sirak","ADJUSTMENTS"};
        ByteBuffer theBUF = FlatBuffersCodec.listToByteBuffer("dudes", dudes);
        
        Message theMsg = Message.getRootAsMessage(theBUF);
        if (theMsg.dataType() == Type.List) {
            System.out.println("successs!");
            
            List liszt = (List)theMsg.data(new List());
            System.out.println("list type: " + liszt.type());
            for (int j = 0; j < liszt.contentsLength(); j++) {
                System.out.println("content: " + liszt.contents(j));
            }
        }
        System.out.println("\n");
        String theName = "jones";
        String thePass = "wowza";
        
        ByteBuffer credBuf = FlatBuffersCodec.credentialsToByteBuffer(theName, thePass);
        Message credMsg = Message.getRootAsMessage(credBuf);
        
        Credentials myCred = (Credentials)credMsg.data(new Credentials());
        System.out.println(myCred.username() +  "..." + myCred.password());
        
        try {
            Credentials myCred2 = FlatBuffersCodec.byteBufToData(credBuf, Credentials.class);
            System.out.println(myCred2.username() +  "..." + myCred2.password());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        //---Testing GameCreationRequest----
        String gameCreateName = "TestName";
        String gameTypeName = "rps";
        short gameCapacity = 2;
        String capacityString = Integer.toBinaryString(gameCapacity);
        System.out.println("Capacity (binary): " + capacityString);
        String gameCreatePw = "null";
        
        int gameCreateOffset = builder.createString(gameCreateName);
        int gameTypeOffset = builder.createString(gameTypeName);
        int gamePwOffset = builder.createString(gameCreatePw);
        
        int req = GameCreationRequest.createGameCreationRequest(builder, gameCreateOffset, gameTypeOffset, gameCapacity, gamePwOffset);
        Message.startMessage(builder);
        Message.addDataType(builder, Data.GameCreationRequest);
        Message.addData(builder, req);
        int finReq = Message.endMessage(builder);
        builder.finish(finReq);
        
        buf = builder.dataBuffer();
        
        Message reqMessage = Message.getRootAsMessage(buf);
        GameCreationRequest gcr = (GameCreationRequest) reqMessage.data(new GameCreationRequest());
        System.out.println("---Game Creation Request info---");
        if (gcr.name().contentEquals("TestName")) {
            System.out.println("Name: TestName (succcess)" );
        }
        else {
            System.out.println("GCR name test failed, val: " + gcr.name());
        }
        
        if (gcr.type().contentEquals("rps")) {
            System.out.println("Type: rps (success)");
        }
        else {
            System.out.println("GCR type test failed, val: " + gcr.type());
        }
        
        System.out.println("Capacity as stored: " + Integer.toBinaryString(gcr.capacity()));
        if (gcr.capacity() == 2) {
            System.out.println("Capacity: 2 (success)");
        }
        else {
            System.out.println("GCR capacity test failed, val: " + gcr.capacity());
        }
        
        if (gcr.password().contentEquals("null")) {
            System.out.println("Password: null (success)");
        }
        else {
            System.out.println("GCR password test failed, val: " + gcr.password());
        }

        
    }
}
