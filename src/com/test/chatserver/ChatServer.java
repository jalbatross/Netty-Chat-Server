package com.test.chatserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

import io.netty.bootstrap.ServerBootstrap;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import sun.misc.IOUtils;
    
/**
 * A simple chat server meant to be used between people who have a chat client.
 */
public class ChatServer {
 
	static final boolean SSL = System.getProperty("ssl") != null;
	
    static final int DEFAULT_PORT = Integer.parseInt(System.getProperty("port", SSL? "8443" : "8080"));
   
    private int port;
    
    public ChatServer(int port) {
        this.port = port;
    }
    
    private static byte[] loadFile(File file) throws IOException {
	    InputStream is = new FileInputStream(file);

	    long length = file.length();
	    if (length > Integer.MAX_VALUE) {
	        // File is too large
	    }
	    byte[] bytes = new byte[(int)length];
	    
	    int offset = 0;
	    int numRead = 0;
	    while (offset < bytes.length
	           && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
	        offset += numRead;
	    }

	    if (offset < bytes.length) {
	        throw new IOException("Could not completely read file "+file.getName());
	    }

	    is.close();
	    return bytes;
	}
    
    public void run() throws Exception {
    	
    	//configure SSL
    	final SslContext sslCtx;
        if (SSL) {
            SelfSignedCertificate ssc = new SelfSignedCertificate();
            sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
        } 
        else {
        	sslCtx = null;
        }
    	
        EventLoopGroup bossGroup = new NioEventLoopGroup(); 
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
             .channel(NioServerSocketChannel.class) 
             .handler(new LoggingHandler(LogLevel.INFO))
             .childHandler(new ChannelInitializer<SocketChannel>() { 
                 @Override
                 public void initChannel(SocketChannel ch) throws Exception {
                	 //need ENCODER for updated messages and DECODER to receive messages from clients
                	 ch.pipeline().addLast(new HTTPInitializer(sslCtx));
                	 ch.pipeline().addLast(new ChatServerHandler());
                
                 }
             })
             .option(ChannelOption.SO_BACKLOG, 128)          // (5)
             .childOption(ChannelOption.SO_KEEPALIVE, true); // (6)
    
            // Bind and start to accept incoming connections.
            ChannelFuture f = b.bind(port).sync(); // (7)
    
            // Wait until the server socket is closed.
            // In this example, this does not happen, but you can do that to gracefully
            // shut down your server.
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
    
    public static void main(String[] args) throws Exception {
    	int port = DEFAULT_PORT;
    	
    	System.setProperty("javax.net.ssl.keyStore", "src/keystore.jks");
        System.setProperty("javax.net.ssl.trustStrore", "src/X509_certificate.cer");
        System.setProperty("javax.net.ssl.keyStorePassword", "");
        
        System.out.println(System.getProperty("ssl") + " is the SSL property");
    	if (SSL) {
    		System.out.println("Server is WSS");
    	}
    	else {
    		System.out.println("Server is WS");
    	}
    		
        if (args.length > 0) {
        	try {
        		port = Integer.parseInt(args[0]);
        	}
        	catch (Exception e) {
        		System.out.println("WARNING: Bad argument passed to MAIN. Assigning default port.");
        		port = DEFAULT_PORT;
        	}
        } 

        new ChatServer(port).run();
    }
}