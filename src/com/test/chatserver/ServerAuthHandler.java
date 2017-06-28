package com.test.chatserver;

import java.util.ArrayList;
import java.util.HashSet;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

/**
 * Class used to authenticate users. Current implementation should handle 
 * byte buffers with the following structure:
 * 
 * [ short LEN, short INDEX, char[] NAME, char[] PASSWORD ]
 * 
 * LEN: 2 byte short, represents length of bytebuf
 * INDEX: A 2 byte short corresponding to the index of the first readable byte
 * of PASSWORD.
 * NAME: A char[] with 1-12 characters. Max size of 24 bytes.
 * PASSWORD: A char[] with 1-51 characters. Max size of 102 bytes.
 * 
 * All char[] should be UTF-8 encoded and the ByteBuf is assumed to be
 * Big Endian.
 * 
 * Upon successful registration or authentication of the provided username/password
 * pair, ServerAuthHandler removes itself from the pipeline with handlers that 
 * are useful for server functionality.
 * 
 * @author joey
 *
 */

public class ServerAuthHandler extends ChannelInboundHandlerAdapter {
    String username = new String();
    ChannelGroup allUsers;
    ArrayList<ChannelGroup> lobbies = new ArrayList<ChannelGroup>();
    
    public ServerAuthHandler(ChannelGroup grp, ArrayList<ChannelGroup> lobbies) {
        allUsers = grp;
        this.lobbies = lobbies;
    }
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("[ServerAuthHandler] Auth Handler Called");
        
        LoginAuthorizer login = new LoginAuthorizer();
        
        //Close connection if receive invalid credential
        if (!(msg instanceof ByteBuf)) {
            System.out.println("Bad credential format received");
            //TODO: send ERROR flatbuffer to user
            ctx.close();
            return;
        }
        System.out.println("Received a bytebuf");
        
        ByteBuf credential = (ByteBuf) msg;
        
        //Block until we receive the length and index
        if (credential.readableBytes() < 4) {
            return;
        }
        
        int len = credential.getShort(0);
        int index = credential.getShort(2);
        
        System.out.println("Length of bytebuf:" + len);
        System.out.println("beginning index of pw: " + index);
        
        char[] username = new char[(index - 4)];
        char[] password = new char[(len - index)];
        
        System.out.println("name len: " + (index - 4) + " bytes");
        System.out.println("pw len: "+ (len - index) + " bytes");
        
        for (int i = 0; i < username.length; i ++){
            System.out.println("reading byte: " + (i + 4));
            username[i] = (char) credential.getByte(i + 4);
            credential.setByte(i+ 4, 0);
            System.out.println("got character: " + username[i]);
            
        }
        System.out.println("username receieved: " + username.toString());
        
        for (int i = 0; i < password.length; i++) {
            System.out.println("reading byte: " + (i + index));
            password[i] = (char) credential.getByte(i + index );
            credential.setByte(i + index, 0);
            System.out.println("got character: " + password[i]);
        }
        
        
        
        ctx.close();
        return;

        /*
        //TODO: Add stricter username requirements
        if (credential.length() > 12 || credential.isEmpty()) {
            System.out.println("received bad credential");
            String errorMsg = "Username was longer than 12 chars or empty.";
            ctx.writeAndFlush( new TextWebSocketFrame(errorMsg));
            return;
        }
        
        //TODO: Instead of names.constains(credential), query database for
        // USERNAME and PASSWORD that client will submit here
        if(allUsers.contains(ctx.channel()) || names.contains(credential)) {
            System.out.println("either user already connected or duplicate username");
            ctx.writeAndFlush(new TextWebSocketFrame("That username is being used!"));
            return;
        }
        
        //TODO: Change names.add(credential) to database query I think
        //TODO: Send user list of lobbies and have them choose one after
        //      succsessful login
        if (allUsers.add(ctx.channel()) && names.add(credential)) {
            System.out.println("added user w/ name: " + credential);
            ctx.writeAndFlush(new TextWebSocketFrame("Login Successful! There are "
                    + allUsers.size() + " connected users."));
            ctx.pipeline().remove(this);
            
            //TODO: Make user choose lobby here or create new pipeline handler
            // for lobby selector
            //ctx.pipeline().addLast(new ChatServerLobbySelector());
            ctx.pipeline().addLast(new ChatServerDecoder());
            ctx.pipeline().addLast(new ChatServerHandler(allUsers, credential));
        }
        else {
            System.out.println("[ServerAuthHandler] ERROR while adding valid credential");
            ctx.writeAndFlush(new TextWebSocketFrame("Something weird happened! Sorry."));
            names.remove(credential);
            ctx.close();
        }
        */
        
    }
    
}
