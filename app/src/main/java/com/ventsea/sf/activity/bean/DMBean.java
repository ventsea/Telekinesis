package com.ventsea.sf.activity.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.core.inf.IEntity;
import com.st.letter.lib.Utils;
import com.st.letter.lib.bean.FileType;

public class DMBean implements Parcelable {

    /**
     * {@link com.arialyy.aria.core.inf.IEntity}
     */
    /**
     * 其它状态
     */
    public static final int STATE_OTHER = IEntity.STATE_OTHER;
    /**
     * 失败状态
     */
    public static final int STATE_FAIL = 0;
    /**
     * 完成状态
     */
    public static final int STATE_COMPLETE = 1;
    /**
     * 停止状态
     */
    public static final int STATE_STOP = 2;
    /**
     * 等待状态
     */
    public static final int STATE_WAIT = 3;
    /**
     * 正在执行
     */
    public static final int STATE_RUNNING = 4;
    /**
     * 预处理
     */
    public static final int STATE_PRE = 5;
    /**
     * 预处理完成
     */
    public static final int STATE_POST_PRE = 6;
    /**
     * 删除任务
     */
    public static final int STATE_CANCEL = 7;

    public String url;
    public String title;
    public String filePath;
    public int type;

    public int state;
    public int progress;

    private DMBean() {

    }

    public DMBean(String url) {
        this.url = url;
    }

    protected DMBean(Parcel in) {
        url = in.readString();
        title = in.readString();
        filePath = in.readString();
        type = in.readInt();
        state = in.readInt();
        progress = in.readInt();
    }

    public static final Creator<DMBean> CREATOR = new Creator<DMBean>() {
        @Override
        public DMBean createFromParcel(Parcel in) {
            return new DMBean(in);
        }

        @Override
        public DMBean[] newArray(int size) {
            return new DMBean[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(url);
        dest.writeString(title);
        dest.writeString(filePath);
        dest.writeInt(type);
        dest.writeInt(state);
        dest.writeInt(progress);
    }

    public static DMBean buildDMBean(DownloadEntity entity) {
        if (entity != null) {
            DMBean dmBean = new DMBean();
            dmBean.url = entity.getKey();
            dmBean.title = entity.getFileName();
            dmBean.filePath = entity.getDownloadPath();
            dmBean.type = FileType.getFileType(Utils.getMimeType(entity.getDownloadPath()));

            dmBean.state = entity.getState();
            dmBean.progress = entity.getPercent();
            return dmBean;
        }
        return null;
    }
}
