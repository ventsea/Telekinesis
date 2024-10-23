package com.ventsea.communication.websocket.server;

import android.util.Log;

import com.google.gson.Gson;
import com.ventsea.communication.bean.FrameMessage;
import com.ventsea.communication.bean.TransFileList;
import com.ventsea.communication.http.file.FileServer;
import com.ventsea.communication.threadmode.MyExecutors;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.ImmediateEventExecutor;

public class WServer implements IServer {

    static final String TAG = "WServer";
    private static WServer INSTANCE;
    //当channelGroup内的channel被关闭,则会自动清除
    private ChannelGroup channelGroup;
    private WServerInitializer initializer;
    private IServer.IServerListener serverListener;
    private Channel channel;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    private String deviceName;
    private String ip;
    private String mac;
    private Gson gson = new Gson();

    private WServer() {
    }

    public static WServer getInstance() {
        if (INSTANCE == null) {
            synchronized (WServer.class) {
                if (INSTANCE == null) {
                    INSTANCE = new WServer();
                }
            }
        }
        return INSTANCE;
    }

    @Override
    public void setDeviceInfo(String deviceName, String address, String mac) {
        this.deviceName = deviceName;
        this.ip = address;
        this.mac = mac;
    }

    @Override
    public void startServer(final int port) {
        if (channelGroup != null) return;
        FileServer.getInstance().startServer(port + 1);
        MyExecutors.newFixedThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                channelGroup = new DefaultChannelGroup(ImmediateEventExecutor.INSTANCE);
                bossGroup = new NioEventLoopGroup(1);
                workerGroup = new NioEventLoopGroup();

                try {
                    ServerBootstrap b = new ServerBootstrap();
                    b.group(bossGroup, workerGroup)
                            .channel(NioServerSocketChannel.class)
                            .handler(new LoggingHandler(LogLevel.INFO))
                            .childHandler(initializer = new WServerInitializer(channelGroup, serverListener));

                    channel = b.bind(port).sync().channel();
                    Log.d(TAG, "startServer on port :" + port + '/');
                    if (serverListener != null) serverListener.onServerStart();
                    channel.closeFuture().sync();
                } catch (Exception e) {
                    Log.e(TAG, "startServer error", e);
                } finally {
                    FileServer.getInstance().stopServer();
                    destroy(true);
                }
            }
        });
    }

    @Override
    public void sendFileList(TransFileList fileList) {
        if (fileList == null || fileList.file_map == null) return;
        final FrameMessage message = new FrameMessage(FrameMessage.TYPE_FILE_LIST);
        message.file_list = fileList;
        message.device_name = deviceName;
        message.ip = ip;
        message.mac = mac;
        MyExecutors.newFixedThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                String json = gson.toJson(message);
                TextWebSocketFrame tf = new TextWebSocketFrame(json);
                channelGroup.writeAndFlush(tf);
            }
        });
    }

    @Override
    public void stopServer() {
        destroy(false);
    }

    @Override
    public void setServerListener(IServerListener listener) {
        serverListener = listener;
        if (initializer != null) initializer.setServerListener(listener);
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
                Log.e(TAG, "server channel close");
                channel = null;
            }

            if (channelGroup != null && !channelGroup.isEmpty()) {
                channelGroup.close().sync();    //will clean group
                Log.e(TAG, "server close group sync");
            }

            // Shut down all event loops to terminate all threads.
            if (bossGroup != null) {
                bossGroup.shutdownGracefully();
                bossGroup = null;
            }

            if (workerGroup != null) {
                workerGroup.shutdownGracefully();
                workerGroup = null;
            }

            if (serverListener != null)
                serverListener.onServerStop();

            Log.e(TAG, "server are destroy");
        } catch (Exception e) {
            Log.e(TAG, "server onDisconnect Error");
            Log.e(TAG, e.toString());
            if (serverListener != null)
                serverListener.onServerError();
        } finally {
            channelGroup = null;
            initializer = null;
            serverListener = null;
        }
    }
}
