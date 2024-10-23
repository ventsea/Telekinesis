package com.ventsea.sf.sql;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.ventsea.sf.sql.DMConstant.CREATE_SQL;
import static com.ventsea.sf.sql.DMConstant.CREATE_TEMP_SQL;
import static com.ventsea.sf.sql.DMConstant.DB_NAME;
import static com.ventsea.sf.sql.DMConstant.DROP_SQL;
import static com.ventsea.sf.sql.DMConstant.INSERT_DATA;

/**
 * 废弃
 */
class DMSQLiteHelper extends SQLiteOpenHelper {

    private static DMSQLiteHelper INSTANCE;

    private DMSQLiteHelper(Context context) {
        super(context, DB_NAME, null, 1);
    }

    static DMSQLiteHelper getInstance(Context context) {
        if (null == INSTANCE) {
            synchronized (DMSQLiteHelper.class) {
                if (null == INSTANCE) {
                    INSTANCE = new DMSQLiteHelper(context);
                }
            }
        }
        return INSTANCE;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        switch (newVersion) {
            case 2:
                db.execSQL(CREATE_TEMP_SQL);
                db.execSQL(CREATE_SQL);
                db.execSQL(INSERT_DATA);
                db.execSQL(DROP_SQL);
                break;
        }
    }
}
