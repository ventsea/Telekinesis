package com.ventsea.communication.http.file;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;

class FileServerInitializer extends ChannelInitializer<SocketChannel> {

    private FileServerHandler serverHandler;
    private IFileServer.FileServerListener listener;

    FileServerInitializer(IFileServer.FileServerListener listener) {
        this.listener = listener;
    }

    void setSendFileListener(IFileServer.FileServerListener listener) {
        if (serverHandler != null) serverHandler.setSendFileListener(listener);
    }

    @Override
    protected void initChannel(SocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new HttpObjectAggregator(65536));
        pipeline.addLast(new ChunkedWriteHandler());
        pipeline.addLast(serverHandler = new FileServerHandler(listener));
    }
}
