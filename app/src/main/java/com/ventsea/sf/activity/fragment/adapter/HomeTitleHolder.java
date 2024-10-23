package com.ventsea.sf.activity.fragment.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.ventsea.sf.R;

public class HomeTitleHolder extends RecyclerView.ViewHolder {

    public TextView title;

    public HomeTitleHolder(@NonNull View itemView) {
        super(itemView);
        title = itemView.findViewById(R.id.title);
    }
}
