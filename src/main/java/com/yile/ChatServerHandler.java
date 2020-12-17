package com.yile;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

/**
 * 服务端业务处理器
 */
public class ChatServerHandler extends SimpleChannelInboundHandler<String> {

    /**
     * 定义channel组，管理所有的channel
     * (GlobalEventExecutor.INSTANCE 全局的时间执行器(单例)
     */
    private static ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    /**
     * 表示连接建立，一旦连接，第一个被执行
     * 将当前的channel加入到channelGroup
     * @param ctx
     * @throws Exception
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        //将信息发送到channelGroup所有的channel中，无需遍历，channelGroup会内部遍历
        channelGroup.writeAndFlush("[客户端]" + channel.remoteAddress() + "加入群聊\n");
        //将channel加入channelGroup
        channelGroup.add(channel);
    }

    /**
     * handlerAdded与channelActive ：当有一个客户端连接服务端是，首先调用的是handlerAdded方法然后在调用channelActive方法
     */

    /**
     * 表示channel表示活动状态，提示上线
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println(ctx.channel().remoteAddress() + "上线\n");
    }

    /**
     * 断开连接，将离开的channel信息发送给所有在线的channel
     * 无需调用channelGroup.remove(Object o)方法，handlerRemoved方法会自动删除
     * @param ctx
     * @throws Exception
     */
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        channelGroup.writeAndFlush("[客户端]" + channel.remoteAddress() + "离开群聊\n");
        System.out.println("当前channelGroup大小" + channelGroup.size());
    }


    /**
     * handlerRemoved与channelInactive ：当有一个客户端断开与服务端连接时，首先调用的是channelInactive方法然后在调用handlerRemoved方法
     */
    /**
     * 表示channel表示非活动状态，提示离线
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println(ctx.channel().remoteAddress() + "离线\n");
    }

    /**
     * 聊天业务
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        Channel channel = ctx.channel();
        channelGroup.forEach(ch -> {
            if(channel != ch){ //不是当前channel，直接转发
                ch.writeAndFlush("[客户端]" + channel.remoteAddress() + ":" + msg + "\n");
            }else{
                ch.writeAndFlush("自己发送了：" + msg + "\n");
            }
        });
    }

    //异常处理
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}
