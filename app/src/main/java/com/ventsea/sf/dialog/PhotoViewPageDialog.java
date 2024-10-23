package com.ventsea.sf.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.request.RequestOptions;
import com.github.chrisbanes.photoview.PhotoView;
import com.ventsea.sf.R;
import com.ventsea.sf.view.HackyViewPager;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class PhotoViewPageDialog extends DialogFragment {

    private static final String KEY_URLS = "urls";
    private List<String> mUrls;
    private int initPosition;

    public static void showPhotoPage(AppCompatActivity context, List<String> urlList, int position) {
        PhotoViewPageDialog dialog = new PhotoViewPageDialog();
        dialog.initPosition = position;
        Bundle bundle = new Bundle();
        bundle.putStringArrayList(KEY_URLS, new ArrayList<>(urlList));
        dialog.setArguments(bundle);
        dialog.show(context.getSupportFragmentManager(), "PhotoViewPageDialog");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments != null)
            mUrls = arguments.getStringArrayList(KEY_URLS);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_photoview, container, false);
        HackyViewPager viewPager = view.findViewById(R.id.vp_photo);
        PhotoPageAdapter adapter = new PhotoPageAdapter(getActivity(), mUrls, this);
        viewPager.setAdapter(adapter);
        if (savedInstanceState == null) {
            setCurrentPage(viewPager, adapter);
        }
        return view;
    }

    private void setCurrentPage(ViewPager viewPager, PagerAdapter adapter) {
        try {
            Field field = viewPager.getClass().getField("mCurItem");
            field.setAccessible(true);
            field.setInt(viewPager, initPosition);
        } catch (Exception e) {
            e.printStackTrace();
        }
        adapter.notifyDataSetChanged();
        viewPager.setCurrentItem(initPosition);
    }

    @Override
    public void onStart() {
        super.onStart();
        setCenter();
    }

    private void setCenter() {
        Dialog dialog = getDialog();
        if (dialog != null) {
            Window window = dialog.getWindow();
            if (window != null) {
                window.setBackgroundDrawable(new ColorDrawable(Color.BLACK));
                window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
            }
        }
    }

    private static class PhotoPageAdapter extends PagerAdapter {

        private List<String> urls;
        private LayoutInflater inflater;
        private Fragment mContext;
        private RequestOptions imgOptions = new RequestOptions().priority(Priority.HIGH).placeholder(R.drawable.ic_dm_image_ed).override(800, 800);

        private PhotoPageAdapter(Context context, List<String> urlList, Fragment view) {
            urls = urlList;
            inflater = LayoutInflater.from(context);
            mContext = view;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            View view = inflater.inflate(R.layout.item_vp_photo, container, false);
            PhotoView photoView = view.findViewById(R.id.vp_photo);
            Glide.with(mContext).load(urls.get(position)).apply(imgOptions).into(photoView);
            container.addView(view);
            return view;
        }

        @Override
        public int getCount() {
            return urls == null ? 0 : urls.size();
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
            return view == o;
        }
    }
}
