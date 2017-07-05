package com.test.chatserver;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpMessage;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpContentDecoder;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpObjectDecoder;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;

public class ChatServerProtocolHandler extends HttpRequestDecoder {
    private ByteBuf buf;
    private List<Object> decoded = new Stack<Object>();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("[ChatServerProtocolHandler] channelread called");

        buf = (ByteBuf) msg;
        if (buf.readableBytes() < 5) {
            return;
        }
        
        final int magic1 = buf.getUnsignedByte(buf.readerIndex());
        final int magic2 = buf.getUnsignedByte(buf.readerIndex() + 1);
        
        if (isPost(magic1,magic2)){
            //Find the JSON object, starting from the end of the HTTP message
            //Name + password should not be longer than 128 chars, so adding in the JSON
            //formatting should not change that either
            char[] chars = new char[128];
            int val = 0;
            for (int i = 0; i < chars.length; i++) {
                val = buf.getUnsignedByte(buf.readableBytes() - chars.length + i);
                chars[i] = (char) val;
            }
            
            boolean jsonComplete = false;
            int startJson, endJson, currentIndex;
            
            startJson = endJson = 0;
            currentIndex = chars.length - 1;
            
            //Find the JSON object
            while (!jsonComplete) {
                
                if (chars[currentIndex] == '}') {
                    endJson = currentIndex;
                }
                if (chars[currentIndex] == '{') {
                    startJson = currentIndex;
                    jsonComplete = true;
                }
                currentIndex--;
                
            }
            
            //make sure it's valid
            if (startJson > endJson || endJson - startJson > 100) {
                ctx.close();
                return;
            }
            
            char[] credentials = new char[endJson - startJson + 1];
            
            for (int i = 0; i < credentials.length; i++) {
                credentials[i] = chars[startJson + i];
            }
            
            String json = new String(credentials);
            System.out.println(json);
            //try to parse username and password
            GsonBuilder builder = new GsonBuilder();
            Object o = builder.create().fromJson(json, Object.class);
            
            System.out.println(o);
            
            return;
        } else {
            System.out.println("ProtocolHandler] packet");
            ctx.fireChannelRead(msg);
        }

        return;
    }

    private static boolean isPost(int magic1, int magic2) {
        return  magic1 == 'P' && magic2 == 'O';
    }





}
