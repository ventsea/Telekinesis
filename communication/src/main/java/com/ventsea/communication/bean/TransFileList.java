package com.ventsea.communication.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.ventsea.communication.http.file.FileServer;
import com.ventsea.communication.utils.Utils;

import java.io.File;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.ventsea.medialib.URLConstant.COLON;
import static com.ventsea.medialib.URLConstant.FILE;
import static com.ventsea.medialib.URLConstant.FILE_TYPE;
import static com.ventsea.medialib.URLConstant.HTTP;
import static com.ventsea.medialib.URLConstant.ICON;
import static com.ventsea.medialib.URLConstant.NAME;
import static com.ventsea.medialib.URLConstant.THUMB_IMG;

public class TransFileList {

    public int batch; //列表批次
    public int totalBatch; //总批次
    public Map<Integer, List<TransFile>> file_map; //文件列表

    @Override
    public String toString() {
        return "TransFileList{" +
                "batch=" + batch +
                ", totalBatch=" + totalBatch +
                ", file_map=" + file_map +
                '}';
    }

    /**
     * 仅包含文件类型的类，用于文件传输
     */
    public static class TransFile implements Parcelable {

        private int file_type;
        private String file_name;
        private long file_size;
        private String file_url; //http://xxx.xxx.xx.xx:????/file?dir=xxx
        private String icon_url; //http://xxx.xxx.xx.xx:????/thumb?dir=xxx&app_icon=1

        private TransFile() {
        }

        public int getFileType() {
            return file_type;
        }

        public long getFileSize() {
            return file_size;
        }

        public String getFileName() {
            return file_name;
        }

        public String getFileUrl() {
            if (file_url.contains(FILE)) {
                return file_url.substring(file_url.indexOf(FILE) + FILE.length());
            }
            return file_url;
        }

        public String getIconUrl() {
            if (icon_url.contains(ICON)) {
                return icon_url.substring(icon_url.indexOf(ICON) + ICON.length());
            }
            return icon_url;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof TransFile)) return false;
            TransFile transFile = (TransFile) o;
            return file_type == transFile.file_type &&
                    file_size == transFile.file_size &&
                    ((transFile.file_name != null && file_name != null) && transFile.file_name.equals(file_name)) &&
                    ((transFile.file_url != null && file_url != null) && transFile.file_url.equals(file_url)) &&
                    ((transFile.icon_url != null && icon_url != null) && transFile.icon_url.equals(icon_url));
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(new Object[]{file_type, file_name, file_size, file_url, icon_url});
        }

        private TransFile(Parcel in) {
            file_type = in.readInt();
            file_name = in.readString();
            file_size = in.readLong();
            file_url = in.readString();
            icon_url = in.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(file_type);
            dest.writeString(file_name);
            dest.writeLong(file_size);
            dest.writeString(file_url);
            dest.writeString(icon_url);
        }

        public static final Creator<TransFile> CREATOR = new Creator<TransFile>() {
            @Override
            public TransFile createFromParcel(Parcel in) {
                return new TransFile(in);
            }

            @Override
            public TransFile[] newArray(int size) {
                return new TransFile[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        public static class Build {

            private File file;
            private int type;

            public Build(int fileType, File file) {
                this.file = file;
                this.type = fileType;
            }

            public TransFile build() {
                TransFile transFile = new TransFile();
                transFile.file_url = file.getAbsolutePath();
                transFile.file_name = file.getName();
                transFile.file_size = file.length();
                transFile.file_type = type;
                if (type == FileType.IMG) {
                    transFile.icon_url = file.getAbsolutePath();
                } else if (type == FileType.APK) {
                    transFile.file_name = Utils.getAppNameForDir(Utils.sContext, file.getAbsolutePath());
                    transFile.icon_url = file.getAbsolutePath();
                } else {
                    transFile.icon_url = null;
                }
                transFile.buildCorrectFileBean(FileServer.getInstance().getAddress(), FileServer.getInstance().getPort());
                transFile.file_url = transFile.file_url + NAME + file.getName() + FILE_TYPE + type;
                return transFile;
            }
        }

        private void buildCorrectFileBean(String ip, int port) { //修正url
            try {
                StringBuilder sb = new StringBuilder();
                file_url = sb.append(HTTP).append(ip).append(COLON).append(port).append(FILE).append(URLEncoder.encode(file_url, "UTF-8")).toString();
                sb.delete(0, sb.length());//build file_url 完成， 清除，重新build icon_url
                if (icon_url != null) {
                    String thumb;
                    if (file_type == FileType.IMG) {
                        thumb = THUMB_IMG;
                    } else {
                        thumb = ICON;
                    }
                    icon_url = sb.append(HTTP).append(ip).append(COLON).append(port).append(thumb).append(URLEncoder.encode(icon_url, "UTF-8")).toString();
                }
            } catch (Exception e) {
                Log.e("TransFile", "buildCorrectFileBean error", e);
            }
        }

        @Override
        public String toString() {
            return "TransFile{" +
                    "file_type=" + file_type +
                    ", file_name='" + file_name + '\'' +
                    ", file_size=" + file_size +
                    ", file_url='" + file_url + '\'' +
                    ", icon_url='" + icon_url + '\'' +
                    '}';
        }
    }

}
