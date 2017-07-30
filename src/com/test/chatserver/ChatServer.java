package com.test.chatserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import io.netty.bootstrap.ServerBootstrap;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpHeaders.Names;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.cors.CorsConfig;
import io.netty.handler.codec.http.cors.CorsConfigBuilder;
import io.netty.handler.codec.http.cors.CorsHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import io.netty.util.concurrent.GlobalEventExecutor;
    
/**
 * A simple chat server meant to be used between people who have a chat client.
 */
public class ChatServer {

    public static final long TICKET_CLEANUP_TIME_MS = 30000;
    public static final long TICKET_EXPIRY_TIME_MS = 20000;
    public static final int LOBBY_SIZE = 16;
    public static final int NUM_LOBBIES = 10;
	static boolean SSL = false;
	
	/**
	 * the server should initialize with a channel group,allUsers,and pass this
	 * channelGroup down to the appropriate pipelines so that they can reference and
	 * alter it as they see fit.
	 * 
	 */
	private final ChannelGroup allChannels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
	private final List<NamedChannelGroup> games = Collections.synchronizedList(new ArrayList<NamedChannelGroup>());
	private final List<NamedChannelGroup> lobbies = Collections.synchronizedList(new ArrayList<NamedChannelGroup>());
	private final Map<String, TimeChatMessage> sessionTicketDB = new ConcurrentHashMap<String,TimeChatMessage>();
	
	//static final int DEFAULT_PORT = Integer.parseInt(System.getProperty("port", SSL? "8443" : "8080"));
   
    private int port;
    
    public ChatServer(int port) {
        this.port = port;
    }
    
    public void run() throws Exception {
        
    	final SslContext sslCtx;
        if (SSL) {
        	System.out.print("Running SSL");
            SelfSignedCertificate ssc = new SelfSignedCertificate();
            sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
        } 
        else {
        	System.out.println("Running Websockets insecure");
        	sslCtx = null;
        }
    	System.out.println(" on port " + this.port);
        EventLoopGroup bossGroup = new NioEventLoopGroup(); 
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        
        initLobbies();
        
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
             .channel(NioServerSocketChannel.class) 
             .handler(new LoggingHandler(LogLevel.INFO))
             .childHandler(new ChannelInitializer<SocketChannel>() { 
                 @Override
                 public void initChannel(SocketChannel ch) throws Exception {
                     
                     CorsConfigBuilder builder = CorsConfigBuilder.forAnyOrigin();
                     builder.allowCredentials();
                     builder.allowedRequestMethods(HttpMethod.POST,HttpMethod.OPTIONS,HttpMethod.GET);
                     CorsConfig config = builder.build();
                     
                     ch.pipeline().addFirst("corsHandler", new CorsHandler(config));
                     
                     ch.pipeline().addLast("protocolHandler", new ChatServerProtocolHandler(sessionTicketDB, 
                             lobbies, allChannels, games));
                     ch.pipeline().addLast("authHandler", new ServerAuthHandler(sessionTicketDB));
                	 
                     //ch.pipeline().addLast(new HTTPInitializer(sslCtx));
                 }
             })
             .option(ChannelOption.SO_BACKLOG, 128)          // (5)
             .childOption(ChannelOption.SO_KEEPALIVE, true); // (6)
    
            // Bind and start to accept incoming connections.
            ChannelFuture f = b.bind(port).sync(); // (7)
            
            //Clean every 30 seconds
            java.util.Timer t = new java.util.Timer();
            t.schedule(new TimerTask() {

                        @Override
                        public void run() {
                            System.out.println("cleaning ticket db");
                            for(String key : sessionTicketDB.keySet()) {
                                if (Instant.now().toEpochMilli() - sessionTicketDB.get(key).getTime() 
                                        >= TICKET_EXPIRY_TIME_MS) {
                                    sessionTicketDB.remove(key);
                                }
                            }

                        }
                    }, TICKET_CLEANUP_TIME_MS, TICKET_CLEANUP_TIME_MS);
            
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
    	int port = 8080;
    	
    	
    	//System.setProperty("javax.net.ssl.trustStore", "src/keystore.jks");
    	//SSL = System.getProperty("java.net.ssl.trustStore") != "";
    	
        //System.setProperty("javax.net.ssl.trustStore", "src/X509_certificate.cer");
        //System.setProperty("javax.net.ssl.keyStorePassword", "joseph");
        
        System.out.println(System.getProperty("javax.net.ssl.trustStore") + " is the SSL property");
        	
    	port = SSL ? 8443: 8080;
        if (args.length > 0) {
        	try {
        		port = Integer.parseInt(args[0]);
        	}
        	catch (Exception e) {
        		System.out.println("WARNING: Bad argument passed to MAIN. Assigning default port.");
        	}
        } 

        new ChatServer(port).run();
    }
    
    /**
     * Populates lobbies arrayList with randomly named ChannelGroups
     */
    private void initLobbies() {
        String lobbyNameFile = "src/star_names.txt";

        try (Stream<String> stream = Files.lines(Paths.get(lobbyNameFile))) {
            Set<Integer> nums = new HashSet<Integer>();
            Object[] parsed = stream.toArray();
            
            while (nums.size() < NUM_LOBBIES) {
                nums.add((int) (Math.random() * parsed.length));
            }

            Iterator<Integer> iter = nums.iterator();
            for (int i = 0; i < NUM_LOBBIES; i++) {
                lobbies.add(new NamedChannelGroup((String) parsed[iter.next()], GlobalEventExecutor.INSTANCE));
            }

        }
        catch (IOException e) {
            e.printStackTrace();
        }
        
    }
}