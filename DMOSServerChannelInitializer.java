package com.dmos.dmos_server;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;

public class DMOSServerChannelInitializer extends ChannelInitializer<SocketChannel> {
    private ChannelInboundHandlerAdapter handlerAdapter;
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        //添加编解码
        socketChannel.pipeline().addLast("decoder", new StringDecoder(CharsetUtil.UTF_8));
        socketChannel.pipeline().addLast("encoder", new StringEncoder(CharsetUtil.UTF_8));
        socketChannel.pipeline().addLast("split", new LineBasedFrameDecoder(Integer.MAX_VALUE));

        socketChannel.pipeline().addLast(handlerAdapter);
    }
    DMOSServerChannelInitializer(ChannelInboundHandlerAdapter handlerAdapter){
        this.handlerAdapter = handlerAdapter;
    }
}
