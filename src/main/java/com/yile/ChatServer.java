package com.yile;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

/**
 * 群聊服务端
 */
public class ChatServer {

    /**
     * 服务端端口号
     */
    private int port;

    public ChatServer(int port) {
        this.port = port;
    }


    public void run() {

        //定义一个线程的boss节点
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
        //默认的工作节点,线程数为：cpu核数*2
        NioEventLoopGroup workGroup = new NioEventLoopGroup();

        try {
            //服务启动类
            ServerBootstrap sb = new ServerBootstrap();

            sb.group(bossGroup, workGroup).channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        protected void initChannel(SocketChannel ch) throws Exception {
                            //获取管道
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast("decoder", new StringDecoder())
                                    .addLast("encoder", new StringEncoder())
                                    //自定义业务处理器
                                    .addLast(new ChatServerHandler());
                        }
                    }).option(ChannelOption.SO_BACKLOG, 128)
                    .option(ChannelOption.SO_KEEPALIVE, true);

            //绑定端口
            try {
                ChannelFuture cf = sb.bind(port).sync();
                System.out.println("服务器启动........");
                cf.channel().closeFuture().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("服务器启动........");
        } finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        //启动服务
        new ChatServer(7000).run();
    }
}
