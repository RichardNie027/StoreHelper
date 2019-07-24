package com.tlg.storehelper.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.nec.lib.android.httprequest.net.dialog.CustomProgressDialogUtils;
import com.tlg.storehelper.MyApp;

import java.util.List;

public class DbUtil {

    public static boolean checkGoodsBarcode(String goodsBarcode) {
        boolean result = false;
        SQLiteOpenHelper helper = new SQLiteDbHelper(MyApp.getInstance());
        SQLiteDatabase db = null;
        try {
            db = helper.getReadableDatabase();
            String sql = new StringBuffer().append("select count(*) num").append(" from ").append(SQLiteDbHelper.TABLE_GOODS_BARCODE)
                    .append(" where barcode=?")
                    .toString();
            Cursor cursor = db.rawQuery(sql, new String[] {goodsBarcode});
            if(cursor.moveToFirst()) {
                result = cursor.getInt(0) > 0;
            }
            cursor.close();
        } catch (Throwable t) {
            Toast.makeText(MyApp.getInstance(), "加载数据失败", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(MyApp.getInstance(), "商品资料同步完成", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(MyApp.getInstance(), "商品资料保存失败", Toast.LENGTH_SHORT).show();
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

}
