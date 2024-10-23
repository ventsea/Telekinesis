package com.ventsea.directlib.wm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.util.Log;

public class WifiReceiverManager {

    private static final String TAG = "WifiReceiverManager";
    private static final String ACTION_AP = "android.net.wifi.WIFI_AP_STATE_CHANGED";
    private static WifiReceiver wifiReceiver;

    private static OpenAPListener openAPListener;
    private static WifiConnectListener wifiConnectListener;
    private static WifiEnabledListener wifiEnabledListener;

    public static void registerWifiReceiver(Context context) {
        if (null != wifiReceiver) return;
        wifiReceiver = new WifiReceiver(context);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION); //判断wifi状态
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION); //判断wifi连接
        intentFilter.addAction(ACTION_AP);
        context.registerReceiver(wifiReceiver, intentFilter);
        Log.d(TAG, "registerWifiReceiver");
    }

    public static void unregisterWifiReceiver(Context context) {
        if (null == wifiReceiver) return;
        try {
            openAPListener = null;
            wifiConnectListener = null;
            wifiEnabledListener = null;
            context.unregisterReceiver(wifiReceiver);
        } catch (Exception e) {
            Log.e(TAG, "unregisterWifiReceiver error", e);
        } finally {
            wifiReceiver = null;
        }
    }

    public static void setOpenAPListener(OpenAPListener listener) {
        openAPListener = listener;
    }

    public static void setWifiConnectListener(WifiConnectListener listener) {
        wifiConnectListener = listener;
    }

    public static void setWifiEnabledListener(WifiEnabledListener listener) {
        wifiEnabledListener = listener;
    }

    private static class WifiReceiver extends BroadcastReceiver {

        private Handler mHandler = new Handler();
        private boolean mConnected;

        public WifiReceiver(Context context) {
            mConnected = checkWifiConnected(context);
        }

        @Override
        public void onReceive(final Context context, Intent intent) {
            if (isInitialStickyBroadcast()) {
                Log.d(TAG, "isInitialStickyBroadcast");
                return;
            }
            String action = intent.getAction();

            //wifi连接的广播监听
            if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
                mHandler.removeCallbacksAndMessages(null);
                NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                NetworkInfo.State state = info.getState();
                if (state.equals(NetworkInfo.State.DISCONNECTED) && mConnected) {
                    mConnected = false;
                    if (wifiConnectListener != null) wifiConnectListener.onWifiDisconnect();
                } else if (state.equals(NetworkInfo.State.CONNECTED)) {
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mConnected = true;
                            if (wifiConnectListener != null) wifiConnectListener.onWifiConnect();
                        }
                    }, 2000);
                }
            }

            //wifi状态的广播监听
            if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
                handlerWifiStatus(intent);
            }

            //热点的广播监听
            if (ACTION_AP.equals(action)) {
                int state = intent.getIntExtra("wifi_state", 0);
                Log.d(TAG, "wifi_state = " + state);
                switch (state) {
                    case 11: //已关闭
                        if (openAPListener != null) openAPListener.onAPDisabled();
                        break;
                    case 13: //已开启
                        if (openAPListener != null) openAPListener.onAPEnabled();
                        break;
                    default:
                        break;
                }
            }
        }

        private void handlerWifiStatus(Intent intent) {
            int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
            //对wifi的状态进行处理
            switch (wifiState) {
                case WifiManager.WIFI_STATE_ENABLED:
                    //wifi已经打开..
                    if (wifiEnabledListener != null)
                        wifiEnabledListener.onWifiEnabled();
                    break;
                case WifiManager.WIFI_STATE_ENABLING:
                    //wifi打开中..
                    break;
                case WifiManager.WIFI_STATE_DISABLED:
                    //wifi关闭了..
                    if (wifiEnabledListener != null)
                        wifiEnabledListener.onWifiDisabled();
                    break;
                case WifiManager.WIFI_STATE_DISABLING:
                    //wifi关闭中..
                    break;
                case WifiManager.WIFI_STATE_UNKNOWN:
                    //未知状态..
                    break;
            }
        }
    }

    public static boolean checkWifiConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = null;
        if (cm != null) {
            info = cm.getActiveNetworkInfo();
        }
        return info != null && (info.getType() == ConnectivityManager.TYPE_WIFI) && info.isConnected();
    }

    public static boolean checkWifiEnable(Context context) {
        WifiManager manager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        return manager != null && manager.isWifiEnabled();
    }

    public interface OpenAPListener {
        void onAPEnabled();

        void onAPDisabled();
    }

    public interface WifiConnectListener {
        void onWifiConnect();

        void onWifiDisconnect();
    }

    public interface WifiEnabledListener {
        void onWifiEnabled();

        void onWifiDisabled();
    }
}
