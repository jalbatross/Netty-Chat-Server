package com.test.chatserver;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

/**
 * A class used to transmit chat data between clients. Unlike the
 * ChatMessage class, TimeChatMessage has an additional time field
 * that should be used to indicate the time that the server received
 * the chat message to broadcast to other users.
 * 
 * time is a long value corresponding to the ms since Unix epoch (UTC).
 * 
 * 
 * @author jalbatross (Joey Albano)
 *
 */

public class TimeChatMessage extends ChatMessage{
    private long time;
    
    public TimeChatMessage(long time, String author, String msg) {
        super(author, msg);
        this.time = time;
    }
    
    public TimeChatMessage(ChatMessage chatMsg, long time) {
        super(chatMsg.author, chatMsg.message);
        this.time = time;
    }
    
    public TimeChatMessage(String author, String msg) {
        super(author, msg);
        time = Instant.now().toEpochMilli();
    }
    
    public TimeChatMessage(ChatMessage chatMsg) {
        super(chatMsg.author, chatMsg.message);
        time = Instant.now().toEpochMilli();
    }
    
    public void setZonedDateTime(long ts) {
        this.time = ts;
    }
    
    /**
     * Returns the TimeChatMessage object as a formatted string using the
     * local machine's time formatting.
     */
    public String toString() {
        LocalDateTime date = 
                LocalDateTime.ofInstant(Instant.ofEpochMilli(time), 
                                        ZoneId.systemDefault());
        String timeStr = date.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM));
        
        return timeStr + " " + this.author + ": " + this.message;
    }
    
    public long getTime() {return time;}
}
