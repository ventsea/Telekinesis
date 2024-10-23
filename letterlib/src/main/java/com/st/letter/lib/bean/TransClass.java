package com.st.letter.lib.bean;

import android.os.Parcel;
import android.os.Parcelable;


import com.st.letter.lib.media.LocalApp;
import com.st.letter.lib.media.LocalAudio;
import com.st.letter.lib.media.LocalContacts;
import com.st.letter.lib.media.LocalDocs;
import com.st.letter.lib.media.LocalImages;
import com.st.letter.lib.media.LocalVideo;

import java.util.List;

public class TransClass implements Parcelable {

    private int type; //首页条目详情类型

    public List<LocalApp.App> apps;
    public List<LocalAudio.Audio> audios;
    public List<LocalContacts.Contact> contacts;
    public List<LocalDocs.Doc> docs;
    public List<LocalVideo.Video> videos;
    public List<LocalImages.Image> images;

    public TransClass(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    @Override
    public String toString() {
        return "TransClass{" +
                "type=" + type +
                ", apps=" + apps +
                ", audios=" + audios +
                ", contacts=" + contacts +
                ", docs=" + docs +
                ", videos=" + videos +
                ", images=" + images +
                '}';
    }

    protected TransClass(Parcel in) {
    }

    public static final Creator<TransClass> CREATOR = new Creator<TransClass>() {
        @Override
        public TransClass createFromParcel(Parcel in) {
            return new TransClass(in);
        }

        @Override
        public TransClass[] newArray(int size) {
            return new TransClass[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }
}
