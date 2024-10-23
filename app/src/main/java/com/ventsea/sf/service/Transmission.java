package com.ventsea.sf.service;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;

import com.google.gson.Gson;
import com.st.letter.lib.UriComponent;
import com.st.letter.lib.Utils;
import com.st.letter.lib.bean.FrameMessage;
import com.st.letter.lib.bean.TransClass;
import com.st.letter.lib.bean.TransFolder;
import com.st.letter.lib.bean.TransHome;
import com.st.letter.lib.media.LocalApp;
import com.st.letter.lib.media.LocalAudio;
import com.st.letter.lib.media.LocalContacts;
import com.st.letter.lib.media.LocalDocs;
import com.st.letter.lib.media.LocalImages;
import com.st.letter.lib.media.LocalVideo;
import com.st.letter.lib.media.URLConstant;
import com.st.letter.lib.server.CoreServer;
import com.st.letter.lib.server.CustomResponse;
import com.st.letter.lib.server.IServerHandlerListener;
import com.ventsea.directlib.WDirect;
import com.ventsea.sf.app.NFSApplication;
import com.ventsea.sf.service.http.AsyncHttp;
import com.ventsea.sf.service.http.listener.DisposeDataHandler;
import com.ventsea.sf.service.http.listener.DisposeDataListener;
import com.ventsea.sf.service.http.request.CommonRequest;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.Request;

public class Transmission implements IServerHandlerListener, DisposeDataListener {

    private static final String TAG = "Transmission";
    private Set<ClientStatusListener> clientListeners;
    private Set<ServerStatusListener> serverListeners;
    private Set<SendFileListener> sendFileListeners;
    private Handler mHandler;
    private Gson gson;
    public static final int SERVER_PORT = 8686;
    public static final String SERVER_IP = "192.168.49.1";
    private static final String ROOT = Environment.getExternalStorageDirectory().getAbsolutePath();

    private static final String PATH_ID = "/id";
    private static final String PATH_GOODBYE = "/fk";
    private static final String PATH_MSG = "/msg";

    private static Transmission INSTANCE;

    private Transmission() {
        clientListeners = new LinkedHashSet<>();
        serverListeners = new LinkedHashSet<>();
        sendFileListeners = new LinkedHashSet<>();
        mHandler = new Handler();
        gson = new Gson();
    }

    public static Transmission getInstance() {
        if (INSTANCE == null) {
            synchronized (Transmission.class) {
                if (INSTANCE == null) {
                    INSTANCE = new Transmission();
                }
            }
        }
        return INSTANCE;
    }

    private void init() {
        CoreServer.getInstance().initServer(NFSApplication.sContext, buildUriComponent());
        CoreServer.getInstance().setListener(this);
    }

    private UriComponent buildUriComponent() {
        UriComponent component = new UriComponent();
        component.addUri(PATH_ID);
        component.addUri(PATH_GOODBYE);
        component.addUri(PATH_MSG);
        return component;
    }

    /**
     * start service
     *
     * @param context ...
     */
    public void startServer(Context context) {
        init();
        LiveServer.startServer(context);
    }

