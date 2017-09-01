package com.test.chatserver;



import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import Schema.Chat;
import Schema.Data;
import Schema.Message;
import Schema.Request;
import Schema.RequestType;
import Schema.GameCreationRequest;
import Schema.GameUpdate;
import Schema.ListType;
import game.GameType;
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
import io.netty.util.AttributeKey;
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

import javax.xml.validation.Schema;

/**
 * Handles a server-side chat channel. Tail of the pipeline for incoming data.
 */
public class ChatServerHandler extends ChannelInboundHandlerAdapter { // (1
	
    private final List<NamedChannelGroup> lobbies;
    private final ChannelGroup globalChannelGroup;
    private final String username;
    
    private final AttributeKey<Boolean> INGAMEKEY = AttributeKey.valueOf("inGame");
    
    private Channel ch;
    private boolean init = false;

    private NamedChannelGroup currentChatLobby = null;
    private GameLobby currentGameLobby = null;
    
    private List<GameLobby> gameLobbies;
       
    public ChatServerHandler(ChannelHandlerContext ctx, String username, List<NamedChannelGroup> lobbies,
            ChannelGroup allChannels, List<GameLobby> gameLobbies) {
        this.username = username;
        this.lobbies = lobbies;
        this.globalChannelGroup = allChannels;
        this.ch = ctx.channel();
        this.gameLobbies = gameLobbies;
        
        ctx.channel().attr(INGAMEKEY).set(false);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        System.out.println("[ChatServerHandler] Disconnecting user: " + username);
        
        //Perform cleanup
        for (NamedChannelGroup lobby : lobbies) {
            if (lobby.contains(username)) {
                
                Channel closeCh = lobby.getChannel(username);
                lobby.remove(username);
                closeCh.close();
                
                if (!lobby.isEmpty()) {
                    lobby.writeAndFlush(new BinaryWebSocketFrame(lobbyUserList(lobby)));
                }
                
            }

        }
        
        synchronized (gameLobbies) {
            Stack<GameLobby> emptyLobbies = new Stack<GameLobby>();

            for (GameLobby gameLobby : gameLobbies) {
                if (gameLobby.remove(username) && !gameLobby.isEmpty()) {
                    gameLobby.writeAndFlush(new BinaryWebSocketFrame(gameLobbyUserList(gameLobby)));
                }
                
                if (gameLobby.isEmpty()) {
                    emptyLobbies.add(gameLobby);
                }
            }

            // clean up
            while (!emptyLobbies.isEmpty()) {
                gameLobbies.remove(emptyLobbies.pop());
            }
        }
        
        
    }
    
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception 
	{
		System.out.println("\n[ChatServerHandler] channelRead called!");
		
		if (!init) {
		    
	        addChannelToGlobalGroup(this.ch);
	        
	        if (!addUserToFirstLobby(this.ch, username)) {
	            ctx.close();
	            return;
	        }
	        
            ch.writeAndFlush(new BinaryWebSocketFrame(lobbyConnectMessage()));
            currentChatLobby.write(new BinaryWebSocketFrame(lobbyUserList(currentChatLobby)));
            
	        init = true;	        
	        return;
		}
		
		if ((msg instanceof TextWebSocketFrame)) {
			System.out.println("[ChatServerHandler] received TextWebSocketFrame!\n------");	
			
			String strMsg = ((TextWebSocketFrame) msg).text();
			
			if (strMsg.startsWith("/connect ")) {
			    //get lobby name
			    String lobbyName = strMsg.substring(9);
			    if (lobbyName.contentEquals(currentChatLobby.name())) {
			        return;
			    }
			    
			    if (connectToLobby(lobbyName)) {
                    ch.writeAndFlush(new BinaryWebSocketFrame(lobbyConnectMessage()));
                    currentChatLobby.writeAndFlush(new BinaryWebSocketFrame(lobbyUserList(currentChatLobby)));
                    ch.writeAndFlush(new BinaryWebSocketFrame(lobbiesData()));
			    }
			    return;
			}
			
			else if (strMsg.startsWith("/join ")) {
			    String gameLobbyName = strMsg.substring(6);

			    if (currentGameLobby != null) {
			        return;
			    }
			    
			    synchronized(gameLobbies) {
			        
			        for (GameLobby lobby : gameLobbies) {
			            if (lobby.name().contentEquals(gameLobbyName) 
			                    && !lobby.isFull()) {
			                currentGameLobby = lobby;
			                currentGameLobby.add(this.ch, this.username);
			                currentGameLobby.writeAndFlush(new BinaryWebSocketFrame(gameLobbyUserList(currentGameLobby)));
			                
			                return;
			            }
			        }
			        
			        
			    }
			    
			    return;
			    
			}
			else if (strMsg.startsWith("/kick ")) {

			    String kickedUserName = strMsg.substring(6);
	            System.out.println("[ChatServerHandler] " + this.username + " tried to kick "
	                        + kickedUserName);
			    //check if it came from host or is malformed
			    if (!this.username.contentEquals(currentGameLobby.host()) 
			            || kickedUserName.length() > 10) {
			        this.ch.close();
			        
			        currentGameLobby.writeAndFlush(new BinaryWebSocketFrame(gameLobbyUserList(currentGameLobby)));
			        return;
			    }
			    
			    //sends empty game lobby users to kicked client so that client
			    //knows they are kicked
			    Channel kickedChannel = currentGameLobby.getChannel(kickedUserName);
			    
			    kickedChannel.pipeline().fireChannelRead(new TextWebSocketFrame("/leave"));
			    kickedChannel.writeAndFlush(emptyLobbyUserList(ListType.GAME_LOBBY_USERS));
			    
			    //kick user and update other users in the lobby
			    currentGameLobby.kick(kickedUserName);
			    
			    currentGameLobby.writeAndFlush(new BinaryWebSocketFrame(gameLobbyUserList(currentGameLobby)));
			    return;
			}
            //Stamp message with current time
            TimeChatMessage timeMessage = new TimeChatMessage(username, strMsg);
            
            ByteBuffer data = FlatBuffersCodec.chatToByteBuffer(timeMessage);
            ByteBuf buf = Unpooled.copiedBuffer(data);
            
            
            
            currentChatLobby.writeAndFlush(new BinaryWebSocketFrame(buf));
            
		}
        else if (msg instanceof BinaryWebSocketFrame) {
            System.out.println("[ChatServerHandler] Received BinaryWebSocketFrame");
            BinaryWebSocketFrame data = (BinaryWebSocketFrame) msg;
            ByteBuf dataBuffer = data.content();

            Message fbMsg = Message.getRootAsMessage(dataBuffer.nioBuffer());
            System.out.println("[ChatServerHandler]Data type: " + fbMsg.dataType());
            if (fbMsg.dataType() == Data.GameCreationRequest) {
                // If user is not currently in a Game Lobby, create a new game 
                // lobby with them as the host
                if (ctx.channel().attr(INGAMEKEY).get() == false) {
                    System.out.println("[ChatServerHandler] Got game creation request");

                    GameCreationRequest request = (GameCreationRequest) fbMsg.data(new GameCreationRequest());

                    if (!validGameRequest(request)) {
                        ctx.close();
                        System.out.println("[ChatServerHandler] User: " + username + " sent malformed"
                                + " game lobby request data, closed connection.");
                        return;
                    }
                    GameLobby gameLobby = new GameLobby(request.name(), request.type(), request.capacity(), request.bestOf());
                    gameLobby.setPassword(request.password());

                    // Add lobby host
                    if (gameLobby.add(this.ch, this.username)) {
                        System.out.println("successfully added user: " + username + " to gamelobby");
                        gameLobby.setHost(this.username);
                        gameLobbies.add(gameLobby);
                        currentGameLobby = gameLobby;
                    }
                    else {
                        return;
                    }

                    // send back to client so that they know lobby creation was
                    // successful
                    ch.write(data);
                    ch.writeAndFlush(new BinaryWebSocketFrame(gameLobbyUserList(currentGameLobby)));
                              
                    return;

                }
                else {
                    System.out.println("[ChatServerHandler] " + username + " was in game and tried making a lobby");
                }
            }
            else if (fbMsg.dataType() == Data.Request) {
                Request request = (Request) fbMsg.data(new Request());
                switch(request.type()) {
                    case RequestType.CHAT_LOBBIES:
                        ch.writeAndFlush(new BinaryWebSocketFrame(lobbiesData()));
                        return;
                    case RequestType.CURRENT_CHAT_LOBBY_INFO:
                        ch.writeAndFlush(lobbyConnectMessage());
                        return;
                    case RequestType.GAME_LOBBIES:
                        ch.writeAndFlush(new BinaryWebSocketFrame(gameLobbiesData()));
                        return;
                    case RequestType.LEAVE_GAME:
                        synchronized (gameLobbies) {
                            Stack<GameLobby> emptyLobbies = new Stack<GameLobby>();

                            for (GameLobby gameLobby : gameLobbies) {
                                if (gameLobby.remove(username) && !gameLobby.isEmpty()) {
                                    gameLobby.writeAndFlush(new BinaryWebSocketFrame(gameLobbyUserList(gameLobby)));
                                }
                                
                                if (gameLobby.isEmpty()) {
                                    emptyLobbies.add(gameLobby);
                                }
                            }

                            // clean up
                            while (!emptyLobbies.isEmpty()) {
                                gameLobbies.remove(emptyLobbies.pop());
                            }
                        }
                        currentGameLobby = null;
                        return;
                    case RequestType.START_GAME:
                        //Do nothing if there are not enough users to start
                        //Only hosts can start game
                        if (currentGameLobby.size() < 2 || !currentGameLobby.isHost(this.username)) {
                            return;
                        }
                        
                        //Remove this game lobby from the lobby list (prevent joiners)
                        gameLobbies.remove(currentGameLobby);
                        
                        
                        
                        //Create the game and set user as being in game
                        RPS newGame = new RPS(currentGameLobby.getUsers(), currentGameLobby.bestOf());
                        ctx.channel().attr(INGAMEKEY).set(true);
                        
                        //Assign each lobby user the game's handler server side
                        for (Channel user : currentGameLobby.channelMap.values()) {
                            user.pipeline().addLast("rpsGame", new ServerRPSHandler(newGame, currentGameLobby, currentGameLobby.getUser(user)));
                            
                        }
                        
                        //currentGameLobby = null;
                        return;
                    default: 
                        System.out.println("ERROR: Unk RequestType received");
                        ctx.close();
                        return;
              
                }
            }
            else if (fbMsg.dataType() == Data.GameUpdate) {
                System.out.println("[ChatServerHandler] Received game update");
                ctx.fireChannelRead((GameUpdate) fbMsg.data(new GameUpdate()));
            }
            else {
                System.out.println("[ChatServerHandler] Received unk binary data");
            }

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
	 * Checks if a GameCreationRequest has valid parameters. If 
	 * any of the parameters are invalid, returns false. Otherwise
	 * returns true.
	 * 
	 * @param request    A GameCreationRequest
	 * @return           True if all parameters are valid, false 
	 *                   otherwise.
	 */
	private boolean validGameRequest(GameCreationRequest request) {
	    if (request == null) {
	        return false;
	    }
	   
        return validGameName(request.name()) 
                && validGameType(request.type())
                && validGameCapacity(request.type(), request.capacity())
                && validGamePassword(request.password());
    }
	
	/**
	 * Returns true if the Game name is nonempty and at most 
	 * GAME_NAME_MAX_LEN characters long, false otherwise.
	 * 
	 * @param name   Name of a Game
	 * @return       True if name is valid, false otherwise
	 */
    private boolean validGameName(String name) {
        return name.length() <= ChatServer.GAME_NAME_MAX_LEN
                && name.length() > 0;
    }
    
    /**
     * Returns true if type exists, false otherwise.
     * 
     * @param type    A game type
     * @return        True if the game type is recognized, false
     *                otherwise
     */
    private boolean validGameType(String type) {
        return GameType.typeExists(type);
    }
    
    /**
     * Returns true if capacity is valid for game type, false otherwise.
     * 
     * @param type      Game type
     * @param capacity  Maximum game capacity 
     * @return          True if capacity <= the maximum capacity of type.
     */
    private boolean validGameCapacity(String type, int capacity) {
        switch (GameType.fromString(type)) {
            case RPS:
                return capacity == 2;
            case COUP:
                return capacity == 2;
            default:
                break;
        }
        return false; 
    }
	
    /**
     * Returns true if the Game password is no longer than 
     * GAME_PASSWORD_MAX_LEN chars long, 
     * false otherwise.
     * 
     * @param password   A game password
     * @return           True if password is of valid length
     */
    private boolean validGamePassword(String password) {
        return password.length() <= ChatServer.GAME_PASSWORD_MAX_LEN;
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
            
            if (!lobbies.get(i).add(ch, username)) {
                return false;
            }

            currentChatLobby = lobbies.get(i);
            return true;
        }
        
        return false;
    }
    
    /**
     * Attempts to place this channel and corresponding username into the
     * NamedChannelGroup in lobbies indicated by lobbyName.
     * 
     * Returns false if the user is already in the lobby or if placement
     * in the NamedChannelGroup fails for any other reason. Otherwise,
     * the user is placed into the NamedChannelGroup with name lobbyName
     * and returns true.
     * 
     * @param lobbyName Name of the lobby to connect to
     * @return          True if the connection was successful, 
     *                  false otherwise.
     */
    private boolean connectToLobby(String lobbyName) {
        for (int i = 0; i < lobbies.size(); i++) {
            if (lobbyName.equals(lobbies.get(i).name()) && 
                !lobbies.get(i).isFull() &&
                !lobbies.get(i).contains(ch) && 
                !lobbies.get(i).contains(username)) {
            
                currentChatLobby.remove(ch);
                
                currentChatLobby = lobbies.get(i);
                currentChatLobby.add(ch, username);
                
                return true;
                

                
                
            }
        }
        return false;
    }
    
    /**
     * Returns a ByteBuf of the list of server lobbies serialized with FlatBuffers.
     * Each entry in the list is in the following format:
     * lobbyName,numUserInLobby/lobbyCapacity
     * 
     * For example, the lobby MyLobby with 3 users connected out of a maximum of 10
     * would be recorded in the list as;
     * MyLobby,3/10
     * 
     * 
     * 
     * @see {@link Schema.List}
     * 
     * @return ByteBuf containing list of NamedChannelGroups in lobbies.
     */
    private ByteBuf lobbiesData() {
        String[] lobbyList = new String[lobbies.size()];
        
        for (int j = 0; j < lobbies.size(); j++) {
            lobbyList[j] = lobbies.get(j).lobbyInfo();
        }

        ByteBuffer lobbyData = FlatBuffersCodec.listToByteBuffer(ListType.LOBBIES, lobbyList);
        return Unpooled.copiedBuffer(lobbyData);
    }
    
    private ByteBuf gameLobbiesData() {
        int numGameLobbies = gameLobbies.size();
        String[] gameLobbyList = new String[numGameLobbies];
        
        for (int i = 0; i < numGameLobbies; i++) {
            gameLobbyList[i] = gameLobbies.get(i).lobbyInfo();
        }
        
        ByteBuffer gameLobbyData = FlatBuffersCodec.listToByteBuffer(ListType.GAMES, gameLobbyList);
        return Unpooled.copiedBuffer(gameLobbyData);
    }

    private ByteBuf lobbyConnectMessage() {
        TimeChatMessage timeMessage = new TimeChatMessage("Server", "You are now connected to "
                + currentChatLobby.name());
        ByteBuffer data = FlatBuffersCodec.chatToByteBuffer(timeMessage);
        return Unpooled.copiedBuffer(data);
    }

    /**
     * Returns a ByteBuf of a FlatBuffers serialized List object of a 
     * NamedChannelGroup's users.
     * 
     * @see{@link Schema}
     * 
     * @param gameLobby   A NamedChannelGroup
     * @return            ByteBuf containing the NamedChannelGroup's users in a 
     *                    FlatBuffers serialized String[]
     */
    private ByteBuf lobbyUserList(NamedChannelGroup lobby) {
        ArrayList<String> users = lobby.getUsers();
        
        //Prefix user list with lobby name
        users.add(0, lobby.name());
        
        String[] userList = users.toArray(new String[users.size()]);
        ByteBuffer buffer =  FlatBuffersCodec.listToByteBuffer(ListType.USERS, userList);
        return Unpooled.copiedBuffer(buffer);
    }
    
    /**
     * Returns a ByteBuf of a FlatBuffers serialized List object of a 
     * GameLobby's users.
     * 
     * @see{@link Schema}
     * 
     * @param gameLobby   A GameLobby
     * @return            ByteBuf containing the GameLobby's users in a 
     *                    FlatBuffers serialized String[]
     */
    private ByteBuf gameLobbyUserList(GameLobby gameLobby) {
        
        ArrayList<String> users = gameLobby.getUsers();
        
        //mark the host 
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).contentEquals(gameLobby.host())) {
                users.set(i, users.get(i) + ",host");
                break;
            }
        }
        
        String[] userList = users.toArray(new String[users.size()]);
        ByteBuffer buffer = FlatBuffersCodec.listToByteBuffer(ListType.GAME_LOBBY_USERS, userList);
        
        return Unpooled.copiedBuffer(buffer);
    }
    
    /**
     * An empty LobbyUserList.
     * @param type  Type of lobby user list, i.e. games, gameUsers, users
     * @return      BinaryWebSocketFrame of FlatBuffers serialized list
     */
    private BinaryWebSocketFrame emptyLobbyUserList(String type) {
        ByteBuffer buffer = FlatBuffersCodec.listToByteBuffer(type, new String[0]);
        ByteBuf buf = Unpooled.copiedBuffer(buffer);
        
        return new BinaryWebSocketFrame(buf);
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