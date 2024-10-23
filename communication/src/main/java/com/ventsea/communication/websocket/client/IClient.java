package com.ventsea.communication.websocket.client;

import com.ventsea.communication.bean.FrameMessage;
import com.ventsea.communication.bean.TransFileList;

public interface IClient {
    void setDeviceInfo(String deviceName, String mac);
    void startClient(String remoteAddress, int remotePort);
    void setClientListener(IClientListener listener);
    void sendFileList(TransFileList fileList);
    void stopClient();

    interface IClientListener {
        /**
         * 已连接服务器
         */
        void onConnectServer();

        /**
         * 断开
         */
        void onDisConnectServer();

        void onConnectError();

        void onClientReceiverMessage(FrameMessage message);
    }
}
