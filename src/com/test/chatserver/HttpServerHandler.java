package com.test.chatserver;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
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
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.handler.timeout.IdleStateHandler;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;

public class HttpServerHandler extends ChannelInboundHandlerAdapter {
	WebSocketServerHandshaker handshaker;
	
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("httpseverhandler caleld");
        
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
                byte[] CONTENT = { 'N','i','k','F','u','r','g','s'};
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

                //Adding new handler to the existing pipeline to handle WebSocket Messages
                ctx.pipeline().replace(this, "websocketHandler", new WebSocketHandler());
                ctx.pipeline().addLast(new IdleStateHandler(5, 3, 10));
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

}