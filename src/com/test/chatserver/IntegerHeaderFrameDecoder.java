package com.test.chatserver;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

public class IntegerHeaderFrameDecoder extends ReplayingDecoder<DecoderState> {

    private int len;

    public IntegerHeaderFrameDecoder() {
        super(DecoderState.READ_LENGTH);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> out) throws Exception {
        switch (state()) {
        case READ_LENGTH:
            len = buf.readInt();
            if (len <= 0){
                throw new Exception("Received empty FlatBuffer");
            }
            checkpoint(DecoderState.READ_CONTENT);
        case READ_CONTENT:
            ByteBuf frame = buf.readBytes(len);
            checkpoint(DecoderState.READ_LENGTH);
            out.add(frame);
            break;
        default:
            throw new Error("Shouldn't reach here.");
        }
    }

}