    public void addServerListener(final ServerStatusListener listener) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                serverListeners.add(listener);
            }
        });
    }

    public void removeServerListener(final ServerStatusListener listener) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                serverListeners.remove(listener);
            }
        });
    }

    /**
     * start client
     *
     * @param context ...
     */
    public void startClient(Context context) {
        init();
        LiveServer.startClient(context);
    }

    void startClient() {
        Request request = CommonRequest.createGet(URLConstant.HTTP + SERVER_IP + ":" + SERVER_PORT + PATH_ID);
        AsyncHttp.sendRequest(request, new DisposeDataHandler(new DisposeDataListener() {
            @Override
            public void onResponseListener(boolean success, Object responseObj) {
                if (success) {
                    for (ClientStatusListener listener : clientListeners) {
                        if (listener != null) listener.onConnectServer();
                    }
                } else {
                    for (ClientStatusListener listener : clientListeners) {
                        if (listener != null) listener.onConnectError();
                    }
                }
            }
        }));
    }

    void closeClient() {
        Request request = CommonRequest.createGet(URLConstant.HTTP + SERVER_IP + ":" + SERVER_PORT + PATH_GOODBYE);
        AsyncHttp.sendRequest(request, new DisposeDataHandler(new DisposeDataListener() {
            @Override
            public void onResponseListener(boolean success, Object responseObj) {

            }
        }));
        for (ClientStatusListener listener : clientListeners) {
            if (listener != null) listener.onDisConnectServer();
        }
    }

    public void addClientListener(final ClientStatusListener listener) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                clientListeners.add(listener);
            }
        });
    }

    public void removeClientListener(final ClientStatusListener listener) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                clientListeners.remove(listener);
            }
        });
    }

    /**
     * send listener
     *
     * @param listener ...
     */
    public void addSendFileListener(final SendFileListener listener) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                sendFileListeners.add(listener);
            }
        });
    }

    public void removeSendListener(final SendFileListener listener) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                sendFileListeners.remove(listener);
            }
        });
    }

    public void requestPage(String url) {
        FrameMessage message = new FrameMessage(FrameMessage.MSG_TYPE_REQ_FOLDER_URL);
        message.folder_url = url;
        sendMsg(message);
    }

    public void requestHome(int type) {
        FrameMessage message = new FrameMessage(FrameMessage.MSG_TYPE_REQ_HOME);
        message.home = new TransHome(type, 0);
        sendMsg(message);
    }

    public void requestClass(int type) {
        FrameMessage message = new FrameMessage(FrameMessage.MSG_TYPE_REQ_CLASS);
        message.clazz = new TransClass(type);
        sendMsg(message);
    }

    // TODO: 2018/12/17 发送其他信息

    private void sendMsg(FrameMessage message) {
        AsyncHttp.sendRequest(CommonRequest.createPost(URLConstant.HTTP + SERVER_IP + ":" + SERVER_PORT + PATH_MSG,
                gson.toJson(message)), new DisposeDataHandler(this, FrameMessage.class));
    }

    /**
     * 下载文件列表
     *
     * @param context ...
     * @param list    文件Uri列表
     */
    public void downloadFileList(Context context, ArrayList<Uri> list) {
        LiveServer.addDownloadQueue(context, list);
    }

    public void downloadFile(Context context, Uri uri) {
        LiveServer.addDownloadQueue(context, uri);
    }

    public void stopDownloadFile(Context context, Uri uri) {
        LiveServer.stopDownload(context, uri);
    }

    public void cleanListener() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                clientListeners.clear();
                serverListeners.clear();
                sendFileListeners.clear();
            }
        });
    }

    public void stopServer(Context context) {
        cleanListener();
        LiveServer.stop(context);
    }

    /*============================================================================================*/

    @Override
    public void onServerStart() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                for (ServerStatusListener listener : serverListeners) {
                    if (listener != null) listener.onServerStart();
                }
            }
        });
    }

    @Override
    public void onServerStop() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                for (ServerStatusListener listener : serverListeners) {
                    if (listener != null) listener.onServerStop();
                }
                stopServer(NFSApplication.sContext);
                WDirect.getInstance().stopDirect();
            }
        });
    }

    @Override
    public void onSendStarted(final String remoteAddress, final Uri uri) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                for (SendFileListener listener : sendFileListeners) {
                    if (listener != null) listener.onSendStart(remoteAddress, uri);
                }
            }
        });
    }

    @Override
    public void onSendProgress(final String remoteAddress, final Uri uri, final long progress, final long total) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                for (SendFileListener listener : sendFileListeners) {
                    if (listener != null)
                        listener.onSendProgress(remoteAddress, uri, progress, total);
                }
            }
        });
    }

    @Override
    public void onSendCompleted(final String remoteAddress, final Uri uri) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                for (SendFileListener listener : sendFileListeners) {
                    if (listener != null) listener.onSendCompleted(remoteAddress, uri);
                }
            }
        });
    }

    @Override
    public void onSendError(final String remoteAddress, final Uri uri) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                for (SendFileListener listener : sendFileListeners) {
                    if (listener != null) listener.onSendError(remoteAddress, uri);
                }
            }
        });
    }

    /**
     * 子线程
     */
    @Override
    public void onServerRead(String url, CustomResponse response) {
        if (PATH_MSG.equals(url)) {
            responseMsg(response);
        }
    }

    private void responseMsg(CustomResponse response) {
        FrameMessage message = gson.fromJson(response.getHttpBody(), FrameMessage.class);
        if (message == null) return;
        int type = message.getMessageType();
        if (type == FrameMessage.MSG_TYPE_REQ_HOME) {
            responseHome(message.home.getType(), response);
        }
        if (type == FrameMessage.MSG_TYPE_REQ_FOLDER_URL) {
            responseFolder(message.folder_url, response);
        }
        if (type == FrameMessage.MSG_TYPE_REQ_CLASS) {
            responseClass(message.clazz.getType(), response);
        }
    }

    private void responseClass(int classType, final CustomResponse response) {
        final FrameMessage message = new FrameMessage(FrameMessage.MSG_TYPE_RES_CLASS);
        message.clazz = new TransClass(classType);
        switch (classType) {
            case TransHome.TYPE_APP:
                message.clazz.apps = LocalApp.scanAllApp(NFSApplication.sContext);
                break;
            case TransHome.TYPE_AUDIO:
                message.clazz.audios = LocalAudio.scanAllAudio(NFSApplication.sContext);
                break;
            case TransHome.TYPE_CONTACT:
                message.clazz.contacts = LocalContacts.fastScanAllContacts(NFSApplication.sContext);
                break;
            case TransHome.TYPE_DOC:
                LocalDocs docs = new LocalDocs(false);
                final AtomicInteger r = new AtomicInteger(10);
                docs.scanAllDocs(new File(ROOT), new LocalDocs.ScanDocsResultListener() {
                    @Override
                    public void onFinish(List<LocalDocs.Doc> files) {
                        message.clazz.docs = files;
                        response.setResponseContent(gson.toJson(message));
                        r.set(0);
                    }
                });
                while (r.get() > 0) {
                    SystemClock.sleep(1000);
                    r.getAndDecrement();
                }
                return;
            case TransHome.TYPE_IMAGE:
                message.clazz.images = LocalImages.scanAllImages(NFSApplication.sContext);
                break;
            case TransHome.TYPE_VIDEO:
                message.clazz.videos = LocalVideo.scanAllVideo(NFSApplication.sContext);
                break;
        }
        response.setResponseContent(gson.toJson(message));
    }

    private void responseFolder(String dir, CustomResponse response) {
        if (dir.startsWith(TransFolder.MSG_PATH_INDEX)) {
            if (dir.equals(TransFolder.MSG_PATH_INDEX)) {
                response.setResponseContent(getResponsePage(null));
            } else {
                dir = dir.substring(dir.indexOf(TransFolder.MSG_PATH_INDEX) + TransFolder.MSG_PATH_INDEX.length());
                response.setResponseContent(getResponsePage(dir));
            }
        }
    }

    private void responseHome(int type, final CustomResponse response) {
        final FrameMessage message = new FrameMessage(FrameMessage.MSG_TYPE_RES_HOME);
        message.total_size = Utils.getAvailableStorage();
        switch (type) {
            case TransHome.TYPE_APP:
                message.home = new TransHome(type, LocalApp.scanAllApp(NFSApplication.sContext).size());
                break;
            case TransHome.TYPE_AUDIO:
                message.home = new TransHome(type, LocalAudio.scanAllAudio(NFSApplication.sContext).size());
                break;
            case TransHome.TYPE_CONTACT:
                message.home = new TransHome(TransHome.TYPE_CONTACT, LocalContacts.fastScanAllContacts(NFSApplication.sContext).size());
                break;
            case TransHome.TYPE_DOC:
                LocalDocs docs = new LocalDocs(false);
                final AtomicInteger r = new AtomicInteger(10);
                docs.scanAllDocs(new File(Environment.getExternalStorageDirectory().getAbsolutePath()), new LocalDocs.ScanDocsResultListener() {
                    @Override
                    public void onFinish(List<LocalDocs.Doc> files) {
                        message.home = new TransHome(TransHome.TYPE_DOC, files.size());
                        response.setResponseContent(gson.toJson(message));
                        r.set(0);
                    }
                });
                while (r.get() > 0) {
                    SystemClock.sleep(1000);
                    r.getAndDecrement();
                }
                return;
            case TransHome.TYPE_IMAGE:
                message.home = new TransHome(TransHome.TYPE_IMAGE, LocalImages.scanAllImages(NFSApplication.sContext).size());
                break;
            case TransHome.TYPE_VIDEO:
                message.home = new TransHome(TransHome.TYPE_VIDEO, LocalVideo.scanAllVideo(NFSApplication.sContext).size());
                break;
        }
        response.setResponseContent(gson.toJson(message));
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
            return gson.toJson(message);
        }
        return null;
    }

    private FrameMessage buildFolderMessage(File file) {
        FrameMessage message = new FrameMessage(FrameMessage.MSG_TYPE_RES_FOLDER);
        message.folder = new TransFolder();
        message.folder.nFiles = new ArrayList<>();
        List<TransFolder.NFile> cache = new ArrayList<>();
        File[] files = file.listFiles();
        for (File file1 : files) {
            if (file1.getName().startsWith(".")) continue;
            TransFolder.NFile nFile = new TransFolder.NFile.Build(SERVER_IP, SERVER_PORT, file1).build();
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

    /**
     * 主线程
     */
    @Override
    public void onResponseListener(boolean success, Object responseObj) {
        if (success) {
            if (responseObj instanceof FrameMessage) {
                for (ClientStatusListener listener : clientListeners) {
                    listener.onClientReceiverMessage((FrameMessage) responseObj);
                }
            }
        }
    }
}
