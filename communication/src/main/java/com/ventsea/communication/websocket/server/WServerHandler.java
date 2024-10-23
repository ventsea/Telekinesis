package com.ventsea.communication.websocket.server;

import android.os.Environment;
import android.util.Log;

import com.google.gson.Gson;
import com.ventsea.communication.bean.FrameMessage;
import com.ventsea.communication.bean.TransClass;
import com.ventsea.communication.bean.TransFolder;
import com.ventsea.communication.bean.TransHome;
import com.ventsea.communication.bean.TransUrl;
import com.ventsea.communication.utils.Utils;
import com.ventsea.medialib.LocalApp;
import com.ventsea.medialib.LocalAudio;
import com.ventsea.medialib.LocalContacts;
import com.ventsea.medialib.LocalDocs;
import com.ventsea.medialib.LocalImages;
import com.ventsea.medialib.LocalVideo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.CharsetUtil;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static io.netty.handler.codec.rtsp.RtspResponseStatuses.NOT_FOUND;

class WServerHandler extends ChannelInboundHandlerAdapter {

    private static final String TAG = WServer.TAG;
    private ChannelGroup group; //管理当前已连接的客户端
    private IServer.IServerListener serverListener;
    private WebSocketServerHandshaker handShaker;
    private Gson mGson = new Gson();
    private String ROOT = Environment.getExternalStorageDirectory().getAbsolutePath();
    private long totalSize;

    WServerHandler(ChannelGroup group, IServer.IServerListener listener) {
        this.group = group;
        this.serverListener = listener;
    }

