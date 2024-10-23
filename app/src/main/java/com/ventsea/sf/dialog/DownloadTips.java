package com.ventsea.sf.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.st.letter.lib.bean.TransFolder;
import com.ventsea.sf.R;

public class DownloadTips extends DialogFragment {

    private static final String FN = "fn";
    private TransFolder.NFile mFile;
    private ClickListener mListener;

    public static void showTips(Activity activity, TransFolder.NFile file, ClickListener listener) {
        if (file == null) return;
        DownloadTips dialog = new DownloadTips();
        dialog.setClickListener(listener);
        Bundle bundle = new Bundle();
        bundle.putParcelable(FN, file);
        dialog.setArguments(bundle);
        dialog.show(activity.getFragmentManager(), "DownloadTips");
    }

    public void setClickListener(ClickListener listener) {
        mListener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            Bundle arguments = getArguments();
            mFile = arguments.getParcelable(FN);
        } else {
            dismiss();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_download_tips, container, false);
        if (mFile == null) {
            dismiss();
            return view;
        }
        TextView content = view.findViewById(R.id.dialog_content);
        content.setText(getString(R.string.ask_down_file, mFile.getName()));
        Button cancel = view.findViewById(R.id.dialog_cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onCancelClick();
                }
                dismiss();
            }
        });
        Button positive = view.findViewById(R.id.dialog_positive);
        positive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onPositiveClick(mFile);
                }
                dismiss();
            }
        });
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        setCenter();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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
                window.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT)); //需要圆角，所以背景设置透明
                window.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);
            }
        }
    }

    public interface ClickListener {
        void onCancelClick();

        void onPositiveClick(TransFolder.NFile file);
    }
}
