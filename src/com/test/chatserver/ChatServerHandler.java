package com.test.chatserver;



import io.netty.buffer.ByteBuf;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;

/**
 * Handles a server-side chat channel. Only accepts Strings.
 */
public class ChatServerHandler extends SimpleChannelInboundHandler<String> { // (1)
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, String str) throws Exception {
		String finalMessage = new String("Name: " + str);
		ctx.writeAndFlush(finalMessage);
		
	}

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) { // (4)
        // Close the connection when an exception is raised.
        cause.printStackTrace();
        ctx.close();
    }


}