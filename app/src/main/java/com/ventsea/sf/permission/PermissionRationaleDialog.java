package com.ventsea.sf.permission;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ventsea.sf.R;

import java.util.ArrayList;
import java.util.List;

public class PermissionRationaleDialog extends DialogFragment {

    private static final String PL = "PL";
    private TextView mCancel, mAuthorize;
    private RecyclerView mRecycler;
    private List<String> mPermissionList;
    private PermissionClickListener mClickListener;

    public static void showRational(Activity activity, List<String> list, PermissionClickListener listener) {
        PermissionRationaleDialog dialog = new PermissionRationaleDialog();
        dialog.setClickListener(listener);
        Bundle bundle = new Bundle();
        bundle.putStringArrayList(PL, new ArrayList<>(list));
        dialog.setArguments(bundle);
        dialog.show(activity.getFragmentManager(), "RationaleTips");
    }

    private void setClickListener(PermissionClickListener listener) {
        mClickListener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            dismiss();
        } else {
            Bundle arguments = getArguments();
            mPermissionList = arguments.getStringArrayList(PL);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mPermissionList != null)
            outState.putStringArrayList(PL, new ArrayList<>(mPermissionList));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_permission, container, false);
        findView(view);
        if (mPermissionList == null) {
            dismiss();
        } else {
            setData();
        }
        return view;
    }

    private void setData() {
        mRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecycler.setAdapter(new Adapter(getActivity(), mPermissionList));
        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mClickListener.onRefuseClick();
                dismiss();
            }
        });
        mAuthorize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mClickListener.onNextClick();
                dismiss();
            }
        });
    }

    private void findView(View view) {
        mCancel = view.findViewById(R.id.permission_cancel);
        mAuthorize = view.findViewById(R.id.permission_authorize);
        mRecycler = view.findViewById(R.id.permission_list);
    }

    private static class Adapter extends RecyclerView.Adapter<ViewHolder> {

        private List<String> mList;
        private LayoutInflater mInflater;

        private Adapter(Context context, List<String> pList) {
            mInflater = LayoutInflater.from(context);
            mList = pList;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            return new ViewHolder(mInflater.inflate(R.layout.item_dialog_permission, viewGroup, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
            viewHolder.mTitle.setText(mList.get(i));
        }

        @Override
        public int getItemCount() {
            return mList == null ? 0 : mList.size();
        }
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView mTitle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mTitle = itemView.findViewById(R.id.p_name);
        }
    }
}
