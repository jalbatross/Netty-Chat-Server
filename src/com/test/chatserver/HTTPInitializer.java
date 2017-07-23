package com.test.chatserver;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.ssl.SslContext;

public class HTTPInitializer extends ChannelInitializer<SocketChannel> {
	private Map<String,TimeChatMessage> ticketDB;
	private List<NamedChannelGroup> lobbies;
	private ChannelGroup channels;
	
	private final SslContext sslCtx;
	public HTTPInitializer (SslContext sslCtx) {
		this.sslCtx = sslCtx;
		
	}
	
    public HTTPInitializer(Map<String, TimeChatMessage> ticketDB) {
        this.ticketDB = ticketDB;
        sslCtx = null;
    }

    public HTTPInitializer(Map<String, TimeChatMessage> ticketDB, List<NamedChannelGroup> lobbies,
            ChannelGroup allChannels) {
        this.ticketDB = ticketDB;
        this.channels = allChannels;
        this.lobbies = lobbies;
        
        sslCtx = null;
        // TODO Auto-generated constructor stub
    }

    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();
        if (sslCtx != null) {
            pipeline.addLast(sslCtx.newHandler(socketChannel.alloc()));
        }
        
        pipeline.addLast("httpServerCodec", new HttpServerCodec());
        pipeline.addLast("httpHandler", new HttpServerHandler(ticketDB, lobbies, channels));

    }
}