package com.ventsea.communication.http.file;

import android.net.Uri;
import android.util.Log;

import io.netty.channel.ChannelProgressiveFuture;
import io.netty.channel.ChannelProgressiveFutureListener;

class MyChannelProgressiveFutureListener implements ChannelProgressiveFutureListener {

    private static final String TAG = "ChannelProgress";
    private IFileServer.FileServerListener sendListener;
    private String address;
    private Uri uri;

    void setParameter(String address, Uri uri) {
        this.address = address;
        this.uri = uri;
    }

    void setProgressListener(IFileServer.FileServerListener listener) {
        sendListener = listener;
    }

    @Override
    public void operationProgressed(ChannelProgressiveFuture future, long progress, long total) {
        if (sendListener != null) sendListener.onSendProgress(address, uri, progress, total);
    }

    @Override
    public void operationComplete(ChannelProgressiveFuture future) {
        Log.d(TAG, "operationComplete");
        if (sendListener != null) sendListener.onSendCompleted(address, uri);
    }
}
