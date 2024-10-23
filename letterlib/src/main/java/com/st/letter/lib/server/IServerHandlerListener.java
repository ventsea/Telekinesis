package com.st.letter.lib.server;

import com.st.letter.lib.IServerListener;

public interface IServerHandlerListener extends IServerListener {
    void onServerRead(String url, CustomResponse response);
}
