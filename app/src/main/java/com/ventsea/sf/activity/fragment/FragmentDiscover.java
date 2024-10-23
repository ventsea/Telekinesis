package com.ventsea.sf.activity.fragment;

import android.app.Activity;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.ventsea.directlib.IWifiDirectListener;
import com.ventsea.directlib.WDirect;
import com.ventsea.sf.R;
import com.ventsea.sf.activity.fragment.adapter.DevicesAdapter;

import java.util.ArrayList;
import java.util.List;


public class FragmentDiscover extends Fragment implements IWifiDirectListener {

    private static final String TAG = "FragmentDiscover";
    private static final String KEY_STATE = "STATE";
    private static final String KEY_CONNECT = "CONNECT";
    private int mState;
    private RecyclerView mDevicesRecycler;
    private ProgressBar mProgress;
    private DevicesAdapter mAdapter;
    private List<WifiP2pDevice> mDevices;
    private boolean mCanClick;
    private boolean mConnecting;
    private Handler mHandler;
    private Runnable mClickRunnable;
    private Runnable mTimeOutRunnable;
    private FloatingActionButton mDiscover;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mState = savedInstanceState.getInt(KEY_STATE, 0);
            mConnecting = savedInstanceState.getBoolean(KEY_CONNECT, false);
        }
        mHandler = new Handler(Looper.getMainLooper());
        mDevices = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_devices, container, false);
        mDevicesRecycler = view.findViewById(R.id.device_recycler);
        mProgress = view.findViewById(R.id.connect_progress);
        mDiscover = view.findViewById(R.id.discover);
        if (mState == 0) {
            Log.d(TAG, "mState != 0");
        } else {
            Log.d(TAG, "mState == 0");
        }
        getReceiverActivity();
        WDirect.getInstance().addDirectListener(this);
        WDirect.getInstance().discoverGroup();
        mDiscover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mConnecting) return;
                if (mCanClick) {
                    rotateView(mDiscover);
                    WDirect.getInstance().discoverGroup();
                    mCanClick = false;
                    initClickable();
                }
            }
        });
        if (mConnecting) mProgress.setVisibility(View.VISIBLE);
        return view;
    }

    private void rotateView(View view) {
        RotateAnimation animation = new RotateAnimation(
                0.0f, 360.0f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setDuration(500);
        view.startAnimation(animation);
    }

    private void getReceiverActivity() {
        Activity activity = getActivity();
        if (activity instanceof EventListener) {
            EventListener listener = (EventListener) activity;
            listener.onShowTitle(getTag());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        WDirect.getInstance().removeDirectListener(this);
        if (mClickRunnable != null) mHandler.removeCallbacks(mClickRunnable);
        if (mTimeOutRunnable != null) mHandler.removeCallbacks(mTimeOutRunnable);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_STATE, 1);
        outState.putBoolean(KEY_CONNECT, mConnecting);
    }

    private void showDevicesList() {
        if (mAdapter == null) {
            mDevicesRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
            mDevicesRecycler.setAdapter(mAdapter = new DevicesAdapter(getActivity(), mDevices));
            mAdapter.setClickDeviceListener(new DevicesAdapter.ClickDeviceListener() {
                @Override
                public void onDeviceClick(WifiP2pDevice device) {
                    if (mConnecting) return;
                    WDirect.getInstance().connectGroup(device);
                    mProgress.setVisibility(View.VISIBLE);
                    mConnecting = true;
                    mHandler.postDelayed(getTimeOutRunnable(), 15000);
                }
            });
        } else {
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        initClickable();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        WDirect.getInstance().removeDirectListener(this);
    }

    private void initClickable() {
        mHandler.removeCallbacks(getCanClick());
        mHandler.postDelayed(getCanClick(), 1000);
    }

    private Runnable getCanClick() {
        if (mClickRunnable != null) return mClickRunnable;
        return mClickRunnable = new Runnable() {
            @Override
            public void run() {
                mCanClick = true;
            }
        };
    }

    private Runnable getTimeOutRunnable() {
        if (mTimeOutRunnable != null) return mTimeOutRunnable;
        return mTimeOutRunnable = new Runnable() {
            @Override
            public void run() {
                FragmentActivity activity = getActivity();
                if (activity != null) {
                    Toast.makeText(activity, "连接超时", Toast.LENGTH_SHORT).show();
                    activity.onBackPressed();
                }
            }
        };
    }

    @Override
    public void wifiP2pEnabled() {

    }

    @Override
    public void wifiP2pDisabled() {

    }

    @Override
    public void wifiP2pPeersAvailable(WifiP2pDeviceList deviceList) {
        if (mDevices != null) {
            mDevices.clear();
            mDevices.addAll(deviceList.getDeviceList());
            showDevicesList();
        }
    }

    @Override
    public void wifiP2pConnected(String deviceName) {

    }

    @Override
    public void wifiP2pDisConnected() {

    }
}
