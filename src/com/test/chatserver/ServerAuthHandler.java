package com.test.chatserver;

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
    HashSet<String> names;
    
    public ServerAuthHandler(ChannelGroup grp, HashSet<String> usernames) {
        allUsers = grp;
        names = usernames;
    }
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("[ServerAuthHandler] Auth Handler Called");
        
        if (!(msg instanceof TextWebSocketFrame)) {
            ctx.close();
            return;
        }
       
        String credential = ((TextWebSocketFrame) msg).text();
        System.out.println("[AuthHandler] Got credential " + credential);
        if (credential.length() > 12 || credential.isEmpty()) {
            System.out.println("received bad credential");
            String errorMsg = "Username was longer than 12 chars or empty.";
            ctx.writeAndFlush( new TextWebSocketFrame(errorMsg));
            return;
        }
        
        
        if (allUsers.add(ctx.channel()) && names.add(credential)) {
            System.out.println("added user w/ name: " + credential);
            
            ctx.pipeline().remove(this);
            ctx.pipeline().addLast(new ChatServerDecoder());
            ctx.pipeline().addLast(new ChatServerHandler(allUsers, credential));
        }
        else {
            System.out.println("either user already connected or duplicate username");
            ctx.writeAndFlush(new TextWebSocketFrame("try a different username"));
        }
        
    }
    
    
}
