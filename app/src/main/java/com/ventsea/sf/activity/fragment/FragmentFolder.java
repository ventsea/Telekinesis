package com.ventsea.sf.activity.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.st.letter.lib.bean.FrameMessage;
import com.st.letter.lib.bean.TransFolder;
import com.ventsea.sf.R;
import com.ventsea.sf.activity.fragment.adapter.FolderAdapter;
import com.ventsea.sf.activity.fragment.adapter.FolderClickListener;
import com.ventsea.sf.service.ClientStatusListener;
import com.ventsea.sf.service.Transmission;

public class FragmentFolder extends Fragment implements ClientStatusListener {

    private static final String TAG = "FragmentFolder";
    private static final String URL = "URL";
    private static final String KEY_STATE = "STATE";
    private String mUrl;
    private RecyclerView mFolderRecycler;
    private ProgressBar mProgress;
    private Handler mHandler;
    private int mState;
    private TransFolder mTransFolder;
    private Runnable mShowView;
    private EventListener mListener;
    private LinearLayout mNotThing;

    public static FragmentFolder newInstance(String url) {
        Bundle bundle = new Bundle();
        bundle.putString(URL, url);
        FragmentFolder folder = new FragmentFolder();
        folder.setArguments(bundle);
        return folder;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mUrl = bundle.getString(URL);
        }
        if (savedInstanceState != null) {
            mState = savedInstanceState.getInt(KEY_STATE, 0);
        }
        mHandler = new Handler(Looper.getMainLooper());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_folder, container, false);
        Log.d(TAG, "createView");
        mProgress = view.findViewById(R.id.folder_progress);
        mProgress.setVisibility(View.VISIBLE);
        mFolderRecycler = view.findViewById(R.id.folder_recycler);
        mNotThing = view.findViewById(R.id.folder_nothing);
        getReceiverActivity();
        Transmission.getInstance().addClientListener(this);
        Transmission.getInstance().requestPage(mUrl);
        if (mState == 1) {
            Log.d(TAG, "reload " + mState);
        }
        return view;
    }

    private void getReceiverActivity() {
        Activity activity = getActivity();
        if (activity instanceof EventListener) {
            mListener = (EventListener) activity;
            mListener.onShowTitle(mUrl);
        }
    }

    @Override
    public void onDestroyView() {
        Transmission.getInstance().removeClientListener(this);
        if (mShowView != null) mHandler.removeCallbacks(mShowView);
        mListener = null;
        super.onDestroyView();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "fragment save state");
        outState.putInt(KEY_STATE, 1);
    }

    private Runnable getShowListRunnable() {
        if (mShowView == null) mShowView = new Runnable() {
            @Override
            public void run() {
                mProgress.setVisibility(View.GONE);
                if (mTransFolder != null && mTransFolder.nFiles != null && mTransFolder.nFiles.size() <= 0) {
                    mNotThing.setVisibility(View.VISIBLE);
                    return;
                }
                mFolderRecycler.setVisibility(View.VISIBLE);
                mFolderRecycler.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
                mFolderRecycler.setAdapter(new FolderAdapter(getActivity(), mTransFolder, new FolderClickListener() {
                    @Override
                    public void onFolderClick(String dir) {
                        if (mListener != null) mListener.onLoadNextPage(dir);
                    }

                    @Override
                    public void onFileClick(TransFolder.NFile file) {
                        if (mListener != null) mListener.onOpenFile(file);
                    }
                }, FragmentFolder.this));
            }
        };
        return mShowView;
    }

    @Override
    public void onConnectServer() {

    }

    @Override
    public void onDisConnectServer() {

    }

    @Override
    public void onConnectError() {

    }

    @Override
    public void onClientReceiverMessage(FrameMessage message) {
        if (message.getMessageType() == FrameMessage.MSG_TYPE_RES_FOLDER) {
            mTransFolder = message.folder;
            mHandler.post(getShowListRunnable());
        }
    }
}
