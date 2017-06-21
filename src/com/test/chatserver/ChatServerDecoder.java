package com.test.chatserver;

import java.util.Arrays;
import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrameDecoder;

/**
 * Used to decode received BinaryWebSocketFrames into byte buffers. 
 * For this application, we expect to receive 14 bytes from clients for processing.
 * 
 * 
 * @author joey
 *
 */

public class ChatServerDecoder extends SimpleChannelInboundHandler<Object> {
    
    public static final int MESSAGE_SIZE = 14;
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("[ChatServerDecoder] Sending upstream!");
        Channel ch = ctx.channel();
        
        //Only need to decode BinaryWebSocketFrames
        //Send all other messages upstream as-is
        if (!(msg instanceof BinaryWebSocketFrame)) {
            ctx.fireChannelRead(msg);
            return;
        }
        
        System.out.println("[ChatServerDecoder] Decoder received binary frame");
        BinaryWebSocketFrame frame = (BinaryWebSocketFrame) msg;
        
        
        if (frame.content().readableBytes() < MESSAGE_SIZE) {
            return;
        }
        
        byte[] bytes = new byte[14];
        frame.content().readBytes(bytes);
        ByteBuf buf = Unpooled.copiedBuffer(bytes);
        
        ctx.fireChannelRead(buf);
        
    }
    

}
