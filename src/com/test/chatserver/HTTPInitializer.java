package com.test.chatserver;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.ssl.SslContext;

public class HTTPInitializer extends ChannelInitializer<SocketChannel> {
	private Map<String,TimeChatMessage> ticketDB;
	private final SslContext sslCtx;
	public HTTPInitializer (SslContext sslCtx) {
		this.sslCtx = sslCtx;
		
	}
	
    public HTTPInitializer(Map<String, TimeChatMessage> ticketDB) {
        this.ticketDB = ticketDB;
        sslCtx = null;
    }

    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();
        if (sslCtx != null) {
            pipeline.addLast(sslCtx.newHandler(socketChannel.alloc()));
        }
        
        pipeline.addLast("httpServerCodec", new HttpServerCodec());
        pipeline.addLast("httpHandler", new HttpServerHandler(ticketDB));

    }
}