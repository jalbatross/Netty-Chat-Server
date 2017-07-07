package com.test.chatserver;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

public class ServerPing extends ChannelInboundHandlerAdapter {
    private static final int timeoutMs = 5000;
    private long lastPingTime = 0;

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        System.out.println("pinger added");
        lastPingTime = System.currentTimeMillis();
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
                ctx.writeAndFlush(new PingWebSocketFrame());
                ctx.writeAndFlush(new TextWebSocketFrame("pinging"));
            }
        }
    }

}
