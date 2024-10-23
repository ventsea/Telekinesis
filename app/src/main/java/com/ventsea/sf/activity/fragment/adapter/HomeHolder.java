package com.ventsea.sf.activity.fragment.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ventsea.sf.R;

public class HomeHolder extends RecyclerView.ViewHolder {

    public ImageView icon;
    public TextView title, detail, count;
    public HomeHolder(@NonNull View itemView) {
        super(itemView);
        icon = itemView.findViewById(R.id.icon);
        title = itemView.findViewById(R.id.title);
        detail = itemView.findViewById(R.id.detail);
        count = itemView.findViewById(R.id.count);
    }
}
