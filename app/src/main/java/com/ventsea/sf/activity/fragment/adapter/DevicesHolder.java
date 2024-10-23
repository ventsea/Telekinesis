package com.ventsea.sf.activity.fragment.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import com.ventsea.sf.R;

class DevicesHolder extends RecyclerView.ViewHolder {

    Button deviceName;

    DevicesHolder(View itemView) {
        super(itemView);
        deviceName = itemView.findViewById(R.id.device_name);
    }
}
