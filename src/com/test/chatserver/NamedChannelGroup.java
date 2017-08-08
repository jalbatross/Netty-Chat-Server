package com.test.chatserver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;

import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import io.netty.channel.ServerChannel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.EventExecutor;

public class NamedChannelGroup extends DefaultChannelGroup {
    
    protected BiMap<String, Channel> channelMap;
    public NamedChannelGroup(String name, EventExecutor executor) {
        super(name, executor);
        
        //initialize biMap
        BiMap<String,Channel> userChannelMap = HashBiMap.create();
        channelMap = Maps.synchronizedBiMap(userChannelMap);
    }
    
    public boolean add(Channel channel, String username) {
        if (super.add(channel)) {
            channelMap.put(username, channel);
            return true;
        }
        
        return false;
    }
    
    public boolean remove(String username) {
        //get channel corresponding to user
        Channel theChannel = channelMap.get(username);
        if (theChannel == null) {
            return false;
        }
        super.remove(theChannel);
        channelMap.remove(username);
        
        return true;
    }
    
    public boolean remove(Channel aChannel) {
        String username = channelMap.inverse().get(aChannel);
        return remove(username);
    }
    
    public boolean containsUser(String username) {
        return channelMap.containsKey(username);
    }
    
    public int numUsers() {
        return channelMap.size();
    }

    public String getUser(Channel channel) {
        return channelMap.inverse().get(channel);
    }
    public Channel getChannel(String username) {
        return channelMap.get(username);
    }
    
    public void clear() {
        super.clear();
        channelMap.clear();
    }
    
    public boolean contains (String username) {
        return channelMap.containsKey(username);
    }
    
    
    /**
     * Gets the list of users in the channelGroup as an ArrayList. 
     * 
     * @return   ArrayList<String> of users
     */
    public ArrayList<String> getUsers() {
        String[] usersArray = channelMap.keySet().toArray(new String[channelMap.size()]);
        ArrayList<String> arr = new ArrayList<String>(Arrays.asList(usersArray));
        
        return arr;
    }

    public boolean isFull() {
        return this.size() >= ChatServer.LOBBY_SIZE;
    }
    
    public String lobbyInfo() {
        return this.name() + ","+this.size()+"/"+ChatServer.LOBBY_SIZE;
    }

}
