package com.test.chatserver;

import java.util.List;
import java.util.Stack;

import game.GameType;
import game.RPS;
import game.ServerGame;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;

public class ServerRPSHandler extends ChannelInboundHandlerAdapter {
    private final RPS game;
    private final GameLobby lobby;
    
    public ServerRPSHandler(RPS game, GameLobby lobby, GameType type) 
            throws Exception {
        if (lobby.size() == 0 || game == null || type == null) {
            throw new NullPointerException();
        }
        
        
        this.game = game;
        this.lobby = lobby;
        
    }
    
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        //TODO: Notify connected users that handler was added
        
        //TODO: Send each connected user an instance of FlatBuffers serialized
        //      RPS Game
        //TODO: Set timer for RPS to emit decision after N seconds.
    }
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof Schema.GameUpdate)) {
            return;
        }
        //TODO: Process GameUpdate from Channel. Pass the byte[] into RPS processAction
        //      method, update the RPS game accordingly.
        //TODO: Close connection on exception - malformed data
        
    }
    
}
