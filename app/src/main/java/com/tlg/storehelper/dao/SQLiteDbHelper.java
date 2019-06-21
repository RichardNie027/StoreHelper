package com.tlg.storehelper.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * SQLiteDbHelper helper = new SQLiteDbHelper(getApplicationContext());
 * SQLiteDatabase database = helper.getWritableDatabase();
 */
public class SQLiteDbHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "store_db.db";
    public static final int DB_VERSION = 2;
    public static final String TABLE_INVENTORY = "inventory";
    public static final String TABLE_INVENTORY_DETAIL = "inventory_detail";
    public static final String TABLE_GOODS_BARCODE = "goods_barcode";

    //创建 inventory 表的 sql 语句
    private static final String INVENTORY_CREATE_TABLE_SQL = "create table if not exists " + TABLE_INVENTORY + "("
            + "id integer primary key autoincrement,"   //ID
            + "store_code varchar(20) not null,"        //店编
            + "list_date varchar(19) not null,"         //盘点日期
            + "idx integer not null,"                   //序号
            + "username varchar(20) not null,"          //创建用户
            + "list_no varchar(20) not null,"           //盘点单号
            + "create_time varchar(19) not null,"       //创建时间
            + "last_time varchar(19) not null"          //修改时间
            + ");";

    private static final String INVENTORY_DETAIL_CREATE_TABLE_SQL = "create table if not exists " + TABLE_INVENTORY_DETAIL + "("
            + "id integer primary key autoincrement,"   //ID
            + "pid integer not null,"                   //主表ID
            + "bin_coding varchar(20) not null,"        //货架编码
            + "barcode varchar(30) not null,"           //商品条码
            + "quantity integer not null"               //数量
            + ");";

    private static final String GOODS_BARCODE_CREATE_TABLE_SQL = "create table if not exists " + TABLE_GOODS_BARCODE + "("
            + "id char(32) primary key,"                //ID
            + "goods_id char(32) not null,"             //商品ID
            + "size_id char(32) not null,"              //尺码ID
            + "size_desc varchar(20) not null,"         //尺码显示
            + "barcode varchar(30) not null"            //商品条码
            + ");";

    public SQLiteDbHelper(Context context) {
        // 传递数据库名与版本号给父类
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // 在这里通过 db.execSQL 函数执行 SQL 语句创建所需要的表
        // 创建表
        sqLiteDatabase.execSQL(INVENTORY_CREATE_TABLE_SQL);
        sqLiteDatabase.execSQL(INVENTORY_DETAIL_CREATE_TABLE_SQL);
        sqLiteDatabase.execSQL(GOODS_BARCODE_CREATE_TABLE_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // 数据库版本号变更会调用 onUpgrade 函数，在这根据版本号进行升级数据库
        sqLiteDatabase.execSQL("drop table if exists " + TABLE_GOODS_BARCODE);
        sqLiteDatabase.execSQL("drop table if exists " + TABLE_INVENTORY_DETAIL);
        sqLiteDatabase.execSQL("drop table if exists " + TABLE_INVENTORY);
        this.onCreate(sqLiteDatabase);
    }
}
