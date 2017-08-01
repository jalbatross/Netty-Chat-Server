package com.test.chatserver;

import io.netty.util.concurrent.EventExecutor;

public class GameLobby extends NamedChannelGroup {
    protected String gameType;
    
    public GameLobby(String name, EventExecutor executor) {
        super(name, executor);
        gameType = "N/A";
    }
    
    public GameLobby(String name, EventExecutor executor, String type) {
        super(name,executor);
        gameType = type;
    }
    
    @Override
    public String lobbyInfo() {
        String ret = this.name() + "," + this.gameType + "," + this.size() + "/"
                + ChatServer.LOBBY_SIZE;
        return ret;
    }

}
