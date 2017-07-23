package com.test.chatserver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import io.netty.channel.ServerChannel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.EventExecutor;

public class NamedChannelGroup extends DefaultChannelGroup {
    
    protected Set<String> userSet = Collections.synchronizedSet(new HashSet<String>());

    public NamedChannelGroup(String name, EventExecutor executor) {
        super(name, executor);
        // TODO Auto-generated constructor stub
    }
    
    public boolean addUser(String username) {
        return userSet.add(username);
    }
    
    public boolean removeUser(String username) {
        return userSet.remove(username);
    }
    
    public boolean containsUser(String username) {
        return userSet.contains(username);
    }
    
    public int numUsers() {
        return userSet.size();
    }
    
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
