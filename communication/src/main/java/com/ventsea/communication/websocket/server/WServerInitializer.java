package com.ventsea.communication.websocket.server;

import java.util.concurrent.TimeUnit;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.timeout.IdleStateHandler;

public class WServerInitializer extends ChannelInitializer<SocketChannel> {

    private ChannelGroup group;
    private WServerHandler handler;
    private IServer.IServerListener serverListener;

    WServerInitializer(ChannelGroup group, IServer.IServerListener listener) {
        this.group = group;
        this.serverListener = listener;
    }

    void setServerListener(IServer.IServerListener listener) {
        if (handler != null) handler.setServerListener(listener);
    }

    @Override
    protected void initChannel(SocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new HttpObjectAggregator(64 * 1024));
            /*一般http请求或者响应,解码器都将其解码成为多个消息对象,主要是httpRequest/httpResponse, httpContent, lastHttpContent.
               然后反复调用messageReceive这个方法,HttpObjectAggregator 这个handler就是将同一个http请求或响应的多个消息对象变成一个 fullHttpRequest完整的消息对象。
               FullHttpRequest的协议头和Form数据是在一起的，不需要分开读，DefaultHttpRequest与HttpContent，FullHttpRequest就是通过HttpObjectAggregator把两者合在一起。*/
        pipeline.addLast(new IdleStateHandler(8, 0, 0, TimeUnit.SECONDS));
        pipeline.addLast(handler = new WServerHandler(group, serverListener));
    }
}
