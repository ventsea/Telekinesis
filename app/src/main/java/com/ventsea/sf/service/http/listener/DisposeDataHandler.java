package com.ventsea.sf.service.http.listener;

@SuppressWarnings("WeakerAccess")
public class DisposeDataHandler {

    public DisposeDataListener mListener;
    public Class<?> mClass = null;

    public DisposeDataHandler(DisposeDataListener listener) {
        this.mListener = listener;
    }

    public DisposeDataHandler(DisposeDataListener listener, Class<?> clazz) {
        this.mListener = listener;
        this.mClass = clazz;
    }
}
