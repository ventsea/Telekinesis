package com.st.letter.lib.bean;

public class FrameMessage {
    public static final int MSG_TYPE_REQ_HOME = 0;
    public static final int MSG_TYPE_REQ_FOLDER_URL = 1;
    public static final int MSG_TYPE_REQ_CLASS = 2;
    public static final int MSG_TYPE_REQ_FILE_LIST = 3;

    public static final int MSG_TYPE_RES_HOME = 4;
    public static final int MSG_TYPE_RES_CLASS = 5;
    public static final int MSG_TYPE_RES_FOLDER = 6;
    public static final int MSG_TYPE_RES_FILE_LIST = 7;

    private int msg_type; //消息类型
    public String ip;
    public String device_name;
    public String mac;
    public long total_size;

    public FrameMessage(int messageType) {
        this.msg_type = messageType;
    }

    public int getMessageType() {
        return msg_type;
    }

    public String folder_url; //地址，通常用于客户端请求
    public TransFolder folder; //文件夹（包含文件），通常用于服务端返回
    public TransHome home; //首页
    public TransClass clazz; //首页类型
    public TransFileList file_list;

    @Override
    public String toString() {
        return "FrameMessage{" +
                "msg_type=" + msg_type +
                ", ip='" + ip + '\'' +
                ", device_name='" + device_name + '\'' +
                ", mac='" + mac + '\'' +
                ", total_size=" + total_size +
                '}';
    }
}
