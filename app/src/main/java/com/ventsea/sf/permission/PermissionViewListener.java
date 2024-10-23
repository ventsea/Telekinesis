package com.ventsea.sf.permission;

import android.Manifest;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.RequiresApi;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.R;
import static android.os.Build.VERSION_CODES.S;
import static android.os.Build.VERSION_CODES.TIRAMISU;

//方便实现该接口时直接获取权限
public interface PermissionViewListener {

    //存储
    String WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;

    //设备信息
    String READ_PHONE_STATE = Manifest.permission.READ_PHONE_STATE;

    //相机
    String CAMERA = Manifest.permission.CAMERA;

    //SMS
    String READ_SMS = Manifest.permission.READ_SMS;

    //联系人
    String READ_CONTACTS = Manifest.permission.READ_CONTACTS;
    String WRITE_CONTACTS = Manifest.permission.WRITE_CONTACTS;

    //通话记录
    String READ_CALL_LOG = Manifest.permission.READ_CALL_LOG;
    String WRITE_CALL_LOG = Manifest.permission.WRITE_CALL_LOG;

    //定位
    String ACCESS_COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;

    //修改设置（独立申请）
    String ACTION_MANAGE_WRITE_SETTINGS = SDK_INT >= M ? Settings.ACTION_MANAGE_WRITE_SETTINGS : "android.settings.action.MANAGE_WRITE_SETTINGS";

    //悬浮窗（独立申请）
    String ACTION_MANAGE_OVERLAY_PERMISSION = SDK_INT >= M ? Settings.ACTION_MANAGE_OVERLAY_PERMISSION : "android.settings.action.MANAGE_OVERLAY_PERMISSION";

    String ACTION_MANAGER_STORAGE = SDK_INT >= R ? Manifest.permission.MANAGE_EXTERNAL_STORAGE : "android.permission.MANAGE_EXTERNAL_STORAGE";

    String NEARBY_WIFI_DEVICES = SDK_INT >= TIRAMISU ? Manifest.permission.NEARBY_WIFI_DEVICES : "android.permission.NEARBY_WIFI_DEVICES";

    //全部权限已允许
    void onPermissionsGranted(int code);

    /**
     * 请求结果
     *
     * @param code              code
     * @param refusePermissions 拒绝列表
     */
    void onPermissionsRefused(int code, String[] refusePermissions);
}