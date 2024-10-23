package com.ventsea.communication.bean;

import java.util.ArrayList;
import java.util.List;

public class FileType {

    private static FileType INSTANCE;

    public static final int APK = 0;
    public static final int IMG = 1;
    public static final int AUDIO = 2;
    public static final int VIDEO = 3;
    public static final int SMS = 4;
    public static final int MMS = 5;
    public static final int CONTACT = 6;
    public static final int CALL_LOG = 7;
    public static final int DOC = 8;
    public static final int OTHER = 9;
    public static final int UN_KNOW = -1;

    private static final List<String> IMG_LIST = new ArrayList<>();
    private static final List<String> AUDIO_LIST = new ArrayList<>();
    private static final List<String> VIDEO_LIST = new ArrayList<>();
    private static final List<String> DOC_LIST = new ArrayList<>();

    private FileType() {
    }

    static {
        IMG_LIST.add("image/jpeg");
        IMG_LIST.add("image/pngg");

        AUDIO_LIST.add("audio/x-mpeg");

        VIDEO_LIST.add("video/mp4");

        DOC_LIST.add("text/plain");
        DOC_LIST.add("application/msword");
        DOC_LIST.add("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        DOC_LIST.add("application/vnd.ms-excel");
        DOC_LIST.add("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        DOC_LIST.add("application/vnd.ms-powerpoint");
        DOC_LIST.add("application/vnd.openxmlformats-officedocument.presentationml.presentation");
        DOC_LIST.add("application/pdf");
    }

    public static FileType getInstance() {
        if (INSTANCE == null) {
            synchronized (FileType.class) {
                if (INSTANCE == null) {
                    INSTANCE = new FileType();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * 适用于传输文件夹（传输文件时应当遵循指定的文件类型而不应该调用这里）
     *
     * @param path 文件路径
     * @return 文件类型
     */
    public int getFileType(String path) {
        if (path == null) return UN_KNOW;
        if (path.equalsIgnoreCase("application/vnd.android.package-archive")) return APK;
        if (IMG_LIST.contains(path) || path.contains("image")) return IMG;
        if (AUDIO_LIST.contains(path) || path.contains("audio")) return AUDIO;
        if (VIDEO_LIST.contains(path) || path.contains("video")) return VIDEO;
        if (DOC_LIST.contains(path) || path.contains("text")) return DOC;
        if (path.contains("application")) return OTHER;
        return UN_KNOW;
    }
}
