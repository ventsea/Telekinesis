package com.st.letter.lib.bean;

import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.st.letter.lib.Utils;

import java.io.File;
import java.net.URLEncoder;
import java.util.List;

import static com.st.letter.lib.media.URLConstant.COLON;
import static com.st.letter.lib.media.URLConstant.FILE;
import static com.st.letter.lib.media.URLConstant.FILE_TYPE;
import static com.st.letter.lib.media.URLConstant.HTTP;
import static com.st.letter.lib.media.URLConstant.NAME;
import static com.st.letter.lib.media.URLConstant.THUMB_IMG;

public class TransFolder {

    public static final String MSG_PATH_INDEX = "/index";

    public List<NFile> nFiles;

    @Override
    public String toString() {
        return "TransFolder{" +
                "nFiles=" + nFiles +
                '}';
    }

    /**
     * 包含文件夹的文件类。用于传输文件夹
     */
    public static class NFile implements Parcelable {
        private String iconUrl;
        private boolean isFile;
        private String mimeType;
        private long size;
        private long date;
        private String dir;
        private String name;

        private NFile() {
        }

        public String getIconUrl() {
            return iconUrl;
        }

        public boolean getIsFile() {
            return isFile;
        }

        public String getMimeType() {
            return mimeType;
        }

        public long getSize() {
            return size;
        }

        public long getDate() {
            return date;
        }

        public String getDir() {
            return dir;
        }

        public String getName() {
            return name;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        private NFile(Parcel in) {
            iconUrl = in.readString();
            isFile = in.readByte() != 0;
            mimeType = in.readString();
            size = in.readLong();
            date = in.readLong();
            dir = in.readString();
            name = in.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(iconUrl);
            dest.writeByte((byte) (isFile ? 1 : 0));
            dest.writeString(mimeType);
            dest.writeLong(size);
            dest.writeLong(date);
            dest.writeString(dir);
            dest.writeString(name);
        }

        public static final Creator<NFile> CREATOR = new Creator<NFile>() {
            @Override
            public NFile createFromParcel(Parcel in) {
                return new NFile(in);
            }

            @Override
            public NFile[] newArray(int size) {
                return new NFile[size];
            }
        };

        public static class Build {

            private File mFile;
            private String ROOT = Environment.getExternalStorageDirectory().getAbsolutePath();
            private String address;
            private int port;

            public Build(String address, int port, File file) {
                this.mFile = file;
                this.address = address;
                this.port = port;
            }

            public NFile build() {
                NFile file = new NFile();
                file.isFile = mFile.isFile();
                file.name = mFile.getName();
                if (file.isFile) {
                    file.mimeType = Utils.getMimeType(mFile.getAbsolutePath());
                    file.size = mFile.length();
                    file.date = mFile.lastModified();
                }
                if (file.isFile) {
                    file.dir = mFile.getAbsolutePath();
                    if (FileType.getFileType(file.mimeType) == FileType.IMG) {
                        file.iconUrl = mFile.getAbsolutePath();
                    } else {
                        file.iconUrl = null;
                    }
                    //最后才build（只有文件时，才会build下载dir）
                    file.buildCorrectFileBean(address, port);
                    //仅encode dir ,其他附带信息不进行encode
                    file.dir = file.dir + NAME + mFile.getName() + FILE_TYPE + file.mimeType;
                } else {
                    file.dir = getSimplifyDir(mFile.getAbsolutePath());
                }
                return file;
            }


            private String getSimplifyDir(String dir) {
                if (dir.startsWith(ROOT)) {
                    dir = dir.substring(dir.indexOf(ROOT) + ROOT.length());
                }
                return dir;
            }
        }

        private void buildCorrectFileBean(String ip, int port) {
            try {
                StringBuilder sb = new StringBuilder();
                dir = sb.append(HTTP).append(ip).append(COLON).append(port).append(FILE).append(URLEncoder.encode(dir, "UTF-8")).toString();
                sb.delete(0, sb.length()); //build dir 完成， 清除，重新build iconUrl
                if (iconUrl != null) {
                    iconUrl = sb.append(HTTP).append(ip).append(COLON).append(port).append(THUMB_IMG).append(URLEncoder.encode(iconUrl, "UTF-8")).toString();
                }
            } catch (Exception e) {
                Log.e("TransFile", "buildCorrectFileBean error", e);
            }
        }

        @Override
        public String toString() {
            return "NFile{" +
                    "iconUrl='" + iconUrl + '\'' +
                    ", isFile=" + isFile +
                    ", mimeType='" + mimeType + '\'' +
                    ", size=" + size +
                    ", date=" + date +
                    ", dir='" + dir + '\'' +
                    '}';
        }
    }
}
