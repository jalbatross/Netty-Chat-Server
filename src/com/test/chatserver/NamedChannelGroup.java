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

/**
 * NamedChannelGroup
 * 
 * An extension of the DefaultChannelGroup class provided by Netty
 * which adds usernames to each channel added using a Bidirectional 
 * Map (BiMap) from Google's Guava library.
 * 
 * Functionally it is identical to DefaultChannelGroup except for the
 * fact that each channel added to the NamedChannelGroup must have
 * a unique corresponding username associated with it.
 * 
 * @author jalbatross (Joey Albano)
 *
 */

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
    
    @Override
    public boolean remove(Object o){
        if (o instanceof String) {
            return remove((String) o);
        }
        else if (o instanceof Channel) {
            return remove((Channel) o);
        }
        
        return false;
    }
    
    private boolean remove(String username) {
        //get channel corresponding to user
        Channel theChannel = channelMap.get(username);
        if (theChannel == null) {
            return false;
        }
        
        //Remove the channel from the DefaultChannelGroup
        boolean channelRemoved = super.remove(theChannel);
        
        //Remove the username/channel mapping
        boolean mappingRemoved = channelMap.remove(username) == null ? false: true;
        
        return (channelRemoved && mappingRemoved);
    }
    
    private boolean remove(Channel aChannel) {
        String username = channelMap.inverse().get(aChannel);
        return remove(username);
    }
    
    /**
     * 
     * @param username   a username
     * @return true if the username is found in the NamedChannelGroup,
     *         false otherwise
     */
    public boolean containsUser(String username) {
        return channelMap.containsKey(username);
    }
    
    public int numUsers() {
        return channelMap.size();
    }

    /**
     * Returns a corresponding username to a channel in the NamedChannelGroup
     * 
     * If the correspoding user is not found, either because the channel was not
     * in the NamedChannelGroup or there was no username corresponding to that
     * channel, returns null.
     * 
     * @param channel    A channel in the NamedChannelGroup
     * @return           Username corresponding to that channel if the channel
     *                   is in the NamedChannelGroup and has a corresponding 
     *                   username, null otherwise.
     */
    public String getUser(Channel channel) {
        return channelMap.inverse().get(channel);
    }
    
    /**
     * Get the corresponding channel of a username in the NamedChannelGroup
     * 
     * If the username is not present in the group or there is no channel 
     * corresponding to the username or both, returns null. Otherwise gets
     * the corresponding channel of username in the NamedChannelGroup.
     * 
     * @param username     A username
     * @return             Channel corresponding to the username in this
     *                     if the username is in the group. Null otherwise.
     */
    public Channel getChannel(String username) {
        return channelMap.get(username);
    }
    
    public void clear() {
        super.clear();
        channelMap.clear();
    }
    
    /**
     * @param username   a username
     * @return           true if username is an element in the NamedChannelGroup,
     *                   false otherwise.
     */
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
