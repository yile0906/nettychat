package com.yile;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import javax.sound.sampled.Port;
import java.util.Scanner;

public class ChatClient {
    //主机地址
    private final String host;
    private final int port;

    public ChatClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void run() {
        EventLoopGroup eventGroup = new NioEventLoopGroup();

        try {
            Bootstrap bootstrap = new Bootstrap();

            bootstrap.group(eventGroup).channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ChannelPipeline pipeline = ch.pipeline();
                    pipeline.addLast("decoder", new StringDecoder())
                            .addLast("encoder", new StringEncoder())
                            //自定义业务处理器
                            .addLast(new ChatHandler());
                }
            });
            try {
                ChannelFuture channelFuture = bootstrap.connect(host,port).sync();
                Channel channel = channelFuture.channel();
                System.out.println("-------------" + channel.localAddress() + "------------------");
                //发动信息业务
                Scanner scanner = new Scanner(System.in);
                while (scanner.hasNextLine()){
                    String s = scanner.nextLine();
                    channel.writeAndFlush(s);
                }
                channel.closeFuture().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


        } finally {
            eventGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        new ChatClient("127.0.0.1",7000).run();
    }
}
