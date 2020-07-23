package com.sanshengshui.netty.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * @author 穆书伟
 * @date 2018年9月18号
 * @description 服务端启动程序
 *
 * TODO 1、服务端由两种线程池，用于Acceptor的React主线程和用于I/O操作的React从线程池； 客户端只有用于连接及IO操作的React的主线程池；
 * TODO 2、ServerBootstrap中定义了服务端React的"从线程池"对应的相关配置，都是以child开头的属性。 而用于"主线程池"channel的属性都定义在AbstractBootstrap中；
 *
 *
 */
public final class Server {
    public  static void main(String[] args) throws Exception {
        //TODO 主线程池group
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        // todo 子线程池childGroup
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            //todo server启动类；
            ServerBootstrap server = new ServerBootstrap();
            server.group(bossGroup,workerGroup)
                    //TODO 无论是服务端还是客户端，channel调用的都是基类的channel方法
                    .channel(NioServerSocketChannel.class)

                    //TODO handler： 设置主通道的处理器， 对于服务端而言就是ServerSocketChannel，也就是用来处理Acceptor的操作；
                    //TODO　对于客户端的SocketChannel，主要是用来处理 业务操作；
                    //todo 处理Acceptor操作；
                    .handler(new LoggingHandler(LogLevel.ERROR))

                    //TODO 对于服务端而言，有两种通道需要处理， 一种是ServerSocketChannel：用于处理用户连接的accept操作，
                    //TODO 另一种是SocketChannel，表示对应客户端连接。而对于客户端，一般都只有一种channel，也就是SocketChannel。
                    .childHandler(new ServerInitializer());
            ChannelFuture f = server.bind(8888);

            //todo 让线程进入wait状态，也就是main线程暂时不会执行到finally里面，nettyserver也持续运行，如果监听到关闭事件，可以优雅的关闭通道和nettyserver
            f.channel().closeFuture().sync();
        } finally {

            //todo 关闭线程池；
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
