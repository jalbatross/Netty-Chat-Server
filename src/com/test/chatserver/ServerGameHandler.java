package com.test.chatserver;

import java.util.List;
import java.util.Stack;

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

public class ServerGameHandler extends ChannelInboundHandlerAdapter {
    private final RPS game;
    private final NamedChannelGroup gameLobby;
    
    public ServerGameHandler(RPS game, NamedChannelGroup lobby) throws Exception {
        if (lobby.size() == 0 || game == null) {
            throw new NullPointerException();
        }
        
        this.game = game;
        gameLobby = lobby;
        
    }
    
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        TimeChatMessage alert = new TimeChatMessage("Server", "You are in an rps game!");
        ByteBuf buf = Unpooled.copiedBuffer(FlatBuffersCodec.chatToByteBuffer(alert));
        ctx.channel().writeAndFlush(new BinaryWebSocketFrame(buf));
        
        TimeChatMessage gameState = new TimeChatMessage("Server", game.gameState());
        buf = Unpooled.copiedBuffer(FlatBuffersCodec.chatToByteBuffer(gameState));
        ctx.channel().writeAndFlush(new BinaryWebSocketFrame(buf));
    }
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof TextWebSocketFrame)) {
            return;
        }
        String command = ((TextWebSocketFrame) msg).text();
        if (!command.contentEquals("rock") && !command.contentEquals("paper") && !command.contentEquals("scissors")) {
            return;
        }
        System.out.println("[ServerGameHandler] Received command from: " + gameLobby.getUsernameFromChannel(ctx.channel()));
        game.processChoice(command, gameLobby.getUsernameFromChannel(ctx.channel()));
        System.out.println("[ServerGameHandler] Processed game choice");
        
        if(game.readyToDeclare()) {
            TimeChatMessage winner = new TimeChatMessage("Server", game.declareWinner());
            for (Channel ch : gameLobby) {
                
                ByteBuf buf = Unpooled.copiedBuffer(FlatBuffersCodec.chatToByteBuffer(winner));
                
                ch.writeAndFlush(new BinaryWebSocketFrame(buf));
                System.out.println("[ServerGameHandler] Sent message to channel: " + ch.id());
                ch.pipeline().removeLast();
                System.out.println("[ServerGameHandler] Removed " + gameLobby.getUsernameFromChannel(ch));
            }
            
        }
    }
    
}
