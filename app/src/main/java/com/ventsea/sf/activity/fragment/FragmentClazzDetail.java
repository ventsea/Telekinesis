package com.ventsea.sf.activity.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.st.letter.lib.bean.FrameMessage;
import com.st.letter.lib.bean.TransHome;
import com.st.letter.lib.media.LocalApp;
import com.st.letter.lib.media.LocalAudio;
import com.st.letter.lib.media.LocalDocs;
import com.st.letter.lib.media.LocalImages;
import com.st.letter.lib.media.LocalVideo;
import com.ventsea.sf.R;
import com.ventsea.sf.activity.fragment.adapter.ClazzDetailFragmentAdapter;
import com.ventsea.sf.service.ClientStatusListener;
import com.ventsea.sf.service.Transmission;

import java.util.ArrayList;
import java.util.List;


public class FragmentClazzDetail extends Fragment implements ClientStatusListener {

    private static final String TAG = "ClazzDetail";
    private static final String KEY_TAB = "tabList";
    private static final String KEY_TYPE = "tabType";
    private static final String KEY_END = "end";
    private View mView;

    private int mType;
    private boolean mComplete;
    private List<String> mDetailTabList;
    private List<List<Object>> mDetailList;

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private LinearLayout nothingView;
    private ProgressBar loadingView;

