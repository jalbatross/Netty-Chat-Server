package com.test.chatserver;

import java.util.Stack;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;

public class ServerGameHandler extends ChannelInboundHandlerAdapter {
    private final ServerGame game;
    private final ChannelGroup connectedUsers;
    
    public ServerGameHandler(ServerGame game, Stack<Channel> users) throws Exception {
        if (users.size() <= 0 || users == null || game == null) {
            throw new NullPointerException();
        }
        
        this.game = game;
        connectedUsers = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
        while (!users.isEmpty()) {
            connectedUsers.add(users.pop());
        }
        
    }
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof BinaryWebSocketFrame)) {
            return;
        }
    }
    
}
