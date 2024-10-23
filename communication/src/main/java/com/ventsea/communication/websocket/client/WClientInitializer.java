package com.ventsea.communication.websocket.client;

import java.net.URI;
import java.util.concurrent.TimeUnit;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketClientCompressionHandler;
import io.netty.handler.timeout.IdleStateHandler;

public class WClientInitializer extends ChannelInitializer<SocketChannel> {

    private WClientHandler handler;

    WClientInitializer(URI uri, IClient.IClientListener clientListener) {
        handler = new WClientHandler(WebSocketClientHandshakerFactory.newHandshaker(
                uri,
                WebSocketVersion.V13,
                null,
                true,
                new DefaultHttpHeaders()), clientListener);
    }

    WClientHandler getHandler() {
        return handler;
    }

    void setClientListener(IClient.IClientListener listener) {
        handler.setClientListener(listener);
    }

    @Override
    protected void initChannel(SocketChannel ch) {
        ChannelPipeline p = ch.pipeline();
        p.addLast(new HttpClientCodec());
        p.addLast(new HttpObjectAggregator(8192));
        p.addLast(new IdleStateHandler(8, 0, 0, TimeUnit.SECONDS));
        p.addLast(WebSocketClientCompressionHandler.INSTANCE, handler);
    }
}
