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