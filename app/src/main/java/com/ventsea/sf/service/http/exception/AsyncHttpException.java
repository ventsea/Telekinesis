package com.ventsea.sf.service.http.exception;

public class AsyncHttpException extends Exception {

    private static final long serialVersionUID = 1L;

    private int code;
    private Object msg;

    public AsyncHttpException(int code, Object msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public Object getMsg() {
        return msg;
    }
}
