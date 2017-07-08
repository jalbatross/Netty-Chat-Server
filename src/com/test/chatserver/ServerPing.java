package com.test.chatserver;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

public class ServerPing extends ChannelInboundHandlerAdapter {

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        System.out.println("pinger added");
        ctx.writeAndFlush(new PingWebSocketFrame());
        ctx.writeAndFlush(new TextWebSocketFrame("ping"));

    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            if (e.state() == IdleState.READER_IDLE) {
                System.out.println("TIMEOUT for user: " + ctx.channel().toString());
                ctx.close();
            } 
            else if (e.state() == IdleState.WRITER_IDLE) {
                //send ping
                ctx.writeAndFlush(new PingWebSocketFrame());
                ctx.writeAndFlush(new TextWebSocketFrame("ping"));
            }
        }
    }

}
