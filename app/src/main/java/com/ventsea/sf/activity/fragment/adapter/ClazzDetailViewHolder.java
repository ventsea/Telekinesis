package com.ventsea.sf.activity.fragment.adapter;

import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ventsea.sf.R;

class ClazzDetailViewHolder extends RecyclerView.ViewHolder {

    ImageView icon, more;
    TextView title, sizeAndData;

    ClazzDetailViewHolder(@NonNull View itemView) {
        super(itemView);
        icon = itemView.findViewById(R.id.icon);
        more = itemView.findViewById(R.id.more);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            more.setBackgroundResource(R.drawable.bg_item_select);
        }
        sizeAndData = itemView.findViewById(R.id.size_data);
        title = itemView.findViewById(R.id.title);
    }
}
