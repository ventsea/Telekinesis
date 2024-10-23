package com.ventsea.sf.activity.fragment.adapter;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ventsea.sf.R;

import java.util.List;

public class DevicesAdapter extends RecyclerView.Adapter<DevicesHolder> {

    private List<WifiP2pDevice> mDeviceList;
    private LayoutInflater mInflater;
    private ClickDeviceListener mClickListener;

    public DevicesAdapter(Context context, List<WifiP2pDevice> list) {
        this.mDeviceList = list;
        this.mInflater = LayoutInflater.from(context);
    }

    public void setClickDeviceListener(ClickDeviceListener listener) {
        mClickListener = listener;
    }

    @NonNull
    @Override
    public DevicesHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DevicesHolder(mInflater.inflate(R.layout.item_devices, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull DevicesHolder holder, int position) {
        final WifiP2pDevice device = mDeviceList.get(position);
        if (device.deviceName != null) {
            holder.deviceName.setText(device.deviceName);
            holder.deviceName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mClickListener != null) mClickListener.onDeviceClick(device);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mDeviceList.size();
    }

    public interface ClickDeviceListener {
        void onDeviceClick(WifiP2pDevice device);
    }
}
