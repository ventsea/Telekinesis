package com.ventsea.sf.activity.fragment.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.st.letter.lib.bean.TransFolder;
import com.st.letter.lib.bean.TransHome;
import com.ventsea.sf.R;

import java.util.List;

public class CookieAdapter extends RecyclerView.Adapter<CookieHolder> {

    private List<String> cookieFolders;
    private LayoutInflater inflater;
    private CookieClickListener clickListener;
    private String deviceName;

    public CookieAdapter(Context context, List<String> list, String deviceName, CookieClickListener listener) {
        cookieFolders = list;
        inflater = LayoutInflater.from(context);
        clickListener = listener;
        this.deviceName = deviceName;
    }

    @NonNull
    @Override
    public CookieHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new CookieHolder(inflater.inflate(R.layout.item_popup_folder, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull CookieHolder homeHolder, int i) {
        String url = cookieFolders.get(i);
        switch (url) {
            case TransFolder.MSG_PATH_INDEX:
                homeHolder.title.setText(deviceName);
                break;
            case TransHome.MSG_PATH_HOME:
                homeHolder.title.setText(R.string.preview);
                break;
            default:
                homeHolder.title.setText(url.substring(url.lastIndexOf("/")));
                break;
        }
        homeHolder.itemView.setTag(url);
        homeHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = (String) v.getTag();
                if (clickListener != null) clickListener.onCookieClick(url);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cookieFolders.size();
    }
}
