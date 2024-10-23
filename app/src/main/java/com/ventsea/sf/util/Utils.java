package com.ventsea.sf.util;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class Utils {

    public static String readableFileSize(long size) {
        int decimal = 1024;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            decimal = 1000;
        }
        if (size <= 0)
            return "0";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(decimal));
        return new DecimalFormat("#,##0.##").format(size / Math.pow(decimal, digitGroups)) + " " + units[digitGroups];
    }

    public static String readableDateTime(long time) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        format.setTimeZone(TimeZone.getTimeZone("GMT+0"));
        Date date = new Date(time);
        return format.format(date);
    }

    public static String readableDuration(long duration) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+0"));
        return sdf.format(new Date(duration));
    }

    public static synchronized <T> void saveListData(Context context, List<T> list, String type, String tag) {
        File file = context.getCacheDir();
        File cache = new File(file, "cache_list_" + type + tag);
        if (cache.exists()) {
            cache.delete();
        }
        ObjectOutputStream outputStream = null;
        try {
            outputStream = new ObjectOutputStream(new FileOutputStream(cache));
            outputStream.writeObject(list);
        } catch (IOException e) {
            Log.d("SAVE_DATA", "saveListData error", e);
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static synchronized <T> List<T> getListData(Context context, String type, String tag) {
        File file = context.getCacheDir();
        File cache = new File(file, "cache_list_" + type + tag);
        if (!cache.exists()) {
            return null;
        }
        List<T> list = null;
        ObjectInputStream inputStream = null;
        try {
            inputStream = new ObjectInputStream(new FileInputStream(cache));
            list = (List<T>) inputStream.readObject();
        } catch (Exception e) {
            Log.d("SAVE_DATA", "getListData error", e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return list;
    }

    public static long getVersionCode(Context context) {
        long versionCode = 0;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                versionCode = context.getPackageManager().
                        getPackageInfo(context.getPackageName(), 0).getLongVersionCode();
            } else {
                versionCode = context.getPackageManager().
                        getPackageInfo(context.getPackageName(), 0).versionCode;
            }
        } catch (PackageManager.NameNotFoundException ignore) {
        }
        return versionCode;
    }

    public static String getVersionName(Context context) {
        String vn = "UNKNOWN";
        try {
            vn = context.getPackageManager().
                    getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException ignore) {
        }
        return vn;
    }

    /**
     * 获取屏幕高度(px)
     */
    public static int getScreenHeight(Context context) {
        return context.getResources().getDisplayMetrics().heightPixels;
    }

    /**
     * 获取屏幕宽度(px)
     */
    public static int getScreenWidth(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }
}
