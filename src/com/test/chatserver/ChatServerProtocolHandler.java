package com.test.chatserver;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpMessage;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpObjectDecoder;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;

public class ChatServerProtocolHandler extends HttpRequestDecoder {
    private ByteBuf buf;
    private Stack<Object> decoded = new Stack<Object>();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("[ChatServerProtocolHandler] channelread called");

        buf = (ByteBuf) msg;
        if (buf.readableBytes() < 5) {
            return;
        }

        System.out.println(buf.getCharSequence(0, buf.readableBytes(), Charset.defaultCharset()));
        final int magic1 = buf.getUnsignedByte(buf.readerIndex());
        final int magic2 = buf.getUnsignedByte(buf.readerIndex() + 1);
        
        if (isHttp(magic1,magic2)){
            super.decode(ctx, buf, decoded);
            System.out.println("[ProtocolHandler] got http request");
            while (!decoded.isEmpty()){
                
                if (decoded.peek() instanceof LastHttpContent) {
                    Object content = (Object) decoded.pop();
                    System.out.println("last: " + content);
                    continue;
                }
                
                Object obj = (Object) decoded.pop();

                System.out.println(obj.toString());
                HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, 
                        HttpResponseStatus.ACCEPTED);
                ctx.writeAndFlush(response);
            }
            
            return;
        } else {
            System.out.println("ProtocolHandler] packet");
            ctx.fireChannelRead(msg);
        }

        return;
    }

    private static boolean isHttp(int magic1, int magic2) {
        return  magic1 == 'G' && magic2 == 'E' || // GET
                magic1 == 'P' && magic2 == 'O' || // POST
                magic1 == 'P' && magic2 == 'U' || // PUT
                magic1 == 'H' && magic2 == 'E' || // HEAD
                magic1 == 'O' && magic2 == 'P' || // OPTIONS
                magic1 == 'P' && magic2 == 'A' || // PATCH
                magic1 == 'D' && magic2 == 'E' || // DELETE
                magic1 == 'T' && magic2 == 'R' || // TRACE
                magic1 == 'C' && magic2 == 'O'; // CONNECT
    }


}