    void setServerListener(IServer.IServerListener listener) {
        serverListener = listener;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        Log.d(TAG, "channelActive | ctx " + ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof HttpRequest) {
            handleHttpRequest(ctx, (HttpRequest) msg);
        } else if (msg instanceof WebSocketFrame) {
            handleWebSocketFrame(ctx, (WebSocketFrame) msg);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    /**
     * 断开连接
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        Log.d(TAG, "channelInactive | ctx " + ctx);
        String ip = getRemoteIp(ctx);
        Log.d(TAG, ip + " was disconnect");
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        Log.d(TAG, "handlerRemoved | ctx " + ctx);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {//超时事件
            IdleStateEvent idleEvent = (IdleStateEvent) evt;
            if (idleEvent.state() == IdleState.READER_IDLE) {//读 //IdleState.WRITER_IDLE //IdleState.ALL_IDLE
                ctx.channel().close();
            }
        }
        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        Log.e(TAG, "exceptionCaught | ctx " + ctx + ", Throwable : " + cause.getMessage());
        ctx.close();
    }

    private String getRemoteIp(ChannelHandlerContext ctx) {
        String s = ctx.channel().remoteAddress().toString();
        s = s.substring(1, s.indexOf(":"));
        return s;
    }

    private void handleHttpRequest(ChannelHandlerContext ctx, HttpRequest req) {
        if (!req.decoderResult().isSuccess()) {
            Log.d(TAG, "handle a bad request");
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST));
            return;
        }

        if (req.method() != GET) {
            Log.d(TAG, "POST methods");
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN));
            return;
        }

        String uri = req.uri();
        Log.d(TAG, "req uri : " + uri);

        //send favicon.io
        if ("/favicon.ico".equals(uri)) {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, NOT_FOUND));
            return;
        }

        if ("/websocket".equals(uri)) {
            // Handshake
            WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(getWebSocketLocation(req), null, true, 5 * 1024 * 1024);
            handShaker = wsFactory.newHandshaker(req);
            if (handShaker == null) {
                //版本不支持
                Log.e(TAG, "UnsupportedVersion");
                WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
            } else {
                try {
                    Log.d(TAG, "waiting handshake");
                    handShaker.handshake(ctx.channel(), req);
//            String msg = JOINED + ctx.channel();
//            group.writeAndFlush(new FrameMessage(new Head(msg.getBytes("UTF-8").length, 1), msg));   //通知所有连接的客户端, ctx.channel()加入了组
                    group.add(ctx.channel());
                    totalSize = Utils.getAvailableStorage();
                    if (serverListener != null) serverListener.onClientConnect(getRemoteIp(ctx));
                    Log.d(TAG, "handshake success");
                    return;
                } catch (Exception e) {
                    Log.d(TAG, "handshake error", e);
                }
            }
            return;
        }

        sendError(ctx, HttpResponseStatus.NOT_FOUND);
    }

    private static String getWebSocketLocation(HttpRequest req) {
        String location = req.headers().get(HttpHeaderNames.HOST) + "/websocket";
        return "ws://" + location;
    }

    private void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, status, Unpooled.copiedBuffer("Failure: " + status + "\r\n", CharsetUtil.UTF_8));
        response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");
        // Close the connection as soon as the error message is sent.
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private static void sendHttpResponse(ChannelHandlerContext ctx, HttpRequest req, FullHttpResponse res) {
        // Generate an error page if response getStatus code is not OK (200).
        if (res.status().code() != 200) {
            ByteBuf buf = Unpooled.copiedBuffer(res.status().toString(), CharsetUtil.UTF_8);
            res.content().writeBytes(buf);
            buf.release();
            HttpUtil.setContentLength(res, res.content().readableBytes());
        }

        // Send the response and close the connection if necessary.
        ChannelFuture f = ctx.channel().writeAndFlush(res);
        if (!HttpUtil.isKeepAlive(req) || res.status().code() != 200) {
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }

    private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
        // Check for closing frame
        if (frame instanceof CloseWebSocketFrame) {
            handShaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
            return;
        }
        if (frame instanceof PingWebSocketFrame) {
            ctx.writeAndFlush(new PongWebSocketFrame(frame.content().retain()));  //return callback channelReadComplete, it will flush;
            return;
        }

        if (frame instanceof TextWebSocketFrame) {
            //获取信息, 更新UI, 添加到下载列表, 开启下载;
            String text = ((TextWebSocketFrame) frame).text();
            FrameMessage message = mGson.fromJson(text, FrameMessage.class);
            if (message == null) return;
            int type = message.getMessageType();
            if (type == FrameMessage.TYPE_URL) {
                Log.d(TAG, "server receiver url");
                responseFolder(message.url, ctx);
                return;
            }
            if (type == FrameMessage.TYPE_FOLDER) {
                Log.d(TAG, "server not support receiver folder");
                return;
            }
            if (type == FrameMessage.TYPE_HANDSHAKE || type == FrameMessage.TYPE_FILE_LIST) {
                Log.d(TAG, "server receiver type : " + type); //0: 握手，信息对等。 1：文件列表
                if (serverListener != null) {
                    serverListener.onServerReceiverMessage(message);
                }
            }
            if (type == FrameMessage.TYPE_REQ_CLASS) {
                responseClass(message.class_type, ctx);
                return;
            }
            return;
        }
        if (frame instanceof BinaryWebSocketFrame) {
            Log.d(TAG, "BinaryWebSocketFrame");
        }
    }

    private void responseClass(int classType, final ChannelHandlerContext ctx) {
        final FrameMessage message = new FrameMessage(FrameMessage.TYPE_RES_CLASS);
        message.class_type = classType;
        message.clazz = new TransClass();
        switch (classType) {
            case TransHome.TYPE_APP:
                message.clazz.apps = LocalApp.scanAllApp(Utils.sContext);
                ctx.writeAndFlush(new TextWebSocketFrame(mGson.toJson(message)));
                break;
            case TransHome.TYPE_AUDIO:
                List<LocalAudio.Audio> audios = LocalAudio.scanAllAudio(Utils.sContext);
                List<List<LocalAudio.Audio>> audiosList = Utils.averageAssign(audios, 200);
                int i = 0;
                for (List<LocalAudio.Audio> list : audiosList) {
                    message.clazz.totalBatch = audiosList.size();
                    message.clazz.batch = i;
                    message.clazz.audios = list;
                    ctx.writeAndFlush(new TextWebSocketFrame(mGson.toJson(message)));
                    i++;
                }
                break;
            case TransHome.TYPE_CONTACT:
                List<LocalContacts.Contact> contacts = LocalContacts.fastScanAllContacts(Utils.sContext);
                List<List<LocalContacts.Contact>> contactsList = Utils.averageAssign(contacts, 200);
                int i1 = 0;
                for (List<LocalContacts.Contact> list : contactsList) {
                    message.clazz.totalBatch = contactsList.size();
                    message.clazz.batch = i1;
                    message.clazz.contacts = list;
                    ctx.writeAndFlush(new TextWebSocketFrame(mGson.toJson(message)));
                    i1++;
                }
                break;
            case TransHome.TYPE_DOC:
                LocalDocs docs = new LocalDocs(false);
                docs.scanAllDocs(new File(ROOT), new LocalDocs.ScanDocsResultListener() {
                    @Override
                    public void onFinish(List<LocalDocs.Doc> files) {
                        if (ctx != null && ctx.channel() != null && ctx.channel().isActive()) {
                            List<List<LocalDocs.Doc>> lists = Utils.averageAssign(files, 200);
                            int i = 0;
                            for (List<LocalDocs.Doc> list : lists) {
                                message.clazz.totalBatch = lists.size();
                                message.clazz.batch = i;
                                message.clazz.docs = list;
                                ctx.writeAndFlush(new TextWebSocketFrame(mGson.toJson(message)));
                                i++;
                            }
                        }
                    }
                });
                return;
            case TransHome.TYPE_IMAGE:
                List<LocalImages.Image> images = LocalImages.scanAllImages(Utils.sContext);
                List<List<LocalImages.Image>> imagesList = Utils.averageAssign(images, 200);
                int i2 = 0;
                for (List<LocalImages.Image> list : imagesList) {
                    message.clazz.totalBatch = imagesList.size();
                    message.clazz.batch = i2;
                    message.clazz.images = list;
                    ctx.writeAndFlush(new TextWebSocketFrame(mGson.toJson(message)));
                    i2++;
                }
                break;
            case TransHome.TYPE_VIDEO:
                List<LocalVideo.Video> videos = LocalVideo.scanAllVideo(Utils.sContext);
                List<List<LocalVideo.Video>> videosList = Utils.averageAssign(videos, 200);
                int i3 = 0;
                for (List<LocalVideo.Video> list : videosList) {
                    message.clazz.totalBatch = videosList.size();
                    message.clazz.batch = i3;
                    message.clazz.videos = list;
                    ctx.writeAndFlush(new TextWebSocketFrame(mGson.toJson(message)));
                    i3++;
                }
                break;
        }
    }

    private void responseFolder(TransUrl url, ChannelHandlerContext ctx) {
        if (url == null || url.getUrl() == null) return;
        String dir = url.getUrl();
        if (dir.startsWith(TransUrl.HOME)) {
            getResponseHomePage(ctx);
            return;
        }
        String response = null;
        if (dir.startsWith(TransUrl.INDEX)) {
            if (dir.equals(TransUrl.INDEX)) {
                response = getResponsePage(null);
            } else {
                dir = dir.substring(dir.indexOf(TransUrl.INDEX) + TransUrl.INDEX.length());
                response = getResponsePage(dir);
            }
        }
        if (response != null) ctx.writeAndFlush(new TextWebSocketFrame(response));
    }

    private void getResponseHomePage(ChannelHandlerContext ctx) {
        ctx.writeAndFlush(new TextWebSocketFrame(getAppListInfo()));
        ctx.writeAndFlush(new TextWebSocketFrame(getAudioListInfo()));
        ctx.writeAndFlush(new TextWebSocketFrame(getContactListInfo()));
        getDocListInfo(ctx);
        ctx.writeAndFlush(new TextWebSocketFrame(getImageListInfo()));
        ctx.writeAndFlush(new TextWebSocketFrame(getVideoListInfo()));
    }

    private String getAppListInfo() {
        TransHome transHome = new TransHome(TransHome.TYPE_APP, LocalApp.scanAllApp(Utils.sContext).size());
        return responseHome(transHome);
    }

    private String getAudioListInfo() {
        TransHome transHome = new TransHome(TransHome.TYPE_AUDIO, LocalAudio.scanAllAudio(Utils.sContext).size());
        return responseHome(transHome);
    }

    private String getContactListInfo() {
        TransHome transHome = new TransHome(TransHome.TYPE_CONTACT, LocalContacts.fastScanAllContacts(Utils.sContext).size());
        return responseHome(transHome);
    }

    private void getDocListInfo(final ChannelHandlerContext ctx) {
        LocalDocs docs = new LocalDocs(false);
        docs.scanAllDocs(new File(ROOT), new LocalDocs.ScanDocsResultListener() {
            @Override
            public void onFinish(List<LocalDocs.Doc> files) {
                if (ctx != null && ctx.channel() != null && ctx.channel().isActive()) {
                    TransHome transHome = new TransHome(TransHome.TYPE_DOC, files.size());
                    ctx.writeAndFlush(new TextWebSocketFrame(responseHome(transHome)));
                }
            }
        });
    }

    private String getImageListInfo() {
        TransHome transHome = new TransHome(TransHome.TYPE_IMAGE, LocalImages.scanAllImages(Utils.sContext).size());
        return responseHome(transHome);
    }

    private String getVideoListInfo() {
        TransHome transHome = new TransHome(TransHome.TYPE_VIDEO, LocalVideo.scanAllVideo(Utils.sContext).size());
        return responseHome(transHome);
    }

    private String responseHome(TransHome transHome) {
        FrameMessage message = new FrameMessage(FrameMessage.TYPE_HOME);
        message.home = transHome;
        message.total_size = totalSize;
        return mGson.toJson(message);
    }

    private String getResponsePage(String dir) {
        File file;
        if (dir == null) {
            file = new File(ROOT);
        } else {
            file = new File(ROOT + dir);
        }
        if (file.exists() && file.isDirectory()) {
            FrameMessage message = buildFolderMessage(file);
            return mGson.toJson(message);
        }
        return null;
    }

    private FrameMessage buildFolderMessage(File file) {
        FrameMessage message = new FrameMessage(FrameMessage.TYPE_FOLDER);
        message.folder = new TransFolder();
        message.folder.nFiles = new ArrayList<>();
        List<TransFolder.NFile> cache = new ArrayList<>();
        File[] files = file.listFiles();
        for (File file1 : files) {
            if (file1.getName().startsWith(".")) continue;
            TransFolder.NFile nFile = new TransFolder.NFile.Build(file1).build();
            Log.d(TAG, "file1 " + nFile);
            if (file1.isFile()) {
                cache.add(nFile);
            } else {
                message.folder.nFiles.add(nFile);
            }
        }
        message.folder.nFiles.addAll(cache);
        return message;
    }
}
