package com.test.chatserver;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

public class ChatServerMessageEncoder extends ChannelOutboundHandlerAdapter implements ChannelOutboundHandler {
	@Override
	public void read(ChannelHandlerContext ctx) {
		System.out.println("[ChatServerMessageEncoder] OutboundHandler read called!");
		ctx.read();
		/*
		if ((ctx. instanceof TextWebSocketFrame)) {
			System.out.println("ChatServerHandler received TextWebSocketFrame!");
			//send the frame downstream to client
			
		}
		else {
			System.out.println("ChatServerHandler received unknown type of frame!");
		}*/
	}
	

}
