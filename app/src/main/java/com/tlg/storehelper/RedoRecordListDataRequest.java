package com.tlg.storehelper;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.nec.application.MyApplication;
import com.tlg.storehelper.dao.SQLiteDbHelper;
import com.tlg.storehelper.loadmoreview.AsynDataRequest;
import com.tlg.storehelper.loadmoreview.PageContent;
import com.tlg.storehelper.vo.InventoryRedoVo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 复盘汇总的数据请求
 */
public class RedoRecordListDataRequest implements AsynDataRequest {

    private long mInventoryListId;
    private String mInventoryBinCoding;

    //分页属性
    private int mRecordPerPage;
    private int mPage;
    private int mRecordCount;
    private int mPageCount;

    private List<InventoryRedoVo> mInventoryRedoList = new ArrayList<>();
    private List<String> mInventoryRedoKeyList = new ArrayList<>();
    private Map<String, Integer> mRedoDataMap = new LinkedHashMap<>();

    private RequireRedoDataListener mListener;

    @Override
    public void fetchData(int page, int what, Handler handler, Bundle dataBundle) {
        this.mRecordPerPage = 20;
        this.mPage = page;
        PageContent<InventoryRedoVo> pageContent = new PageContent<InventoryRedoVo>(page, mRecordPerPage);

        mInventoryListId = dataBundle.getLong(RedoRecordFragment.sInventoryListIdLabel);
        mInventoryBinCoding = dataBundle.getString(RedoRecordFragment.sInventoryBinCodingLabel);
        loadData();
        if(this.mListener != null)
            mRedoDataMap = this.mListener.onNeedRedoData(mInventoryRedoKeyList);

        pageContent.hasMore = page < mPageCount-1;
        for (int i = 0; i < mInventoryRedoList.size(); i++) {
            InventoryRedoVo vo = mInventoryRedoList.get(i);
            if(mRedoDataMap != null && mRedoDataMap.size() > 0) {
                int quantity = mRedoDataMap.get(vo.barcode);
                vo.quantity2 = quantity;
                vo.quantity3 = vo.quantity1 - vo.quantity2;
            }
            pageContent.datas.add(vo);
        }

        Message message = handler.obtainMessage();
        message.what = what;
        Bundle messageBundle = message.getData();
        messageBundle.putSerializable(PAGE_CONTENT, pageContent);
        message.setData(messageBundle);
        handler.sendMessage(message);

    }

    private void loadData() {
        Map<String, Integer> redoNewDataMap = null;
        if(mListener != null)
            redoNewDataMap = mListener.beforeDataRequest();   //先获取复盘新条码数据，有则视为第一页，数据库数据从第二页开始
        boolean hasNewData = redoNewDataMap != null && !redoNewDataMap.isEmpty();
        if(mPage == 0 && hasNewData) {
            mInventoryRedoList.clear();
            mInventoryRedoKeyList.clear();
            mRedoDataMap.clear();
            for(String key: redoNewDataMap.keySet()) {
                InventoryRedoVo inventoryDetailVo = new InventoryRedoVo(key, 0, redoNewDataMap.get(key), -redoNewDataMap.get(key));
                mInventoryRedoList.add(inventoryDetailVo);
                mInventoryRedoKeyList.add(inventoryDetailVo.barcode);
            }
        }

        SQLiteOpenHelper helper = new SQLiteDbHelper(MyApplication.getInstance());
        SQLiteDatabase db = null;
        try {
            db = helper.getReadableDatabase();
            String sql = null;
            Cursor cursor = null;

            //取记录总数
            sql = new StringBuffer().append("select count(distinct barcode)").append(" from ").append(SQLiteDbHelper.TABLE_INVENTORY_DETAIL)
                    .append(" where pid=? and bin_coding=?")
                    .append(" group by pid,bin_coding")
                    .toString();
            cursor = db.rawQuery(sql, new String[]{Long.toString(mInventoryListId), mInventoryBinCoding});
            if (cursor.moveToFirst()) {
                mRecordCount = cursor.getInt(0);
            } else {
                mRecordCount = 0;
            }
            mPageCount =  (int)Math.ceil((double)mRecordCount/(double)mRecordPerPage);
            if(redoNewDataMap != null && !redoNewDataMap.isEmpty()) {
                mRecordCount += redoNewDataMap.size();    //先计算页数，再累加
                mPageCount++;
            }
            cursor.close();

            if(mPage > 0 || !hasNewData) {
                //取记录明细
                mInventoryRedoList.clear();
                mInventoryRedoKeyList.clear();
                mRedoDataMap.clear();
                sql = new StringBuffer().append("select barcode, sum(quantity) as quantity").append(" from ").append(SQLiteDbHelper.TABLE_INVENTORY_DETAIL)
                        .append(" where pid=? and bin_coding=?")
                        .append(" group by barcode")
                        .append(" order by barcode asc")
                        .append(" limit ?,").append(mRecordPerPage)
                        .toString();
                cursor = db.rawQuery(sql, new String[]{Long.toString(mInventoryListId), mInventoryBinCoding, Integer.toString((mPage + (hasNewData ? -1 : 0)) * mRecordPerPage)});
                while (cursor.moveToNext()) {
                    String barcode = cursor.getString(cursor.getColumnIndex("barcode"));
                    int quantity = cursor.getInt(cursor.getColumnIndex("quantity"));
                    InventoryRedoVo inventoryDetailVo = new InventoryRedoVo(barcode, quantity, 0, 0);
                    mInventoryRedoList.add(inventoryDetailVo);
                    mInventoryRedoKeyList.add(inventoryDetailVo.barcode);
                }
                cursor.close();
            }
        } catch (Throwable t) {
            Log.e(this.getClass().getName(), t.getMessage(), t);
            Toast.makeText(MyApplication.getInstance(), "加载数据失败", Toast.LENGTH_SHORT).show();
        } finally {
            db.close();
        }
    }

    public void setmListener(RequireRedoDataListener mListener) {
        this.mListener = mListener;
    }

    /**
     * 复盘数量
     */
    public interface RequireRedoDataListener {
        /**
         * 需要获得复盘实际数量
         */
        Map<String, Integer> onNeedRedoData(List<String> barcodeList);

        /**
         * 获得复盘出现的新商品记录
         * @return
         */
        Map<String, Integer> beforeDataRequest();
    }

}
