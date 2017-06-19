package com.test.chatserver;



import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

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
			TextWebSocketFrame frameMsg = (TextWebSocketFrame) msg;
			
			try {
			    new JsonParser().parse(frameMsg.text());
			    
			    //Encapsulate message into Json
	            String timeStamp = new Timestamp(System.currentTimeMillis()).toString();
	            
	            Gson gson = new Gson();

	            ChatMessage message = gson.fromJson(frameMsg.text(), ChatMessage.class);
	            message.setStamp(timeStamp);
	            
	            //Send it back as a Json
	            TextWebSocketFrame JsonMessage = new TextWebSocketFrame(new Gson().toJson(message));
	            ctx.writeAndFlush(JsonMessage);}
			
	        catch (JsonParseException e) {
                System.out.println("[ChatServerHandler] Received nonJSON from client.");
            }
			
		}
		else {
			System.out.println("[ChatServerHandler] received unknown type of frame!");
		}
			
		
		
		
	}
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) { // (4)
        // Close the connection when an exception is raised.
        cause.printStackTrace();
        ctx.close();
    }


}