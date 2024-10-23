package com.ventsea.communication.utils;

import android.util.Log;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

public class P2PUtil {

    public static String getP2PAddress() {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface network : interfaces) {
                if (!network.getName().startsWith("p2p")) {//获取direct 的ip地址
                    continue;
                }
                List<InetAddress> addresses = Collections.list(network.getInetAddresses());
                for (InetAddress address : addresses) {
                    if (!address.isLoopbackAddress()) {
                        String host = address.getHostAddress();
                        boolean isIPv4 = host.indexOf(':') < 0;
                        if (isIPv4) {
                            return host;
                        }
                    }
                }
            }
        } catch (Exception ex) {
            Log.w("P2P", "getP2PAddress error", ex);
        }
        return "";
    }
}
