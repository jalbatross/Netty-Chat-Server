package com.test.chatserver;

import game.GameType;
import io.netty.channel.Channel;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.GlobalEventExecutor;

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
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public void setHost(String hostUsername) {
        this.host = hostUsername;
    }
    
    public String host() {
        return this.host;
    }
    
    public boolean isHost(String username) {
        return username.contentEquals(host);
    }
    
    public void kick(String user) {
        if (user.contentEquals(this.host)) {
            return;
        }
        
        super.remove(user);
    }
    
    public String password() {
        return password;
    }
    
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
