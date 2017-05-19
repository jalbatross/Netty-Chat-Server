package com.test.chatserver;



import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;

/**
 * Handles a server-side chat channel. Only accepts Strings.
 */
public class ChatServerHandler extends ChannelInboundHandlerAdapter { // (1
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
	{
		//receive the message as a bytebuf
		ByteBuf in = (ByteBuf) msg;
		
		//convert it to a string
		String message = in.toString(CharsetUtil.UTF_8);
		
		ChannelFuture cf = ctx.write(Unpooled.copiedBuffer(message,CharsetUtil.UTF_8));
		ctx.flush();
		if (!cf.isSuccess()) {
			System.out.println(cf.cause());
		}
		
	}
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) { // (4)
        // Close the connection when an exception is raised.
        cause.printStackTrace();
        ctx.close();
    }


}