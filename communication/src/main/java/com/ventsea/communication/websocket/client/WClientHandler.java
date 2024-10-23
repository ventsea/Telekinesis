package com.ventsea.communication.websocket.client;

import android.os.SystemClock;
import android.util.Log;

import com.google.gson.Gson;
import com.ventsea.communication.bean.FrameMessage;
import com.ventsea.communication.threadmode.MyExecutors;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketHandshakeException;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.CharsetUtil;

class WClientHandler extends SimpleChannelInboundHandler<Object> {

    private static final String TAG = WClient.TAG;
    private WebSocketClientHandshaker handshaker;
    private IClient.IClientListener clientListener;
    private ChannelPromise handshakeFuture;
    private Gson mGson = new Gson();
    private static final boolean DEBUG = false;

    WClientHandler(WebSocketClientHandshaker handShaker, IClient.IClientListener clientListener) {
        handshaker = handShaker;
        this.clientListener = clientListener;
    }

    void setClientListener(IClient.IClientListener listener) {
        clientListener = listener;
    }

    ChannelFuture handshakeFuture() {
        return handshakeFuture;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        handshakeFuture = ctx.newPromise();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        handshaker.handshake(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        Log.d(TAG, "WebSocket Client disconnected!");
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        Log.d(TAG, "WebSocket Client handler removed!");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
        Channel ch = ctx.channel();
        if (!handshaker.isHandshakeComplete()) {
            try {
                handshaker.finishHandshake(ch, (FullHttpResponse) msg);
                Log.d(TAG, "WebSocket Client connected!");
                handshakeFuture.setSuccess();
                sendPing(ctx);
                if (clientListener != null) {
                    clientListener.onConnectServer();
                }
            } catch (WebSocketHandshakeException e) {
                Log.e(TAG, "WebSocket Client failed to connect!");
                handshakeFuture.setFailure(e);
                if (clientListener != null) {
                    clientListener.onConnectError();
                }
            }
            return;
        }

        if (msg instanceof FullHttpResponse) {
            FullHttpResponse response = (FullHttpResponse) msg;
            throw new IllegalStateException(
                    "Unexpected FullHttpResponse (getStatus=" + response.status() +
                            ", content=" + response.content().toString(CharsetUtil.UTF_8) + ')');
        }

        WebSocketFrame frame = (WebSocketFrame) msg;
        if (frame instanceof TextWebSocketFrame) {
            handleTextWebSocket((TextWebSocketFrame) frame);
        } else if (frame instanceof PongWebSocketFrame) {
            if (DEBUG) {
                Log.d(TAG, "WebSocket Client received pong");
            }
        } else if (frame instanceof CloseWebSocketFrame) {
            Log.d(TAG, "WebSocket Client received closing");
            ch.close();
        }
    }

    private void sendPing(final ChannelHandlerContext ctx) {
        MyExecutors.newFixedThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    SystemClock.sleep(2000);
                    if (!ctx.channel().isActive()) {
                        break;
                    }
                    if (DEBUG) {
                        Log.d(TAG, "WebSocket Client send ping");
                    }
                    ctx.writeAndFlush(new PingWebSocketFrame(Unpooled.wrappedBuffer(new byte[]{8, 1, 8, 1})));
                }
            }
        });
    }

    private void handleTextWebSocket(TextWebSocketFrame frame) {
        Log.d(TAG, "WebSocket Client received message : " + frame.text());
        FrameMessage message = mGson.fromJson(frame.text(), FrameMessage.class);
        if (message == null) return;
        if (clientListener != null) {
            clientListener.onClientReceiverMessage(message);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent idleEvent = (IdleStateEvent) evt;
            if (idleEvent.state() == IdleState.READER_IDLE) { //IdleState.WRITER_IDLE //IdleState.ALL_IDLE
                Log.e(TAG, "IdleState reader time out");
                ctx.channel().close();
            }
        }
        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        Log.e(TAG, "WebSocket Client exceptionCaught!", cause);
        if (!handshakeFuture.isDone()) {
            handshakeFuture.setFailure(cause);
            if (clientListener != null) {
                clientListener.onConnectError();
            }
        }
        ctx.close();
    }
}
