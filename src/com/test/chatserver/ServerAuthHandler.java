package com.test.chatserver;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import Schema.Credentials;
import Schema.Message;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

/**
 * Class used to authenticate users. Current implementation should handle 
 * FlatBuffers Serialized credentials as ByteBufs. Each serialized Credentials
 * should be prefixed by a 4 byte integer indicating the size in bytes of
 * the serialized FlatBuffer.
 * 
 * Upon successful registration or authentication of the provided username/password
 * pair, ServerAuthHandler removes itself from the pipeline with handlers that 
 * are useful for server functionality.
 * 
 * @author jalbatross (Joey Albano)
 *
 */

public class ServerAuthHandler extends ChannelInboundHandlerAdapter {
    private String username = new String();
    private ChannelGroup allUsers;
    private ArrayList<ChannelGroup> lobbies = new ArrayList<ChannelGroup>();
    
    private Channel ch;
    
    private static int MAX_MSG_LEN = FlatBuffersCodec.SERIALIZED_CRED_LEN;
    
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ch = ctx.channel();
    }
    
    public ServerAuthHandler(ChannelGroup grp, ArrayList<ChannelGroup> lobbies) {
        allUsers = grp;
        this.lobbies = lobbies;
    }
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("[ServerAuthHandler] Auth Handler Called");
        
        LoginAuthorizer login = new LoginAuthorizer();
        
        //Close connection if receive invalid credential
        if (!(msg instanceof ByteBuf)) {
            System.out.println("Bad credential format received");
            //TODO: send ERROR flatbuffer to user
            ctx.close();
            return;
        }
        
        ByteBuf buf = (ByteBuf) msg;
        
        if (buf.readableBytes() > MAX_MSG_LEN) {
            System.out.println("too large msg");
            ctx.close();
            return;
        }

        Message received = Message.getRootAsMessage(buf.nioBuffer());
        
        if (received.dataType() != Schema.Type.Credentials) {
            System.out.println("ServerAuthHandler didn't receive Credentials!");
            ctx.close();
            return;
        }
        //Convert credential 
        Credentials credentials = FlatBuffersCodec.byteBufToData(buf.nioBuffer(), Credentials.class);
       
        System.out.println("received user: " + credentials.username() +
                "\nreceived pass: " + credentials.password()); 
        String user = credentials.username();
        char[] pass = credentials.password().toCharArray();
        
        if (login.verifyUser(user, pass)) {
            ByteBuffer auth = FlatBuffersCodec.authToByteBuffer(true);
            
            byte[] len = new byte[4];
            len = ByteBuffer.wrap(len).putInt(auth.remaining()).array();
            
            //Prepend flatbuffer with length
            ByteBuf lenPrefix = Unpooled.copiedBuffer(len);
            ByteBuf authBuf = Unpooled.copiedBuffer(auth);

            //Write to channel
            ch.write(lenPrefix);
            ChannelFuture cf = ch.writeAndFlush(authBuf);
            if (!cf.isSuccess()) {
                System.out.println(cf.cause());
            }
            
            System.out.println("correct user and pass!");
            
            
        }
        else {
            System.out.println("Wrong user and pass");
            
        }
        Arrays.fill(pass, '0');
        

        return;

        /*
        //TODO: Add stricter username requirements
        if (credential.length() > 12 || credential.isEmpty()) {
            System.out.println("received bad credential");
            String errorMsg = "Username was longer than 12 chars or empty.";
            ctx.writeAndFlush( new TextWebSocketFrame(errorMsg));
            return;
        }
        
        //TODO: Instead of names.constains(credential), query database for
        // USERNAME and PASSWORD that client will submit here
        if(allUsers.contains(ctx.channel()) || names.contains(credential)) {
            System.out.println("either user already connected or duplicate username");
            ctx.writeAndFlush(new TextWebSocketFrame("That username is being used!"));
            return;
        }
        
        //TODO: Change names.add(credential) to database query I think
        //TODO: Send user list of lobbies and have them choose one after
        //      succsessful login
        if (allUsers.add(ctx.channel()) && names.add(credential)) {
            System.out.println("added user w/ name: " + credential);
            ctx.writeAndFlush(new TextWebSocketFrame("Login Successful! There are "
                    + allUsers.size() + " connected users."));
            ctx.pipeline().remove(this);
            
            //TODO: Make user choose lobby here or create new pipeline handler
            // for lobby selector
            //ctx.pipeline().addLast(new ChatServerLobbySelector());
            ctx.pipeline().addLast(new ChatServerDecoder());
            ctx.pipeline().addLast(new ChatServerHandler(allUsers, credential));
        }
        else {
            System.out.println("[ServerAuthHandler] ERROR while adding valid credential");
            ctx.writeAndFlush(new TextWebSocketFrame("Something weird happened! Sorry."));
            names.remove(credential);
            ctx.close();
        }
        */
        
    }
    
}
