package com.ventsea.sf.activity.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ventsea.sf.R;
import com.ventsea.sf.activity.fragment.adapter.ClazzDetailViewAdapter;
import com.ventsea.sf.app.NFSApplication;
import com.ventsea.sf.util.Utils;

import java.util.List;

public class FragmentClazzDetailPage extends Fragment {

    private static final String KEY_TYPE = "type";
    private static final String KEY_POSITION = "position";
    private static final String KEY_LIST = "page_list";
    private List<Object> mDetailList;
    private SwipeRefreshLayout mSwipe;
    private RecyclerView mRecycler;
    private int mPosition;
    private int mType;

    public static FragmentClazzDetailPage getInstance(int type, List<Object> lists, int i) {
        FragmentClazzDetailPage page = new FragmentClazzDetailPage();
        page.setData(lists);
        Bundle bundle = new Bundle();
        bundle.putInt(KEY_TYPE, type);
        bundle.putInt(KEY_POSITION, i);
        page.setArguments(bundle);
        return page;
    }

    public void setData(List<Object> lists) {
        mDetailList = lists;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments != null) {
            mPosition = arguments.getInt(KEY_POSITION);
            mType = arguments.getInt(KEY_TYPE);
        }
        if (savedInstanceState != null) {
            if (1 == savedInstanceState.getInt(KEY_LIST, 0)) {
                mDetailList = Utils.getListData(NFSApplication.sContext, String.valueOf(mType), String.valueOf(mPosition));
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_vp_img, container, false);
        findView(view);
        setContent();
        return view;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_LIST, 1);
        Utils.saveListData(NFSApplication.sContext, mDetailList, String.valueOf(mType), String.valueOf(mPosition));
    }

    private void findView(View view) {
        mSwipe = view.findViewById(R.id.srl_img);
        mRecycler = view.findViewById(R.id.rv_img);
    }

    private void setContent() {
        if (mDetailList == null) return;
        mRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecycler.setAdapter(new ClazzDetailViewAdapter(getContext(), mDetailList));
        mSwipe.setEnabled(false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

}
