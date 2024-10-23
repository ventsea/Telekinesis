package com.ventsea.sf.activity.fragment.uibean;

import android.os.Parcel;
import android.os.Parcelable;

public class Clazz implements Parcelable{
    public static final int TYPE_TITLE = -1;
    public static final int TYPE_STORAGE = -2;
    public int clazzType;
    public String title;
    public int count;
    public long size;
    public boolean loaded;

    public Clazz() {

    }

    public static Clazz build(int type) {
        Clazz clazz = new Clazz();
        clazz.clazzType = type;
        clazz.count = 0;
        clazz.loaded = false;
        return clazz;
    }

    public Clazz(Parcel in) {
        clazzType = in.readInt();
        title = in.readString();
        count = in.readInt();
        size = in.readLong();
        loaded = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(clazzType);
        dest.writeString(title);
        dest.writeInt(count);
        dest.writeLong(size);
        dest.writeByte((byte) (loaded ? 1 : 0));
    }

    public static final Parcelable.Creator<Clazz> CREATOR = new Parcelable.Creator<Clazz>() {
        @Override
        public Clazz createFromParcel(Parcel in) {
            return new Clazz(in);
        }

        @Override
        public Clazz[] newArray(int size) {
            return new Clazz[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }
}
