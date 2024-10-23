package com.ventsea.sf.permission;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.ventsea.sf.R;

import java.util.ArrayList;
import java.util.List;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.M;
import static android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS;

import static com.ventsea.sf.permission.PermissionViewListener.ACTION_MANAGER_STORAGE;
import static com.ventsea.sf.permission.PermissionViewListener.ACTION_MANAGE_OVERLAY_PERMISSION;
import static com.ventsea.sf.permission.PermissionViewListener.ACTION_MANAGE_WRITE_SETTINGS;

public final class PermissionHelper {

    /**
     * 检查并请求权限
     *
     * @param code        code
     * @param ac          activity
     * @param permissions 请求的权限数组
     * @param listener    监听
     */
    public static void check(final int code, final Activity ac, String[] permissions,
                             final PermissionViewListener listener) {
        if (SDK_INT < M) {
            listener.onPermissionsGranted(code);
            return;
        }
        final List<String> refused = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(ac, permission) == PERMISSION_GRANTED)
                continue;
            refused.add(permission);
        }
        if (refused.isEmpty()) {
            listener.onPermissionsGranted(code);
            return;
        }
        //不用解释用途
        List<String> normalize = null;
        //应该解释用途
        List<String> rationale = null;
        for (String permission : refused) {
            //是否应该解释该权限用途。true: 该权限在请求中被拒绝过，但是未选择不再提醒；false : 该权限首次请求时，或者被选中不再询问后
            if (ActivityCompat.shouldShowRequestPermissionRationale(ac, permission)) {
                if (null == rationale) rationale = new ArrayList<>();
                rationale.add(permission);
            } else {
                if (null == normalize) normalize = new ArrayList<>();
                normalize.add(permission);
            }
        }
        refused.clear();
        if (null != rationale) refused.addAll(rationale);
        if (null != normalize) refused.addAll(normalize);
        if (null != rationale) {
            //存在需要解释用途的权限
            final String[] permissionList = refused.toArray(new String[refused.size()]);
            showRationale(ac, permissionList, new PermissionClickListener() {
                @Override
                public void onNextClick() {
                    ActivityCompat.requestPermissions(ac, permissionList, code);
                }

                @Override
                public void onRefuseClick() {
                    listener.onPermissionsRefused(code, permissionList);
                }
            });
        } else {
            //不存在需要解释用途的权限，直接申请
            ActivityCompat.requestPermissions(ac, refused.toArray(new String[refused.size()]), code);
        }
    }

    @TargetApi(M)
    public static void doResult(final Activity ac, final PermissionViewListener listener, final int code,
                                String[] permissions, int[] results) {
        if (null == listener || (permissions.length == 0 && results.length == 0)) {
            ac.onRequestPermissionsResult(code, permissions, results);
            return;
        }
        List<String> refused = null;
        for (int i = 0; i < results.length; i++) {
            int result = results[i];
            if (result == PERMISSION_GRANTED)
                continue;
            if (null == refused) refused = new ArrayList<>();
            refused.add(permissions[i]);
        }
        if (null == refused || refused.size() == 0) {
            //全部同意
            listener.onPermissionsGranted(code);
        } else {
            final String[] permissionList = refused.toArray(new String[refused.size()]);
            for (String permission : refused) {
                //检查是否应该解释(未选择不再提醒),优先处理需要解释的权限
                if (ActivityCompat.shouldShowRequestPermissionRationale(ac, permission)) {
                    check(code, ac, permissionList, listener);
                    return;
                }
            }
            showRationale(ac, permissionList, new PermissionClickListener() {
                @Override
                public void onNextClick() {
                    Intent intent = new Intent(ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", ac.getPackageName(), null));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    ac.startActivity(intent);
                }

                @Override
                public void onRefuseClick() {
                    listener.onPermissionsRefused(code, permissionList);
                }
            });
        }
    }

    /**
     * 不常用敏感权限
     *
     * @param code       code
     * @param permission {@link PermissionViewListener#ACTION_MANAGE_WRITE_SETTINGS}
     *                   {@link PermissionViewListener#ACTION_MANAGE_OVERLAY_PERMISSION}
     * @param context    activity
     * @param listener   view
     *                   <p>
     *                   注意：该方法请在 onResume 中使用
     */
    /*===============系统设置和窗口管理权限 仅限主界面调用==================*/
    public static void checkUnusual(final int code, String permission, final Activity context, final PermissionViewListener listener) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ACTION_MANAGE_WRITE_SETTINGS.equals(permission)) {
                if (Settings.System.canWrite(context)) {
                    listener.onPermissionsGranted(code);
                } else {
                    showSystemSettingRationale(context, new PermissionClickListener() {
                        @Override
                        public void onNextClick() {
                            requestSystemSetting(context);
                        }

                        @Override
                        public void onRefuseClick() {
                            listener.onPermissionsRefused(code, new String[]{ACTION_MANAGE_WRITE_SETTINGS});
                        }
                    });
                }
            }
            if (ACTION_MANAGE_OVERLAY_PERMISSION.equals(permission)) {
                if (Settings.canDrawOverlays(context)) {
                    listener.onPermissionsGranted(code);
                } else {
                    showWindowManagerRationale(context, new PermissionClickListener() {
                        @Override
                        public void onNextClick() {
                            requestWindowManager(context);
                        }

                        @Override
                        public void onRefuseClick() {
                            listener.onPermissionsRefused(code, new String[]{ACTION_MANAGE_OVERLAY_PERMISSION});
                        }
                    });
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && ACTION_MANAGER_STORAGE.equals(permission)) {
                if (Environment.isExternalStorageManager()) {
                    listener.onPermissionsGranted(code);
                } else {
                    Intent appIntent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    appIntent.setData(Uri.parse("package:" + context.getPackageName()));
                    //appIntent.setData(Uri.fromParts("package", activity.getPackageName(), null));
                    try {
                        context.startActivity(appIntent);
                    } catch (ActivityNotFoundException ex) {
                        Intent allFileIntent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                        context.startActivity(allFileIntent);
                    }
                }
            }
        } else {
            listener.onPermissionsGranted(code);
        }
    }

    private static void requestSystemSetting(Activity context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return;
        PackageManager manager = context.getPackageManager();
        Intent intent = new Intent(ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + context.getPackageName()));
        List<ResolveInfo> info = manager.queryIntentActivities(intent, 0);
        if (info.size() > 0) {
            context.startActivity(intent);
        }
    }

    private static void requestWindowManager(Activity context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return;
        PackageManager manager = context.getPackageManager();
        Intent intent = new Intent(ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + context.getPackageName()));
        List<ResolveInfo> info = manager.queryIntentActivities(intent, 0);
        if (info.size() > 0) {
            context.startActivity(intent);
        }
    }
    /*===============end==================*/

    /**
     * 解析权限
     */
    private static void showRationale(Activity context, String[] permissions,
                                      PermissionClickListener listener) {
        ArrayList<String> cache = new ArrayList<>();
        for (String s : permissions) {
            switch (s) {
                case PermissionViewListener.ACCESS_COARSE_LOCATION:
                    cache.add(context.getString(R.string.location_info));
                    break;
                case PermissionViewListener.WRITE_CALL_LOG:
                    cache.add(context.getString(R.string.write_call_log));
                    break;
                case PermissionViewListener.READ_CALL_LOG:
                    cache.add(context.getString(R.string.read_call_log));
                    break;
                case PermissionViewListener.WRITE_CONTACTS:
                    cache.add(context.getString(R.string.write_contact));
                    break;
                case PermissionViewListener.READ_CONTACTS:
                    cache.add(context.getString(R.string.read_contact));
                    break;
                case PermissionViewListener.READ_SMS:
                    cache.add(context.getString(R.string.read_sms));
                    break;
                case PermissionViewListener.CAMERA:
                    cache.add(context.getString(R.string.open_camera));
                    break;
                case PermissionViewListener.READ_PHONE_STATE:
                    cache.add(context.getString(R.string.device_information));
                    break;
                case PermissionViewListener.WRITE_EXTERNAL_STORAGE:
                    cache.add(context.getString(R.string.storage));
                    break;
                case "android.permission.NEARBY_WIFI_DEVICES":
                    cache.add(context.getString(R.string.nearby_wifi_device));
                    break;
                default:
                    break;

            }
        }
        PermissionRationaleDialog.showRational(context, cache, listener);
    }

    /**
     * 解析系统设置权限
     */
    private static void showSystemSettingRationale(Activity context, PermissionClickListener listener) {

    }

    /**
     * 解析窗口权限
     */
    private static void showWindowManagerRationale(Activity context, PermissionClickListener listener) {

    }
}