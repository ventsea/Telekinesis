package com.ventsea.communication.websocket.server;

import com.ventsea.communication.bean.FrameMessage;
import com.ventsea.communication.bean.TransFileList;

public interface IServer {
    void setDeviceInfo(String deviceName, String address, String mac);
    void startServer(int port);
    void setServerListener(IServerListener listener);
    void sendFileList(TransFileList fileList);
    void stopServer();

    interface IServerListener {
        void onServerStart();

        void onClientConnect(String address);

        void onServerStop();

        void onServerError();

        void onServerReceiverMessage(FrameMessage message);
    }
}
