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
import com.tlg.storehelper.loadmorerecycler.AsynDataRequest;
import com.tlg.storehelper.loadmorerecycler.PageContent;
import com.tlg.storehelper.vo.InventoryRedoDetailVo;
import com.tlg.storehelper.vo.InventoryRedoVo;

import java.util.ArrayList;
import java.util.HashMap;
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
    private Map<String, Integer> mRedoDataMap = new HashMap<>();

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
        List<InventoryRedoVo> mRedoNewList = mListener.beforeDataRequest();   //先获取复盘新条码数据
        if(mRedoNewList != null && mPage == 0 && mRedoNewList.size() > 0) {
            mInventoryRedoList.clear();
            mInventoryRedoKeyList.clear();
            mRedoDataMap.clear();
            for(InventoryRedoVo vo: mRedoNewList) {
                InventoryRedoVo inventoryDetailVo = new InventoryRedoVo(vo.barcode, vo.quantity1, vo.quantity2, vo.quantity3);
                mInventoryRedoList.add(inventoryDetailVo);
                mInventoryRedoKeyList.add(inventoryDetailVo.barcode);
            }
            return;
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
                    .append(" group by barcode")
                    .toString();
            cursor = db.rawQuery(sql, new String[]{Long.toString(mInventoryListId)});
            if (cursor.moveToFirst()) {
                mRecordCount = cursor.getInt(0);
            } else {
                mRecordCount = 0;
            }
            mPageCount =  (int)Math.ceil((double)mRecordCount/(double)mRecordPerPage) + 1;
            if(mRedoNewList != null)
                mRecordCount += mRedoNewList.size();    //先计算页数，再累加
            cursor.close();

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
            cursor = db.rawQuery(sql, new String[]{Long.toString(mInventoryListId), mInventoryBinCoding, Integer.toString((mPage+((mRedoNewList != null && !mRedoNewList.isEmpty())?1:0))*mRecordPerPage)});
            while (cursor.moveToNext()) {
                String barcode = cursor.getString(cursor.getColumnIndex("barcode"));
                int quantity = cursor.getInt(cursor.getColumnIndex("quantity"));
                InventoryRedoVo inventoryDetailVo = new InventoryRedoVo(barcode, quantity, 0, 0);
                mInventoryRedoList.add(inventoryDetailVo);
                mInventoryRedoKeyList.add(inventoryDetailVo.barcode);
            }
            cursor.close();
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
        List<InventoryRedoVo> beforeDataRequest();
    }

}
