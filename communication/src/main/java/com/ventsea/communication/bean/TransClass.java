package com.ventsea.communication.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.ventsea.medialib.LocalApp;
import com.ventsea.medialib.LocalAudio;
import com.ventsea.medialib.LocalContacts;
import com.ventsea.medialib.LocalDocs;
import com.ventsea.medialib.LocalImages;
import com.ventsea.medialib.LocalVideo;

import java.util.List;

public class TransClass implements Parcelable {

    public int batch; //列表批次
    public int totalBatch; //总批次

    public List<LocalApp.App> apps;
    public List<LocalAudio.Audio> audios;
    public List<LocalContacts.Contact> contacts;
    public List<LocalDocs.Doc> docs;
    public List<LocalVideo.Video> videos;
    public List<LocalImages.Image> images;

    public TransClass() {

    }

    @Override
    public String toString() {
        return "TransClass{" +
                "batch=" + batch +
                ", totalBatch=" + totalBatch +
                ", apps=" + apps +
                ", audios=" + audios +
                ", contacts=" + contacts +
                ", docs=" + docs +
                ", videos=" + videos +
                ", images=" + images +
                '}';
    }

    protected TransClass(Parcel in) {
        batch = in.readInt();
        totalBatch = in.readInt();
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
        dest.writeInt(batch);
        dest.writeInt(totalBatch);
    }
}
