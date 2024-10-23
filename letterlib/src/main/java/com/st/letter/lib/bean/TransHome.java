package com.st.letter.lib.bean;

import android.os.Parcel;
import android.os.Parcelable;

public class TransHome implements Parcelable {

    public static final String MSG_PATH_HOME = "/home";

    public static final int TYPE_APP = 0;
    public static final int TYPE_AUDIO = 1;
    public static final int TYPE_CONTACT = 2;
    public static final int TYPE_DOC = 3;
    public static final int TYPE_IMAGE = 4;
    public static final int TYPE_VIDEO = 5;

    private int type;
    private int size;

    public TransHome(int type, int size) {
        this.type = type;
        this.size = size;
    }

    public int getType() {
        return type;
    }

    public int getSize() {
        return size;
    }

    @Override
    public String toString() {
        return "TransHome{" +
                "type=" + type +
                ", size=" + size +
                '}';
    }

    protected TransHome(Parcel in) {
        type = in.readInt();
        size = in.readInt();
    }

    public static final Creator<TransHome> CREATOR = new Creator<TransHome>() {
        @Override
        public TransHome createFromParcel(Parcel in) {
            return new TransHome(in);
        }

        @Override
        public TransHome[] newArray(int size) {
            return new TransHome[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(type);
        dest.writeInt(size);
    }
}
