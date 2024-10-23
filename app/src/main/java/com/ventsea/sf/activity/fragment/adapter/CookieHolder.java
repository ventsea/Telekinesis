package com.ventsea.sf.activity.fragment.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.ventsea.sf.R;

class CookieHolder extends RecyclerView.ViewHolder {

    TextView title;

    CookieHolder(@NonNull View itemView) {
        super(itemView);
        title = itemView.findViewById(R.id.title);
    }
}
