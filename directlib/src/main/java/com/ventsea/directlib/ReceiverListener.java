package com.ventsea.directlib;

interface ReceiverListener {
    void wifiP2PEnabled();
    void wifiP2PDisabled();
    void wifiP2PPeersChanged();
    void wifiP2PConnected();
    void wifiP2PDisConnected();
}
