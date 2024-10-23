package com.ventsea.communication.bean;

public class FrameMessage {
    public static final int TYPE_HANDSHAKE = 0;
    public static final int TYPE_FILE_LIST = 1;
    public static final int TYPE_FOLDER = 2;
    public static final int TYPE_URL = 3;
    public static final int TYPE_HOME = 4;
    public static final int TYPE_REQ_CLASS = 5;
    public static final int TYPE_RES_CLASS = 6;

    private int message_type; //消息类型
    public int class_type; //首页条目详情类型
    public String ip;
    public String device_name;
    public String mac;
    public long total_size;

    public FrameMessage(int messageType) {
        this.message_type = messageType;
    }

    public int getMessageType() {
        return message_type;
    }

    public TransFileList file_list;
    public TransFolder folder; //文件夹（包含文件），通常用于服务端返回
    public TransUrl url; //地址，通常用于客户端请求
    public TransHome home; //首页
    public TransClass clazz; //首页类型

    @Override
    public String toString() {
        return "FrameMessage{" +
                "message_type=" + message_type +
                ", ip='" + ip + '\'' +
                ", device_name='" + device_name + '\'' +
                ", mac='" + mac + '\'' +
                ", total_size=" + total_size +
                ", file_list=" + file_list +
                ", folder=" + folder +
                ", url=" + url +
                ", home=" + home +
                ", clazz=" + clazz +
                '}';
    }
}
