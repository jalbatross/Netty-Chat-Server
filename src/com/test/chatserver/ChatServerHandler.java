package com.test.chatserver;



import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import Schema.Chat;
import Schema.Message;
import game.RPS;
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
    private final ChannelGroup globalChannelGroup;
    private final String username;
    private Channel ch;
    private boolean init = false;

    private NamedChannelGroup currentLobby;
    private List<NamedChannelGroup> gameLobbies;
        
    public ChatServerHandler(ChannelHandlerContext ctx, String username, List<NamedChannelGroup> lobbies, ChannelGroup channels) {
        this.username = username;
        this.lobbies = lobbies;
        this.globalChannelGroup = channels;
        this.ch = ctx.channel();
        
    }
    
    public ChatServerHandler(ChannelHandlerContext ctx, String username, List<NamedChannelGroup> lobbies,
            ChannelGroup allChannels, List<NamedChannelGroup> gameLobbies) {
        this.username = username;
        this.lobbies = lobbies;
        this.globalChannelGroup = allChannels;
        this.ch = ctx.channel();
        this.gameLobbies = gameLobbies;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        System.out.println("[ChatServerHandler] Disconnecting user: " + username);
        //remove username from current channelGroup
        currentLobby.remove(username);
        System.out.println("[ChatServerHandler] Num users in lobby: " + currentLobby.size());
        
    }
    
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception 
	{
		System.out.println("\n[ChatServerHandler] channelRead called!");
		
		if (!init) {
		    //System.out.println("[ChatServerHandler] Added new instance of handler to " + username);
	        
	        addChannelToGlobalGroup(this.ch);
	        if (!addUserToFirstLobby(this.ch, username)) {
	            //System.out.println("Failed to add user to lobby");
	            ctx.close();
	            return;
	        }
	        
	        init = true;	        
	        return;
		}
		
		if ((msg instanceof TextWebSocketFrame)) {
			System.out.println("[ChatServerHandler] received TextWebSocketFrame!\n------");	
			String strMsg = ((TextWebSocketFrame) msg).text();
			
			if (strMsg.equalsIgnoreCase("/lobbies")) {
			    
			    // sends a String[] of all lobbies in the server in the following format:
			    // lobbyName,lobbySize/lobbyCapacity
			    // for example, for the lobby myLobby with 2 users out of 10 capacity:
			    // myLobby,2/10
			    String[] lobbyList = new String[lobbies.size()];
			    for (int i = 0; i < lobbies.size(); i++) {
			        lobbyList[i]=lobbies.get(i).name();
			        lobbyList[i] += "," + lobbies.get(i).numUsers() + "/" + ChatServer.LOBBY_SIZE; 
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
			            
			            currentLobby = lobbies.get(i);
			            currentLobby.add(ch, username);
			            
			            TimeChatMessage timeMessage = new TimeChatMessage("Server", "Switched to " + currentLobby.name());
		                
		                ByteBuffer data = FlatBuffersCodec.chatToByteBuffer(timeMessage);
		                ByteBuf buf = Unpooled.copiedBuffer(data);
		                
		                ch.writeAndFlush(new BinaryWebSocketFrame(buf));
		                
		                ArrayList<String> users = currentLobby.getUsers();
	                    users.add(0, currentLobby.name());
	                    System.out.println(users.toString());
	                    String[] userList = users.toArray(new String[users.size()]);
	                    
	                    //update users for all people in lobby
		                ByteBuffer userData = FlatBuffersCodec.listToByteBuffer("users", userList);
		                ByteBuf userBuf = Unpooled.copiedBuffer(userData);
		                currentLobby.writeAndFlush(new BinaryWebSocketFrame(userBuf));
		               
		                String[] lobbyList = new String[lobbies.size()];
		                for (int j = 0; j < lobbies.size(); j++) {
		                    lobbyList[j]=lobbies.get(j).name();
		                    lobbyList[j] += "," + lobbies.get(j).numUsers() + "/" + ChatServer.LOBBY_SIZE; 
		                }
		                ByteBuffer lobbyData = FlatBuffersCodec.listToByteBuffer("lobbies", lobbyList);
		                ByteBuf lobbyBuf = Unpooled.copiedBuffer(lobbyData);
		                ch.writeAndFlush(new BinaryWebSocketFrame(lobbyBuf));
		                
			            return;
			        }
			    }
			    
			    return;
			}
			else if (strMsg.contentEquals("rock") || strMsg.contentEquals("paper") || strMsg.contentEquals("scissors")) {
			    ctx.fireChannelRead(msg);
			    return;
			}
			else if (strMsg.startsWith("/play rps ")) {
			    String gameLobbyName = strMsg.substring(10);
			    if (gameLobbies.isEmpty()){
			        return;
			    }
			    
			    NamedChannelGroup gameLobby = null;
			    for (int i = 0 ; i < gameLobbies.size(); i++) {
			        if (gameLobbyName.contentEquals(gameLobbies.get(i).name())) {
			            gameLobby = gameLobbies.get(i);
			            break;
			        }
			    }
			    if (gameLobby == null) {
			        return;
			    }
			    
			    gameLobby.add(ch, username);

			    ArrayList<String> players = gameLobby.getUsers();
			    
			    RPS rpsGame = null;
			    try {
			        rpsGame = new RPS(2, players);
                }
                catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    return;
                }
			    
			    System.out.println("[ChatServerHandler] Successfully created game lobby " + gameLobby.name());
			    System.out.println("[ChatServerHandler] Users: " + gameLobby.getUsers());
			    
			    
			    for (Channel chan : gameLobby) {
			        chan.pipeline().addLast("gameHandler", new ServerGameHandler(rpsGame, gameLobby));
			    }
			    
			    gameLobbies.remove(gameLobby);
			    return;
			    
			}
			else if (strMsg.contentEquals("/play rps")) {
			    System.out.println("[ChatServerHandler] Received RPS request");
			    NamedChannelGroup rpsLobby = new NamedChannelGroup("RPS", GlobalEventExecutor.INSTANCE);
			    gameLobbies.add(rpsLobby);
			 
			    //add the person who made the game, their username, and channel map
			    rpsLobby.add(ch, username);
			    
			    System.out.println("Made new game lobby for rps named: " + rpsLobby.name());
			    System.out.println("People in lobby: " + rpsLobby.getUsers());
			    
			    return;
			}
			else if (strMsg.contentEquals("/games")) {
			    // sends a String[] of all games in the server in the following format:
                // lgameName,gameSize/gameCapacity
			    int numGames = gameLobbies.size();
                String[] gameList = new String[numGames];
                for (int i = 0; i < numGames; i++) {
                    gameList[i]=gameLobbies.get(i).name();
                    gameList[i] += "," + gameLobbies.get(i).numUsers() + "/2";
                }
                ByteBuffer gameLobbyData = FlatBuffersCodec.listToByteBuffer("games", gameList);
                ByteBuf gameBuf = Unpooled.copiedBuffer(gameLobbyData);
                ch.writeAndFlush(new BinaryWebSocketFrame(gameBuf));
                
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
            globalChannelGroup.writeAndFlush(deltaArr);
		}
		else if (msg instanceof CloseWebSocketFrame) {
		    System.out.println("[ChatServerHandler] Received request to close connection");
		    System.out.println("[ChatServerHandler] Channel grp before removal: " + globalChannelGroup.toString());
		    
		    final String dcMsg = username + " disconnected";
		    //try to close connection
            ChannelFuture cf = ctx.channel().close();
            /*
            cf.addListener(new ChannelFutureListener() {
                public void operationComplete(ChannelFuture future) {
                    //Broadcast removal of user from channel group to all channels connected
                    if (!channels.isEmpty()) {
                        TextWebSocketFrame dcFrame = new TextWebSocketFrame(dcMsg);
                        channels.writeAndFlush(dcFrame);
                    }
                }
            });
            */
		}
		else {
			System.out.println("[ChatServerHandler] received unknown type of frame!");
		}
			
		
		
		
	}
	
	/**
	 * Attempts to add aChannel to the first NamedChannelGroup (lobby) that
	 * it can find in lobbies. Returns true if aChannel and aUsername were
	 * successfully placed in one of the lobbies, false otherwise.
	 * 
	 * @param aChannel   a Channel
	 * @param aUsername  Username corresponding to aChannel
	 * @return           True if aChannel and aUsername were placed in the same
	 *                   NamedChannelGroup in lobbies, false otherwise
	 */
    private boolean addUserToFirstLobby(Channel aChannel, String aUsername) {
        for (int i = 0; i < lobbies.size(); i++) {

            if (lobbies.get(i).isFull() || lobbies.get(i).contains(aChannel) 
                    || lobbies.get(i).contains(aUsername)){
                continue;
            }
            
            if (lobbies.get(i).add(ch, username)) {
                System.out.println("[ChatServerHandler] Add success");
            }

            currentLobby = lobbies.get(i);

            //System.out.println("[ChatServerHandler] Added " + username + " to " + currentLobby.name());

            // Send user connect message
            //TODO: Encapsulate in a function
            TimeChatMessage timeMessage = new TimeChatMessage("Server", "Connected to " + currentLobby.name());

            ByteBuffer data = FlatBuffersCodec.chatToByteBuffer(timeMessage);
            ByteBuf buf = Unpooled.copiedBuffer(data);

            ch.writeAndFlush(new BinaryWebSocketFrame(buf));

            // update user list
            // user list is prepended with lobby name for client
            //TODO: Encapsulate in a function
            ArrayList<String> users = currentLobby.getUsers();
            users.add(0, currentLobby.name());
            System.out.println(users.toString());
            String[] userList = users.toArray(new String[users.size()]);
            ByteBuffer userData = FlatBuffersCodec.listToByteBuffer("users", userList);
            ByteBuf userBuf = Unpooled.copiedBuffer(userData);
            currentLobby.writeAndFlush(new BinaryWebSocketFrame(userBuf));

            // update lobby list
            //TODO: Encapsulate in a function
            String[] lobbyList = new String[lobbies.size()];
            for (int j = 0; j < lobbies.size(); j++) {
                lobbyList[j] = lobbies.get(j).name();
                lobbyList[j] += "," + lobbies.get(j).numUsers() + "/" + ChatServer.LOBBY_SIZE;
            }
            ByteBuffer lobbyData = FlatBuffersCodec.listToByteBuffer("lobbies", lobbyList);
            ByteBuf lobbyBuf = Unpooled.copiedBuffer(lobbyData);
            currentLobby.writeAndFlush(new BinaryWebSocketFrame(lobbyBuf));

            return true;
        }
        
        return false;
    }

    /**
     * Adds user to the Global channel group
     */
    private void addChannelToGlobalGroup(Channel aChannel) {
        globalChannelGroup.add(aChannel);
        
    }

    /**
     * Generates a Message containing RPS challenge info in a ByteBuf.
     * @param chalenger    Person initiating RPS challenge
     * @param challenged   Receiving RPS challenge
     * @return             Challenge FlatBuffers Message in ByteBuf format
     */
    private ByteBuf makeChallengeMessageBuf(String challenger, String challenged) {
        String challengeMessage = challenged + ", " + challenger + " wants to play"
                + " Rock, Paper, Scissors!";
        
        TimeChatMessage msg = new TimeChatMessage("Server", challengeMessage);
        ByteBuffer buf = FlatBuffersCodec.chatToByteBuffer(msg);
        
        return Unpooled.copiedBuffer(buf);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) { // (4)
        // Close the connection when an exception is raised.
        cause.printStackTrace();
        ctx.close();
    }


}