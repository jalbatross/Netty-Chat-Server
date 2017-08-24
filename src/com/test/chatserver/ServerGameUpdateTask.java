package com.test.chatserver;

import java.nio.ByteBuffer;
import java.util.TimerTask;

import game.RPS;
import game.ServerGame;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;

public class ServerGameUpdateTask extends TimerTask {
    
    private final ServerGame game;
    private final Channel ch;
    
    public ServerGameUpdateTask(ServerGame game, Channel ch) {
        if (game == null || ch == null) {
            throw new NullPointerException();
        }
        this.game = game;
        this.ch = ch;
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
            rpsGame.resetState();
        }
          
    }

}
