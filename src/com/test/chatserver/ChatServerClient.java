package com.test.chatserver;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.ssl.util.SelfSignedCertificate;
 
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Scanner;

import com.google.gson.Gson;
 
/**
 * 
 * Websocket chat server client to be used with simple chat server.
 * Sends messages to the chat server as JSON objects and receives
 * updates to the client chatbox as JSON objects.
 * 
 * 
 * 
 * @author Jalbatross (Joey Albano)
 *
 */

public class ChatServerClient {
	static final String URL = System.getProperty("url", "wss://127.0.0.1:8443/websocket");
	static private String name = new String();
	
	public static void main(String[] args) throws Exception {
	    
	    //get username
	    System.out.println("Hello! Before we begin, what's your name?");
	    Scanner reader = new Scanner(System.in);  // Reading from System.in
	    name = reader.next();
	    
		URI uri = new URI(URL);
		String scheme = uri.getScheme() == null? "ws" : uri.getScheme();
		final String host = uri.getHost() == null? "127.0.0.1" : uri.getHost();
		final int port;
		if (uri.getPort() == -1) {   	  
			if ("ws".equalsIgnoreCase(scheme)) {
				port = 80;
			} 
			else if ("wss".equalsIgnoreCase(scheme)) {
				port = 443;
			} 
			else {
                  port = -1;
            }
        }
	    else {
	    	port = uri.getPort();
        }
		
		if (!"ws".equalsIgnoreCase(scheme) && !"wss".equalsIgnoreCase(scheme)) {
			System.err.println("Only WS(S) is supported.");
			return;
		}
		
		final boolean ssl = "wss".equalsIgnoreCase(scheme);
		final SslContext sslCtx;
		if (ssl) {
			System.out.println("Connecting to server with wss");
			sslCtx = SslContextBuilder.forClient()
                     .trustManager(InsecureTrustManagerFactory.INSTANCE).build();
          } 
		else {
			sslCtx = null;
        }
		
		EventLoopGroup group = new NioEventLoopGroup();
		try {
              // Connect with V13 (RFC 6455 aka HyBi-17). You can change it to V08 or V00.
              // If you change it to V00, ping is not supported and remember to change
              // HttpResponseDecoder to WebSocketHttpResponseDecoder in the pipeline.
			final WebSocketClientHandler handler =
					new WebSocketClientHandler(
                        WebSocketClientHandshakerFactory.newHandshaker(
                        uri, WebSocketVersion.V13, null, false, new DefaultHttpHeaders()));
			
			Bootstrap b = new Bootstrap();
			b.group(group)
            .channel(NioSocketChannel.class)
            .handler(new ChannelInitializer<SocketChannel>() {
            	@Override
            	protected void initChannel(SocketChannel ch) {
            		ChannelPipeline p = ch.pipeline();
            		if (sslCtx != null) {
            			p.addLast(sslCtx.newHandler(ch.alloc(), host, port));
            		}
            		p.addLast(
                              new HttpClientCodec(),
                              new HttpObjectAggregator(8192),
                              handler);
                }
            });
			
			Channel ch = b.connect(uri.getHost(), port).sync().channel();
			handler.handshakeFuture().sync();
			
			//User input
			BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
			while (true) {
				String msg = console.readLine();
				if (msg == null) {
					break;
                } 
				else if ("bye".equals(msg.toLowerCase())) {
					ch.writeAndFlush(new CloseWebSocketFrame());
					ch.closeFuture().sync();
					break;
                }
				else if ("ping".equals(msg.toLowerCase())) {
					WebSocketFrame frame = new PingWebSocketFrame(Unpooled.wrappedBuffer(new byte[] { 8, 1, 8, 1 }));
					ch.writeAndFlush(frame);
                } 
				else {
				    ChatMessage message = new ChatMessage(name, msg);
				    String sendMessage = new Gson().toJson(message);
					WebSocketFrame frame = new TextWebSocketFrame(sendMessage);
					ch.writeAndFlush(frame);
                }
            }
        } 
		finally {
			group.shutdownGracefully();
		}
	}
}