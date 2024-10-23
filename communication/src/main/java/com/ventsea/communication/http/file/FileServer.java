package com.ventsea.communication.http.file;

import android.util.Log;

import com.ventsea.communication.threadmode.MyExecutors;
import com.ventsea.communication.utils.P2PUtil;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class FileServer implements IFileServer {

    static final String TAG = "FileServer";
    private static FileServer INSTANCE;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel channel;
    private FileServerInitializer initializer;
    private String localAddress;
    private int port;
    private FileServerListener fileServerListener;

    private FileServer() {
    }

    public static FileServer getInstance() {
        if (INSTANCE == null) {
            synchronized (FileServer.class) {
                if (INSTANCE == null)
                    INSTANCE = new FileServer();
            }
        }
        return INSTANCE;
    }

    public String getAddress() {
        return localAddress;
    }

    public int getPort() {
        return port;
    }

    public void startServer(final int port) {
        if (bossGroup != null || workerGroup != null || channel != null) {
            Log.d(TAG, "plz check file server are running ?");
            return;
        }

        this.port = port;
        MyExecutors.newFixedThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                bossGroup = new NioEventLoopGroup(1);
                workerGroup = new NioEventLoopGroup();

                try {
                    ServerBootstrap b = new ServerBootstrap();
                    b.group(bossGroup, workerGroup)
                            .channel(NioServerSocketChannel.class)
                            .childHandler(initializer = new FileServerInitializer(fileServerListener));

                    channel = b.bind(port).sync().channel();
                    localAddress = P2PUtil.getP2PAddress();
                    Log.d(TAG, "file open server on " + port + ", address : " + localAddress);
                    channel.closeFuture().sync();
                    Log.e(TAG, "file server are stop");
                } catch (InterruptedException e) {
                    Log.e(TAG, "file server open error", e);
                } finally {
                    destroy(true);
                }
            }
        });
    }

    @Override
    public void stopServer() {
        destroy(false);
    }

    @Override
    public void setSendFileListener(FileServerListener listener) {
        fileServerListener = listener;
        if (initializer != null) initializer.setSendFileListener(listener);
    }

    private synchronized void destroy(boolean self) {
        if (self) {
            close();
        } else {
            MyExecutors.newFixedThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    close();
                }
            });
        }
    }

    private void close() {
        try {
            if (channel != null) {
                channel.close();
            }

            if (bossGroup != null) {
                bossGroup.shutdownGracefully();
            }

            if (workerGroup != null) {
                workerGroup.shutdownGracefully();
            }
            Log.e(TAG, "file server are destroy");
        } catch (Exception e) {
            Log.e(TAG, "file server destroy error", e);
        } finally {
            workerGroup = null;
            bossGroup = null;
            channel = null;
            initializer = null;
        }
    }
}
