package com.tlg.storehelper.activity.inventory;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.nec.lib.android.utils.AndroidUtil;
import com.tlg.storehelper.MyApp;
import com.tlg.storehelper.dao.SQLiteDbHelper;
import com.nec.lib.android.loadmoreview.AsynDataRequest;
import com.nec.lib.android.loadmoreview.PageContent;
import com.tlg.storehelper.vo.InventoryTotalVo;

import java.util.ArrayList;
import java.util.List;

/**
 * 明细记录的数据请求
 * 没有使用InventoryActivity的属性mInventoryDetailList（无序号），而是重新分批加载数据
 */
public class TotalRecordListDataRequest implements AsynDataRequest {

    private String mInventoryListId;

    //分页属性
    private int mPageSize;
    private int mPage;
    private int mRecordCount;
    private int mPageCount;

    private List<InventoryTotalVo> mInventoryTotalList = new ArrayList<InventoryTotalVo>();

    @Override
    public void fetchData(int page, int what, Handler handler, Bundle dataBundle, Activity activity) {
        this.mPageSize = 12;
        this.mPage = page;
        PageContent<InventoryTotalVo> pageContent = new PageContent<InventoryTotalVo>(page, mPageSize);

        mInventoryListId = dataBundle.getString(TotalRecordFragment.sInventoryListIdLabel);
        loadData();

        pageContent.hasMore = page < mPageCount-1;
        pageContent.datas.addAll(mInventoryTotalList);

        Message message = handler.obtainMessage();
        message.what = what;
        Bundle messageBundle = message.getData();
        messageBundle.putSerializable(PAGE_CONTENT, pageContent);
        message.setData(messageBundle);
        handler.sendMessage(message);

    }

    private void loadData() {
        SQLiteOpenHelper helper = new SQLiteDbHelper(MyApp.getInstance());
        SQLiteDatabase db = null;
        try {
            db = helper.getReadableDatabase();
            String sql = null;
            Cursor cursor = null;

            //取记录总数
            sql = new StringBuffer().append("select count(distinct binCoding)").append(" from ").append(SQLiteDbHelper.TABLE_INVENTORY_DETAIL)
                    .append(" where pid=?")
                    .toString();
            cursor = db.rawQuery(sql, new String[]{mInventoryListId});
            if (cursor.moveToFirst()) {
                mRecordCount = cursor.getInt(0);
            } else {
                mRecordCount = 0;
            }
            mPageCount =  (int)Math.ceil((double)mRecordCount/(double) mPageSize);
            cursor.close();

            //取记录明细
            mInventoryTotalList.clear();
            sql = new StringBuffer().append("select binCoding, count(distinct barcode) as barcodes, sum(quantity) as quantities").append(" from ").append(SQLiteDbHelper.TABLE_INVENTORY_DETAIL)
                    .append(" where pid=?")
                    .append(" group by binCoding")
                    .append(" order by binCoding asc")
                    .append(" limit ?,").append(mPageSize)
                    .toString();
            cursor = db.rawQuery(sql, new String[]{mInventoryListId, Integer.toString(mPage* mPageSize)});
            while (cursor.moveToNext()) {
                String binCoding = cursor.getString(cursor.getColumnIndex("binCoding"));
                int barcodes = cursor.getInt(cursor.getColumnIndex("barcodes"));
                int quantities = cursor.getInt(cursor.getColumnIndex("quantities"));
                InventoryTotalVo inventoryTotalVo = new InventoryTotalVo(binCoding, barcodes, quantities);
                mInventoryTotalList.add(inventoryTotalVo);
            }
            cursor.close();
        } catch (Throwable t) {
            Log.e(this.getClass().getName(), t.getMessage(), t);
            AndroidUtil.showToast("加载数据失败");
        } finally {
            db.close();
        }
    }

}
