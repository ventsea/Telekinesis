package com.ventsea.sf.activity.fragment.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.ventsea.sf.R;

class FolderHolder extends RecyclerView.ViewHolder {

    TextView mTitle;

    FolderHolder(View itemView) {
        super(itemView);
        mTitle = itemView.findViewById(R.id.folder_title);
    }
}
