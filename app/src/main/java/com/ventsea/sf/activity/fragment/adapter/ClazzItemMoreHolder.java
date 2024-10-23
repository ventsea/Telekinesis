package com.ventsea.sf.activity.fragment.adapter;

import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.ventsea.sf.R;

class ClazzItemMoreHolder extends RecyclerView.ViewHolder {

    TextView title;

    ClazzItemMoreHolder(@NonNull View itemView) {
        super(itemView);
        title = itemView.findViewById(R.id.title);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            title.setBackgroundResource(R.drawable.bg_item_select);
        }
    }
}
