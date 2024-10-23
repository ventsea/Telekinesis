package com.ventsea.sf.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ventsea.sf.R;
import com.ventsea.sf.activity.base.BaseActivity;
import com.ventsea.sf.app.NFSApplication;
import com.ventsea.sf.util.Utils;

public class AppInfoActivity extends BaseActivity {

    private static final String ACTION_HELP = "help";
    private static final String ACTION_WARNING = "warning";
    private static final String ACTION_ABOUT = "about";

    private RecyclerView mAboutView;
    private TextView mQaView;
    private RelativeLayout mWarningView;

    public static void startHelp(Context context) {
        startForAction(context, ACTION_HELP);
    }

    public static void startWarning(Context context) {
        startForAction(context, ACTION_WARNING);
    }

    public static void startAbout(Context context) {
        startForAction(context, ACTION_ABOUT);
    }

    private static void startForAction(Context context, String action) {
        Intent i = new Intent(context, AppInfoActivity.class);
        i.putExtra("action", action);
        context.startActivity(i);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appinfo);
        findView();
        Intent intent = getIntent();
        String action = intent.getStringExtra("action");
        if (action != null) {
            if (action.equals(ACTION_HELP)) {
                showHelp();
                setToolBar(getString(R.string.help));
            }
            if (action.equals(ACTION_WARNING)) {
                showWarning();
                setToolBar(getString(R.string.warning));
            }
            if (action.equals(ACTION_ABOUT)) {
                showAbout();
                setToolBar(getString(R.string.info));
            }
        }
    }

    private void findView() {
        mAboutView = findViewById(R.id.about_content);
        mAboutView.setVisibility(View.GONE);
        mQaView = findViewById(R.id.qa_content);
        mQaView.setVisibility(View.GONE);
        mWarningView = findViewById(R.id.warning_content);
        mWarningView.setVisibility(View.GONE);
    }

    private void setToolBar(String title) {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(title);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    /**
     * 使用问题
     */
    private void showHelp() {
        mQaView.setText(Html.fromHtml(getResources().getString(R.string.q_n_a)));
        mQaView.setVisibility(View.VISIBLE);
    }

    /**
     * 隐私注意事项
     */
    private void showWarning() {
        mWarningView.setVisibility(View.VISIBLE);
    }

    /**
     * 版本，开放源代码许可，隐私权政策，服务条款
     */
    private void showAbout() {
        mAboutView.setVisibility(View.VISIBLE);
        mAboutView.setLayoutManager(new LinearLayoutManager(this));
        mAboutView.setAdapter(new AboutAdapter(this, new ClickListener() {
            @Override
            public void onClick(int position) {
                switch (position) {
                    case 0:
                        break;
                    case 1:
                        OSLActivity.start(AppInfoActivity.this);
                        break;
                    case 2:
                        break;
                    default:
                    case 3:
                        break;
                }
            }
        }));
    }

    private static class AboutAdapter extends RecyclerView.Adapter {

        private LayoutInflater inflater;
        private ClickListener clickListener;

        private AboutAdapter(Context context, ClickListener listener) {
            inflater = LayoutInflater.from(context);
            clickListener = listener;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            return new AboutHolder(inflater.inflate(R.layout.item_about_info, viewGroup, false));
        }

        @Override
        public void onBindViewHolder(final @NonNull RecyclerView.ViewHolder viewHolder, int i) {
            if (viewHolder instanceof AboutHolder) {
                AboutHolder holder = (AboutHolder) viewHolder;
                switch (i) {
                    case 0:
                        holder.title.setText(R.string.version);
                        holder.desc.setText(Utils.getVersionName(NFSApplication.sContext));
                        break;
                    case 1:
                        holder.title.setText(R.string.osl);
                        holder.desc.setText(R.string.osl_details);
                        break;
                    case 2:
                        holder.title.setText(R.string.privacy_policy);
                        holder.desc.setVisibility(View.GONE);
                        break;
                    case 3:
                        holder.title.setText(R.string.terms_of_service);
                        holder.desc.setVisibility(View.GONE);
                        break;
                }
            }
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (clickListener != null)
                        clickListener.onClick(viewHolder.getAdapterPosition());
                }
            });
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }

    private static class AboutHolder extends RecyclerView.ViewHolder {

        private TextView title;
        private TextView desc;

        private AboutHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.about_title);
            desc = itemView.findViewById(R.id.about_desc);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private interface ClickListener {
        void onClick(int position);
    }
}
