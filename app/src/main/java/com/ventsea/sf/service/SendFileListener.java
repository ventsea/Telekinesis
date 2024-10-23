package com.ventsea.sf.service;

import android.net.Uri;

public interface SendFileListener {
    void onSendStart(String remoteAddress, Uri uri);
    void onSendProgress(String remoteAddress, Uri uri, long progress, long total);
    void onSendCompleted(String remoteAddress, Uri uri);
    void onSendError(String remoteAddress, Uri uri);
}
