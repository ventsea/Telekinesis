package com.ventsea.communication.websocket.client;

import android.util.Log;

import com.google.gson.Gson;
import com.ventsea.communication.bean.FrameMessage;
import com.ventsea.communication.bean.TransFileList;
import com.ventsea.communication.bean.TransUrl;
import com.ventsea.communication.http.file.FileServer;
import com.ventsea.communication.threadmode.MyExecutors;

import java.net.URI;
import java.net.URISyntaxException;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

public class WClient implements IClient {
    static final String TAG = "WClient";
    private static WClient INSTANCE;
    private EventLoopGroup group;
    private WClientInitializer initializer;
    private IClient.IClientListener clientListener;
    private Channel channel;

    private String deviceName;
    private String ip;
    private String mac;
    private Gson gson = new Gson();

    private WClient() {
    }

    public static WClient getInstance() {
        if (INSTANCE == null) {
            synchronized (WClient.class) {
                if (INSTANCE == null) {
                    INSTANCE = new WClient();
                }
            }
        }
        return INSTANCE;
    }

    @Override
    public void setDeviceInfo(String deviceName, String mac) {
        this.deviceName = deviceName;
        this.mac = mac;
    }

    @Override
    public void startClient(String address, final int port) {
        if (group != null) return;
        String s = "ws://" + address + ":" + port + "/websocket";

        final URI uri;
        try {
            uri = new URI(s);
        } catch (URISyntaxException e) {
            Log.e(TAG, "address or port unusual", e);
            return;
        }

        this.ip = address;
        FileServer.getInstance().startServer(port + 1);
        MyExecutors.newFixedThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                group = new NioEventLoopGroup();
                try {
                    Bootstrap bootstrap = new Bootstrap();
                    bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000);
                    bootstrap.group(group)
                            .channel(NioSocketChannel.class)
                            .handler(initializer = new WClientInitializer(uri, clientListener));
                    Log.d(TAG, "client start connect " + uri.getHost() + ":" + port);
                    channel = bootstrap.connect(uri.getHost(), port).sync().channel();
                    initializer.getHandler().handshakeFuture().sync();
                    Log.d(TAG, "client handshake success on " + port);
                    channel.closeFuture().sync();
                    Log.d(TAG, "client close on " + port);
                } catch (Exception e) {
                    Log.e(TAG, "startClient error, Error : " + e.getMessage());
                } finally {
                    FileServer.getInstance().stopServer();
                    destroy(true);
                }
            }
        });

    }

    @Override
    public void sendFileList(TransFileList fileList) {
        if (fileList == null || fileList.file_map == null || channel == null) return;
        final FrameMessage message = new FrameMessage(FrameMessage.TYPE_FILE_LIST);
        message.file_list = fileList;
        message.device_name = deviceName;
        message.ip = ip;
        message.mac = mac;
        sendText(gson.toJson(message));
    }

    public void requestFolderPage(final String url) {
        if (channel == null || url == null) return;
        final FrameMessage message = new FrameMessage(FrameMessage.TYPE_URL);
        message.ip = ip;
        message.device_name = deviceName;
        message.url = new TransUrl(url);
        sendText(gson.toJson(message));
    }

    public void requestClass(final int type) {
        if (channel == null) return;
        FrameMessage message = new FrameMessage(FrameMessage.TYPE_REQ_CLASS);
        message.class_type = type;
        sendText(gson.toJson(message));
    }

    private void sendText(final String text) {
        MyExecutors.newFixedThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG, "client requestFolderPage...");
                channel.writeAndFlush(new TextWebSocketFrame(text));
            }
        });
    }

    @Override
    public void stopClient() {
        destroy(false);
    }

    @Override
    public void setClientListener(IClientListener listener) {
        clientListener = listener;
        if (initializer != null) initializer.setClientListener(listener);
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
                Log.e(TAG, "channel close");
                channel = null;
            }

            // Shut down all event loops to terminate all threads.
            if (group != null) {
                group.shutdownGracefully();
                group = null;
            }

            if (clientListener != null)
                clientListener.onDisConnectServer();

            Log.e(TAG, "client are destroy");
        } catch (Exception e) {
            Log.e(TAG, "client close error", e);
        } finally {
            initializer = null;
            clientListener = null;
        }
    }
}
