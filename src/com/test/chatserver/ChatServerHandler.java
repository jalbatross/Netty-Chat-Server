package com.test.chatserver;



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

/**
 * Handles a server-side chat channel. Only accepts Strings.
 */
public class ChatServerHandler extends ChannelInboundHandlerAdapter { // (1
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) 
	{
		System.out.println("\nChatServerHandler channelRead called!");
		
		if ((msg instanceof TextWebSocketFrame)) {
			System.out.println("ChatServerHandler received TextWebSocketFrame!");
			
			//send message back to client (all clients?)
			ChannelFuture cf = ctx.write(msg);
			
			//this is failing; how does the program still manage to flush the msg back along the pipeline?
			//TODO: Test flushing with channel future if possible; can flush anything but ONLY write bytebuf?
			//TODO: Figure out why msg is null; it should be a TextWebSocketFrame?
			//Another hypothesis is that the msg doesn't actually get here - it's earlier along the pipeline
			//and simply flushed back!!
			//TODO: Check that the TextWebSocketFrame is actually getting here by outputting to console
			//      the TextWebSocketFrame WHEN the channelRead function is called.
			if (!cf.isSuccess()){ 
				System.out.println("ChatServerHandler write failed because" + cf.cause());
			}
			ctx.writeAndFlush(msg);
			
		}
		else {
			System.out.println("ChatServerHandler received unknown type of frame!");
		}
		
		
	}
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) { // (4)
        // Close the connection when an exception is raised.
        cause.printStackTrace();
        ctx.close();
    }


}