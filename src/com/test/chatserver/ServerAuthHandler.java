package com.test.chatserver;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import Schema.Auth;
import Schema.Credentials;
import Schema.Message;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.CharsetUtil;

/**
 * Class used to authenticate users. Current implementation should handle
 * FlatBuffers Serialized credentials as ByteBufs. Each serialized Credentials
 * should be prefixed by a 4 byte integer indicating the size in bytes of the
 * serialized FlatBuffer OR a JSON object conforming to the following schema:
 * {username: someUser, password: somePass}
 * 
 * Upon successful registration or authentication of the provided
 * username/password pair, ServerAuthHandler removes itself from the pipeline
 * with handlers that are useful for server functionality.
 * 
 * @author jalbatross (Joey Albano)
 *
 */

public class ServerAuthHandler extends ChannelInboundHandlerAdapter {

    private ChannelGroup allUsers;
    private List<ChannelGroup> lobbies = new ArrayList<ChannelGroup>();
    private LoginAuthorizer login = new LoginAuthorizer();
    private Map<String,TimeChatMessage> ticketDB;

    //User credentials
    private String username, pwdStr;
    private char[] pwdChar;

    private Channel ch;

    private static int MAX_MSG_LEN = FlatBuffersCodec.SERIALIZED_CRED_LEN;

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ch = ctx.channel();
    }

    public ServerAuthHandler(ChannelGroup grp, List<ChannelGroup> lobbies) {
        allUsers = grp;
        this.lobbies = lobbies;
    }

    public ServerAuthHandler(Map<String, TimeChatMessage> sessionTicketDB) {
        ticketDB = sessionTicketDB;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("[ServerAuthHandler] Auth Handler Called");

        if (msg instanceof ByteBuf) {
            ByteBuf buf = (ByteBuf) msg;

            if (buf.readableBytes() > MAX_MSG_LEN) {
                System.out.println("too large msg");
                ctx.close();
                return;
            }

            // Validate and get credentials
            if (!validateFlatbuffersCredentials(buf)) {
                ctx.close();
                Arrays.fill(pwdChar, '0');
                ((ByteBuf) msg).clear();
                return;
            }

            // Check DB for credentials
            if (login.verifyUser(username, pwdChar)) {
                
                //Send authorized packet to client
                ByteBuffer auth = FlatBuffersCodec.authToByteBuffer(true);

                ByteBuffer temp = ByteBuffer.allocate(4);
                temp.putInt(auth.remaining());
                byte[] len = temp.array();

                // Prepend flatbuffer with length
                ByteBuf lenPrefix = Unpooled.copiedBuffer(len);
                ByteBuf authBuf = Unpooled.copiedBuffer(auth);

                System.out.println("len prefix data: " + lenPrefix.getInt(0));

                // Write to channel
                ch.write(lenPrefix);
                ch.writeAndFlush(authBuf);
                System.out.println("correct user and pass!");

            } 
            else {
                System.out.println("Wrong user and pass");

            }

            // Clear sensitive information
            Arrays.fill(pwdChar, '0');
            ((ByteBuf) msg).clear();

        }

        else if (msg instanceof HttpContent) {
            HttpContent httpCred = (HttpContent) msg;
            
            if (!validateHttpCredentials(httpCred)) {
                ctx.close();
                return;
            }

            if (login.verifyUser(username, pwdStr)) {
                System.out.println("[AuthHandler] Got correct user/pass (HTTP)");
                
                String ticket = generateTicket(username, ctx.channel().remoteAddress(),ctx.channel().id());
                
                //generate timestamped message with username as author and IP address as message content
                InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
                TimeChatMessage value = new TimeChatMessage(username, address.getAddress().toString());
                ticketDB.put(ticket, value);
                
                ctx.writeAndFlush(httpAuthResponse(ticket));
            } 
            else {
                System.out.println("[AuthHandler] Got wrong user/pass (HTTP)");
                ctx.writeAndFlush(httpDeniedResponse());
            }

            httpCred.release();
        }
       

        return;

    }

    /**
     * Private helper method to validate received credentials from a client 
     * that sends credentials via HTTP POST.
     * 
     * Returns false if the JsonParser fails to correctly parse the JSON object
     * OR if we fail to extract a username and password from expected JSON object
     * fields after parsing.
     * 
     * A valid JSON credential looks like: 
     * {username: someUser, password: somePass}
     * 
     * If valid credentials are received, then the username and pwdStr fields of
     * the class are set to the received credentials.
     * 
     * @param httpCred   JSON credentials as HttpContent
     * @return           True if credentials are correctly formatted, 
     *                   false otherwise. Sets this username and 
     *                   this pwdStr to received credentials.
     */
    private boolean validateHttpCredentials(HttpContent httpCred) {
        String jsonContent = httpCred.content().toString(StandardCharsets.UTF_8);
        JsonObject obj = new JsonObject();
        try {
            obj = new JsonParser().parse(jsonContent).getAsJsonObject();
            this.username = obj.get("username").getAsString();
            this.pwdStr = obj.get("password").getAsString();

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("failed to parse JSON during auth!");
            System.out.println("Check credentials: bad formatting OR invalid creds");
            System.out.println("Credentials: " + httpCred.toString());
            return false;
        }

        return true;  
    }

    /**
     * Private helper method that validates received ByteBuf credentials.
     * 
     * Returns false when the ByteBuf of the data does not have a valid schema
     * type or if the FlatBuffersCodec fails to parse Credentials from
     * buf.
     * 
     * If the function returns false, it will clear the pwdChar array of this
     * by filling it with '0' for security.
     * 
     * Returns true otherwise, setting this.username and this.pwdChar to 
     * the Credentials parsed from buf.
     * 
     * @param buf     A ByteBuf containing a serialized FlatBuffers Credentials
     * @return        True if buf contains valid FlatBuffers Credentials. Sets
     *                this.username and this.pwdChar to those credentials.
     *                
     *                False if we fail to parse Credentials from buf, then
     *                clears pwdChar.
     *                
     * @see           Schema.Credentials
     */
    private boolean validateFlatbuffersCredentials(ByteBuf buf) {
        Message received = Message.getRootAsMessage(buf.nioBuffer());

        if (received.dataType() != Schema.Type.Credentials) {
            System.out.println("ServerAuthHandler didn't receive Credentials!");
            return false;
        }

        // Convert credential
        try {
        Credentials credentials = FlatBuffersCodec.byteBufToData(buf.nioBuffer(), Credentials.class);
        this.username = credentials.username();
        this.pwdChar = credentials.password().toCharArray();
        }
        catch (Exception e) {
            System.out.println("[ServerAuthHandler] Couldn't get credentials from FlatBuffers!");
            e.printStackTrace();
            Arrays.fill(this.pwdChar, '0');
            return false;
        }

        return true;
    }
    
    /**
     * Creates an authorization response to browser client
     * 
     * 
     * 
     * @param username    their username
     * 
     * @return    username authorized http response
     */
    private FullHttpResponse httpAuthResponse(String ticket) {
        FullHttpResponse resp = new DefaultFullHttpResponse( HttpVersion.HTTP_1_1, 
                HttpResponseStatus.OK, 
                Unpooled.copiedBuffer(ticket, 
                CharsetUtil.UTF_8));

        resp.headers().add(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        resp.headers().add(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        resp.headers().set(HttpHeaderNames.CONTENT_LENGTH, resp.content().readableBytes());
        
        return resp;
    }
    
    /**
     * Creates an authorization denied HTTP response to browser client
     * 
     * @return   denied http response
     */
    private FullHttpResponse httpDeniedResponse() {
        FullHttpResponse resp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK,
                Unpooled.copiedBuffer("Denied\r\n",CharsetUtil.UTF_8));
        
        resp.headers().add(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        resp.headers().add(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        resp.headers().set(HttpHeaderNames.CONTENT_LENGTH, resp.content().readableBytes());
        
        return resp;
    }
    
    /**
     * 
     * @param username
     * @param ip
     * @param chId
     * @return
     */
    private String generateTicket(String username, SocketAddress ip, ChannelId chId) {
        
        String seed = username + ip.toString() + chId.toString() + Instant.now().toEpochMilli();

        UUID ticketId = UUID.nameUUIDFromBytes(seed.getBytes());
        
        return ticketId.toString();
    }

}
