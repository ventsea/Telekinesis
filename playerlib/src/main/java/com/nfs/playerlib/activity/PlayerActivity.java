package com.nfs.playerlib.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Log;
import com.google.android.exoplayer2.util.Util;
import com.nfs.playerlib.R;

/**
 * 通过注入实现播放器的组件的概念的的思想贯穿了整个Exo库
 */
public class PlayerActivity extends AppCompatActivity {

    private static final String TAG = "EXO_PLAY";
    private static final String KEY_URL = "url";
    private static final String KEY_PLAY = "play";

    //播放器实例
    private SimpleExoPlayer mPlayer;

    private PlayerView mPlayerView;
    private ProgressBar mLoadView;

    private String mUrl;
    private boolean mPlayWhenReady;
    private boolean mPause;
    private int currentWindow;
    private long playbackPosition;

    public static void startPlay(Context context, String url) {
        Intent intent = new Intent(context, PlayerActivity.class);
        intent.putExtra(KEY_URL, url);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mUrl = savedInstanceState.getString(KEY_URL);
            mPlayWhenReady = savedInstanceState.getBoolean(KEY_PLAY);
        } else {
            mPlayWhenReady = true;
            Intent intent = getIntent();
            mUrl = intent.getStringExtra(KEY_URL);
        }
        setContentView(R.layout.activity_player_view);
        findView();
        initPlayerView();
        playVideo();
    }

    private void initPlayerView() {
        //轨道选择器
        DefaultTrackSelector selector = new DefaultTrackSelector(new AdaptiveTrackSelection.Factory());
        //加载控制器,用于控制MediaSource何时缓冲更多的媒体资源以及缓冲多少媒体资源.在创建播放器的时候被注入.
        LoadControl control = new DefaultLoadControl();
        //渲染器,用于渲染媒体文件.当创建播放器的时候被注入.
        RenderersFactory renderersFactory = new DefaultRenderersFactory(this);
        mPlayer = ExoPlayerFactory.newSimpleInstance(this, renderersFactory, selector, control);
        mPlayer.setPlayWhenReady(mPlayWhenReady);
        mPlayer.addListener(new Player.EventListener() {
            @Override
            public void onTimelineChanged(Timeline timeline, @Nullable Object manifest, int reason) {
                Log.d(TAG, "onTimelineChanged");
            }

            @Override
            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
                Log.d(TAG, "onTracksChanged");
            }

            @Override
            public void onLoadingChanged(boolean isLoading) {
                Log.d(TAG, "onLoadingChanged");
            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                Log.d(TAG, "onPlayerStateChanged");
                if (mPause) return;
                mPlayWhenReady = playWhenReady;
            }

            @Override
            public void onRepeatModeChanged(int repeatMode) {
                Log.d(TAG, "onRepeatModeChanged");
            }

            @Override
            public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {
                Log.d(TAG, "onShuffleModeEnabledChanged");
            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {
                Log.d(TAG, "onPlayerError");
            }

            @Override
            public void onPositionDiscontinuity(int reason) {
                Log.d(TAG, "onPositionDiscontinuity");
            }

            @Override
            public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
                Log.d(TAG, "onPlaybackParametersChanged");
            }

            @Override
            public void onSeekProcessed() {
                Log.d(TAG, "onSeekProcessed");
            }
        });
        mPlayerView.setPlayer(mPlayer);
    }

    private void playVideo() {
        //测量播放过程中的带宽
        DefaultBandwidthMeter meter = new DefaultBandwidthMeter();
        //生成加载(下载)媒体数据的DataSource实例。
        DataSource.Factory factory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, getAppName(this, getPackageName())), meter);
        //代表将要被播放的媒体的MediaSource
        MediaSource source = new ExtractorMediaSource.Factory(factory).createMediaSource(Uri.parse(mUrl));

        mPlayer.prepare(source, false, false);
        mPlayer.seekTo(currentWindow, playbackPosition);
    }

    private void findView() {
        mPlayerView = findViewById(R.id.player_view);
        mLoadView = findViewById(R.id.player_load);
    }

    @Override
    protected void onStart() {
        super.onStart();
        fullScreen();
    }

    private void fullScreen() {
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
        getWindow().getDecorView().setSystemUiVisibility(uiOptions);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPause = true;
        mPlayerView.onPause();
        mPlayer.setPlayWhenReady(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPause = false;
        mPlayerView.onResume();
        mPlayer.setPlayWhenReady(mPlayWhenReady);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPlayer != null) mPlayer.release();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_URL, mUrl);
        outState.putBoolean(KEY_PLAY, mPlayWhenReady);
    }

    /**
     * Return the application's name.
     *
     * @param packageName The name of the package.
     * @return the application's name
     */
    private String getAppName(Context context, final String packageName) {
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(packageName, 0);
            return pi == null ? null : pi.applicationInfo.loadLabel(pm).toString();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "";
        }
    }
}
