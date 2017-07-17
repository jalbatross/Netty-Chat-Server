package com.test.chatserver;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpMessage;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpHeaders.Values;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class HttpServerHandler extends ChannelInboundHandlerAdapter {
	WebSocketServerHandshaker handshaker;
	public static final int PING_TIMER_SECONDS = 3;
	public static final int PING_TIMEOUT_SECONDS = 5;
	public static final long TICKET_TIMEOUT_MS = 30000;
	
	private ChannelGroup allChannels;
    private List<ChannelGroup> lobbies;
	private Map<String,TimeChatMessage> ticketDB;
	
    public HttpServerHandler(Map<String, TimeChatMessage> ticketDB) {
        this.ticketDB = ticketDB;
    }

    public HttpServerHandler(Map<String, TimeChatMessage> ticketDB,List<ChannelGroup> lobbies, 
            ChannelGroup channels) {
        this.ticketDB = ticketDB;
        this.allChannels = channels;
        this.lobbies = lobbies;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        
        if (msg instanceof FullHttpMessage) {
            System.out.println("Full HTTP Message Received");
        }
        else if (msg instanceof HttpRequest) {

            if (msg instanceof FullHttpRequest) {
                System.out.println("Full HTTP Request");
            }

            HttpRequest httpRequest = (HttpRequest) msg;
            
            System.out.println("Http Request Received");

            HttpHeaders headers = httpRequest.headers();
            
            //Non Websockets
            if (headers.get("Upgrade") == null) {
                byte[] CONTENT = "Need to do websockets!!".getBytes();
                System.out.println("No upgrade in headers. Skipping.");
                
                FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK,
                        Unpooled.wrappedBuffer(CONTENT));
                response.headers().set("CONTENT_TYPE", "text/plain");
                response.headers().set("CONTENT_LENGTH", response.content().readableBytes());

                response.headers().set("CONNECTION", HttpHeaderValues.KEEP_ALIVE);
                ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
                return;
            }
            
            System.out.println("Headers: " + headers.names().toString());
            
            System.out.println("Connection : " +headers.get("Connection"));
            System.out.println("Upgrade : " + headers.get("Upgrade"));
            
            if (headers.get("Connection").equalsIgnoreCase("Upgrade") ||
                    headers.get("Upgrade").equalsIgnoreCase("WebSocket")) {
                
                QueryStringDecoder decoder = new QueryStringDecoder(httpRequest.uri());
                String ticketId = decoder.parameters().get("ticket").get(0);
                
                //Make sure the client has a valid WS ticket
                if (!validSession(ctx, ticketId)) {
                    //TODO: send message to client on fail
                    ctx.close();
                    return;
                }
                System.out.println("Valid session, proceed to handshake");
                String username = ticketDB.remove(ticketId).author;
                
                //Adding new handler to the existing pipeline to handle WebSocket Messages
                ctx.pipeline().replace(this, "websocketHandler", new WebSocketHandler());
                ctx.pipeline().addLast("chatHandler", new ChatServerHandler(ctx, username, lobbies, allChannels));
                ctx.pipeline().addLast(new IdleStateHandler(PING_TIMEOUT_SECONDS, PING_TIMER_SECONDS, 0));
                ctx.pipeline().addLast( "serverPing", new ServerPing());
                System.out.println("WebSocketHandler added to the pipeline");

                System.out.println("Opened Channel : " + ctx.channel());

                System.out.println("Handshaking....");
                //Do the Handshake to upgrade connection from HTTP to WebSocket protocol
                handleHandshake(ctx, httpRequest);
                System.out.println("Handshake is done");
                
            }
        } 
        else {
            System.out.println("Incoming request is unknown");
            //send something to client to let them know they aren't using WS
        }

    }

    /* Do the handshaking for WebSocket request */
    protected void handleHandshake(ChannelHandlerContext ctx, HttpRequest req) throws URISyntaxException {
        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(getWebSocketURL(req),
                                                                                          null, true);
        handshaker = wsFactory.newHandshaker(req);
        if (handshaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
        } 
        else {
            handshaker.handshake(ctx.channel(), req);
        }
    }


    protected String getWebSocketURL(HttpRequest req) {
        System.out.println("Req URI : " + req.uri());
        String url =  "ws://" + req.headers().get("Host") + req.uri() ;
        System.out.println("Constructed URL : " + url);
        return url;
    }
    
    /**
     * Checks whether or not a given ticketId is present in the 
     * session ticket database. Then, checks if the ticket is expired by
     * comparing the timestamp on the ticket in the db to the current moment
     * and makes sure that the remote IP address of the current channel
     * matches the one in the ticket DB.
     * 
     * If all of these things are true, returns true. Otherwise returns false.
     * 
     * @param ctx        Channel handler context
     * @param ticketId   Ticket id from websockets handshake request
     * @return           True if ticketId is valid
     */
    private boolean validSession(ChannelHandlerContext ctx, String ticketId) {

        if (ticketDB.get(ticketId) != null) {
            TimeChatMessage ticketInfo = ticketDB.get(ticketId);

            if (Instant.now().toEpochMilli() - ticketInfo.getTime() >= TICKET_TIMEOUT_MS) {
                System.out.println("Ticket expired");
                return false;
            }

            String addy = ((InetSocketAddress) ctx.channel().remoteAddress()).getAddress().toString();

            if (!addy.equals(ticketInfo.message)) {
                System.out.println("remote address mismatch");
                return false;
            }
            
            return true;
        }
        
        return false;

    }

}