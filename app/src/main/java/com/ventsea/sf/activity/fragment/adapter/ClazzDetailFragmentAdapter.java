package com.ventsea.sf.activity.fragment.adapter;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.List;

public class ClazzDetailFragmentAdapter extends FragmentStatePagerAdapter {

    private List<String> titleList;
    private List<Fragment> fragmentList;

    public ClazzDetailFragmentAdapter(List<Fragment> fragments, List<String> titles, FragmentManager fm) {
        super(fm);
        fragmentList = fragments;
        titleList = titles;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return titleList.get(position);
    }

    @Override
    public Fragment getItem(int i) {
        return fragmentList.get(i);
    }

    @Override
    public int getCount() {
        return titleList.size();
    }
}
