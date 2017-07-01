package com.test.chatserver;

import java.util.ArrayList;
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
 *  Decodes ByteBufs by blocking until they are ready to be passed down
 *  the channel pipeline.
 *  
 *  Each segment of data should be prefixed by a 4 byte integer indicating
 *  its length.
 * 
 * @author joey
 *
 */

public class ChatClientDecoder extends ChannelInboundHandlerAdapter {
    
    public static final int MAX_BYTES = 1024;
    private List<Object> out = new ArrayList<Object>();
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("[ChatClientDecoder] Sending upstream!");
        
        if (!(msg instanceof ByteBuf)) {
            System.out.println("Client decoder received non bytebuf, sending upstream");
            ctx.fireChannelRead(msg);
            return;
        }
        
        ByteBuf buf = (ByteBuf) msg;
        
        IntegerHeaderFrameDecoder decoder = new IntegerHeaderFrameDecoder();
        
        decoder.decode(ctx, buf, out);
       
        ctx.fireChannelRead(out.get(0));
        
        return;
        
    }
    
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("[ClientDecoder] Decoder Active");
    }
    
    
    

}
