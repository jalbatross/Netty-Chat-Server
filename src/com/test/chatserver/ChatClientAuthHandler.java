package com.test.chatserver;

import Schema.Auth;
import Schema.Message;
import Schema.Type;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.AttributeKey;

/**
 * Handles FlatBuffers Messages with data type Auth as defined in Schema
 * 
 * Flags the "authorized" attribute of the channel to TRUE if the 
 * Auth has verified as true and false otherwise then forwards the msg
 * in the pipeline upstream
 * 
 * Simply forwards msg upstream if the dataType of the flatBufMsg is not 
 * Auth.
 * 
 * @author jalbatross (Joey Albano)
 *
 */
public class ChatClientAuthHandler extends ChannelInboundHandlerAdapter {
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("[ChatClientAuth] ChannelRead called");
        ByteBuf buf = (ByteBuf) msg;
        Message flatbufMsg = Message.getRootAsMessage(buf.nioBuffer());
        
        if (flatbufMsg.dataType() == Schema.Type.Auth){
            System.out.println("[ChatClientAuth] Received Auth packet");
            Auth auth = (Auth) flatbufMsg.data(new Auth());
            
            ctx.channel().attr(AttributeKey.valueOf("authorized")).set(auth.verified());
            if (auth.verified()) {
                System.out.println("User verified!");
                System.out.println("Ticket: " + auth.ticket());
            }
            else {
                System.out.println("User login failed");
            }
        }
        
        ctx.fireChannelRead(msg);
        
        
    }

}
