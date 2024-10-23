package com.ventsea.directlib;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WDirect implements IWifiDirect, ReceiverListener {

    @SuppressLint("StaticFieldLeak")
    private static WDirect INSTANCE;
    private WifiManager mWifiManager;
    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private Set<IWifiDirectListener> mOtherListener;
    private Context mContext;
    private Runnable mRequestGroupRunnable;
    private boolean mGroupCreated;
    private boolean mConnectGroup;
    private Handler mHandler;
    private DirectBroadCast mDirectBroadCast;
    private ExecutorService executorService;

    private WDirect() {
    }

    public static WDirect getInstance() {
        if (INSTANCE == null) {
            synchronized (WDirect.class) {
                if (INSTANCE == null)
                    INSTANCE = new WDirect();
            }
        }
        return INSTANCE;
    }

    public void init(Context context, boolean support, IWifiDirectListener listener) {
        if (mContext != null) {
            Log.d(TAG, "WDirect has been initialized before");
            return;
        }
        mHandler = new Handler();
        mContext = context.getApplicationContext();
        mOtherListener = Collections.synchronizedSet(new HashSet<IWifiDirectListener>());
        mManager = (WifiP2pManager) mContext.getSystemService(Context.WIFI_P2P_SERVICE);
        mWifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        addDirectListener(listener);
        mChannel = mManager.initialize(mContext, Looper.getMainLooper(), new WifiP2pManager.ChannelListener() {
            @Override
            public void onChannelDisconnected() {
                Log.d(TAG, "initialize onChannelDisconnected");
            }
        });
        setDeviceName();
        if (support)
            setChannel();
        mDirectBroadCast = DirectBroadCast.startReceiver(mContext, this);
        executorService = Executors.newSingleThreadExecutor();
        Log.d(TAG, "WDirect initialized");
    }

    private void setDeviceName() {
        if (mManager == null || mChannel == null) {
            return;
        }
        try {
            Method method = mManager.getClass().getMethod("setDeviceName", WifiP2pManager.Channel.class, String.class, WifiP2pManager.ActionListener.class);
            method.setAccessible(true);
            method.invoke(mManager, mChannel, DEVICE_NAME, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onFailure(int reason) {

                }
            });
        } catch (Exception e) {
            Log.e(TAG, "setDeviceName error : " + e.getMessage());
        }
    }

    private void setChannel() {
        if (mManager == null || mChannel == null) {
            return;
        }
        try {
            Method method = mManager.getClass().getMethod("setWifiP2pChannels", WifiP2pManager.Channel.class, int.class, int.class, WifiP2pManager.ActionListener.class);
            method.setAccessible(true);
            //信道：1 ，6 ，11
            //oc = 1 : support
            //oc = 0 : normal
            method.invoke(mManager, mChannel, 0, 6, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                }

                @Override
                public void onFailure(int reason) {
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "setChannels error : " + e.getMessage());
        }
    }

    public void createGroup() {
        if (mContext == null) {
            Log.d(TAG, "plz init WDirect first");
            return;
        }
        mManager.createGroup(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "createGroup start succeeded...");
            }

            @Override
            public void onFailure(int reason) {
                Log.d(TAG, "createGroup start failed : " + reason);
                wifiP2PDisabled();
            }
        });
    }

    public void discoverGroup() {
        if (mContext == null) {
            Log.d(TAG, "plz init WDirect first");
            return;
        }

        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "discoverGroup start succeeded...");
            }

            @Override
            public void onFailure(int reason) {
                Log.d(TAG, "discoverPeers start failed...");
            }
        });
    }

    public void connectGroup(WifiP2pDevice device) {
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        Log.d(TAG, "apply for a connection" + device.deviceName + "...");
        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                //success logic
                Log.d(TAG, "successful request connect group...");
            }

            @Override
            public void onFailure(int reason) {
                //failure logic
                Log.d(TAG, "request connect group failed!");
            }
        });
    }

    public void addDirectListener(final IWifiDirectListener listener) {
        if (mOtherListener == null) {
            Log.d(TAG, "plz init WDirect first");
            return;
        }
        if (listener != null) {
            mOtherListener.add(listener);
        }
    }

    /**
     * 及时除移监听，预防内存泄露
     *
     * @param listener ...
     */
    public void removeDirectListener(final IWifiDirectListener listener) {
        if (mOtherListener == null) return;
        if (listener != null) {
            mOtherListener.remove(listener);
        }
    }

    //主动断开，清理监听
    public void stopDirect() {
        if (mOtherListener != null) {
            mOtherListener.clear();
        }
        if (mManager == null || mChannel == null) return;

        if (mDirectBroadCast != null) {
            mDirectBroadCast.removeReceiverListener();
            mDirectBroadCast = null;
        }
        mManager.stopPeerDiscovery(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "stopPeerDiscovery succeeded");
            }

            @Override
            public void onFailure(int reason) {
                Log.d(TAG, "stopPeerDiscovery failed");
            }
        });
        if (mRequestGroupRunnable != null) mHandler.removeCallbacks(mRequestGroupRunnable);

        mManager.cancelConnect(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "cancelConnect succeeded");
            }

            @Override
            public void onFailure(int reason) {
                Log.d(TAG, "cancelConnect failed");
            }
        });
        mManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "removeGroup succeeded");
            }

            @Override
            public void onFailure(int reason) {
                Log.d(TAG, "onFailure failed");
            }
        });
        mContext = null;
        mGroupCreated = false;
        mConnectGroup = false;
        mManager = null;
        mChannel = null;
    }

    /*--------------------------------------------------------------------------------------------*/

    @Override
    public void wifiP2PEnabled() {
        if (mOtherListener != null) {
            for (IWifiDirectListener listener : mOtherListener) {
                if (listener != null) listener.wifiP2pEnabled();
            }
        }
    }

    @Override
    public void wifiP2PDisabled() {
        if (mOtherListener != null) {
            for (IWifiDirectListener listener : mOtherListener) {
                if (listener != null) listener.wifiP2pDisabled();
            }
        }
        notifyLiveServerP2PDisabled();
        stopDirect();
    }

    private void notifyLiveServerP2PDisabled() {
        Log.d(TAG, "notifyLiveServerP2PDisabled");
        Intent intent = new Intent("com.ventsea.directlib.p2p.disabled");
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    @Override
    public void wifiP2PPeersChanged() {
        mWifiManager.getScanResults();
        mManager.requestPeers(mChannel, new WifiP2pManager.PeerListListener() {
            @Override
            public void onPeersAvailable(final WifiP2pDeviceList peers) {
                if (peers != null) {
                    if (mOtherListener != null) {
                        for (IWifiDirectListener listener : mOtherListener) {
                            if (listener != null) listener.wifiP2pPeersAvailable(peers);
                        }
                    }
                }
            }
        });
    }

    @Override
    public void wifiP2PConnected() {
        mManager.requestConnectionInfo(mChannel, new WifiP2pManager.ConnectionInfoListener() {
            @Override
            public void onConnectionInfoAvailable(final WifiP2pInfo info) {
                executorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        final String address = info.groupOwnerAddress.toString();
                        mHandler.post(getRequestGroupInfoRunnable(info, address.substring(1)));
                    }
                });
            }
        });
    }

    @Override
    public void wifiP2PDisConnected() {
        if (mOtherListener != null) {
            for (IWifiDirectListener listener : mOtherListener) {
                if (listener != null) listener.wifiP2pDisConnected();
            }
        }
        if (mConnectGroup) {
            notifyLiveServerP2PDisabled();
            stopDirect();
        }
    }

    /*--------------------------------------------------------------------------------------------*/

    private Runnable getRequestGroupInfoRunnable(final WifiP2pInfo info, final String address) {
        return mRequestGroupRunnable = new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "onConnectionInfoAvailable [groupOwnerAddress : " + address + "]");
                mManager.requestGroupInfo(mChannel, new WifiP2pManager.GroupInfoListener() {
                    @Override
                    public void onGroupInfoAvailable(WifiP2pGroup group) {
                        if (group == null) {
                            Log.d(TAG, "onGroupInfoAvailable, group = null");
                            return;
                        }
                        String passWorld = group.getPassphrase();
                        Log.d(TAG, "passWorld : " + passWorld);
                        final WifiP2pDevice owner = group.getOwner();
                        Log.d(TAG, "onGroupInfoAvailable [group networkName : " + group.getNetworkName() +
                                ", owner deviceName : " + owner.deviceName +
                                ", owner connect status : " + owner.status + "]");
                        for (WifiP2pDevice device : group.getClientList()) {
                            Log.d(TAG, "\ndevices : [" + device + "]");
                        }

                        if (info.groupFormed) {
                            if (info.isGroupOwner) {
                                if (mGroupCreated) return;
                                mGroupCreated = true;
                                Log.d(TAG, "group created successfully");
                                if (mOtherListener != null) {
                                    for (IWifiDirectListener listener : mOtherListener) {
                                        if (listener != null)
                                            listener.wifiP2pConnected(DEVICE_NAME);
                                    }
                                }
                            } else {
                                if (mConnectGroup) return;
                                mConnectGroup = true;
                                Log.d(TAG, "connect Group Owner");
                                if (mOtherListener != null) {
                                    for (IWifiDirectListener listener : mOtherListener) {
                                        if (listener != null)
                                            listener.wifiP2pConnected(owner.deviceName);
                                    }
                                }
                            }
                        } else {
                            if (mOtherListener != null) {
                                for (IWifiDirectListener listener : mOtherListener) {
                                    if (listener != null) listener.wifiP2pDisConnected();
                                }
                            }
                            Log.d(TAG, "group build failed!");
                        }
                    }
                });
            }
        };
    }
}
