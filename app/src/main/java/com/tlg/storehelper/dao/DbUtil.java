package com.tlg.storehelper.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.util.Log;

import com.nec.lib.android.httprequest.net.dialog.CustomProgressDialogUtils;
import com.nec.lib.android.utils.AndroidUtil;
import com.nec.lib.android.utils.DateUtil;
import com.nec.lib.android.utils.SQLiteUtil;
import com.nec.lib.android.utils.StringUtil;
import com.tlg.storehelper.MyApp;
import com.tlg.storehelper.vo.GoodsPopularityVo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class DbUtil {

    /**查询条码，截断0/1/2/3/4/5位，匹配返回真实条码，否则返回""空字符串*/
    public static String checkGoodsBarcode(String goodsBarcode) {
        return checkGoodsBarcode(goodsBarcode, false);
    }

    /**查询条码，截断0/1/2/3/4/5位，匹配返回真实条码或货号，否则返回""空字符串*/
    public static String checkGoodsBarcode(String goodsBarcode, boolean returnGoodsNo) {
        String result = "";
        SQLiteOpenHelper helper = new SQLiteDbHelper(MyApp.getInstance());
        SQLiteDatabase db = null;
        try {
            db = helper.getReadableDatabase();
            String filedName = returnGoodsNo ? "goodsNo" : "barcode";
            for(int i : new int[] {0,1,2,3,4,5}) {
                String sql = new StringBuffer().append("select ").append(filedName).append(" from ").append(SQLiteDbHelper.TABLE_GOODS_BARCODE)
                        .append(" where barcode='").append(StringUtil.truncateRight(goodsBarcode,i)).append("'").append(" limit 1")
                        .toString();
                Cursor cursor = db.rawQuery(sql, new String[] {});
                if(cursor.moveToFirst()) {
                    result = cursor.getString(0);
                }
                cursor.close();
                if(!result.isEmpty())
                    break;
            }
        } catch (Throwable t) {
            AndroidUtil.showToast("查询条码失败");
            result = "";
        } finally {
            db.close();
        }
        return result;
    }

    /**查询货号，存在返回true*/
    public static boolean checkGoodsNo(String goodsNo) {
        boolean result = false;
        SQLiteOpenHelper helper = new SQLiteDbHelper(MyApp.getInstance());
        SQLiteDatabase db = null;
        try {
            db = helper.getReadableDatabase();
            String sql = new StringBuffer().append("select count(*) as num").append(" from ").append(SQLiteDbHelper.TABLE_GOODS_BARCODE)
                    .append(" where goodsNo='").append(goodsNo).append("'")
                    .toString();
            Cursor cursor = db.rawQuery(sql, new String[] {});
            if(cursor.moveToFirst()) {
                result = cursor.getInt(0) > 0;
            }
            cursor.close();
        } catch (Throwable t) {
            AndroidUtil.showToast("查询货号失败");
            result = false;
        } finally {
            db.close();
        }
        return result;
    }

    /**模糊查询货号，返回货号列表。字母以星号代替*/
    public static LinkedHashMap<String, String> checkGoodsNoList(String storeCodeFirstLetter, String incompleteGoodsNo, int maxSize) {
        LinkedHashMap<String, String> result = new LinkedHashMap<>();
        incompleteGoodsNo = "%" + incompleteGoodsNo.replaceAll("[#*]", "_") + "%";
        SQLiteOpenHelper helper = new SQLiteDbHelper(MyApp.getInstance());
        SQLiteDatabase db = null;
        try {
            db = helper.getReadableDatabase();
            String sql = new StringBuffer().append("select a.goodsNo, a.goodsName||' @'||a.goodsNo as goodsName").append(" from ").append(SQLiteDbHelper.TABLE_GOODS)
                    .append(" a left join goods_popularity b on a.goodsNo=b.goodsNo")
                    .append(" where a.goodsNo like '").append(incompleteGoodsNo).append("'").append(" and a.storeHeaders like '%").append(storeCodeFirstLetter).append("%'")
                    .append(" order by b.popularity DESC,a.createTime desc").append(" limit " + maxSize)
                    .toString();
            Cursor cursor = db.rawQuery(sql, new String[] {});
            while (cursor.moveToNext()) {
                result.put(cursor.getString(0), cursor.getString(1));
            }
            cursor.close();
        } catch (Throwable t) {
            AndroidUtil.showToast("模糊查询货号失败");
            result.clear();
        } finally {
            db.close();
        }
        return result;
    }

    public static void saveGoodsList(List<String> goodsList, List<String> goodsBarcodeList, boolean onEmpty) {

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
                    if(onEmpty) {
                        db.delete(SQLiteDbHelper.TABLE_GOODS, null, null);
                        db.delete(SQLiteDbHelper.TABLE_GOODS_BARCODE, null, null);
                    }
                    for (String goods : goodsList) {
                        String[] valueArray = goods.split("\\|");
                        Object[] valueObjArray = new Object[valueArray.length];
                        for(int i=0; i<valueArray.length; i++)
                            valueObjArray[i] = valueArray[i];
                        try {
                            valueObjArray[4] = Integer.valueOf(valueArray[4]);
                        } catch (Exception e) {
                            valueObjArray[4] = Integer.valueOf(0);
                        }
                        ContentValues contentValues1 = SQLiteUtil.toContentValues(new Goods(valueArray[0],valueArray[1],valueArray[2],valueArray[3],(Integer)valueObjArray[4],valueArray[5],valueArray[6],valueArray[7]));
                        long result1 = onEmpty ? -1 : db.update(SQLiteDbHelper.TABLE_GOODS, contentValues1, "goodsNo=?", new String[]{valueArray[0]});
                        if(result1 <= 0L) {
                            String sql = new StringBuffer().append("insert into ").append(SQLiteDbHelper.TABLE_GOODS).append(" values(?,?,?,?,?,?,?,?)").toString();
                            db.execSQL(sql, valueObjArray);
                        }
                    }
                    for (String goodsBarcode : goodsBarcodeList) {
                        String[] valueArray = goodsBarcode.split("\\|");
                        ContentValues contentValues1 = SQLiteUtil.toContentValues(new GoodsBarcode(valueArray[0],valueArray[1],valueArray[2]));
                        long result1 = onEmpty ? -1 : db.update(SQLiteDbHelper.TABLE_GOODS_BARCODE, contentValues1, "barcode=?", new String[]{valueArray[0]});
                        if(result1 <= 0L) {
                            String sql = new StringBuffer().append("insert into ").append(SQLiteDbHelper.TABLE_GOODS_BARCODE).append(" values(?,?,?)").toString();
                            db.execSQL(sql, valueArray);
                        }
                    }
                    result = true;
                    db.setTransactionSuccessful();
                } catch (Throwable t) {
                    Log.e(this.getClass().getName(), t.getMessage(), t);
                } finally {
                    if (db != null) {
                        db.endTransaction();
                        db.close();
                    }
                }
                return result;
            }
        }.execute();
    }

    public static void saveGoodsPopularity(List<GoodsPopularityVo> list) {

        //在新线程保存数据
        new AsyncTask<List<GoodsPopularityVo>, Integer, Boolean>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                //CustomProgressDialogUtils.getInstance().showProgress(MyApp.getInstance(), "正在保存商品热度");
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                super.onPostExecute(aBoolean);
                CustomProgressDialogUtils.getInstance().dismissProgress();
                if(aBoolean)
                    ;//AndroidUtil.showToast("商品热度同步完成");
                else
                    AndroidUtil.showToast("商品热度保存失败");
            }

            @Override
            protected Boolean doInBackground(List<GoodsPopularityVo>... lists) {
                boolean result = false;
                SQLiteOpenHelper helper = new SQLiteDbHelper(MyApp.getInstance());
                SQLiteDatabase db = null;
                try {
                    db = helper.getWritableDatabase();
                    db.beginTransaction();
                    db.delete(SQLiteDbHelper.TABLE_GOODS_POPULARITY, null, null);
                    for (GoodsPopularityVo vo : list) {
                        String sql = new StringBuffer().append("insert into ").append(SQLiteDbHelper.TABLE_GOODS_POPULARITY).append(" values(?,?)").toString();
                        db.execSQL(sql, new Object[]{vo.goodsNo, vo.popularity});
                    }
                    result = true;
                    db.setTransactionSuccessful();
                } catch (Throwable t) {
                    Log.e(this.getClass().getName(), t.getMessage(), t);
                } finally {
                    if (db != null) {
                        db.endTransaction();
                        db.close();
                    }
                }
                return result;
            }
        }.execute();
    }

    public static Inventory getInventory(SQLiteDatabase db, String id) {
        Inventory inventory = new Inventory();
        SQLiteOpenHelper helper = new SQLiteDbHelper(MyApp.getInstance());
        boolean dbIsNull = db == null;
        SQLiteDatabase _db = db;
        try {
            if(dbIsNull)
                _db = helper.getReadableDatabase();
            String sql = new StringBuffer().append("select *").append(" from ").append(SQLiteDbHelper.TABLE_INVENTORY)
                    .append(" where id=?")
                    .toString();
            Cursor cursor = _db.rawQuery(sql, new String[]{id});
            if (cursor.moveToFirst()) {
                inventory.id = cursor.getString(cursor.getColumnIndex("id"));
                inventory.storeCode = cursor.getString(cursor.getColumnIndex("storeCode"));
                inventory.listDate = DateUtil.fromStr(cursor.getString(cursor.getColumnIndex("listDate")));
                inventory.idx = cursor.getInt(cursor.getColumnIndex("idx"));
                inventory.username = cursor.getString(cursor.getColumnIndex("username"));
                inventory.listNo = cursor.getString(cursor.getColumnIndex("listNo"));
                inventory.status = cursor.getString(cursor.getColumnIndex("status"));
                inventory.createTime = DateUtil.fromStr(cursor.getString(cursor.getColumnIndex("createTime")));
                inventory.lastTime = DateUtil.fromStr(cursor.getString(cursor.getColumnIndex("lastTime")));
            }
            cursor.close();
        } catch (Throwable t) {
            Log.e(DbUtil.class.getName(), t.getMessage(), t);
        } finally {
            if(dbIsNull)
                _db.close();
        }
        return inventory;
    }

    public static boolean saveInventoty(SQLiteDatabase db, Inventory inventory) {
        boolean result = false;
        SQLiteOpenHelper helper = new SQLiteDbHelper(MyApp.getInstance());
        boolean dbIsNull = db == null;
        SQLiteDatabase _db = db;
        try {
            if(dbIsNull) {
                _db = helper.getWritableDatabase();
                _db.beginTransaction();
            }
            ContentValues contentValues2 = SQLiteUtil.toContentValues(inventory);
            long result2 = _db.update(SQLiteDbHelper.TABLE_INVENTORY, contentValues2, "id=?", new String[]{inventory.id});
            if(result2 <= 0L)
                throw new Exception("更新记录出错");
            if(dbIsNull)
                _db.setTransactionSuccessful();
            result = true;
        } catch (Throwable t) {
            Log.e(DbUtil.class.getName(), t.getMessage(), t);
        } finally {
            if(dbIsNull) {
                _db.endTransaction();
                _db.close();
            }
        }
        return result;
    }

    public static InventoryDetail getInventoryDetail(SQLiteDatabase db, String id) {
        InventoryDetail inventoryDetail = new InventoryDetail();
        SQLiteOpenHelper helper = new SQLiteDbHelper(MyApp.getInstance());
        boolean dbIsNull = db == null;
        SQLiteDatabase _db = db;
        try {
            if(dbIsNull)
                _db = helper.getReadableDatabase();
            String sql = new StringBuffer().append("select *").append(" from ").append(SQLiteDbHelper.TABLE_INVENTORY_DETAIL)
                    .append(" where id=?")
                    .toString();
            Cursor cursor = _db.rawQuery(sql, new String[]{id});
            if (cursor.moveToFirst()) {
                inventoryDetail.id = cursor.getString(cursor.getColumnIndex("id"));
                inventoryDetail.pid = cursor.getString(cursor.getColumnIndex("pid"));
                inventoryDetail.idx = cursor.getInt(cursor.getColumnIndex("idx"));
                inventoryDetail.binCoding = cursor.getString(cursor.getColumnIndex("binCoding"));
                inventoryDetail.barcode = cursor.getString(cursor.getColumnIndex("barcode"));
                inventoryDetail.quantity = cursor.getInt(cursor.getColumnIndex("quantity"));
            }
            cursor.close();
        } catch (Throwable t) {
            Log.e(DbUtil.class.getName(), t.getMessage(), t);
        } finally {
            if(dbIsNull)
                _db.close();
        }
        return inventoryDetail;
    }
}
