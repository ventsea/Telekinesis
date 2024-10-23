package com.ventsea.communication.bean;

public class TransUrl {
    public static final String HOME = "/home";
    public static final String INDEX = "/index";

    private String url;

    public TransUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public String toString() {
        return "TransUrl{" +
                "url='" + url + '\'' +
                '}';
    }
}
