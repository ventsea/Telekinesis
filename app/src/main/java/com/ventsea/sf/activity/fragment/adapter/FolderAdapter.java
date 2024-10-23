package com.ventsea.sf.activity.fragment.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.st.letter.lib.bean.FileType;
import com.st.letter.lib.bean.TransFolder;
import com.ventsea.sf.R;
import com.ventsea.sf.util.Utils;

public class FolderAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private LayoutInflater mInflater;
    private TransFolder mTransFolder;
    private Context mContext;
    private FolderClickListener mListener;
    private Fragment mFragment;

    public FolderAdapter(Context context, TransFolder transFolder, FolderClickListener listener, Fragment fragment) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mTransFolder = transFolder;
        mListener = listener;
        mFragment = fragment;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case 0:
                return new FolderHolder(mInflater.inflate(R.layout.item_folder, parent, false));
            case 1:
            default:
                return new FileHolder(mInflater.inflate(R.layout.item_file, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        TransFolder.NFile file = mTransFolder.nFiles.get(position);
        if (holder instanceof FileHolder) {
            setFileView((FileHolder) holder, file);
        } else if (holder instanceof FolderHolder) {
            setFolderView((FolderHolder) holder, file);
        }
    }

    private void setFileView(FileHolder holder, TransFolder.NFile nFile) {
        int type = FileType.getFileType(nFile.getMimeType());
        if (type == FileType.IMG) {
            String url = nFile.getIconUrl();
            Glide.with(mFragment)
                    .load(url)
                    .into(holder.mFileIcon);
        } else {
            Glide.with(mFragment).clear(holder.mFileIcon);
        }

        switch (type) {
            case FileType.APK:
                holder.mItemIcon.setImageResource(R.drawable.ic_apk);
                break;
            case FileType.IMG:
                holder.mItemIcon.setImageResource(R.drawable.ic_pic);
                break;
            case FileType.AUDIO:
                holder.mItemIcon.setImageResource(R.drawable.ic_audio);
                break;
            case FileType.VIDEO:
                holder.mItemIcon.setImageResource(R.drawable.ic_video);
                break;
            case FileType.DOC:
            case FileType.OTHER:
            case FileType.UN_KNOW:
            default:
                holder.mItemIcon.setImageResource(R.drawable.ic_file);
                break;
        }

        holder.mFileTitle.setText(nFile.getName());
        holder.mSizeAndDate.setText(mContext.getString(R.string.size_date, Utils.readableFileSize(nFile.getSize()), Utils.readableDateTime(nFile.getDate())));
        holder.itemView.setTag(nFile);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    TransFolder.NFile file = (TransFolder.NFile) v.getTag();
                    mListener.onFileClick(file);
                }
            }
        });
    }

    private void setFolderView(FolderHolder holder, TransFolder.NFile nFile) {
        holder.mTitle.setText(nFile.getName());
        holder.itemView.setTag(nFile);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    TransFolder.NFile file = (TransFolder.NFile) v.getTag();
                    mListener.onFolderClick(file.getDir());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        if (mTransFolder != null && mTransFolder.nFiles != null && mTransFolder.nFiles.size() > 0) {
            return mTransFolder.nFiles.size();
        }
        return 0;
    }

    @Override
    public int getItemViewType(int position) {
        if (mTransFolder != null && mTransFolder.nFiles != null && mTransFolder.nFiles.size() > position) {
            return mTransFolder.nFiles.get(position).getIsFile() ? 1 : 0;
        }
        return 2;
    }
}
