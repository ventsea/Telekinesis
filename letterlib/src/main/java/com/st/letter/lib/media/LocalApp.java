package com.st.letter.lib.media;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Log;

import java.io.File;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import static com.st.letter.lib.media.URLConstant.COLON;
import static com.st.letter.lib.media.URLConstant.FILE;
import static com.st.letter.lib.media.URLConstant.HTTP;
import static com.st.letter.lib.media.URLConstant.ICON;
import static com.st.letter.lib.media.URLConstant.NAME;

public class LocalApp {

    /**
     * 获取能够打开的app（存在入口Activity）
     *
     * @param context ...
     * @return AppList
     */
    public static List<App> scanAllApp(Context context) {
        List<App> appList = new ArrayList<>();

        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        PackageManager pm = context.getApplicationContext().getPackageManager();

        List<ResolveInfo> resolveInfoList = pm.queryIntentActivities(intent, 0);
        for (ResolveInfo info : resolveInfoList) {
            String pkgName = info.activityInfo.packageName;
            if (pkgName.equals(context.getPackageName())) continue;
            App app = new App();
            try {
                PackageInfo packageInfo = pm.getPackageInfo(pkgName, 0);
                app.vn = packageInfo.versionName;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            try {
                ApplicationInfo applicationInfo = pm.getApplicationInfo(pkgName, PackageManager.GET_META_DATA);
                app.data = applicationInfo.sourceDir;
                app.tgSdk = applicationInfo.targetSdkVersion;
                File file = new File(app.data);
                if (file.exists()) {
                    app.size = file.length();
                } else {
                    continue;
                }
            } catch (Exception e) {
                continue;
            }
            app.packageName = pkgName;
            app.label = info.loadLabel(pm).toString();
            appList.add(app);
        }
        return appList;
    }

    public static class App extends LocalDB {
        public String packageName;
        public String label;
        public String iconUrl;
        public long size;
        public String vn;
        public int tgSdk;

        public App() {

        }

        @Override
        public String toString() {
            return "App{" +
                    "packageName='" + packageName + '\'' +
                    ", label='" + label + '\'' +
                    ", iconUrl='" + iconUrl + '\'' +
                    ", size='" + size + '\'' +
                    ", vn='" + vn + '\'' +
                    ", tgSdk='" + tgSdk + '\'' +
                    ", data='" + data + '\'' +
                    '}';
        }

        @Override
        public void buildCorrectFileBean(String ip, int port) {
            try {
                StringBuilder sb = new StringBuilder();
                if (data != null) {
                    iconUrl = sb.append(HTTP).append(ip).append(COLON).append(port).append(ICON).append(URLEncoder.encode(data, "UTF-8")).toString();
                    sb.delete(0, sb.length()); //build dir 完成， 清除，重新build data
                    data = sb.append(HTTP).append(ip).append(COLON).append(port).append(FILE).append(URLEncoder.encode(data, "UTF-8")).toString();
                    data = data + NAME + packageName + ".apk";
                }
            } catch (Exception e) {
                Log.e("TransFile", "buildCorrectFileBean error", e);
            }
        }
    }
}
