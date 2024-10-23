package com.ventsea.directlib;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import static android.net.wifi.p2p.WifiP2pManager.EXTRA_WIFI_P2P_DEVICE;

class DirectBroadCast extends BroadcastReceiver {

    private ReceiverListener mListener;
    private static final String TAG = IWifiDirect.TAG;

    private DirectBroadCast(ReceiverListener listener) {
        mListener = listener;
    }

    static DirectBroadCast startReceiver(Context context, ReceiverListener listener) {
        DirectBroadCast broadCast = new DirectBroadCast(listener);
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        filter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        filter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        filter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        context.getApplicationContext().registerReceiver(broadCast, filter);
        return broadCast;
    }

    void removeReceiverListener() {
        mListener = null;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (isInitialStickyBroadcast()) {
            if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action) || WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
                Log.d(TAG, "isInitialStickyBroadcast : " + action);
                return;
            }
        }

        //可用发生改变
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) { //不过滤粘性广播
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            Log.d(TAG, "android.net.wifi.p2p.STATE_CHANGED, state : " + (state == 2 ? "ENABLED" : "DISABLED"));
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                if (mListener != null) mListener.wifiP2PEnabled();
            } else {
                if (mListener != null) mListener.wifiP2PDisabled();
            }
            return;
        }

        //列表发生改变
        if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) { //不过滤粘性
            Log.d(TAG, "android.net.wifi.p2p.PEERS_CHANGED");
            if (mListener != null) mListener.wifiP2PPeersChanged();
            return;
        }

        //连接发生改变
        if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) { //过滤粘性
            NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            boolean isConnect = networkInfo.isConnected();
            Log.d(TAG, "android.net.wifi.p2p.CONNECTION_STATE_CHANGE : " + isConnect);
            if (isConnect) {
                if (mListener != null) mListener.wifiP2PConnected();
            } else {
                if (mListener != null) mListener.wifiP2PDisConnected();
            }
            return;
        }

        //本机设备发生改变
        if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) { //过滤粘性
            WifiP2pDevice device = intent.getParcelableExtra(EXTRA_WIFI_P2P_DEVICE); //获取修改后的名称
            Log.d(TAG, "android.net.wifi.p2p.THIS_DEVICE_CHANGED, WifiP2pDevice Name : " + (device != null ? device.deviceName : "null"));
        }
    }
}
