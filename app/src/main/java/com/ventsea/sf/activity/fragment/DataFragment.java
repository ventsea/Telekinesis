package com.ventsea.sf.activity.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import java.util.List;

public class DataFragment extends Fragment {

    private List<List<Object>> mLists;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    public void setData(List<List<Object>> lists) {
        mLists = lists;
    }

    public List<List<Object>> getData() {
        return mLists;
    }
}
