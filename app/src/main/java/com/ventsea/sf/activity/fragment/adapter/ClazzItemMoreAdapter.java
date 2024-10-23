package com.ventsea.sf.activity.fragment.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ventsea.sf.R;

import java.util.List;

public class ClazzItemMoreAdapter extends RecyclerView.Adapter<ClazzItemMoreHolder> {

    private LayoutInflater mInflater;
    private List<Integer> mActions;
    private ClazzActionClickListener mListener;
    private Object o;

    public ClazzItemMoreAdapter(Context context, Object o, List<Integer> list,  ClazzActionClickListener listener) {
        mInflater = LayoutInflater.from(context);
        mActions = list;
        mListener = listener;
        this.o = o;
    }

    @NonNull
    @Override
    public ClazzItemMoreHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ClazzItemMoreHolder(mInflater.inflate(R.layout.item_popup_action, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ClazzItemMoreHolder clazzItemMoreHolder, int i) {
        final int action = mActions.get(i);
        switch (action) {
            case 0:
                clazzItemMoreHolder.title.setText(R.string.download);
                break;
            case 1:
                clazzItemMoreHolder.title.setText(R.string.file_info);
                break;
            case 2:
                clazzItemMoreHolder.title.setText(R.string.insert);
                break;
            case 3:
                clazzItemMoreHolder.title.setText(R.string.view);
                break;
            default:
                break;
        }
        clazzItemMoreHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) mListener.onActionClick(o, action);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mActions == null ? 0 : mActions.size();
    }
}
