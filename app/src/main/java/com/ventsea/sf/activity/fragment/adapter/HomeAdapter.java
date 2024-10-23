package com.ventsea.sf.activity.fragment.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ventsea.sf.R;
import com.ventsea.sf.activity.fragment.uibean.Clazz;
import com.ventsea.sf.app.NFSApplication;
import com.ventsea.sf.util.Utils;

import java.util.List;

public class HomeAdapter extends RecyclerView.Adapter {

    private List<Clazz> clazzList;
    private LayoutInflater inflater;
    private Context context;
    private ClazzClickListener clazzClickListener;

    public HomeAdapter(Context context, List<Clazz> list, ClazzClickListener listener) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        clazzList = list;
        clazzClickListener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        if (i == Clazz.TYPE_TITLE) {
            return new HomeTitleHolder(inflater.inflate(R.layout.item_home_title, viewGroup, false));
        } else {
            return new HomeHolder(inflater.inflate(R.layout.item_home_clazz, viewGroup, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        Clazz clazz = clazzList.get(i);
        if (viewHolder instanceof HomeHolder) {
            HomeHolder holder = (HomeHolder) viewHolder;
            switch (i) {
                case 1:
                    holder.title.setText(R.string.app);
                    holder.icon.setImageResource(R.drawable.ic_home_app);
                    holder.detail.setVisibility(View.GONE);
                    holder.count.setVisibility(View.VISIBLE);
                    break;
                case 2:
                    holder.title.setText(R.string.contact);
                    holder.icon.setImageResource(R.drawable.ic_home_contact);
                    holder.detail.setVisibility(View.GONE);
                    holder.count.setVisibility(View.VISIBLE);
                    break;
                case 3:
                    holder.title.setText(R.string.image);
                    holder.icon.setImageResource(R.drawable.ic_home_img);
                    holder.detail.setVisibility(View.GONE);
                    holder.count.setVisibility(View.VISIBLE);
                    break;
                case 4:
                    holder.title.setText(R.string.audio);
                    holder.icon.setImageResource(R.drawable.ic_home_audio);
                    holder.detail.setVisibility(View.GONE);
                    holder.count.setVisibility(View.VISIBLE);
                    break;
                case 5:
                    holder.title.setText(R.string.video);
                    holder.icon.setImageResource(R.drawable.ic_home_video);
                    holder.detail.setVisibility(View.GONE);
                    holder.count.setVisibility(View.VISIBLE);
                    break;
                case 6:
                    holder.title.setText(R.string.doc);
                    holder.icon.setImageResource(R.drawable.ic_home_doc);
                    holder.detail.setVisibility(View.GONE);
                    holder.count.setVisibility(View.VISIBLE);
                    break;
                case 8:
                    holder.title.setText(R.string.internal_storage_device);
                    holder.icon.setImageResource(R.drawable.ic_home_phone);
                    holder.detail.setVisibility(View.VISIBLE);
                    holder.count.setVisibility(View.GONE);
                    clazz.loaded = true;
                    holder.detail.setText(NFSApplication.sContext.getString(R.string.available_space, Utils.readableFileSize(clazz.size)));
                    break;
            }
            if (clazz.loaded) {
                holder.count.setText(context.getString(R.string.clazz_count, clazz.count));
                holder.itemView.setTag(i);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int i = (int) v.getTag();
                        if (clazzClickListener != null) clazzClickListener.onClazzClick(i);
                    }
                });
            } else {
                holder.count.setText("...");
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });
            }
        }
        if (viewHolder instanceof HomeTitleHolder) {
            HomeTitleHolder holder = (HomeTitleHolder) viewHolder;
            if (i == 0) {
                holder.title.setText(R.string.clazz);
            } else {
                holder.title.setText(R.string.storage_device);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        return clazzList.get(position).clazzType;
    }

    @Override
    public int getItemCount() {
        return clazzList.size();
    }
}
