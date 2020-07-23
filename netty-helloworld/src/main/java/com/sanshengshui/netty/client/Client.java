package com.sanshengshui.netty.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public final class Client {
    public static void main(String[] args) throws Exception {
        EventLoopGroup workerThreadPool = new NioEventLoopGroup();
        try {
            Bootstrap client = new Bootstrap();
            client.group(workerThreadPool)
                    .channel(NioSocketChannel.class)
                    .handler(new ClientInitializer());
            Channel channel = client.connect("127.0.0.1",8888).sync().channel();
            ChannelFuture channelFuture = null;
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            for (;;) {
                String line = in.readLine();
                if (line == null) {
                    break;
                }

                // Sends the received line to the server.
                channelFuture = channel.writeAndFlush(line + "\r\n");

                // If user typed the 'bye' command, wait until the server closes
                // the connection.
                if ("bye".equals(line.toLowerCase())) {
                    channel.closeFuture().sync();
                    break;
                }
            }

            // Wait until all messages are flushed before closing the channel.
            if (channelFuture != null) {
                channelFuture.sync();
            }
        } finally {
            workerThreadPool.shutdownGracefully();
        }
    }
}
