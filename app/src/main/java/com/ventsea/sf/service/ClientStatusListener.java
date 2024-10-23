package com.ventsea.sf.service;

import com.st.letter.lib.bean.FrameMessage;

public interface ClientStatusListener {
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
