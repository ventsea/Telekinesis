package com.ventsea.sf.sql;

/**
 * 废弃
 */
class DMConstant {
    /*数据库名称*/
    static final String DB_NAME = "nfs_dm.db";

    /*表名*/
    private static final String TEMP_NAME = "temp_dm_list";
    static final String NAME = "dm_list";

    /*列*/
    static final String S_URL = "_url";
    static final String S_TITLE = "_title";
    static final String S_FP = "_fp";
    static final String S_TYPE = "_type";
    static final String S_STATUS = "_status";

    /*创建表*/
    static final String CREATE_SQL = "create table " + NAME + " ("// 建表
            + "_id" + " integer primary key autoincrement,"
            + S_URL + " varchar unique , "
            + S_TITLE + " varchar , "
            + S_FP + " varchar , "
            + S_TYPE + " int , "
            + S_STATUS + " int"
            + ")";

    static final String CREATE_TEMP_SQL = "alter table " + NAME + " rename to " + TEMP_NAME;
    static final String INSERT_DATA = "insert into " + NAME + " select *,'' from " + TEMP_NAME;
    static final String DROP_SQL = "drop table " + TEMP_NAME;
}
