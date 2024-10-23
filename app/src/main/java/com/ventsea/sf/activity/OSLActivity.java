package com.ventsea.sf.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ventsea.sf.R;
import com.ventsea.sf.activity.base.BaseActivity;

public class OSLActivity extends BaseActivity {

    public static void start(Context context) {
        context.startActivity(new Intent(context, OSLActivity.class));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_osl);
        setToolBar();
        RecyclerView recyclerview = findViewById(R.id.osl_content);
        recyclerview.setLayoutManager(new LinearLayoutManager(this));
        recyclerview.setAdapter(new Adapter(this));
    }

    private void setToolBar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.osl);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private static class Adapter extends RecyclerView.Adapter<Holder> {

        private LayoutInflater inflater;

        private Adapter(Context context) {
            inflater = LayoutInflater.from(context);
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            return new Holder(inflater.inflate(R.layout.item_osl_info, viewGroup, false));
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int i) {
            String title;
            switch (i) {
                case 0:
                    title = "Gson";
                    break;
                case 1:
                    title = "Netty";
                    break;
                case 2:
                    title = "Aria";
                    break;
                case 3:
                    title = "OkHttp";
                    break;
                case 4:
                    title = "Glide";
                    break;
                case 5:
                    title = "PhotoView";
                    break;
                case 6:
                    title = "ExoPlayer";
                    break;
                default:
                    title = "";
                    break;
            }
            holder.title.setText(title);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        }

        @Override
        public int getItemCount() {
            return 7;
        }
    }

    private static class Holder extends RecyclerView.ViewHolder {

        private TextView title;

        private Holder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.osl_title);
        }
    }
}
