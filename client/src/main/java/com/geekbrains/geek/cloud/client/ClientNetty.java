package com.geekbrains.geek.cloud.client;

import com.geekbrains.geek.cloud.common.ProtoHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;
import java.util.Arrays;


public class ClientNetty {
    private Channel clientChannel;
    private ClientController cc;

    public ClientNetty(ClientController cc) {
        this.cc = cc;
    }

    public void run() {
        new Thread(()->{
            EventLoopGroup group = new NioEventLoopGroup();
            try {
                Bootstrap b = new Bootstrap();
                b.group(group);
                b.channel(NioSocketChannel.class);
                b.remoteAddress(new InetSocketAddress("localhost",8189));
                b.handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new ProtoHandler((m)->{
                            if (m.startsWith("/ok")) {
                                cc.refreshLocalFilesList();
                                cc.refreshServerFileList(getMsg(m).split(" "));
                                cc.setAuthorized(true);
                            }
                            if (m.startsWith("/err")) {
                                cc.updateErrorMsg(getMsg(m));
                            }
                            if (m.startsWith("/prog")) {
                                String[] tmp = getMsg(m).split(" ");
                                cc.addProgress(Double.parseDouble(tmp[0]), Long.parseLong(tmp[1]));
                            }
                            if (m.startsWith("/if100")) {
                                if (cc.getProgress().getValue()>=1) {
                                    cc.resetProgress();
                                }
                            }
                        }, false));
                        clientChannel = ch;
                    }
                });
                ChannelFuture f = b.connect().sync();
                f.channel().closeFuture().sync();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    group.shutdownGracefully().sync();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private String getMsg(String msg) {
        String[] tmp = msg.split(" ");
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < tmp.length; i++) {
            sb.append(tmp[i] + " ");
        }
        return sb.toString();
    }

    public Channel getClientChannel() {
        return clientChannel;
    }
}
