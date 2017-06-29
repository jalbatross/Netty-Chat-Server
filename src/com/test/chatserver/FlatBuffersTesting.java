package com.test.chatserver;

import java.nio.ByteBuffer;

import com.google.flatbuffers.FlatBufferBuilder;

import Schema.*;
public class FlatBuffersTesting {
    
    public static void main (String[] args) {
        FlatBufferBuilder builder = new FlatBufferBuilder(1024);
        
        int user = builder.createString("wahoo");
        int pw = builder.createString("Jones");
        
        //make credential
        int cred = Credentials.createCredentials(builder, user, pw);
        
        //make auth
        int verif = Auth.createAuth(builder, false);
        
        //make chat
        int chatMsg = Chat.createChat(builder, 6969, 
                builder.createString("joe"), builder.createString("BLOGGS"));
        
        //make lobbies
        int[] lobbies = new int[4];

        lobbies[0] = builder.createString("Tasma");
        lobbies[1] = builder.createString("Ceres");
        lobbies[2] = builder.createString("Larc");
        lobbies[3] = builder.createString("Gevergreen");
        
        //int lobbyVec = Lobbies.createListVector(builder, lobbies);
        
        int lobster = Lobbies.createListVector(builder, new int[] {
                builder.createString("WOO"),
                builder.createString("lalala")
        });
        int lobbyVec = Lobbies.createLobbies(builder, lobster);
        
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
        Message.addDataType(builder, Data.Lobbies);
        Message.addData(builder, lobbyVec);
        int lobbyMsg = Message.endMessage(builder);
        builder.finish(lobbyMsg);
        buf = builder.dataBuffer();
        Message lobbyFin = Message.getRootAsMessage(buf);
        messages[3] = lobbyFin;
        
        messages[4] = authMsg;
        
      //expect: cred , auth, chat, lobby, auth"
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
                    break;
                case Type.Lobbies:
                    System.out.println("got lobbies");
                    Lobbies lobs = (Lobbies)messages[i].data(new Lobbies());
                    System.out.println("num of lobbies: " + lobs.listLength());
                    for (int j = 0; j < lobs.listLength(); j++) {
                        System.out.println("lobby " + j + ": " + lobs.list(j));
                    }
                    break;
                default:
                    System.out.println("oh no");
                    break;
            }
            System.out.println("---------");
        }
        

        
    }
}
