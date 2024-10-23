package com.st.letter.lib.media;

public abstract class LocalDB implements LocalBean {

    public String data;

    public String getMimeType(String filePath) {
        if (!filePath.contains(".") || filePath.endsWith(".")) {
            return "<unKnown>";
        }
        return filePath.substring(filePath.lastIndexOf(".") + 1);
    }
}
