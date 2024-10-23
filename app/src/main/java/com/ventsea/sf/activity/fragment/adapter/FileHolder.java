package com.ventsea.sf.activity.fragment.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ventsea.sf.R;

class FileHolder extends RecyclerView.ViewHolder {

    ImageView mFileIcon;
    ImageView mItemIcon;
    TextView mFileTitle;
    TextView mSizeAndDate;

    FileHolder(View itemView) {
        super(itemView);
        mFileIcon = itemView.findViewById(R.id.file_icon);
        mItemIcon = itemView.findViewById(R.id.item_icon);
        mFileTitle = itemView.findViewById(R.id.file_title);
        mSizeAndDate = itemView.findViewById(R.id.file_size_date);
    }
}
