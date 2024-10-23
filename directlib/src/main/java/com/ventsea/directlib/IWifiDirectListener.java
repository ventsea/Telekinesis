package com.ventsea.directlib;

import android.net.wifi.p2p.WifiP2pDeviceList;

public interface IWifiDirectListener {
    void wifiP2pEnabled();
    void wifiP2pDisabled();
    void wifiP2pPeersAvailable(WifiP2pDeviceList deviceList);
    void wifiP2pConnected(String deviceName);
    void wifiP2pDisConnected();
}
