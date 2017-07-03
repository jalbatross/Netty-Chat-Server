package com.test.chatserver;

import java.util.ArrayList;
import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequestDecoder;

public class ChatServerHttpHandler extends HttpRequestDecoder {
    List<Object> requests = new ArrayList<Object>();
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        super.decode(ctx, (ByteBuf) msg, requests);
        System.out.println("[chat server http handler finished decode]");
        HttpObject http = (HttpObject) requests.get(0);
        System.out.println(http);
        ctx.close();
        return;
    }
    
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        System.out.println("chatserverhttphandler was added");
    }
}
