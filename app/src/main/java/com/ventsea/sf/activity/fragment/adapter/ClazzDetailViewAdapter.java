package com.ventsea.sf.activity.fragment.adapter;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.request.RequestOptions;
import com.st.letter.lib.media.LocalApp;
import com.st.letter.lib.media.LocalAudio;
import com.st.letter.lib.media.LocalContacts;
import com.st.letter.lib.media.LocalDocs;
import com.st.letter.lib.media.LocalImages;
import com.st.letter.lib.media.LocalVideo;
import com.ventsea.sf.R;
import com.ventsea.sf.activity.fragment.EventListener;
import com.ventsea.sf.app.NFSApplication;
import com.ventsea.sf.util.Utils;

import java.util.ArrayList;
import java.util.List;

public class ClazzDetailViewAdapter extends RecyclerView.Adapter<ClazzDetailViewHolder> {

    private List<Object> mList;
    private LayoutInflater mInflater;
    private Context mContext;
    private RequestOptions imgOptions = new RequestOptions().priority(Priority.NORMAL).placeholder(R.drawable.ic_dm_image_ed).error(R.drawable.ic_dm_image_ed).override(64, 64);
    private RequestOptions videoOptions = new RequestOptions().priority(Priority.NORMAL).placeholder(R.drawable.ic_dm_video_ed).error(R.drawable.ic_dm_video_ed).override(64, 64);
    private RequestOptions contactOptions = new RequestOptions().placeholder(R.drawable.ic_contact).error(R.drawable.ic_contact);
    private RequestOptions audioOptions = new RequestOptions().placeholder(R.drawable.ic_audio).error(R.drawable.ic_audio);
    private RequestOptions docOptions = new RequestOptions().placeholder(R.drawable.ic_dm_doc_ed).error(R.drawable.ic_dm_doc_ed);

    public ClazzDetailViewAdapter(Context context, List<Object> list) {
        mInflater = LayoutInflater.from(context);
        mList = list;
        mContext = context;
    }

    @NonNull
    @Override
    public ClazzDetailViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ClazzDetailViewHolder(mInflater.inflate(R.layout.item_vp_fragment_img, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final ClazzDetailViewHolder holder, int i) {
        Object o = mList.get(i);
        holder.more.setTag(o);
        holder.more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getContext() instanceof EventListener) {
                    EventListener listener = (EventListener) v.getContext();
                    listener.onItemMoreClick(v, v.getTag());
                }
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        if (o instanceof LocalApp.App) {
            bindApp(holder, (LocalApp.App) o);
        } else if (o instanceof LocalAudio.Audio) {
            bindAudio(holder, (LocalAudio.Audio) o);
        } else if (o instanceof LocalVideo.Video) {
            bindVideo(holder, (LocalVideo.Video) o);
        } else if (o instanceof LocalDocs.Doc) {
            bindDoc(holder, (LocalDocs.Doc) o);
        } else if (o instanceof LocalImages.Image) {
            bindImage(holder, (LocalImages.Image) o);
        } else if (o instanceof LocalContacts.Contact) {
            bindContact(holder, (LocalContacts.Contact) o);
        } else {
            Glide.with(mContext).clear(holder.icon);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            holder.itemView.setBackgroundResource(R.drawable.bg_item_select);
        }
    }

    private void bindApp(ClazzDetailViewHolder holder, LocalApp.App app) {
        Glide.with(mContext)
                .load(app.iconUrl)
                .into(holder.icon);
        holder.title.setText(app.label);
        holder.sizeAndData.setText(NFSApplication.sContext.getString(R.string.size_date, Utils.readableFileSize(app.size), app.packageName));
    }

    private void bindAudio(ClazzDetailViewHolder holder, LocalAudio.Audio audio) {
        Glide.with(mContext)
                .load(NFSApplication.sContext.getResources().getDrawable(R.drawable.ic_audio))
                .apply(audioOptions)
                .into(holder.icon);
        holder.title.setText(audio.title);
        holder.sizeAndData.setText(NFSApplication.sContext.getString(R.string.size_date, Utils.readableFileSize(audio.size), Utils.readableDuration(audio.duration)));
    }

    private void bindVideo(ClazzDetailViewHolder holder, LocalVideo.Video video) {
        Glide.with(mContext)
                .load(video.iconUrl)
                .apply(videoOptions)
                .into(holder.icon);
        holder.title.setText(video.title);
        holder.sizeAndData.setText(NFSApplication.sContext.getString(R.string.size_date, Utils.readableFileSize(video.size), Utils.readableDuration(video.duration)));
        holder.itemView.setTag(video);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getContext() instanceof EventListener) {
                    EventListener listener = (EventListener) v.getContext();
                    LocalVideo.Video video1 = (LocalVideo.Video) v.getTag();
                    listener.onShowPlayerView(video1.data);
                }
            }
        });
    }

    private void bindDoc(ClazzDetailViewHolder holder, LocalDocs.Doc doc) {
        Glide.with(mContext)
                .load(NFSApplication.sContext.getResources().getDrawable(R.drawable.ic_dm_doc_ed))
                .apply(docOptions)
                .into(holder.icon);
        holder.title.setText(doc.title);
        holder.sizeAndData.setText(Utils.readableFileSize(doc.size));
    }

    private void bindImage(final ClazzDetailViewHolder holder, LocalImages.Image image) {
        Glide.with(mContext)
                .load(image.iconUrl)
                .apply(imgOptions)
                .into(holder.icon);
        holder.title.setText(image.title);
        holder.sizeAndData.setText(Utils.readableFileSize(image.size));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getContext() instanceof EventListener) {
                    EventListener listener = (EventListener) v.getContext();
                    List<String> list = new ArrayList<>();
                    for (Object o : mList) {
                        LocalImages.Image image1 = (LocalImages.Image) o;
                        list.add(image1.data);
                    }
                    if (list.size() > 0) {
                        listener.onShowPhotoPage(list, holder.getAdapterPosition());
                    }
                }
            }
        });
    }

    private void bindContact(ClazzDetailViewHolder holder, LocalContacts.Contact contact) {
        Glide.with(mContext)
                .load(NFSApplication.sContext.getResources().getDrawable(R.drawable.ic_contact))
                .apply(contactOptions)
                .into(holder.icon);
        holder.title.setText(contact.name);
        String phone;
        if (contact.phone != null && !contact.phone.isEmpty()) {
            phone = contact.phone.get(0);
            if (contact.phone.size() > 1) {
                phone = phone + ", ...";
            }
        } else {
            phone = "";
        }
        holder.sizeAndData.setText(phone);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }
}
