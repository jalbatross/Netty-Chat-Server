package com.test.chatserver;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

public class ChatServerMessageEncoder extends ChannelOutboundHandlerAdapter {
	@Override
	public void read(ChannelHandlerContext ctx)
	{
		System.out.println("OutboundHandler channelRead called!");
		
		/*
		if ((msg instanceof TextWebSocketFrame)) {
			System.out.println("ChatServerHandler received TextWebSocketFrame!");
			//send the frame downstream to client
			
		}
		else {
			System.out.println("ChatServerHandler received unknown type of frame!");
		}*/
		
		
	}
}
