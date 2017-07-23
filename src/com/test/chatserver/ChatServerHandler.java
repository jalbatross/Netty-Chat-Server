package com.test.chatserver;



import com.google.gson.Gson;
import com.google.gson.JsonObject;
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
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Stack;

/**
 * Handles a server-side chat channel. Tail of the pipeline for incoming data.
 */
public class ChatServerHandler extends ChannelInboundHandlerAdapter { // (1
	
    private final List<NamedChannelGroup> lobbies;
    private final ChannelGroup channels;
    private final String username;
    private Channel ch;

    private NamedChannelGroup currentLobby;
    
    public ChatServerHandler(ChannelGroup group, String username) {
        channels = group;
        this.username = username;
        lobbies = null;
        ch = null;
    }
    
    public ChatServerHandler(ChannelHandlerContext ctx, String username, List<NamedChannelGroup> lobbies, ChannelGroup channels) {
        this.username = username;
        this.lobbies = lobbies;
        this.channels = channels;
        this.ch = ctx.channel();
        
    }

    @Override public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        
        this.channels.add(ch);

        TimeChatMessage timeMessage = new TimeChatMessage("Admin", username + " connected!");
        TextWebSocketFrame JsonMessage = new TextWebSocketFrame(new Gson().toJson(timeMessage));
        channels.writeAndFlush(JsonMessage);
        
        for (int i = 0; i < lobbies.size(); i++) {
            if (lobbies.get(i).size() < ChatServer.LOBBY_SIZE) {
                lobbies.get(i).add(ch);
                currentLobby = lobbies.get(i);
                lobbies.get(i).addUser(username);
                return;
            }
        }
        
        ctx.close();
        System.out.println("[ChatServerHandler] Server was full");
        
    }
    
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) 
	{
		System.out.println("\n[ChatServerHandler] channelRead called!");
		
		if ((msg instanceof TextWebSocketFrame)) {
			System.out.println("[ChatServerHandler] received TextWebSocketFrame!\n------");	
			String strMsg = ((TextWebSocketFrame) msg).text();
			
			if (strMsg.equalsIgnoreCase("/lobbies")) {
			    
			    String[] lobbyList = new String[lobbies.size()];
			    for (int i = 0; i < lobbies.size(); i++) {
			        lobbyList[i]=lobbies.get(i).name();
			    }
	            ByteBuffer lobbyData = FlatBuffersCodec.listToByteBuffer("lobbies", lobbyList);
	            ByteBuf lobbyBuf = Unpooled.copiedBuffer(lobbyData);
	            ch.writeAndFlush(new BinaryWebSocketFrame(lobbyBuf));
	            
	            return;
			}
			if (strMsg.equalsIgnoreCase("/lobby")) {

			    TimeChatMessage timeMessage = new TimeChatMessage("Server", currentLobby.name());
	            
	            ByteBuffer data = FlatBuffersCodec.chatToByteBuffer(timeMessage);
	            ByteBuf buf = Unpooled.copiedBuffer(data);
	            
	            ch.writeAndFlush(new BinaryWebSocketFrame(buf));
	            
	            return;
			}
			if (strMsg.startsWith("/connect ")) {
			    //get lobby name
			    String lobbyName = strMsg.substring(9);
			    if (lobbyName.contentEquals(currentLobby.name())) {
			        return;
			    }
			    //make sure it's valid
			    for (int i = 0; i < lobbies.size(); i++) {
			        if (lobbyName.equals(lobbies.get(i).name()) && 
			            lobbies.get(i).size() < ChatServer.LOBBY_SIZE &&
			            !lobbies.get(i).contains(ch) && 
			            !lobbies.get(i).containsUser(username)) {
			        
			            currentLobby.remove(ch);
			            currentLobby.removeUser(username);
			            
			            lobbies.get(i).add(ch);
			            lobbies.get(i).addUser(username);
			            currentLobby = lobbies.get(i);
			            
			            TimeChatMessage timeMessage = new TimeChatMessage("Server", "Switched to " + currentLobby.name());
		                
		                ByteBuffer data = FlatBuffersCodec.chatToByteBuffer(timeMessage);
		                ByteBuf buf = Unpooled.copiedBuffer(data);
		                
		                ch.writeAndFlush(new BinaryWebSocketFrame(buf));
		                
		                String[] userList = currentLobby.getUsers().toArray(new String[currentLobby.numUsers()]);
		                ByteBuffer userData = FlatBuffersCodec.listToByteBuffer("users", userList);
		                ByteBuf userBuf = Unpooled.copiedBuffer(userData);
		                ch.writeAndFlush(new BinaryWebSocketFrame(userBuf));
		                
			            return;
			        }
			    }
			    
			    return;
			}
            //Stamp message with current time
            TimeChatMessage timeMessage = new TimeChatMessage(username, strMsg);
            
            ByteBuffer data = FlatBuffersCodec.chatToByteBuffer(timeMessage);
            ByteBuf buf = Unpooled.copiedBuffer(data);
            
            
            
            currentLobby.writeAndFlush(new BinaryWebSocketFrame(buf));
            
		}
		else if (msg instanceof ByteBuf) {
		    System.out.println("[ChatServerHandler] Received ByteBuf");
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
		    
		    final String dcMsg = username + " disconnected";
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