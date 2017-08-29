package com.test.chatserver;

import java.nio.ByteBuffer;
import java.util.ArrayList;
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

/**
 * ServerRPSHandler is a ChannelInboundHandlerAdapter designed to 
 * appropriately handle a user's interaction during an RPS game with
 * another user.
 * 
 * It requires a reference to the RPS game, the gameLobby of all the
 * users who wanted to play in the RPS game, and a String username
 * which corresponds to the channel that ServerRPSHandler will be
 * directly interacting with. Its main purpose is handling each user's
 * choice of rock, paper, or scissors during the game and updating the
 * game object accordingly.
 * 
 * In this implementation, when the handler is added it creates a
 * scheduled update task which periodically sends updates to each connected
 * client at UPDATE_INTERVAL millisecond intervals. 
 * 
 * @author jalbatross (Joey Alabno)
 *
 */

public class ServerRPSHandler extends ChannelInboundHandlerAdapter {
    private final RPS game;
    private final GameLobby lobby;
    private final String username;
    
    public static final int UPDATE_INTERVAL = 5000;
    
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
        
        t = ctx.channel().eventLoop().scheduleAtFixedRate(new ServerGameUpdateTask(game, ctx.channel(), this), 
                UPDATE_INTERVAL, 
                UPDATE_INTERVAL, 
                TimeUnit.MILLISECONDS);
    }
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof Schema.GameUpdate)) {
            return;
        }
        System.out.println("[ServerRPSHandler] Got game action");

        Schema.GameUpdate update = (Schema.GameUpdate) msg;
        
        //Player concedes
        if (update.update(0) == 3) {
            System.out.println("[ServerRPSHandler] Got concession from " + username);
            //Stop updates immediately
            t.cancel(false);
            
            //Get users from GameLobby
            ArrayList<String> usersList = lobby.getUsers();
            
            //Get opponent
            String opponent;
            if (usersList.get(0).contentEquals(username)) {
                opponent = usersList.get(1);
            }
            else {
                opponent = usersList.get(0);
            }
            
            byte[] concedeByte = {-1};
            ByteBuffer winUpdateBuf = FlatBuffersCodec.gameUpdateToByteBuffer(concedeByte);
            ByteBuf buf = Unpooled.copiedBuffer(winUpdateBuf);
            
            //Tell opponent they won
            lobby.getChannel(opponent).writeAndFlush(new BinaryWebSocketFrame(buf));
            
            //Destroy this
            ctx.channel().pipeline().remove(this);
            lobby.getChannel(opponent).pipeline().remove("rpsGame");
            
            return;
            
        }
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
        t.cancel(false);
        super.channelInactive(ctx);
        
    }
    
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        t.cancel(false);
        super.handlerRemoved(ctx);
        
    }
    
    
}
