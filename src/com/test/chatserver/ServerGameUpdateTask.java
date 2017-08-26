package com.test.chatserver;

import java.nio.ByteBuffer;
import java.util.TimerTask;

import game.RPS;
import game.ServerGame;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;

public class ServerGameUpdateTask extends TimerTask {
    
    private final ServerGame game;
    private final Channel ch;
    private final ChannelInboundHandlerAdapter handler;
    
    public ServerGameUpdateTask(ServerGame game, Channel ch, ChannelInboundHandlerAdapter handler) {
        if (game == null || ch == null) {
            throw new NullPointerException();
        }
        this.game = game;
        this.ch = ch;
        this.handler = handler;
    }

    @Override
    public void run() {
        ByteBuffer updateBuf = null;
        
        try {
            updateBuf = FlatBuffersCodec.gameUpdateToByteBuffer(game);
            
        }
        catch (Exception e) {
            System.out.println("[ServerTask] Encountered error while making game update");
        }
        
        if (updateBuf == null) {
            System.out.println("[ServerGameTask] Bad update buf");
            return;
        }
        
        ByteBuf buf = Unpooled.copiedBuffer(updateBuf);
        ch.writeAndFlush(new BinaryWebSocketFrame(buf));
        System.out.println("[GameTask] sent update to channel: " + ch.toString());
        if (game instanceof RPS) {
            RPS rpsGame = (RPS) game;
            if (rpsGame.numUpdates() >= rpsGame.numPlayers()) {
                rpsGame.resetState();
            }
        }
        
        if (game.gameOver()) {
            System.out.println("[GameTask] Game over.");
            //Send completed game to clients so that they know the game is over
            //Client handles game overs according to their whim
            try {
                ByteBuffer completedGameData = FlatBuffersCodec.gameToByteBuffer(game);
                ByteBuf gameBuf = Unpooled.copiedBuffer(completedGameData);
                ch.writeAndFlush(new BinaryWebSocketFrame(gameBuf));
            }
            catch (Exception e) {
                System.out.println("[GameTask (rps)] Couldn't create game over game to send to clients.");
                e.printStackTrace();
            }
            
            //Remove the server game handler from the pipeline since the game is completed
            ch.pipeline().remove(handler);
        }
          
    }

}
