package com.ventsea.communication.http.file;

import android.net.Uri;

/**
 * 文件服务，提供http文件下载
 */
public interface IFileServer {

    /**
     * 启动服务（暂不需要监听启动情况）
     * @param port 服务端口
     */
    void startServer(int port);

    /**
     * 停止文件服务 （不需要监听停止情况）
     */
    void stopServer();

    void setSendFileListener(FileServerListener listener);
    /**
     * 文件服务发送文件进度监听
     */
    interface FileServerListener {
        /**
         * 开始发送监听
         * @param remoteAddress 远程地址，用于识别哪个客户端
         * @param uri 文件URI，用于识别哪个文件
         */
        void onSendStarted(String remoteAddress, Uri uri);
        void onSendProgress(String remoteAddress, Uri uri, long progress, long total);
        void onSendCompleted(String remoteAddress, Uri uri);
        void onSendError(String remoteAddress, Uri uri);
    }
}
