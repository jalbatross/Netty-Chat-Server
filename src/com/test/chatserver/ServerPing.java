package com.test.chatserver;

import java.time.Instant;
import java.util.Date;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

public class ServerPing extends ChannelInboundHandlerAdapter {
    private String user;
    
    public ServerPing(String user) {
        this.user = user;
    }
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        //System.out.println("[ServerPing] Pinger added for: " + user);
        ctx.channel().writeAndFlush(new PingWebSocketFrame());

    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            if (e.state() == IdleState.READER_IDLE ||
                e.state() == IdleState.WRITER_IDLE) {
              //send ping
                ctx.channel().writeAndFlush(new PingWebSocketFrame());
            } 
            else if (e.state() == IdleState.ALL_IDLE) {
               // System.out.println("  [ServerPing] TIMEOUT for user: " + user +
               //         " at time: " + new Date(Instant.now().toEpochMilli()));
                ctx.close();
            }
        }
        
        //System.out.println("***End userEventTriggered***\n");
    }
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //System.out.println("[ServerPing] Reading for user " + user + " at " + new Date(Instant.now().toEpochMilli()) );
        if (!(msg instanceof PongWebSocketFrame)) {
            System.out.println("[ServerPing] Passing non-pong frame");
            ctx.fireChannelRead(msg);
        }
    }

}
