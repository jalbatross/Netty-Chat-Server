package com.test.chatserver;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
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
import io.netty.util.AttributeKey;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Scanner;

import com.google.gson.Gson;

import Schema.Credentials;

 
/**
 * 
 * Websocket chat server client to be used with simple chat server.
 * Sends messages to the chat server and receives updates to the client
 *  chatbox via JSON objects.
 * 
 * 
 * 
 * @author Jalbatross (Joey Albano)
 *
 */

public class ChatClient {
	
    //Regular Websockets
    static final String URL = System.getProperty("url", "ws://127.0.0.1:8080/websocket");
    //static final String URL = System.getProperty("url", "ws://34.212.146.20:8080/websocket");
    
    //Websocket Secure
    //static final String URL = System.getProperty("url", "wss://127.0.0.1:8443/websocket");
    //static final String URL = System.getProperty("url", "wss://34.212.146.20:8080/websocket");
    
	private static String name = new String();
	private static String pwd = new String();
	final static AttributeKey<Boolean> AUTHKEY = AttributeKey.valueOf("authorized");
	
	public static void main(String[] args) throws Exception {
	    
	    //get username
	    System.out.println("Hello! Before we begin, what's your name?");
	    Scanner reader = new Scanner(System.in);  // Reading from System.in
	    name = reader.nextLine();
	    
	    System.out.println("and your password?");
	    pwd = reader.nextLine();
	    
		URI uri = new URI(URL);
		String scheme = uri.getScheme() == null? "ws" : uri.getScheme();
		

		//final String host = uri.getHost() == null? "34.212.146.20" : uri.getHost();
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
            		p.addLast(new ChatClientDecoder());
            		p.addLast(new ChatClientAuthHandler());
            		
            		/*
            		if (sslCtx != null) {
            			p.addLast(sslCtx.newHandler(ch.alloc(), host, port));
            		}
            		p.addLast(
                              new HttpClientCodec(),
                              new HttpObjectAggregator(8192),
                              handler);
                    *
                    */
                }
            });
			
			Channel ch = b.connect(uri.getHost(), port).sync().channel();
			ch.attr(AUTHKEY).set(false);
			
			/*
			handler.handshakeFuture().sync().addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    ch.writeAndFlush(new TextWebSocketFrame(name));
                }
			});
			*/
			
			ByteBuffer flatbuf = FlatBuffersCodec.credentialsToByteBuffer(name, pwd);
			
			//Get size of Flatbuffer
			byte[] len = new byte[4];
			len = ByteBuffer.wrap(len).putInt(flatbuf.remaining()).array();
			
			//Prepend flatbuffer with length
			ByteBuf lenPrefix = Unpooled.copiedBuffer(len);
			ByteBuf wordBuf = Unpooled.copiedBuffer(flatbuf);
			
			//Write to channel
			ch.write(lenPrefix);
			ch.writeAndFlush(wordBuf);
			
			Thread.sleep(1000);
			BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
			
			while (ch.attr(AUTHKEY).get() == false) {
			    System.out.println("bad username and pass! try again");
			    
			    System.out.println("enter a username: ");
			    name = console.readLine();
			    System.out.println("enter a password: ");
			    pwd = console.readLine();
			   
			    if (name == null || pwd == null) {
			        return;
			    }
			    flatbuf = FlatBuffersCodec.credentialsToByteBuffer(name, pwd);
	            
	            //Get size of Flatbuffer
	            Arrays.fill(len, (byte) 0); 
	            len = ByteBuffer.wrap(len).putInt(flatbuf.remaining()).array();
	            
	            //Prepend flatbuffer with length
	            lenPrefix = Unpooled.copiedBuffer(len);
	            wordBuf = Unpooled.copiedBuffer(flatbuf);
	            
	            //Write to channel
	            ch.write(lenPrefix);
	            ch.writeAndFlush(wordBuf);
	            Thread.sleep(1000);
			}
			
			System.out.println("user authenticated!!");
			
			//User input
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
				else if ("buf".equals(msg.toLowerCase())){
				    //TODO: Create a bytebuf and send to server
				    //Make an imaginary byte array:
				    //[ short, int, int, int] = 14 bytes
				    //[69, 0, 4, 1050]
				    short lobbyID = 31;
				    int senderID = 2;
				    int receiverID = 3;
				    int value = 985;
				    
				    byte[] bytes = ByteBuffer.allocate(14)
				            .putShort(lobbyID)
				            .putInt(senderID)
				            .putInt(receiverID)
				            .putInt(value).array();
				    ByteBuf myBuf = Unpooled.copiedBuffer(bytes);
				    WebSocketFrame frame = new BinaryWebSocketFrame(myBuf);
				    System.out.println("Sending 31, 2, 3, 985");
				    ch.writeAndFlush(frame);
				}
				else {
					WebSocketFrame frame = new TextWebSocketFrame(msg);
					ch.writeAndFlush(frame);
                }
            }
        } 
		finally {
			group.shutdownGracefully();
		}
	}
}