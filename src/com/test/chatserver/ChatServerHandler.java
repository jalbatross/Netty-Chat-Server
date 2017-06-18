package com.test.chatserver;



import com.google.gson.Gson;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

/**
 * Handles a server-side chat channel. Tail of the pipeline for incoming data.
 */
public class ChatServerHandler extends ChannelInboundHandlerAdapter { // (1
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) 
	{
		System.out.println("\n[ChatServerHandler] channelRead called!");
		
		if ((msg instanceof TextWebSocketFrame)) {
			System.out.println("[ChatServerHandler] received TextWebSocketFrame!");
			
			System.out.println("[ChatServerHandler] TextWebSocketFrame was: " + ((TextWebSocketFrame)msg).text());
			
			
		}
		else {
			System.out.println("[ChatServerHandler] received unknown type of frame!");
		}
			
		//Encapsulate message into Json
		String timeStamp = new Timestamp(System.currentTimeMillis()).toString();
		String author = "User";
		String messageText = ( (TextWebSocketFrame) msg).text();
		ChatMessage message = new ChatMessage(timeStamp, author, messageText);
		
		Gson gson = new Gson();
		String json = gson.toJson(message);
		System.out.println("Json:" + json);
		
		//Send it back as a Json
		TextWebSocketFrame JsonMessage = new TextWebSocketFrame(json);
		ctx.writeAndFlush(JsonMessage);
		
	}
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) { // (4)
        // Close the connection when an exception is raised.
        cause.printStackTrace();
        ctx.close();
    }


}