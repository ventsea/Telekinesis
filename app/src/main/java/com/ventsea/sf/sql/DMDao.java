package com.ventsea.sf.sql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.ventsea.sf.activity.bean.DMBean;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import static com.ventsea.sf.sql.DMConstant.NAME;
import static com.ventsea.sf.sql.DMConstant.S_FP;
import static com.ventsea.sf.sql.DMConstant.S_STATUS;
import static com.ventsea.sf.sql.DMConstant.S_TITLE;
import static com.ventsea.sf.sql.DMConstant.S_TYPE;
import static com.ventsea.sf.sql.DMConstant.S_URL;

/**
 * 废弃
 */
public class DMDao {

    private static final String TAG = "StoreDao";
    private DMSQLiteHelper mySQLiteHelper;

    public DMDao(Context context) {
        if (null == mySQLiteHelper) {
            mySQLiteHelper = DMSQLiteHelper.getInstance(context);
        }
    }

    /*增与改*/
    public void insert(DMBean dmBean) {
        SQLiteDatabase database = mySQLiteHelper.getWritableDatabase();

        boolean exist = false;

        Cursor cursor = database.query(NAME, new String[]{S_URL}, S_URL + " = ?", new String[]{dmBean.url}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            exist = true;
        }
        if (null != cursor) cursor.close();

        ContentValues cv = new ContentValues();
        putValues(dmBean, cv);

        if (exist) {
            int i = database.update(NAME, cv, S_URL + " = ?", new String[]{dmBean.url});
            Log.d(TAG, "update " + i);
        } else {
            long l = database.insert(NAME, null, cv);
            Log.d(TAG, "insert " + l);
        }
        database.close();
    }

    /*查*/
    public List<DMBean> queryAll() {
        List<DMBean> beans = new ArrayList<>();
        SQLiteDatabase database = mySQLiteHelper.getWritableDatabase();
        Cursor cursor = database.query(NAME, null, null, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                DMBean dmBean = getDmBean(cursor);
                beans.add(dmBean);
            } while (cursor.moveToNext());
        }
        if (null != cursor) cursor.close();
        database.close();
        return beans;
    }

    /*删*/
    public void delete(int downloadID) {
        SQLiteDatabase database = mySQLiteHelper.getWritableDatabase();
        int i = database.delete(NAME, S_URL + " = ?", new String[]{String.valueOf(downloadID)});
        Log.d(TAG, "delete " + i);
        database.close();
    }

    public DMBean query(int downloadID) {
        SQLiteDatabase database = mySQLiteHelper.getWritableDatabase();
        Cursor cursor = database.query(NAME, null, S_URL + " = ?", new String[]{String.valueOf(downloadID)}, null, null, null);
        DMBean bean = null;
        if (cursor != null && cursor.moveToFirst()) {
            bean = getDmBean(cursor);
        }
        if (null != cursor) cursor.close();
        database.close();
        return bean;
    }

    public boolean queryExist(int downloadID) {
        boolean exist = false;
        SQLiteDatabase database = mySQLiteHelper.getWritableDatabase();
        String[] columns = new String[]{S_URL}; // 要返回哪几个列的数据.如果传入null就等价于select  *,
        String selection = S_URL + " = ?"; // 查询条件
        String[] selectionArgs = new String[]{String.valueOf(downloadID)};// 条件的值
        Cursor cursor = database.query(NAME, columns, selection, selectionArgs, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            exist = true;
        }
        if (null != cursor) cursor.close();
        database.close();
        return exist;
    }

    /**
     * cursor 顺序应当和表中顺序一致
     */
    private DMBean getDmBean(Cursor cursor) {
        DMBean dmBean = new DMBean(cursor.getString(1));
        dmBean.title = cursor.getString(2);
        dmBean.filePath = cursor.getString(3);
        dmBean.type = cursor.getInt(4);
        dmBean.state = cursor.getInt(5);
        if (dmBean.state == 0) {
            dmBean.state = DMBean.STATE_STOP;
        }
        return dmBean;
    }

    private void putValues(DMBean dmBean, ContentValues cv) {
        //1
        cv.put(S_URL, dmBean.url);
        //2
        cv.put(S_TITLE, dmBean.title);
        //3
        cv.put(S_FP, dmBean.filePath);
        //4
        cv.put(S_TYPE, dmBean.type);
        //5
        cv.put(S_STATUS, dmBean.state);
    }

    public static String toJson(List<String> stringList) {
        if (stringList == null) return null;
        return new JSONArray(stringList).toString();
    }

    public static List<String> toArray(String json) {
        if (json == null) return null;
        try {
            List<String> strings = new ArrayList<>();
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                strings.add((String) array.get(i));
            }
            return strings;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
