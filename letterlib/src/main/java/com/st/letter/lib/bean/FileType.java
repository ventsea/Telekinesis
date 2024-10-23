package com.st.letter.lib.bean;

import java.util.ArrayList;
import java.util.List;

public class FileType {

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

    private static final List<String> IMG_TYPE_LIST = new ArrayList<>();
    private static final List<String> AUDIO_TYPE_LIST = new ArrayList<>();
    private static final List<String> VIDEO_TYPE_LIST = new ArrayList<>();
    private static final List<String> DOC_TYPE_LIST = new ArrayList<>();

    private FileType() {
    }

    static {
        IMG_TYPE_LIST.add("image/jpeg");
        IMG_TYPE_LIST.add("image/pngg");

        AUDIO_TYPE_LIST.add("audio/x-mpeg");

        VIDEO_TYPE_LIST.add("video/mp4");

        DOC_TYPE_LIST.add("text/plain");
        DOC_TYPE_LIST.add("application/msword");
        DOC_TYPE_LIST.add("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        DOC_TYPE_LIST.add("application/vnd.ms-excel");
        DOC_TYPE_LIST.add("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        DOC_TYPE_LIST.add("application/vnd.ms-powerpoint");
        DOC_TYPE_LIST.add("application/vnd.openxmlformats-officedocument.presentationml.presentation");
        DOC_TYPE_LIST.add("application/pdf");
    }

    /**
     * 适用于传输文件夹（传输文件时应当遵循指定的文件类型而不应该调用这里）
     *
     * @param path 文件路径
     * @return 文件类型
     */
    public static int getFileType(String path) {
        if (path == null) return UN_KNOW;
        if (path.equalsIgnoreCase("application/vnd.android.package-archive")) return APK;
        if (IMG_TYPE_LIST.contains(path) || path.contains("image")) return IMG;
        if (AUDIO_TYPE_LIST.contains(path) || path.contains("audio")) return AUDIO;
        if (VIDEO_TYPE_LIST.contains(path) || path.contains("video")) return VIDEO;
        if (DOC_TYPE_LIST.contains(path) || path.contains("text")) return DOC;
        if (path.contains("application")) return OTHER;
        return UN_KNOW;
    }
}
