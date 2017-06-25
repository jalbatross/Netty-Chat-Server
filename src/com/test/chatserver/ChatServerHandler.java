package com.test.chatserver;



import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.nio.ByteBuffer;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

/**
 * Handles a server-side chat channel. Tail of the pipeline for incoming data.
 */
public class ChatServerHandler extends ChannelInboundHandlerAdapter { // (1
	
    final ChannelGroup channels;
    final String username;
    
    public ChatServerHandler(ChannelGroup group, String username) {
        channels = group;
        this.username = username;
    }
    
    @Override public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        Gson gson = new Gson();
        TimeChatMessage timeMessage = new TimeChatMessage("Admin", username + " connected!");
        TextWebSocketFrame JsonMessage = new TextWebSocketFrame(new Gson().toJson(timeMessage));
        channels.writeAndFlush(JsonMessage);
    }
    
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) 
	{
		System.out.println("\n[ChatServerHandler] channelRead called!");
		
		if ((msg instanceof TextWebSocketFrame)) {
			System.out.println("[ChatServerHandler] received TextWebSocketFrame!\n------");	
			TextWebSocketFrame frameMsg = (TextWebSocketFrame) msg;
			
			try {
			    new JsonParser().parse(frameMsg.text());
			    
	            Gson gson = new Gson();

	            ChatMessage message = gson.fromJson(frameMsg.text(), ChatMessage.class);
	            
	            //Stamp message with current time
	            TimeChatMessage timeMessage = new TimeChatMessage(message);
	            
	            //Send it back to every client as a Json
	            TextWebSocketFrame JsonMessage = new TextWebSocketFrame(new Gson().toJson(timeMessage));
	            channels.writeAndFlush(JsonMessage);
			}
			
			
	        catch (JsonParseException e) {
                System.out.println("[ChatServerHandler] Received nonJSON from client.");
            }
			
		}
		else if (msg instanceof ByteBuf) {
		    ByteBuf buf = (ByteBuf) msg;
		    
		    //Get the lobby id
		    short lobbyID = buf.getShort(0);
		    
		    //Get the last 12 bytes and put it into a byte array
		    //First two bytes are used for lobby identification only
		    byte[] slice = new byte[12];
		    buf.slice(2, 12).readBytes(slice);
		    System.out.println("Slice: " + Arrays.toString(slice));
		    
		    System.out.println("[ChatServerHandler] Received ByteBuf: " +
		    "Lobby ID: " + lobbyID + "\n" +
		    "Sender: " + buf.getInt(2) + "\n" +
		    "Receiver: " + buf.getInt(6) + "\n" +
		    "Val: " + buf.getInt(10));
		    
		    //Broadcast the byte array to everyone with same channel (lobby id)
		    //For now this is just broadcast to all users
		    ByteBuf myBuf = Unpooled.copiedBuffer(slice);
            WebSocketFrame deltaArr = new BinaryWebSocketFrame(myBuf);
            channels.writeAndFlush(deltaArr);
		}
		else if (msg instanceof CloseWebSocketFrame) {
		    System.out.println("[ChatServerHandler] Received request to close connection");
		    System.out.println("[ChatServerHandler] Channel grp before removal: " + channels.toString());
		    
		    String dcMsg = username + " disconnected";
		    //try to close connection
            ChannelFuture cf = ctx.channel().close();
            cf.addListener(new ChannelFutureListener() {
                public void operationComplete(ChannelFuture future) {
                    //Broadcast removal of user from channel group to all channels connected
                    if (!channels.isEmpty()) {
                        TextWebSocketFrame dcFrame = new TextWebSocketFrame(dcMsg);
                        channels.writeAndFlush(dcFrame);
                    }
                }
            });
		}
		else {
			System.out.println("[ChatServerHandler] received unknown type of frame!");
		}
			
		
		
		
	}
	
	
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) { // (4)
        // Close the connection when an exception is raised.
        cause.printStackTrace();
        ctx.close();
    }


}