package com.ventsea.sf.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.arialyy.annotations.Download;
import com.arialyy.aria.core.Aria;
import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.core.download.DownloadTask;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.st.letter.lib.Utils;
import com.st.letter.lib.bean.FileType;
import com.ventsea.sf.R;
import com.ventsea.sf.activity.base.BaseActivity;
import com.ventsea.sf.activity.bean.DMBean;
import com.ventsea.sf.service.Transmission;
import com.ventsea.sf.view.DonutProgress;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.content.Intent.ACTION_VIEW;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;

public class DownloadManagerActivity extends BaseActivity implements ButtonClickListener {

    private static final String TAG = "TAG_DM";
    private static final String SAVE_BEAN = "save_list_bean";
    private static final String SAVE_URI = "save_list_uri";
    private List<DMBean> mDMBeanList;
    private List<String> mUriList;
    private RecyclerView mRecyclerView;
    private Adapter mAdapter;
    private boolean mCanClick;
    private Handler mHandler;
    private AlertDialog mTipsDialog;
    private LinearLayout mNoting;

    public static void startManager(Context context) {
        context.startActivity(new Intent(context, DownloadManagerActivity.class));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dm);
        mHandler = new Handler(Looper.getMainLooper());
        mCanClick = true;
        setToolBar();
        findView();
        setDownloadListener();
        if (savedInstanceState != null && savedInstanceState.getParcelableArrayList(SAVE_BEAN) != null) {
            mDMBeanList = savedInstanceState.getParcelableArrayList(SAVE_BEAN);
            mUriList = savedInstanceState.getStringArrayList(SAVE_URI);
        } else {
            List<DownloadEntity> downloadEntityList = Aria.download(this).getTaskList();
            if (downloadEntityList != null) {
                Collections.reverse(downloadEntityList);
                mDMBeanList = new ArrayList<>();
                mUriList = new ArrayList<>();
                for (DownloadEntity entity : downloadEntityList) {
                    mUriList.add(entity.getKey());
                    mDMBeanList.add(DMBean.buildDMBean(entity));
                }
            }
        }
        setData();
    }

    private void setToolBar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.download);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void setData() {
        if (mDMBeanList == null) {
            mNoting.setVisibility(View.VISIBLE);
            return;
        }
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setItemAnimator(null);
        mRecyclerView.setAdapter(mAdapter = new Adapter(this, mDMBeanList, this));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mDMBeanList != null && mUriList != null) {
            ArrayList<DMBean> dmBeans = new ArrayList<>(mDMBeanList);
            ArrayList<String> uris = new ArrayList<>(mUriList);
            outState.putParcelableArrayList(SAVE_BEAN, dmBeans);
            outState.putStringArrayList(SAVE_URI, uris);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mTipsDialog != null && mTipsDialog.isShowing()) {
            mTipsDialog.cancel();
        }
        Aria.download(this).unRegister();
    }

    private void setDownloadListener() {
        Aria.download(this).register();
    }

    private void findView() {
        mRecyclerView = findViewById(R.id.content_download);
        mNoting = findViewById(R.id.dm_nothing);
    }

    @Download.onWait
    public void onTaskWait(DownloadTask task) {
        String key = task.getDownloadEntity().getKey();
        Log.d(TAG, "onTaskWait, Uri : " + decodeUri(key));
        if (mAdapter != null && mUriList != null) {
            if (mUriList.contains(key)) {
                int i = mUriList.indexOf(key);
                if (mDMBeanList.size() > i) {
                    DMBean dmBean = mDMBeanList.get(i);
                    dmBean.state = DMBean.STATE_WAIT;
                    mAdapter.notifyItemChanged(i);
                }
            }
        }
    }

    @Download.onTaskStart
    public void onTaskStart(DownloadTask task) {
        String key = task.getDownloadEntity().getKey();
        Log.d(TAG, "onTaskStart, Uri : " + decodeUri(key));
        if (mAdapter != null && mUriList != null) {
            if (mUriList.contains(key)) {
                int i = mUriList.indexOf(key);
                if (mDMBeanList.size() > i) {
                    DMBean dmBean = mDMBeanList.get(i);
                    dmBean.state = DMBean.STATE_PRE;
                    mAdapter.notifyItemChanged(i);
                }
            }
        }
    }

    @Download.onTaskResume
    public void onTaskResume(DownloadTask task) {
        String key = task.getDownloadEntity().getKey();
        Log.d(TAG, "onTaskResume, Uri : " + decodeUri(key));
        if (mAdapter != null && mUriList != null) {
            if (mUriList.contains(key)) {
                int i = mUriList.indexOf(key);
                if (mDMBeanList.size() > i) {
                    DMBean dmBean = mDMBeanList.get(i);
                    dmBean.state = DMBean.STATE_PRE;
                    mAdapter.notifyItemChanged(i);
                }
            }
        }
    }

    @Download.onTaskStop
    public void onTaskStop(DownloadTask task) {
        String key = task.getDownloadEntity().getKey();
        Log.d(TAG, "onTaskStop, Uri : " + decodeUri(key));
        if (mAdapter != null && mUriList != null) {
            if (mUriList.contains(key)) {
                int i = mUriList.indexOf(key);
                if (mDMBeanList.size() > i) {
                    DMBean dmBean = mDMBeanList.get(i);
                    dmBean.state = DMBean.STATE_STOP;
                    mAdapter.notifyItemChanged(i);
                }
            }
        }
    }

    @Download.onTaskCancel
    public void onTaskCancel(DownloadTask task) {
        String key = task.getDownloadEntity().getKey();
        Log.d(TAG, "onTaskCancel, Uri : " + decodeUri(key));
        if (mAdapter != null && mUriList != null) {
            if (mUriList.contains(key)) {
                int i = mUriList.indexOf(key);
                if (mDMBeanList.size() > i) {
                    mUriList.remove(i);
                    mDMBeanList.remove(i);
                    mAdapter.notifyDataSetChanged();
                }
            }
            mNoting.setVisibility(mDMBeanList.size() == 0 ? View.VISIBLE : View.GONE);
        }
    }

    @Download.onTaskFail
    public void onTaskFail(DownloadTask task, Exception e) {
        String key = task.getDownloadEntity().getKey();
        Log.d(TAG, "onTaskFail, Uri : " + decodeUri(key) + ", e " + e.getMessage());
        if (mAdapter != null && mUriList != null) {
            if (mUriList.contains(key)) {
                int i = mUriList.indexOf(key);
                if (mDMBeanList.size() > i) {
                    DMBean dmBean = mDMBeanList.get(i);
                    dmBean.state = DMBean.STATE_FAIL;
                    mAdapter.notifyItemChanged(i);
                }
            }
        }
    }

    @Download.onTaskComplete
    public void onTaskComplete(DownloadTask task) {
        String key = task.getDownloadEntity().getKey();
        Log.d(TAG, "onTaskComplete, Uri : " + decodeUri(key));
        Log.d(TAG, "onTaskComplete, path : " + task.getDownloadPath());
        if (mAdapter != null && mUriList != null) {
            if (mUriList.contains(key)) {
                int i = mUriList.indexOf(key);
                if (mDMBeanList.size() > i) {
                    DMBean dmBean = mDMBeanList.get(i);
                    dmBean.state = DMBean.STATE_COMPLETE;
                    mAdapter.notifyItemChanged(i);
                }
            }
        }
//        Aria.download(this).load(key).removeRecord();
    }

    @Download.onTaskRunning
    public void onTaskRunning(DownloadTask task) {
        String key = task.getDownloadEntity().getKey();
        long len = task.getFileSize();
        int p = 0;
        if (len != 0) {
            p = (int) (task.getCurrentProgress() * 100 / len);
        }
        if (mAdapter != null && mUriList != null) {
            if (mUriList.contains(key)) {
                int i = mUriList.indexOf(key);
                if (mDMBeanList.size() > i) {
                    DMBean dmBean = mDMBeanList.get(i);
                    dmBean.progress = p;
                    dmBean.state = DMBean.STATE_RUNNING;
                    mAdapter.notifyItemChanged(i);
                }
            }
        }
    }

    private Uri decodeUri(String key) {
        Uri uri = null;
        try {
            key = URLDecoder.decode(key, "UTF-8");
            uri = Uri.parse(key);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return uri;
    }

    @Override
    public void onButtonClickListener(int position) {
        if (mCanClick) {
            if (mDMBeanList.size() > position) {
                DMBean dmBean = mDMBeanList.get(position);
                switch (dmBean.state) {
                    case DMBean.STATE_FAIL:
                        Transmission.getInstance().downloadFile(this, Uri.parse(dmBean.url));
                        break;
                    case DMBean.STATE_CANCEL:
                        break;
                    case DMBean.STATE_COMPLETE:
                        openFile(dmBean);
                        break;
                    case DMBean.STATE_OTHER:
                        break;
                    case DMBean.STATE_POST_PRE:
                        break;
                    case DMBean.STATE_PRE:
                        break;
                    case DMBean.STATE_RUNNING:
                        Transmission.getInstance().stopDownloadFile(this, Uri.parse(dmBean.url));
                        break;
                    case DMBean.STATE_STOP:
                        Transmission.getInstance().downloadFile(this, Uri.parse(dmBean.url));
                        break;
                    case DMBean.STATE_WAIT:
                        break;
                }
            }
            mCanClick = false;
            initCanClick();
        }
    }

    private void openFile(DMBean dmBean) {
        File file = new File(dmBean.filePath);
        if (!file.exists() || !file.isFile()) {
            return;
        }
        if (dmBean.type == FileType.UN_KNOW) {
            Toast.makeText(this, getString(R.string.no_support_class), Toast.LENGTH_SHORT).show();
            return;
        }
        Intent media = new Intent(ACTION_VIEW);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            media.setFlags(FLAG_GRANT_READ_URI_PERMISSION);
            media.setDataAndType(FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file),
                    Utils.getMimeType(file.getAbsolutePath()));
        } else {
            media.setDataAndType(Uri.fromFile(file), Utils.getMimeType(file.getAbsolutePath()));
            media.setFlags(FLAG_ACTIVITY_NEW_TASK);
        }
        try {
            startActivity(media);
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.no_support_class), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onButtonLongClickListener(int position) {
        if (mDMBeanList.size() > position) {
            DMBean dmBean = mDMBeanList.get(position);
            if (dmBean != null) {
//                showItemTips(dmBean, dmBean.state == DMBean.STATE_COMPLETE);
                showItemTips(dmBean, false);
            }
        }
    }

    private void showItemTips(final DMBean dmBean, boolean dir) {
        String[] select = new String[]{getString(R.string.delete)};
        if (dir) {
            select = new String[]{getString(R.string.delete), getString(R.string.open_directory)};
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setItems(select, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    Aria.download(DownloadManagerActivity.this).load(dmBean.url).cancel(true);
                }
                if (which == 1) {
                    openAssignFolder(dmBean.filePath);
                }
            }
        });
        builder.setTitle(dmBean.title);
        mTipsDialog = builder.create();
        mTipsDialog.show();
    }

    private void openAssignFolder(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setDataAndType(Uri.parse(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/"), "*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivity(Intent.createChooser(intent, "Open folder"));
    }

    private void initCanClick() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mCanClick = true;
            }
        }, 1000);
    }


    private static class Adapter extends RecyclerView.Adapter<ViewHolder> {

        private List<DMBean> entityList;
        private LayoutInflater inflater;
        private ButtonClickListener clickListener;
        private Context context;
        private RequestOptions imgOptions = new RequestOptions().placeholder(R.drawable.ic_dm_image_ed).error(R.drawable.ic_dm_image_ed);
        private RequestOptions videoOptions = new RequestOptions().placeholder(R.drawable.ic_dm_video_ed).error(R.drawable.ic_dm_video_ed);
        //private RequestOptions contactOptions = new RequestOptions().placeholder(R.drawable.ic_contact).error(R.drawable.ic_contact);
        private RequestOptions audioOptions = new RequestOptions().placeholder(R.drawable.ic_audio).error(R.drawable.ic_audio);
        private RequestOptions docOptions = new RequestOptions().placeholder(R.drawable.ic_dm_doc_ed).error(R.drawable.ic_dm_doc_ed);
        private RequestOptions apkOptions = new RequestOptions().placeholder(R.drawable.ic_apk).error(R.drawable.ic_apk);

        private Adapter(Context context, List<DMBean> list, ButtonClickListener listener) {
            inflater = LayoutInflater.from(context);
            this.context = context;
            entityList = list;
            clickListener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            return new ViewHolder(inflater.inflate(R.layout.item_download_manager, viewGroup, false));
        }

        @Override
        public void onBindViewHolder(final @NonNull ViewHolder viewHolder, int i) {
            DMBean entity = entityList.get(i);
            if (entity != null) {
                viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        // TODO: 2019/3/12 取消，重新下载
                        return true;
                    }
                });
                viewHolder.title.setText(entity.title);
                viewHolder.desc.setText(entity.filePath);
                switch (entity.type) {
                    case FileType.APK:
                        if (entity.state == DMBean.STATE_COMPLETE) {
                            Glide.with(context)
                                    .load(Utils.convertIconThumb(context.getApplicationContext(), entity.filePath))
                                    .apply(apkOptions)
                                    .into(viewHolder.icon);
                            Log.d(TAG, "apk path " + entity.filePath);
                        } else {
                            Glide.with(context)
                                    .load(R.drawable.ic_dm_android)
                                    .into(viewHolder.icon);
                        }
                        break;
                    case FileType.AUDIO:
                        if (entity.state == DMBean.STATE_COMPLETE) {
                            Glide.with(context)
                                    .load(R.drawable.ic_dm_audio_ed)
                                    .apply(audioOptions)
                                    .into(viewHolder.icon);
                        } else {
                            Glide.with(context)
                                    .load(R.drawable.ic_dm_audio)
                                    .into(viewHolder.icon);
                        }
                        break;
                    case FileType.CALL_LOG:
                        break;
                    case FileType.CONTACT:
                        break;
                    case FileType.DOC:
                        if (entity.state == DMBean.STATE_COMPLETE) {
                            Glide.with(context)
                                    .load(R.drawable.ic_dm_doc_ed)
                                    .apply(docOptions)
                                    .into(viewHolder.icon);
                        } else {
                            Glide.with(context)
                                    .load(R.drawable.ic_dm_doc)
                                    .into(viewHolder.icon);
                        }
                        break;
                    case FileType.IMG:
                        if (entity.state == DMBean.STATE_COMPLETE) {
                            Glide.with(context)
                                    .load(entity.filePath)
                                    .apply(imgOptions)
                                    .into(viewHolder.icon);
                        } else {
                            Glide.with(context)
                                    .load(R.drawable.ic_dm_image)
                                    .into(viewHolder.icon);
                        }
                        break;
                    case FileType.MMS:
                        break;
                    case FileType.OTHER:
                        if (entity.state == DMBean.STATE_COMPLETE) {
                            Glide.with(context)
                                    .load(R.drawable.ic_dm_other_ed)
                                    .into(viewHolder.icon);
                        } else {
                            Glide.with(context)
                                    .load(R.drawable.ic_dm_other)
                                    .into(viewHolder.icon);
                        }
                        break;
                    case FileType.SMS:
                        break;
                    case FileType.UN_KNOW:
                        if (entity.state == DMBean.STATE_COMPLETE) {
                            Glide.with(context)
                                    .load(R.drawable.ic_dm_unknow_ed)
                                    .into(viewHolder.icon);
                        } else {
                            Glide.with(context)
                                    .load(R.drawable.ic_dm_unknow)
                                    .into(viewHolder.icon);
                        }
                        break;
                    case FileType.VIDEO:
                        if (entity.state == DMBean.STATE_COMPLETE) {
                            Glide.with(context)
                                    .load(entity.filePath)
                                    .apply(videoOptions)
                                    .into(viewHolder.icon);
                        } else {
                            Glide.with(context)
                                    .load(R.drawable.ic_dm_video)
                                    .into(viewHolder.icon);
                        }
                        break;
                }
                switch (entity.state) {
                    case DMBean.STATE_FAIL:
                        viewHolder.tips.setVisibility(View.GONE);

                        viewHolder.progress.setVisibility(View.VISIBLE);
                        viewHolder.progress.setProgress(entity.progress);

                        viewHolder.button.setText(R.string.retry);
                        viewHolder.flButton.setVisibility(View.VISIBLE);
                        break;
                    case DMBean.STATE_STOP:
                        viewHolder.tips.setVisibility(View.GONE);

                        viewHolder.progress.setVisibility(View.VISIBLE);
                        viewHolder.progress.setProgress(entity.progress);

                        viewHolder.button.setText(R.string.continue_down);
                        viewHolder.flButton.setVisibility(View.VISIBLE);
                        break;
                    case DMBean.STATE_COMPLETE:
                        viewHolder.tips.setVisibility(View.GONE);

                        viewHolder.progress.setVisibility(View.GONE);

                        viewHolder.button.setText(R.string.open);
                        viewHolder.flButton.setVisibility(View.VISIBLE);
                        break;
                    case DMBean.STATE_RUNNING:
                        viewHolder.tips.setVisibility(View.GONE);

                        viewHolder.progress.setProgress(entity.progress);
                        viewHolder.progress.setVisibility(View.VISIBLE);

                        viewHolder.button.setText(R.string.pause);
                        viewHolder.flButton.setVisibility(View.VISIBLE);
                        break;
                    case DMBean.STATE_PRE:
                        viewHolder.tips.setText(R.string.preparing);
                        viewHolder.tips.setVisibility(View.VISIBLE);

                        viewHolder.progress.setProgress(entity.progress);
                        viewHolder.progress.setVisibility(View.VISIBLE);

                        viewHolder.flButton.setVisibility(View.GONE);
                        break;
                    case DMBean.STATE_WAIT:
                        viewHolder.tips.setText(R.string.waiting);
                        viewHolder.tips.setVisibility(View.VISIBLE);

                        viewHolder.progress.setProgress(entity.progress);
                        viewHolder.progress.setVisibility(View.VISIBLE);

                        viewHolder.flButton.setVisibility(View.GONE);
                        break;
                    case DMBean.STATE_CANCEL:
                        viewHolder.tips.setVisibility(View.GONE);
                        viewHolder.progress.setVisibility(View.GONE);
                        viewHolder.flButton.setVisibility(View.GONE);
                        break;
                }
                viewHolder.button.setTag(i);
                viewHolder.button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (clickListener != null)
                            clickListener.onButtonClickListener((Integer) v.getTag());
                    }
                });
            }
            viewHolder.progress.setShowText(false);
            viewHolder.itemView.setTag(i);
            viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (clickListener != null)
                        clickListener.onButtonLongClickListener((Integer) v.getTag());
                    return false;
                }
            });
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                viewHolder.itemView.setBackgroundResource(R.drawable.bg_item_select);
            }
        }

        @Override
        public int getItemCount() {
            return entityList == null ? 0 : entityList.size();
        }
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView title;
        private TextView desc;
        private FrameLayout flButton;
        private Button button;
        private ImageView icon;
        private TextView tips;
        private DonutProgress progress;

        private ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            desc = itemView.findViewById(R.id.desc);
            flButton = itemView.findViewById(R.id.fl_button);
            button = itemView.findViewById(R.id.button);
            progress = itemView.findViewById(R.id.icon_loading);
            icon = itemView.findViewById(R.id.icon);
            tips = itemView.findViewById(R.id.tv_tips);
        }
    }

}
