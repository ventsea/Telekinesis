package com.ventsea.sf.activity;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.ventsea.directlib.IWifiDirect;
import com.ventsea.directlib.IWifiDirectListener;
import com.ventsea.directlib.WDirect;
import com.ventsea.sf.R;
import com.ventsea.sf.activity.base.BaseActivity;
import com.ventsea.sf.service.LiveServer;
import com.ventsea.sf.service.ServerStatusListener;
import com.ventsea.sf.service.Transmission;
import com.ventsea.sf.view.RippleBackground;

public class ForSendActivity extends BaseActivity implements IWifiDirectListener, ServerStatusListener {

    private static final String TAG = "ForSendActivity";
    private RippleBackground mRipple;
    private Transmission mTransmission;

    public static void start(Context context) {
        context.startActivity(new Intent(context, ForSendActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_for_send);
        setTooBar();
        mRipple = findViewById(R.id.server_ripple);
        TextView name = findViewById(R.id.device_name);
        name.setText(IWifiDirect.DEVICE_NAME);
        mTransmission = Transmission.getInstance();
        mTransmission.addServerListener(this);
        if (savedInstanceState != null || LiveServer.isRunning()) { //重启或者后台强杀
            WDirect.getInstance().addDirectListener(this);
        } else {
            WDirect.getInstance().init(this, true, this);//正常启动
            WDirect.getInstance().createGroup();
        }
        overridePendingTransition(0, 0);
    }

    private void setTooBar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(IWifiDirect.DEVICE_NAME);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mRipple.startRippleAnimation();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mRipple.stopRippleAnimation();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.push_button_out);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        mTransmission.stopServer(this);
        WDirect.getInstance().stopDirect();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTransmission.cleanListener();
        WDirect.getInstance().removeDirectListener(this);
    }

    /*--------------------------------------------------------------------------------------------*/

    @Override
    public void wifiP2pEnabled() {

    }

    @Override
    public void wifiP2pDisabled() {
        Log.e(TAG, "wifiP2pDisabled");
        Toast.makeText(this, getString(R.string.wlan_disable), Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void wifiP2pPeersAvailable(WifiP2pDeviceList deviceList) {

    }

    @Override
    public void wifiP2pConnected(String deviceName) { //owner deviceName, is null
        mTransmission.startServer(this);
    }

    @Override
    public void wifiP2pDisConnected() {
        //服务器不考虑
    }

    /*--------------------------------------------------------------------------------------------*/

    @Override
    public void onServerStart() {

    }

    @Override
    public void onServerStop() {
        Log.e(TAG, "serverStop, finish");
        finish();
    }
}
