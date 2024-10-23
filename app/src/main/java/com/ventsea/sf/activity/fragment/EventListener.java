package com.ventsea.sf.activity.fragment;

import android.view.View;

import com.st.letter.lib.bean.TransFolder;

import java.util.List;

public interface EventListener {
    void onLoadNextPage(String url);
    void onOpenFile(TransFolder.NFile file);
    void onJumpPage(String url);
    void onShowTitle(String url);
    void onOpenClazz(int type);
    void onShowPhotoPage(List<String> urlList, int position);
    void onShowPlayerView(String url);
    void onItemMoreClick(View view, Object o);
}
