package com.test.chatserver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import io.netty.channel.ServerChannel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.EventExecutor;

public class NamedChannelGroup extends DefaultChannelGroup {
    
    protected Set<String> userSet = Collections.synchronizedSet(new HashSet<String>());
    protected Map<String, Channel> channelMap;
    public NamedChannelGroup(String name, EventExecutor executor) {
        super(name, executor);
        channelMap = new ConcurrentHashMap<String, Channel>();
    }
    
    public boolean addUser(String username, Channel aChannel) {
        channelMap.put(username, aChannel);
        return userSet.add(username);
    }
    
    public boolean removeUser(String username) {
        channelMap.remove(username);
        return userSet.remove(username);
    }
    
    public boolean containsUser(String username) {
        return userSet.contains(username);
    }
    
    public int numUsers() {
        return userSet.size();
    }
    public Channel getChannel(String username) {
        return channelMap.get(username);
    }
    
    
    /**
     * Gets the list of users in the channelGroup as an ArrayList. 
     * 
     * @return   ArrayList<String> of users
     */
    public ArrayList<String> getUsers() {
        ArrayList<String> arr = new ArrayList<String>();
        
        synchronized(userSet) {
            Iterator<String> i = userSet.iterator();
            while (i.hasNext()) {
                arr.add(i.next());
            }
        }
        
        return arr;
    }

}
