package com.test.chatserver;

import java.util.Arrays;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpMessage;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

public class WebSocketHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof WebSocketFrame) {
            System.out.println("This is a WebSocket frame");
            System.out.println("Client Channel : " + ctx.channel());
            if (msg instanceof BinaryWebSocketFrame) {
                System.out.println("BinaryWebSocketFrame Received : ");
                //System.out.println( ((BinaryWebSocketFrame) msg).content().array().toString() );
                byte[] bytes = new byte[14];
                ((BinaryWebSocketFrame) msg).content().readBytes(bytes);
                short sessionID = (short)( ((bytes[0]&0xFF)<<8) | (bytes[1]&0xFF) );
                int sender = ((bytes[2] & 0xff) << 24) | ((bytes[3] & 0xff) << 16) |
                        ((bytes[4] & 0xff) << 8)  | (bytes[5] & 0xff);
                int receiver = ((bytes[6] & 0xff) << 24) | ((bytes[7] & 0xff) << 16) |
                        ((bytes[8] & 0xff) << 8)  | (bytes[9] & 0xff);
                int val = ((bytes[10] & 0xff) << 24) | ((bytes[11] & 0xff) << 16) |
                        ((bytes[12] & 0xff) << 8)  | (bytes[13] & 0xff);
                
                System.out.println("Received: "+ sessionID + " " + sender +  " " + receiver +  " " + val);
            } 
            else if (msg instanceof TextWebSocketFrame) {
                System.out.println("TextWebSocketFrame Received");
                //Send textwebsocketframe downstream to ChatServerHandler
                ctx.fireChannelRead(msg);
                
            } 
            else if (msg instanceof PingWebSocketFrame) {
                System.out.println("PingWebSocketFrame Received : ");
                System.out.println( ((PingWebSocketFrame) msg).content());
            } 
            else if (msg instanceof PongWebSocketFrame) {
                System.out.println("PongWebSocketFrame Received : ");
                System.out.println( ((PongWebSocketFrame) msg).content() );
            } 
            else if (msg instanceof CloseWebSocketFrame) {
                System.out.println("CloseWebSocketFrame Received : ");
                System.out.println("ReasonText :" + ((CloseWebSocketFrame) msg).reasonText() );
                System.out.println("StatusCode : " + ((CloseWebSocketFrame) msg).statusCode() );
            } 
            else {
                System.out.println("Unsupported WebSocketFrame");
            }
        }

    }
}