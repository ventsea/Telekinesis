package com.ventsea.sf.activity.fragment;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.st.letter.lib.bean.FrameMessage;
import com.st.letter.lib.bean.TransFolder;
import com.st.letter.lib.bean.TransHome;
import com.ventsea.sf.R;
import com.ventsea.sf.activity.fragment.adapter.ClazzClickListener;
import com.ventsea.sf.activity.fragment.adapter.HomeAdapter;
import com.ventsea.sf.activity.fragment.uibean.Clazz;
import com.ventsea.sf.service.ClientStatusListener;
import com.ventsea.sf.service.Transmission;

import java.util.ArrayList;
import java.util.List;

public class FragmentRemoteHome extends Fragment implements ClientStatusListener, ClazzClickListener {

    private static final String TAG = "RemoteHome";
    private static final String KEY_SAVE_CLAZZ = "save_clazz";
    private SwipeRefreshLayout mRefreshLayout;
    private RecyclerView mRecyclerView;
    private HomeAdapter mHomeAdapter;
    private List<Clazz> mClazzList;
    private EventListener eventListener;
    private Handler mHandler = new Handler();
    private View mView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            initClazzList();
        } else {
            List<Clazz> list = savedInstanceState.getParcelableArrayList(KEY_SAVE_CLAZZ);
            if (list != null) {
                mClazzList = list;
            } else {
                initClazzList();
            }
        }
    }

    private void initClazzList() {
        mClazzList = new ArrayList<>();
        mClazzList.add(Clazz.build(Clazz.TYPE_TITLE));
        mClazzList.add(Clazz.build(TransHome.TYPE_APP));
        mClazzList.add(Clazz.build(TransHome.TYPE_CONTACT));
        mClazzList.add(Clazz.build(TransHome.TYPE_IMAGE));
        mClazzList.add(Clazz.build(TransHome.TYPE_AUDIO));
        mClazzList.add(Clazz.build(TransHome.TYPE_VIDEO));
        mClazzList.add(Clazz.build(TransHome.TYPE_DOC));
        mClazzList.add(Clazz.build(Clazz.TYPE_TITLE));
        mClazzList.add(Clazz.build(Clazz.TYPE_STORAGE));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Transmission.getInstance().addClientListener(this);
        if (mView == null) {
            mView = inflater.inflate(R.layout.fragment_remote_home, container, false);
            findView(mView);
            setRecyclerView();
            if (savedInstanceState == null) {
                requestClazzContent();
            }
        }
        getReceiverActivity();
        return mView;
    }

    private void getReceiverActivity() {
        Activity activity = getActivity();
        if (activity instanceof EventListener) {
            eventListener = (EventListener) activity;
            eventListener.onShowTitle(TransHome.MSG_PATH_HOME);
        }
    }

    private void setRecyclerView() {
        if (mHomeAdapter == null) {
            mHomeAdapter = new HomeAdapter(getActivity(), mClazzList, this);
        }
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(mHomeAdapter);
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestClazzContent();
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mRefreshLayout.setRefreshing(false);
                    }
                }, 3000);
            }
        });
    }

    private void requestClazzContent() {
        Log.d(TAG, "requestClazzContent");
        Transmission.getInstance().requestHome(TransHome.TYPE_APP);
        Transmission.getInstance().requestHome(TransHome.TYPE_CONTACT);
        Transmission.getInstance().requestHome(TransHome.TYPE_IMAGE);
        Transmission.getInstance().requestHome(TransHome.TYPE_AUDIO);
        Transmission.getInstance().requestHome(TransHome.TYPE_VIDEO);
        Transmission.getInstance().requestHome(TransHome.TYPE_DOC);
    }

    private void findView(View view) {
        mRecyclerView = view.findViewById(R.id.rv_home);
        mRefreshLayout = view.findViewById(R.id.srl_home);
        mRefreshLayout.setColorSchemeColors(Color.RED, Color.BLUE, Color.MAGENTA);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(KEY_SAVE_CLAZZ, new ArrayList<Parcelable>(mClazzList));
    }

    @Override
    public void onDestroyView() { //replace 会直接调onDestroyView,但是不会调onSaveInstanceState
        super.onDestroyView();
        Transmission.getInstance().removeClientListener(this);
        mRefreshLayout.setRefreshing(false);
        mHandler.removeCallbacksAndMessages(null);
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
        if (message.getMessageType() == FrameMessage.MSG_TYPE_RES_HOME) {
            int position = 0;
            switch (message.home.getType()) {
                case TransHome.TYPE_APP:
                    position = 1;
                    break;
                case TransHome.TYPE_AUDIO:
                    position = 4;
                    break;
                case TransHome.TYPE_CONTACT:
                    position = 2;
                    break;
                case TransHome.TYPE_DOC:
                    position = 6;
                    break;
                case TransHome.TYPE_IMAGE:
                    position = 3;
                    break;
                case TransHome.TYPE_VIDEO:
                    position = 5;
                    break;
            }
            Clazz clazz = mClazzList.get(position);
            clazz.count = message.home.getSize();
            clazz.loaded = true;
            mClazzList.get(8).size = message.total_size;
            final int finalPosition = position;
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mHomeAdapter != null) {
                        mHomeAdapter.notifyItemChanged(finalPosition);
                        mHomeAdapter.notifyItemChanged(8);
                    }
                }
            });
        }
    }

    @Override
    public void onClazzClick(int position) {
        if (eventListener != null) {
            switch (position) {
                case 1:
                    eventListener.onOpenClazz(TransHome.TYPE_APP);
                    break;
                case 2:
                    eventListener.onOpenClazz(TransHome.TYPE_CONTACT);
                    break;
                case 3:
                    eventListener.onOpenClazz(TransHome.TYPE_IMAGE);
                    break;
                case 4:
                    eventListener.onOpenClazz(TransHome.TYPE_AUDIO);
                    break;
                case 5:
                    eventListener.onOpenClazz(TransHome.TYPE_VIDEO);
                    break;
                case 6:
                    eventListener.onOpenClazz(TransHome.TYPE_DOC);
                    break;
                case 8:
                    eventListener.onLoadNextPage(TransFolder.MSG_PATH_INDEX);
                    break;
                default:
                    break;
            }
        }
    }
}
