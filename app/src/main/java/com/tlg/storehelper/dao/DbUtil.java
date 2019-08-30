package com.tlg.storehelper.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.nec.lib.android.httprequest.net.dialog.CustomProgressDialogUtils;
import com.nec.lib.android.utils.AndroidUtil;
import com.nec.lib.android.utils.DateUtil;
import com.tlg.storehelper.MyApp;

import java.util.List;

public class DbUtil {

    public static boolean checkGoodsBarcode(String goodsBarcode, boolean whole) {
        boolean result = false;
        SQLiteOpenHelper helper = new SQLiteDbHelper(MyApp.getInstance());
        SQLiteDatabase db = null;
        try {
            db = helper.getReadableDatabase();
            String whereSub = whole ? " where barcode='" + goodsBarcode + "'" : " where barcode like '" + goodsBarcode + "%'";
            String sql = new StringBuffer().append("select count(*) num").append(" from ").append(SQLiteDbHelper.TABLE_GOODS_BARCODE)
                    .append(whereSub)
                    .toString();
            Cursor cursor = db.rawQuery(sql, new String[] {});
            if(cursor.moveToFirst()) {
                result = cursor.getInt(0) > 0;
            }
            cursor.close();
        } catch (Throwable t) {
            AndroidUtil.showToast("加载数据失败");
            result = false;
        } finally {
            db.close();
        }
        return result;
    }

    public static void saveGoodsBarcodes(List<String> goodsBarcodeList) {

        //在新线程保存数据
        new AsyncTask<List<String>, Integer, Boolean>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                CustomProgressDialogUtils.getInstance().showProgress(MyApp.getInstance(), "正在保存商品资料");
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                super.onPostExecute(aBoolean);
                CustomProgressDialogUtils.getInstance().dismissProgress();
                if(aBoolean)
                    AndroidUtil.showToast("商品资料同步完成");
                else
                    AndroidUtil.showToast("商品资料保存失败");
            }

            @Override
            protected Boolean doInBackground(List<String>... lists) {
                boolean result = false;
                SQLiteOpenHelper helper = new SQLiteDbHelper(MyApp.getInstance());
                SQLiteDatabase db = null;
                try {
                    db = helper.getWritableDatabase();
                    db.beginTransaction();
                    db.delete(SQLiteDbHelper.TABLE_GOODS_BARCODE, null, null);
                    for (String goodsBarcode : goodsBarcodeList) {
                        String sql = new StringBuffer().append("insert into ").append(SQLiteDbHelper.TABLE_GOODS_BARCODE).append(" values(?)").toString();
                        db.execSQL(sql, new Object[] {goodsBarcode});
                    }
                    result = true;
                    db.setTransactionSuccessful();
                } catch (Throwable t) {
                    Log.e(this.getClass().getName(), t.getMessage(), t);
                } finally {
                    if (db != null) {
                        db.endTransaction();
                    }
                    db.close();
                }
                return result;
            }
        }.execute();
    }

    public static Inventory getInventory(SQLiteDatabase db, String id) {
        Inventory inventory = new Inventory();
        SQLiteOpenHelper helper = new SQLiteDbHelper(MyApp.getInstance());
        SQLiteDatabase _db = db;
        try {
            if(db == null)
                _db = helper.getReadableDatabase();
            String sql = new StringBuffer().append("select *").append(" from ").append(SQLiteDbHelper.TABLE_INVENTORY)
                    .append(" where id=?")
                    .toString();
            Cursor cursor = _db.rawQuery(sql, new String[]{id});
            if (cursor.moveToFirst()) {
                inventory.id = cursor.getString(cursor.getColumnIndex("id"));
                inventory.store_code = cursor.getString(cursor.getColumnIndex("store_code"));
                inventory.list_date = DateUtil.fromStr(cursor.getString(cursor.getColumnIndex("list_date")));
                inventory.idx = cursor.getInt(cursor.getColumnIndex("idx"));
                inventory.username = cursor.getString(cursor.getColumnIndex("username"));
                inventory.list_no = cursor.getString(cursor.getColumnIndex("list_no"));
                inventory.create_time = DateUtil.fromStr(cursor.getString(cursor.getColumnIndex("create_time")));
                inventory.last_time = DateUtil.fromStr(cursor.getString(cursor.getColumnIndex("last_time")));
            }
            cursor.close();
        } catch (Throwable t) {
            Log.e(DbUtil.class.getName(), t.getMessage(), t);
        } finally {
            if(db == null)
                _db.close();
        }
        return inventory;
    }

    public static InventoryDetail getInventoryDetail(SQLiteDatabase db, String id) {
        InventoryDetail inventoryDetail = new InventoryDetail();
        SQLiteOpenHelper helper = new SQLiteDbHelper(MyApp.getInstance());
        SQLiteDatabase _db = db;
        try {
            if(db == null)
                _db = helper.getReadableDatabase();
            String sql = new StringBuffer().append("select *").append(" from ").append(SQLiteDbHelper.TABLE_INVENTORY_DETAIL)
                    .append(" where id=?")
                    .toString();
            Cursor cursor = _db.rawQuery(sql, new String[]{id});
            if (cursor.moveToFirst()) {
                inventoryDetail.id = cursor.getString(cursor.getColumnIndex("id"));
                inventoryDetail.pid = cursor.getString(cursor.getColumnIndex("pid"));
                inventoryDetail.idx = cursor.getInt(cursor.getColumnIndex("idx"));
                inventoryDetail.bin_coding = cursor.getString(cursor.getColumnIndex("bin_coding"));
                inventoryDetail.barcode = cursor.getString(cursor.getColumnIndex("barcode"));
                inventoryDetail.quantity = cursor.getInt(cursor.getColumnIndex("quantity"));
            }
            cursor.close();
        } catch (Throwable t) {
            Log.e(DbUtil.class.getName(), t.getMessage(), t);
        } finally {
            if(db == null)
                _db.close();
        }
        return inventoryDetail;
    }
}
