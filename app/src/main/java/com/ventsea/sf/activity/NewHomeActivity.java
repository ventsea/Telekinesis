package com.ventsea.sf.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.ventsea.directlib.WDirect;
import com.ventsea.sf.R;
import com.ventsea.sf.activity.base.BaseActivity;
import com.ventsea.sf.permission.PermissionHelper;
import com.ventsea.sf.permission.PermissionViewListener;
import com.ventsea.sf.service.LiveServer;
import com.ventsea.sf.test.TestActivity;
import com.ventsea.sf.view.CircularAnim;

public class NewHomeActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener, PermissionViewListener {

    private static final String TAG = "NewHomeActivity";
    private Handler mHandler;
    private DrawerLayout mDrawer;
    private boolean mCanClick;

    private static final int SC = 8080;
    private static final int CC = 8888;
    private static final String[] SP = new String[]{WRITE_EXTERNAL_STORAGE, ACCESS_COARSE_LOCATION, READ_CONTACTS};
    private static final String[] SP_R = new String[]{ACCESS_COARSE_LOCATION, READ_CONTACTS};
    private static final String[] SP_T = new String[]{NEARBY_WIFI_DEVICES, READ_CONTACTS};
    private static final String[] CP = new String[]{WRITE_EXTERNAL_STORAGE, ACCESS_COARSE_LOCATION, WRITE_CONTACTS};
    private static final String[] CP_R = new String[]{ACCESS_COARSE_LOCATION, WRITE_CONTACTS};
    private static final String[] CP_T = new String[]{NEARBY_WIFI_DEVICES, WRITE_CONTACTS};
    private Button s;
    private Button c;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_home);
        setToolBar();
        setNavigationListener();
        setButtonListener();
        mCanClick = true;
        mHandler = new Handler(Looper.getMainLooper());
        checkRunning();
    }

    private void checkRunning() {
        if (LiveServer.isRunning()) {
            if (LiveServer.isServer()) {
                ForSendActivity.start(NewHomeActivity.this);
            } else {
                LiveServer.stop(this);
                WDirect.getInstance().stopDirect();
            }
        }
    }

    private void setButtonListener() {
        s = findViewById(R.id.service_party);
        c = findViewById(R.id.client_party);
        s.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mCanClick) {
                    Log.d(TAG, "click too fast");
                    return;
                }
                mCanClick = false;
                openServer();
                initCanClick();
            }
        });
        c.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mCanClick) {
                    Log.d(TAG, "click too fast");
                    return;
                }
                mCanClick = false;
                openClient();
                initCanClick();
            }
        });
    }

    private void openServer() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            PermissionHelper.check(SC, this, SP_T, this);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            PermissionHelper.check(SC, this, SP_R, this);
        } else {
            PermissionHelper.check(SC, this, SP, this);
        }
    }

    private void openClient() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            PermissionHelper.check(CC, this, CP_T, this);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            PermissionHelper.check(CC, this, CP_R, this);
        } else {
            PermissionHelper.check(CC, this, CP, this);
        }
    }

    private void initCanClick() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mCanClick = true;
            }
        }, 1000);
    }

    private void setNavigationListener() {
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void setToolBar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDrawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(toggle);
        toggle.syncState();
    }

    @Override
    public void onBackPressed() {
        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.new_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startTestActivity();
            return true;
        }

        if (id == R.id.action_dm) {
            DownloadManagerActivity.startManager(this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void startTestActivity() {
        startActivity(new Intent(this, TestActivity.class));
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_email) {
            sendEmail(this, "ventsea@gmail.com");
        } else if (id == R.id.nav_warning) {
            AppInfoActivity.startWarning(this);
        } else if (id == R.id.nav_info) {
            AppInfoActivity.startAbout(this);
        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_help) {
            AppInfoActivity.startHelp(this);
        }
//        mDrawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void sendEmail(Context context, String address) {
        Uri uri = Uri.parse("mailto:" + address);
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, uri);
        emailIntent.putExtra(Intent.EXTRA_EMAIL, address);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "About NearFieldShare");
        // emailIntent.putExtra(Intent.EXTRA_TEXT, content);
        context.startActivity(Intent.createChooser(emailIntent, getString(R.string.select_email)));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionHelper.doResult(this, this, requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SC:
            case CC:
                onPermissionsGranted(requestCode);
                break;
            default:
                break;
        }
    }

    @Override
    public void onPermissionsGranted(int code) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                PermissionHelper.checkUnusual(code, PermissionViewListener.ACTION_MANAGER_STORAGE, this, this);
                return;
            }
        }
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            if (code == SC) {
                CircularAnim.init(400, 400, R.color.rippelcolor);
                CircularAnim.fullActivity(this, s).go(new CircularAnim.OnAnimationEndListener() {
                    @Override
                    public void onAnimationEnd() {
                        ForSendActivity.start(NewHomeActivity.this);
                    }
                });
            }
            if (code == CC) {
                CircularAnim.init(400, 400, R.color.colorAccent);
                CircularAnim.fullActivity(this, c).go(new CircularAnim.OnAnimationEndListener() {
                    @Override
                    public void onAnimationEnd() {
                        ForReceiverActivity.start(NewHomeActivity.this);
                    }
                });
            }
        } else {
            openGPSTips(code);
        }
    }

    private void openGPSTips(final int code) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.Tips).setMessage(R.string.open_gps).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).setPositiveButton(R.string.open, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                openGPS(code);
            }
        });
        builder.show();
    }

    private void openGPS(int code) {
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivityForResult(intent, code);
    }

    @Override
    public void onPermissionsRefused(int code, String[] refusePermissions) {
        if (refusePermissions.length == 1 && (refusePermissions[0].equals(READ_CONTACTS) || refusePermissions[0].equals(WRITE_CONTACTS))) {
            onPermissionsGranted(code);
        }
    }
}