    public static FragmentClazzDetail getInstance(int type) {
        Bundle bundle = new Bundle();
        bundle.putInt(KEY_TYPE, type);
        FragmentClazzDetail detail = new FragmentClazzDetail();
        detail.setArguments(bundle);
        return detail;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        FragmentActivity activity = getActivity();
        if (savedInstanceState != null) {
            List<String> cache = savedInstanceState.getStringArrayList(KEY_TAB);
            if (cache != null) {
                mDetailTabList = cache;
            }
            mType = savedInstanceState.getInt(KEY_TYPE);
            mComplete = savedInstanceState.getBoolean(KEY_END);
            if (activity != null) {
                FragmentManager fm = activity.getSupportFragmentManager();
                DataFragment dataFragment = (DataFragment) fm.findFragmentByTag("data");
                if (dataFragment != null) {
                    mDetailList = dataFragment.getData();
                }
            }
        } else {
            Bundle arguments = getArguments();
            if (arguments != null && activity != null) {
                mType = arguments.getInt(KEY_TYPE);
                mDetailTabList = new ArrayList<>();
                mDetailList = new ArrayList<>();
                FragmentManager fm = activity.getSupportFragmentManager();
                DataFragment dataFragment = new DataFragment();
                fm.beginTransaction().add(dataFragment, "data").commit();
                dataFragment.setData(mDetailList);
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Transmission.getInstance().addClientListener(this);
        if (mView == null) {
            mView = inflater.inflate(R.layout.fragment_remote_img, container, false);
            findView(mView);
            showLoading();
            if (savedInstanceState == null) {
                requestClazzDetail();
            } else {
                if (mComplete) {
                    showContent();
                }
            }
        }
        getReceiverActivity();
        return mView;
    }

    private void showLoading() {
        loadingView.setVisibility(View.VISIBLE);
        nothingView.setVisibility(View.GONE);
    }

    private void showContent() {
        loadingView.setVisibility(View.GONE);
        if (mDetailTabList.size() == 0) {
            nothingView.setVisibility(View.VISIBLE);
            return;
        }

        List<Fragment> fragmentCache = new ArrayList<>();
        List<String> titleCache = new ArrayList<>();
        for (int i = 0; i < mDetailTabList.size(); i++) {
            String title = mDetailTabList.get(i);
            if (title.contains("/")) {
                title = title.substring(title.lastIndexOf("/") + 1);
            }
            fragmentCache.add(FragmentClazzDetailPage.getInstance(mType, mDetailList.get(i), i));
            titleCache.add(title);
            tabLayout.addTab(tabLayout.newTab());
        }
        tabLayout.setupWithViewPager(viewPager, false);
        FragmentStatePagerAdapter adapter = new ClazzDetailFragmentAdapter(fragmentCache, titleCache, getFragmentManager());
        viewPager.setAdapter(adapter);
    }

    private void findView(View view) {
        tabLayout = view.findViewById(R.id.tab_layout);
        viewPager = view.findViewById(R.id.vp_layout);
        nothingView = view.findViewById(R.id.class_nothing);
        loadingView = view.findViewById(R.id.class_loading);
    }

    private void requestClazzDetail() {
        Transmission.getInstance().requestClass(mType);
    }

    private void getReceiverActivity() {
        Activity activity = getActivity();
        if (activity instanceof EventListener) {
            EventListener eventListener = (EventListener) activity;
            eventListener.onShowTitle(String.valueOf(mType));
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mDetailTabList.size() > 0) {
            outState.putInt(KEY_TYPE, mType);
            outState.putStringArrayList(KEY_TAB, new ArrayList<>(mDetailTabList));
            outState.putBoolean(KEY_END, mComplete);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView");
        Transmission.getInstance().removeClientListener(this);
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
        if (message.getMessageType() == FrameMessage.MSG_TYPE_RES_CLASS) {
            switch (mType) {
                case TransHome.TYPE_APP:
                    List<Object> cache = new ArrayList<>();
                    for (LocalApp.App app : message.clazz.apps) {
                        app.buildCorrectFileBean(Transmission.SERVER_IP, Transmission.SERVER_PORT);
                        cache.add(app);
                    }
                    if (!mDetailTabList.contains("Apps")) {
                        mDetailTabList.add("Apps");
                        mDetailList.add(cache);
                    } else {
                        mDetailList.get(0).addAll(cache); //若此处索引越界，则可能已发生数据丢失
                    }
                    break;
                case TransHome.TYPE_AUDIO:
                    for (LocalAudio.Audio audio : message.clazz.audios) {
                        setTitleTab(audio.data, audio);
                        audio.buildCorrectFileBean(Transmission.SERVER_IP, Transmission.SERVER_PORT);
                    }
                    break;
                case TransHome.TYPE_CONTACT:
                    List<Object> cache1 = new ArrayList<Object>(message.clazz.contacts);
                    if (!mDetailTabList.contains("Contacts")) {
                        mDetailTabList.add("Contacts");
                        mDetailList.add(cache1);
                    } else {
                        mDetailList.get(0).addAll(cache1); //若此处索引越界，则可能已发生数据丢失
                    }
                    break;
                case TransHome.TYPE_DOC:
                    for (LocalDocs.Doc doc : message.clazz.docs) {
                        setTitleTab(doc.data, doc);
                        doc.buildCorrectFileBean(Transmission.SERVER_IP, Transmission.SERVER_PORT);
                    }
                    break;
                case TransHome.TYPE_IMAGE:
                    for (LocalImages.Image image : message.clazz.images) {
                        setTitleTab(image.data, image);
                        image.buildCorrectFileBean(Transmission.SERVER_IP, Transmission.SERVER_PORT);
                    }
                    break;
                case TransHome.TYPE_VIDEO:
                    for (LocalVideo.Video video : message.clazz.videos) {
                        setTitleTab(video.data, video);
                        video.buildCorrectFileBean(Transmission.SERVER_IP, Transmission.SERVER_PORT);
                    }
                    break;
            }
            mComplete = true;
            showContent();
        }
    }

    private void setTitleTab(String titleTab, Object object) {
        String title = titleTab.substring(0, titleTab.lastIndexOf("/"));
        if (mDetailTabList.contains(title)) {
            mDetailList.get(mDetailTabList.indexOf(title)).add(object);
        } else {
            List<Object> cache = new ArrayList<>();
            cache.add(object);
            mDetailList.add(cache);
            mDetailTabList.add(title);
        }
    }
}
