package com.dmos.dmos_server.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

@Slf4j
public class DMOSServer {
    public boolean isRunning = false;

    private EventLoopGroup bossGroup = null;
    private EventLoopGroup workGroup = null;
    private ServerBootstrap bootstrap = null;
    public void start(InetSocketAddress socketAddress, ChannelInboundHandlerAdapter handlerAdapter){
        bossGroup = new NioEventLoopGroup(5);
        //new 一个工作线程组
        workGroup = new NioEventLoopGroup(200);
        bootstrap = new ServerBootstrap()
                .group(bossGroup, workGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new DMOSServerChannelInitializer(handlerAdapter))
                .localAddress(socketAddress)
                //设置队列大小
                .option(ChannelOption.SO_BACKLOG, 1024)
                // 两小时内没有数据的通信时,TCP会自动发送一个活动探测数据报文
                .childOption(ChannelOption.SO_KEEPALIVE, true);
        //绑定端口,开始接收进来的连接
        try {
            ChannelFuture future = bootstrap.bind(socketAddress).sync();
            log.info("服务器启动开始监听端口: {}", socketAddress.getPort());
            isRunning = true;
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            log.info("服务端执行结束");
            //关闭主线程组
            bossGroup.shutdownGracefully();
            //关闭工作线程组
            workGroup.shutdownGracefully();
            isRunning = false;
        }

    }

    public void stop(){
        if(bossGroup != null && !bossGroup.isShutdown()){
            bossGroup.shutdownGracefully();
        }
        if(workGroup != null && !workGroup.isShutdown()){
            workGroup.shutdownGracefully();
        }
        isRunning = false;
        log.info("关闭服务器");
    }
}
