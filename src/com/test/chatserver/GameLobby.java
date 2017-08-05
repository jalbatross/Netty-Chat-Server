package com.test.chatserver;

import game.GameType;
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
    
    public void kick(String user) {
        if (user.contentEquals(this.host)) {
            return;
        }
        
        super.remove(user);
    }
    
    public String getPassword() {
        return password;
    }
    
    
    
    @Override
    public String lobbyInfo() {
        String ret = this.name() + "," + this.type.name() + "," + this.size() + "/"
                + this.capacity;
        return ret;
    }

}
