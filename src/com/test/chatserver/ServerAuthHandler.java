package com.test.chatserver;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
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
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("[ServerAuthHandler] Auth Handler Called");
        
        if (!(msg instanceof TextWebSocketFrame)) {
            ctx.close();
            return;
        }
        
        TextWebSocketFrame credential = (TextWebSocketFrame) msg;
        ctx.fireChannelRead(credential);
        
    }
    
    
}
