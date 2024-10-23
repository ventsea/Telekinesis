package com.ventsea.sf.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.ventsea.sf.R;

import java.util.ArrayList;
import java.util.List;

public class DetailInfoDialog extends DialogFragment {

    private static final String KEY_MS = "ms";
    private List<String> mMessage;
    private RecyclerView mRecycler;
    private TextView mOk;

    public static void showTips(Activity activity, ArrayList<String> message) {
        if (message == null) return;
        DetailInfoDialog dialog = new DetailInfoDialog();
        Bundle bundle = new Bundle();
        bundle.putStringArrayList(KEY_MS, message);
        dialog.setArguments(bundle);
        dialog.show(activity.getFragmentManager(), "DetailInfoDialog");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments != null) {
            mMessage = arguments.getStringArrayList(KEY_MS);
        } else {
            dismiss();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_detail_info, container, false);
        if (mMessage == null) {
            dismiss();
            return view;
        }
        findView(view);
        setData();
        return view;
    }

    private void setData() {
        mRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecycler.setAdapter(new InfoAdapter(getActivity(), mMessage));
        mOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mOk.setBackgroundResource(R.drawable.bg_item_select);
        }
    }

    private void findView(View view) {
        mRecycler = view.findViewById(R.id.recycler_info);
        mOk = view.findViewById(R.id.ok);
    }

    private static class InfoAdapter extends RecyclerView.Adapter<InfoHolder> {

        private List<String> infoList;
        private LayoutInflater inflater;

        private InfoAdapter(Context context, List<String> strings) {
            inflater = LayoutInflater.from(context);
            infoList = strings;
        }

        @NonNull
        @Override
        public InfoHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            return new InfoHolder(inflater.inflate(R.layout.item_detail_info, viewGroup, false));
        }

        @Override
        public void onBindViewHolder(@NonNull InfoHolder infoHolder, int i) {
            String s = infoList.get(i);
            if (s != null) {
                infoHolder.info.setText(s);
            }
        }

        @Override
        public int getItemCount() {
            return infoList.size();
        }
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
                Display display = getActivity().getWindowManager().getDefaultDisplay();
                Point point = new Point();
                display.getSize(point);
                int width = (int) (point.x * 0.8);
                window.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);
            }
        }
    }

    private static class InfoHolder extends RecyclerView.ViewHolder {

        private TextView info;

        private InfoHolder(@NonNull View itemView) {
            super(itemView);
            info = itemView.findViewById(R.id.info);
        }
    }

}
