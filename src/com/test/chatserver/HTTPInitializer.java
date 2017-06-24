package com.test.chatserver;

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
	private HashSet<String> usernames;
	public HTTPInitializer (SslContext sslCtx, ChannelGroup group, HashSet<String> names) {
		this.sslCtx = sslCtx;
		allUsers = group;
		usernames = names;
		
	}
	
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();
        if (sslCtx != null) {
            pipeline.addLast(sslCtx.newHandler(socketChannel.alloc()));
        }
        
        pipeline.addLast("httpServerCodec", new HttpServerCodec());
        pipeline.addLast("httpHandler", new HttpServerHandler(allUsers, usernames));

    }
}