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
		System.out.println("OutboundHandler read called!");
		
		/*
		if ((msg instanceof TextWebSocketFrame)) {
			System.out.println("ChatServerHandler received TextWebSocketFrame!");
			//send the frame downstream to client
			
		}
		else {
			System.out.println("ChatServerHandler received unknown type of frame!");
		}*/
		ctx.read();
		ChannelFuture cf = ctx.write("Hello!");
		
		if (!cf.isSuccess()) {
			System.out.println("failed to alter outgoing message because " + cf.cause());
		}
		else {
			ctx.flush();
		}
			
		
	}
	
	/*
	@Override
	 public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
		System.out.println("calling write");
		ctx.write(msg, promise);
		ctx.flush();
	}*/

}
