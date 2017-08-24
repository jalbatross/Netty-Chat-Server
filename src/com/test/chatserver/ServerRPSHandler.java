package com.test.chatserver;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.TimeUnit;

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
import io.netty.util.concurrent.ScheduledFuture;

public class ServerRPSHandler extends ChannelInboundHandlerAdapter {
    private final RPS game;
    private final GameLobby lobby;
    private final String username;
    
    private ScheduledFuture<?> t;
    
    public ServerRPSHandler(RPS game, GameLobby lobby, String user) throws Exception {
        if (game == null || lobby == null) {
            throw new NullPointerException();
        }
        System.out.println("Lobby size: " + lobby.size());
        System.out.println("Users in lobby: " + lobby.getUsers());
        if (lobby.size() > RPS.MAX_PLAYERS || lobby.size() < RPS.MIN_PLAYERS) {
            throw new IndexOutOfBoundsException();
        }
        
        this.game = game;
        this.lobby = lobby;
        this.username = user;
    }
    
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        super.handlerAdded(ctx);
        ByteBuffer data = FlatBuffersCodec.gameToByteBuffer(game);
        ByteBuf buf = Unpooled.copiedBuffer(data);
        
        ctx.channel().writeAndFlush(new BinaryWebSocketFrame(buf));
        
        t = ctx.channel().eventLoop().scheduleAtFixedRate(new ServerGameUpdateTask(game, ctx.channel()), 
                5000, 
                5000, 
                TimeUnit.MILLISECONDS);
        
    }
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof Schema.GameUpdate)) {
            return;
        }
        System.out.println("[ServerRPSHandler] Got game action");
        //TODO: Process GameUpdate from Channel. Pass the byte[] into RPS processAction
        //      method, update the RPS game accordingly.
        Schema.GameUpdate update = (Schema.GameUpdate) msg;
        
        System.out.println("update bytes len: " + update.updateLength());
        byte[] updateBytes = new byte[update.updateLength()];
        System.out.println("---bytes----");
        for (int i = 0; i < updateBytes.length; i++) {
            updateBytes[i] = update.update(i);
            System.out.print(updateBytes[i]);
        }
        System.out.println();
        
        System.out.println("[ServerRPSHandler] Processing bytes: " + updateBytes.toString());
        game.processAction(updateBytes, username);
        //TODO: Close connection on exception - malformed data
        
    }
    
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        t.cancel(false);
    }
    
    
}
