package com.ventsea.sf.activity.fragment.adapter;

import com.st.letter.lib.bean.TransFolder;

public interface FolderClickListener {
    void onFolderClick(String dir);
    void onFileClick(TransFolder.NFile file);
}
