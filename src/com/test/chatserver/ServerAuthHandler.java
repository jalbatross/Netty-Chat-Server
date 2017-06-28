package com.test.chatserver;

import java.util.ArrayList;
import java.util.HashSet;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

/**
 * Class used to authenticate users. Current implementation should receive
 * a TextWebSocketFrame from client and pass it along to the server so that
 * it can record that username to use for the connected client.
 * 
 * Upon successful registration of the username, the Handler removes itself 
 * from the pipeline. If the user does not supply correct credentials in this
 * version, the connection is closed immediately.
 * 
 * @author joey
 *
 */

public class ServerAuthHandler extends ChannelInboundHandlerAdapter {
    String username = new String();
    ChannelGroup allUsers;
    ArrayList<ChannelGroup> lobbies = new ArrayList<ChannelGroup>();
    
    //TODO: Replace HashSet names with database calls
    HashSet<String> names;
    
    public ServerAuthHandler(ChannelGroup grp, ArrayList<ChannelGroup> lobbies) {
        allUsers = grp;
        this.lobbies = lobbies;
    }
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("[ServerAuthHandler] Auth Handler Called");
        LoginAuthorizer login = new LoginAuthorizer();
        
        if (login.verifyUser("admin", "abcdef")) {
            System.out.println("Good auth");
            
        }
        else {
            System.out.println("Bad auth");
        }
        
        //User is trying to do something strange for authentication, close
        //connection immediately.
        if (!(msg instanceof TextWebSocketFrame)) {
            ctx.close();
            return;
        }
       
        String credential = ((TextWebSocketFrame) msg).text();
        System.out.println("[AuthHandler] Got credential " + credential);
        
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
        ctx.close();
        
    }
    
    
}
