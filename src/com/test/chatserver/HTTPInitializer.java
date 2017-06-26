package com.test.chatserver;

import java.util.ArrayList;
import java.util.HashSet;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.ssl.SslContext;

public class HTTPInitializer extends ChannelInitializer<SocketChannel> {
	
	private final SslContext sslCtx;
	private ChannelGroup allUsers;
	private ArrayList<ChannelGroup> lobbies;
	public HTTPInitializer (SslContext sslCtx, ChannelGroup group, 
	        ArrayList<ChannelGroup> lobbies) {
		this.sslCtx = sslCtx;
		allUsers = group;
		this.lobbies = lobbies;
		
	}
	
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();
        if (sslCtx != null) {
            pipeline.addLast(sslCtx.newHandler(socketChannel.alloc()));
        }
        
        pipeline.addLast("httpServerCodec", new HttpServerCodec());
        pipeline.addLast("httpHandler", new HttpServerHandler(allUsers, lobbies));

    }
}