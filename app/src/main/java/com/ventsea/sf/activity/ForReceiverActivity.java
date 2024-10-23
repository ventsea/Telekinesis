package com.ventsea.sf.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.nfs.playerlib.activity.PlayerActivity;
import com.st.letter.lib.bean.FrameMessage;
import com.st.letter.lib.bean.TransFolder;
import com.st.letter.lib.bean.TransHome;
import com.st.letter.lib.media.LocalApp;
import com.st.letter.lib.media.LocalAudio;
import com.st.letter.lib.media.LocalContacts;
import com.st.letter.lib.media.LocalDB;
import com.st.letter.lib.media.LocalDocs;
import com.st.letter.lib.media.LocalImages;
import com.st.letter.lib.media.LocalVideo;
import com.ventsea.directlib.IWifiDirectListener;
import com.ventsea.directlib.WDirect;
import com.ventsea.sf.R;
import com.ventsea.sf.activity.base.BaseActivity;
import com.ventsea.sf.activity.fragment.EventListener;
import com.ventsea.sf.activity.fragment.FragmentClazzDetail;
import com.ventsea.sf.activity.fragment.FragmentDiscover;
import com.ventsea.sf.activity.fragment.FragmentFolder;
import com.ventsea.sf.activity.fragment.FragmentRemoteHome;
import com.ventsea.sf.activity.fragment.adapter.ClazzActionClickListener;
import com.ventsea.sf.activity.fragment.adapter.ClazzItemMoreAdapter;
import com.ventsea.sf.activity.fragment.adapter.CookieClickListener;
import com.ventsea.sf.activity.fragment.adapter.CookieAdapter;
import com.ventsea.sf.app.NFSApplication;
import com.ventsea.sf.dialog.DetailInfoDialog;
import com.ventsea.sf.dialog.DownloadTips;
import com.ventsea.sf.dialog.PhotoViewPageDialog;
import com.ventsea.sf.service.ClientStatusListener;
import com.ventsea.sf.service.Transmission;
import com.ventsea.sf.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ForReceiverActivity extends BaseActivity implements EventListener, IWifiDirectListener, ClientStatusListener, CookieClickListener, ClazzActionClickListener {

    private static final String TAG = "ForReceiverActivity";
    private static final String MENU_TAG = "menuTag";
    private static final String NAME_TAG = "nameTag";
    private static final String FOLDER_TAG = "folderTag";
    private SearchView mSearchView;
    private Transmission mTransmission;
    private Menu aMenu;
    public boolean mOptionMenuOn;
    private FragmentManager fm;
    private TextView mTitle;
    private ImageView mExpand;
    private LinearLayout mTitleContent;
    private String mServerName;
    private List<String> mCacheFolder;
    private PopupWindow mPopupWindow;
    private ExecutorService mExecutor;
    private boolean mConnected;

    public static void start(Context context) {
        context.startActivity(new Intent(context, ForReceiverActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_for_receiver);
        setToolBar();
        mExecutor = Executors.newSingleThreadExecutor();

        checkMenuAndUrl(savedInstanceState);

        WDirect.getInstance().init(this, false, this);
        WDirect.getInstance().addDirectListener(this);
        mTransmission = Transmission.getInstance();
        mTransmission.addClientListener(this);

        fm = getSupportFragmentManager();
        createDiscover();
        overridePendingTransition(0, 0);
    }

    private void checkMenuAndUrl(Bundle bundle) {
        if (bundle == null) {
            mCacheFolder = new ArrayList<>();
            return;
        }
        mCacheFolder = bundle.getStringArrayList(FOLDER_TAG);
        mOptionMenuOn = bundle.getBoolean(MENU_TAG, false);
        mServerName = bundle.getString(NAME_TAG);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(MENU_TAG, mOptionMenuOn);
        outState.putString(NAME_TAG, mServerName);
        if (mCacheFolder == null) mCacheFolder = new ArrayList<>();
        outState.putStringArrayList(FOLDER_TAG, new ArrayList<>(mCacheFolder));
    }

    private void setToolBar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                onBackPressed();
            }
        });
        mTitle = findViewById(R.id.title);
        mExpand = findViewById(R.id.expand);
        mTitleContent = findViewById(R.id.title_content);
    }

    private void createDiscover() {
        Fragment fragment = fm.findFragmentById(R.id.frame_content);
        if (fragment != null) {
            if (fragment instanceof FragmentDiscover) {
                Log.d(TAG, "reload discover view");
                mOptionMenuOn = false;
                showOptionMenu();
            } else {
                Log.d(TAG, "reload page view");
                mOptionMenuOn = true;
                showOptionMenu();
            }
        } else {
            Log.d(TAG, "load discover view");
            mOptionMenuOn = false;
            showOptionMenu();
            Fragment fragment1 = new FragmentDiscover();
            fm.beginTransaction().add(R.id.frame_content, fragment1, "Discover").commit();
        }
    }

    @Override
    public void onLoadNextPage(String url) {
        mOptionMenuOn = true;
        showOptionMenu();
        if (url == null || url.equals(TransHome.MSG_PATH_HOME)) {
            loadHomePage();
            mCacheFolder.add(TransHome.MSG_PATH_HOME);
            return;
        } else if (url.equals(TransFolder.MSG_PATH_INDEX)) {
            loadIndexPage();
            mCacheFolder.add(TransFolder.MSG_PATH_INDEX);
            return;
        }
        url = TransFolder.MSG_PATH_INDEX + url;
        FragmentFolder folder = FragmentFolder.newInstance(url);
        fm.beginTransaction().replace(R.id.frame_content, folder, url).addToBackStack(url).commitAllowingStateLoss();
        mCacheFolder.add(url);
    }

    private void loadIndexPage() {
        FragmentFolder folder = FragmentFolder.newInstance(TransFolder.MSG_PATH_INDEX);
        fm.beginTransaction().replace(R.id.frame_content, folder, TransFolder.MSG_PATH_INDEX).addToBackStack(TransFolder.MSG_PATH_INDEX).commitAllowingStateLoss();
    }

    private void loadHomePage() {
        Fragment remoteHome = new FragmentRemoteHome();
        fm.beginTransaction().replace(R.id.frame_content, remoteHome, TransHome.MSG_PATH_HOME).addToBackStack(TransHome.MSG_PATH_HOME).commitAllowingStateLoss();
    }

    @Override
    public void onOpenFile(TransFolder.NFile file) {
        DownloadTips.showTips(this, file, new DownloadTips.ClickListener() {
            @Override
            public void onCancelClick() {

            }

            @Override
            public void onPositiveClick(TransFolder.NFile file1) {
                Transmission.getInstance().downloadFile(ForReceiverActivity.this, Uri.parse(file1.getDir()));
            }
        });
    }

    @Override
    public void onJumpPage(String url) {
        fm.popBackStackImmediate(url, 0);
    }

    @Override
    public void onShowTitle(String url) {
        setTitle(url);
    }

    private void setTitle(String url) {
        //发现
        if (url.equals("Discover")) {
            mTitle.setText(R.string.discover);
            mExpand.setVisibility(View.GONE);
            mTitleContent.setClickable(false);
            return;
        }

        //预览
        if (url.equals(TransHome.MSG_PATH_HOME)) {
            mTitle.setText(R.string.preview);
            mExpand.setVisibility(View.GONE);
            mTitleContent.setClickable(false);
            return;
        }

        if (url.equals(String.valueOf(TransHome.TYPE_APP))) {
            mTitle.setText(R.string.app);
            mExpand.setVisibility(View.GONE);
            mTitleContent.setClickable(false);
            return;
        }

        if (url.equals(String.valueOf(TransHome.TYPE_DOC))) {
            mTitle.setText(R.string.doc);
            mExpand.setVisibility(View.GONE);
            mTitleContent.setClickable(false);
            return;
        }

        if (url.equals(String.valueOf(TransHome.TYPE_AUDIO))) {
            mTitle.setText(R.string.audio);
            mExpand.setVisibility(View.GONE);
            mTitleContent.setClickable(false);
            return;
        }

        if (url.equals(String.valueOf(TransHome.TYPE_CONTACT))) {
            mTitle.setText(R.string.contact);
            mExpand.setVisibility(View.GONE);
            mTitleContent.setClickable(false);
            return;
        }

        if (url.equals(String.valueOf(TransHome.TYPE_IMAGE))) {
            mTitle.setText(R.string.image);
            mExpand.setVisibility(View.GONE);
            mTitleContent.setClickable(false);
            return;
        }

        if (url.equals(String.valueOf(TransHome.TYPE_VIDEO))) {
            mTitle.setText(R.string.video);
            mExpand.setVisibility(View.GONE);
            mTitleContent.setClickable(false);
            return;
        }

        //根目录
        if (url.equals(TransFolder.MSG_PATH_INDEX)) {
            mTitle.setText(mServerName);
        } else {
            //文件夹
            mTitle.setText(url.substring(url.lastIndexOf("/")));
        }

        mExpand.setVisibility(View.VISIBLE);
        mTitleContent.setClickable(true);
        mTitleContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupCookie();
            }
        });
    }

    @Override
    public void onOpenClazz(int type) {
        Fragment clazzDetail = FragmentClazzDetail.getInstance(type);
        fm.beginTransaction().replace(R.id.frame_content, clazzDetail, String.valueOf(type)).addToBackStack(String.valueOf(type)).commitAllowingStateLoss();
        mCacheFolder.add(String.valueOf(type));
    }

    @Override
    public void onShowPhotoPage(List<String> urlList, int position) {
        PhotoViewPageDialog.showPhotoPage(this, urlList, position);
    }

    @Override
    public void onShowPlayerView(String url) {
        PlayerActivity.startPlay(this, url);
    }

    @Override
    public void onItemMoreClick(View v, Object o) {
        showPopupAction(v, o);
    }

    @Override
    public void onActionClick(Object o, int action) {
        if (mPopupWindow != null && mPopupWindow.isShowing()) {
            mPopupWindow.dismiss();
        }
        if (o instanceof LocalDB) {
            LocalDB data = (LocalDB) o;
            switch (action) {
                case 0:
                    Transmission.getInstance().downloadFile(this, Uri.parse(data.data));
                    break;
                case 1:
                    DetailInfoDialog.showTips(this, buildFileInfo(data));
                    break;
            }
        }
        if (o instanceof LocalContacts.Contact) {
            LocalContacts.Contact contact = (LocalContacts.Contact) o;
            switch (action) {
                case 2:
                    final List<LocalContacts.Contact> list = new ArrayList<>();
                    list.add(contact);
                    // TODO: 2019/3/12 use Snack bar
                    Toast.makeText(NFSApplication.sContext, getString(R.string.import_contacts), Toast.LENGTH_SHORT).show();
                    mExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            LocalContacts.insertAllContacts(NFSApplication.sContext, list, new LocalContacts.ContactsInsertListener() {
                                @Override
                                public void onInsertFinish() {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(NFSApplication.sContext, getString(R.string.Import_completed), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }

                                @Override
                                public void onInsertError() {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(NFSApplication.sContext, getString(R.string.Import_failed), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            });
                        }
                    });
                    break;
                case 3:
                    DetailInfoDialog.showTips(this, buildContactInfo(contact));
                    break;
            }
        }
    }

    private ArrayList<String> buildFileInfo(LocalDB db) {
        if (db == null) return null;
        ArrayList<String> list = new ArrayList<>();
        if (db instanceof LocalAudio.Audio) {
            LocalAudio.Audio audio = (LocalAudio.Audio) db;
            list.add("Title : " + audio.title);

            list.add("Artist : " + (audio.artist == null ? "<unKnown>" : audio.artist));

            list.add("Type : " + audio.type);
            list.add("Size : " + Utils.readableFileSize(audio.size));
            Uri uri = Uri.parse(audio.data);
            list.add("Data : " + uri.getQueryParameter("dir"));
        }

        if (db instanceof LocalImages.Image) {
            LocalImages.Image image = (LocalImages.Image) db;
            list.add("Title : " + image.title);
            list.add("Type : " + image.type);
            list.add("Size : " + Utils.readableFileSize(image.size));
            Uri uri = Uri.parse(image.data);
            list.add("Data : " + uri.getQueryParameter("dir"));
        }

        if (db instanceof LocalVideo.Video) {
            LocalVideo.Video video = (LocalVideo.Video) db;
            list.add("Title : " + video.title);
            list.add("Type : " + video.type);
            list.add("Size : " + Utils.readableFileSize(video.size));
            Uri uri = Uri.parse(video.data);
            list.add("Data : " + uri.getQueryParameter("dir"));
        }

        if (db instanceof LocalDocs.Doc) {
            LocalDocs.Doc doc = (LocalDocs.Doc) db;
            list.add("Title : " + doc.title);
            list.add("Size : " + Utils.readableFileSize(doc.size));
            Uri uri = Uri.parse(doc.data);
            list.add("Data : " + uri.getQueryParameter("dir"));
        }

        if (db instanceof LocalApp.App) {
            LocalApp.App app = (LocalApp.App) db;
            list.add("Label : " + app.label);
            list.add("Package Name : " + app.packageName);
            list.add("Size : " + Utils.readableFileSize(app.size));

            if (app.vn != null)
                list.add("Version Name : " + app.vn);

            if (app.tgSdk > 0)
                list.add("Target SDK : " + app.tgSdk);
        }
        return list;
    }

    private ArrayList<String> buildContactInfo(LocalContacts.Contact contact) {
        if (contact == null) return null;
        ArrayList<String> list = new ArrayList<>();
        list.add("Name : " + contact.name);
        for (String s : contact.phone) {
            list.add("Number : " + s);
        }
        return list;
    }

    @SuppressLint("InflateParams")
    private void showPopupAction(View v, Object o) {
        List<Integer> integers = buildActionList(o);
        if (integers.size() <= 0) return;
        View more = LayoutInflater.from(this).inflate(R.layout.popup_more, null, false);
        final RecyclerView recyclerView = more.findViewById(R.id.more_action);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new ClazzItemMoreAdapter(this, o, integers, this));
        mPopupWindow = new PopupWindow(more, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mPopupWindow.setFocusable(true);
        mPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mPopupWindow.setElevation(30);
        }
        recyclerView.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        int vh = v.getHeight();
        int[] location = new int[2];
        v.getLocationInWindow(location);
        int rvh = recyclerView.getMeasuredHeight();

        if (Utils.getScreenHeight(v.getContext()) - location[1] - rvh < vh) {
            mPopupWindow.showAsDropDown(v, Gravity.NO_GRAVITY, -vh - rvh);
        } else {
            mPopupWindow.showAsDropDown(v, Gravity.NO_GRAVITY, Gravity.NO_GRAVITY);
        }
    }

    private List<Integer> buildActionList(Object o) {
        List<Integer> integers = new ArrayList<>();
        if (o instanceof LocalContacts.Contact) {
            integers.add(2);
            integers.add(3);
        } else {
            integers.add(0);
            integers.add(1);
        }
        return integers;
    }

    @SuppressLint("InflateParams")
    private void showPopupCookie() {
        View view = LayoutInflater.from(this).inflate(R.layout.popup_folder, null, false);
        RecyclerView recyclerView = view.findViewById(R.id.cookie_folder);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new CookieAdapter(this, mCacheFolder, mServerName, this));
        mPopupWindow = new PopupWindow(view, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mPopupWindow.setFocusable(true);
        mPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mPopupWindow.setElevation(30);
        }
        int offsetY = -mTitleContent.getMeasuredHeight();
        mPopupWindow.showAsDropDown(mTitle, Gravity.NO_GRAVITY, offsetY, Gravity.START);
    }

    @Override
    public void onCookieClick(String url) {
        if (mPopupWindow != null && mPopupWindow.isShowing()) {
            mPopupWindow.dismiss();
        }
        mCacheFolder = mCacheFolder.subList(0, mCacheFolder.indexOf(url) + 1); //左闭右开（包含左不包含右）
        onJumpPage(url);
    }

    @Override
    public void onBackPressed() {
        int i = fm.getBackStackEntryCount();
        if (i > 1) {
            mCacheFolder.remove(mCacheFolder.size() - 1);
            super.onBackPressed();
        } else {
            mTransmission.stopServer(this);
            WDirect.getInstance().stopDirect();
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.search_home, menu);
        //找到searchView
//        MenuItem searchItem = menu.findItem(R.id.action_search);
//
//        mSearchView = (SearchView) searchItem.getActionView();
//        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//                if (imm != null) {
//                    imm.hideSoftInputFromWindow(mSearchView.getWindowToken(), 0);
//                }
//                mSearchView.clearFocus();
//                return true;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String newText) {
//                return true;
//            }
//        });
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        aMenu = menu;
        showOptionMenu();
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_dm) {
            DownloadManagerActivity.startManager(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showOptionMenu() {
        if (aMenu != null) {
            for (int i = 0; i < aMenu.size(); i++) {
                aMenu.getItem(i).setVisible(mOptionMenuOn);
                aMenu.getItem(i).setEnabled(mOptionMenuOn);
            }
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.push_button_out);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
        mExecutor.shutdownNow();
        mTransmission.cleanListener();
        WDirect.getInstance().removeDirectListener(this);
        if (mPopupWindow != null && mPopupWindow.isShowing()) mPopupWindow.dismiss();
    }

    @Override
    public void wifiP2pEnabled() {

    }

    @Override
    public void wifiP2pDisabled() {
        Log.e(TAG, "wifiP2pDisabled, finish");
        Toast.makeText(this, getString(R.string.wlan_disable), Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void wifiP2pPeersAvailable(WifiP2pDeviceList deviceList) {

    }

    @Override
    public void wifiP2pConnected(String deviceName) {
        Log.e(TAG, "wifiP2pConnected wow");
        mConnected = true;
        mServerName = deviceName;
        mTransmission.startClient(this);
    }

    @Override
    public void wifiP2pDisConnected() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!mConnected) {
                    Log.e(TAG, "wifiP2pDisConnected, finish");
                    finish();
                }
            }
        }, mConnected ? 1000 : 15000);
        mConnected = false;
    }

    @Override
    public void onConnectServer() {
        onLoadNextPage(TransHome.MSG_PATH_HOME);
    }

    @Override
    public void onDisConnectServer() {
        Log.e(TAG, "disConnectServer, finish");
        finish();
    }

    @Override
    public void onConnectError() {
        Log.e(TAG, "connectError, finish");
        finish();
    }

    @Override
    public void onClientReceiverMessage(FrameMessage message) {

    }
}
 