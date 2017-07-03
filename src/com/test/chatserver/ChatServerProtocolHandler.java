package com.test.chatserver;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ChatServerProtocolHandler extends ChannelInboundHandlerAdapter {
    private ByteBuf buf;
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("[ChatServerProtocolHandler] channelread called");
        
        buf = (ByteBuf) msg;
        if (buf.readableBytes() < 5) {
            return;
        }
        
        final int magic1 = buf.getUnsignedByte(buf.readerIndex());
        final int magic2 = buf.getUnsignedByte(buf.readerIndex() + 1);
        
        char char1 = (char) magic1;
        char char2 = (char) magic2;
        
        System.out.println("[ProtocolHandler] protocol: " + char1 +  char2);
        
        if (char1 == 'G' && char2 == 'E'){
            System.out.println("[ProtocolHandler] get");
            ctx.close();
            
        }
        else if (char1 == 'P' && char2 == 'O') {
            System.out.println("[ProtocolHandler] post");
            ctx.close();
        }
        else {
            System.out.println("ProtocolHandler] packet");
            ctx.fireChannelRead(msg);
        }

        return;
    }
}
