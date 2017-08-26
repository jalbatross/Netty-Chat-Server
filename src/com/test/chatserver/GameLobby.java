package com.test.chatserver;

import game.GameType;
import io.netty.channel.Channel;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.GlobalEventExecutor;

/**
 * GameLobby is a subclass of NamedChannelGroup which contains a reference
 * to a GameType. GameType corresponds to the type of Game that the players
 * in the GameLobby want to play.
 * 
 * In addition to the GameType field, GameLobby objects have capacity, password,
 * and host fields which are used by clients to allow/disallow entry and manage
 * users within the GameLobby client-side.
 * 
 * @author jalbatross (Joey Albano)
 *
 */
public class GameLobby extends NamedChannelGroup {
    protected GameType type;
    protected int capacity;
    protected String password;
    protected String host;
    
    public GameLobby(String name, String type, int capacity) {
        super(name, GlobalEventExecutor.INSTANCE);
        this.type = GameType.fromString(type);
        this.capacity = capacity;
    }
    
    public GameLobby(String name, String type, int capacity, String password) {
        super(name, GlobalEventExecutor.INSTANCE);
        this.type = GameType.fromString(type);
        this.capacity = capacity;
        this.password = password;
    }
    
    /**
     * Sets password of the GameLobby
     * @param password     A String password
     */
    public void setPassword(String password) {
        this.password = password;
    }
    
    /**
     * Sets the host of the GameLobby
     * @param hostUsername    host's username
     */
    public void setHost(String hostUsername) {
        this.host = hostUsername;
    }
    
    /**
     * 
     * @return GameLobby host's username
     */
    public String host() {
        return this.host;
    }
    
    /**
     * 
     * @param username     a username
     * @return             True if username is the host, false otherwise
     */
    public boolean isHost(String username) {
        return username.contentEquals(host);
    }
    
    /**
     * Used by the host of the GameLobby to remove a user from the GameLobby.
     * 
     * No-op if the host tries to kick themselves.
     * 
     * @param user      Username of a user in the GameLobby
     */
    public void kick(String user) {
        if (user.contentEquals(this.host)) {
            return;
        }
        
        super.remove(user);
    }
    
    /**
     * 
     * @return  GameLobby's password
     */
    public String password() {
        return password;
    }
    
    /**
     * @return Maximum capacity of the GameLobby
     */
    public int capacity() {
        return capacity;
    }
    
    
    @Override
    public String lobbyInfo() {
        String ret = this.name() + "," + this.type.name() + "," + this.size() + "/"
                + this.capacity;
        return ret;
    }
    
    /**
     * Assigns a new host that is not the current host
     */
    public void assignNewHost() {
        if (this.size() <= 1 ) {
            return;
        }
        
        for (String username : this.channelMap.keySet()) {
            if (username.contentEquals(host)) {
                continue;
            }
            else {
                host = username;
                break;
            }
        }
        
    }
    
    @Override
    public boolean remove(Object o) {
        String usernameToRemove = null;
        if (o instanceof String) {
            usernameToRemove = (String) o;
        }
        else if (o instanceof Channel) {
            usernameToRemove = getUser((Channel) o);
        }
        else {
            return false;
        }
        
        if (usernameToRemove == null || usernameToRemove.isEmpty()) {
            return false;
        }
        
        if(usernameToRemove.contentEquals(this.host)) {
            assignNewHost();
        }
        
        return super.remove(usernameToRemove);
    }
    
    

}
