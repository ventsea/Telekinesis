package com.ventsea.sf.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.arialyy.annotations.Download;
import com.arialyy.aria.core.Aria;
import com.arialyy.aria.core.download.DownloadTask;
import com.st.letter.lib.server.CoreServer;
import com.ventsea.sf.R;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;

import static com.ventsea.sf.service.Transmission.SERVER_PORT;

public class LiveServer extends Service {

    private static final String TAG_DM = "TAG_DM";
    private static final String EXTRA_LIST = "extra_list";
    private static final String EXTRA_ONE = "extra_one";
    private static final String EXTRA_STOP = "extra_stop";
    private static final String DOWNLOAD_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();

    static final String ACTION = "ACTION";
    static final String ACTION_START_SERVER = "startServer";
    static final String ACTION_START_CLIENT = "startClient";
    static final String ACTION_DOWNLOAD = "download";
    static final String ACTION_STOP = "stopNotification";

    private NotificationManager mNM;
    private static final int NOTIFY_ID = 1001012;

    private static boolean mIsServer;
    private static boolean mRunning;

    private LocalReceiver localReceiver;

    public static boolean isRunning() {
        return mRunning;
    }

    public static boolean isServer() {
        return mIsServer;
    }

    static void startServer(Context context) {
        context.startService(buildIntent(context, ACTION_START_SERVER));
    }

    static void startClient(Context context) {
        context.startService(buildIntent(context, ACTION_START_CLIENT));
    }

    static void addDownloadQueue(Context context, ArrayList<Uri> arrayList) {
        Intent intent = buildIntent(context, ACTION_DOWNLOAD);
        intent.putParcelableArrayListExtra(EXTRA_LIST, arrayList);
        context.startService(intent);
    }

    static void addDownloadQueue(Context context, Uri uri) {
        Intent intent = buildIntent(context, ACTION_DOWNLOAD);
        intent.putExtra(EXTRA_ONE, uri);
        context.startService(intent);
    }

    static void stopDownload(Context context, Uri uri) {
        Intent intent = buildIntent(context, ACTION_DOWNLOAD);
        intent.putExtra(EXTRA_STOP, uri);
        context.startService(intent);
    }

    public static void stop(Context context) {
        context.startService(buildIntent(context, ACTION_STOP));
    }

    private static Intent buildIntent(Context context, String action) {
        Intent intent = new Intent(context.getApplicationContext(), LiveServer.class);
        intent.putExtra(ACTION, action);
        return intent;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mNM = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        registerP2PReceiver();
        Aria.download(this).register();
    }

    private void registerP2PReceiver() {
        IntentFilter intentFilter = new IntentFilter("com.ventsea.directlib.p2p.disabled");
        localReceiver = new LocalReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(localReceiver, intentFilter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getStringExtra(ACTION);
        if (action == null) {
            return super.onStartCommand(intent, flags, startId);
        }
        buildNotification();
        if (ACTION_START_SERVER.equals(action)) {
            mIsServer = true;
            mRunning = true;
            CoreServer.getInstance().startServer(SERVER_PORT);
        }

        if (ACTION_START_CLIENT.equals(action)) {
            mIsServer = false;
            mRunning = true;
            Transmission.getInstance().startClient();
        }

        if (!mRunning) {
            stopSelf();
            return super.onStartCommand(intent, flags, startId);
        }

        if (ACTION_DOWNLOAD.equals(action)) {
            ArrayList<Uri> listExtra = intent.getParcelableArrayListExtra(EXTRA_LIST);
            if (listExtra != null) {
                for (Uri uri : listExtra) {
                    startDownload(uri);
                }
            }
            Uri uri = intent.getParcelableExtra(EXTRA_ONE);
            if (uri != null) {
                startDownload(uri);
            }
            Uri stopUri = intent.getParcelableExtra(EXTRA_STOP);
            if (stopUri != null) {
                stopDownload(stopUri);
            }
        }

        if (ACTION_STOP.equals(action)) {
            stopSelf();
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mRunning = false;
        Aria.download(this).unRegister();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(localReceiver);
        if (mIsServer) {
            CoreServer.getInstance().closeServer();
        } else {
            Transmission.getInstance().closeClient();
        }
    }

    private void buildNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, String.valueOf(NOTIFY_ID));
        builder.setSmallIcon(R.drawable.ic_small_connect_launcher);
        builder.setContentTitle(getString(R.string.app_name));
        builder.setContentText(getString(R.string.keep_running));
        builder.setOngoing(true);
        builder.setSound(null);
        builder.setVibrate(new long[]{0});
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(String.valueOf(NOTIFY_ID), getString(R.string.keep_all), NotificationManager.IMPORTANCE_DEFAULT);
            channel.enableVibration(false);
            channel.enableLights(false);
            channel.setShowBadge(false);
            channel.setSound(null, null);
            mNM.createNotificationChannel(channel);
        }
        startForeground(NOTIFY_ID, builder.build());
    }

    private void startDownload(Uri uri) { //The uri was encode
        if (uri == null) return;
        String name = getNameForUri(uri);
        String filePath = getFilePathForName(name);
        Aria.download(this)
                .load(uri.toString())
                .setFilePath(filePath, true)//设置文件保存的完整路径
                .start();
    }

    private void stopDownload(Uri uri) {
        Aria.download(this).load(uri.toString()).stop();
    }

    private String getFilePathForName(String name) {
        return DOWNLOAD_PATH + "/" + name;
    }

    private String getNameForUri(Uri uri) {
        String name = null;
        String dir = uri.toString();
        try {
            dir = URLDecoder.decode(uri.toString(), "UTF-8");
            uri = Uri.parse(dir);
            name = uri.getQueryParameter("name");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if (name == null) {
            name = dir.substring(dir.lastIndexOf("/") + 1);
        }
        return name;
    }

    @Download.onWait
    public void onTaskWait(DownloadTask task) {

    }

    @Download.onTaskStart
    public void onTaskStart(DownloadTask task) {

    }

    @Download.onTaskResume
    public void onTaskResume(DownloadTask task) {

    }

    @Download.onTaskStop
    public void onTaskStop(DownloadTask task) {

    }

    @Download.onTaskCancel
    public void onTaskCancel(DownloadTask task) {

    }

    @Download.onTaskFail
    public void onTaskFail(DownloadTask task, Exception e) {

    }

    @Download.onTaskComplete
    public void onTaskComplete(DownloadTask task) {

    }

    @Download.onTaskRunning
    public void onTaskRunning(DownloadTask task) {
        long len = task.getFileSize();
        int p = 0;
        if (len != 0) {
            p = (int) (task.getCurrentProgress() * 100 / len);
        }
        Log.d(TAG_DM, "onTaskRunning, Speed : " + task.getSpeed() + ", Progress : " + p);
    }

    private static class LocalReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("IWifiDirect", "LiveServer receive p2p disabled");
            LiveServer.stop(context);
        }
    }
}
