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
import com.tlg.storehelper.vo.InventoryTotalVo;

import java.util.ArrayList;
import java.util.List;

/**
 * 明细记录的数据请求
 * 没有使用InventoryActivity的属性mInventoryDetailList（无序号），而是重新分批加载数据
 */
public class TotalRecordListDataRequest implements AsynDataRequest {

    private long mInventoryListId;
    private int mRecordPerPage;
    private int mPage;
    private int mRecordCount;
    private int mPageCount;
    private List<InventoryTotalVo> mInventoryTotalList = new ArrayList<InventoryTotalVo>();

    @Override
    public void fetchData(int page, int what, Handler handler, Bundle dataBundle) {
        this.mRecordPerPage = 12;
        this.mPage = page;
        PageContent<InventoryTotalVo> pageContent = new PageContent<InventoryTotalVo>(page, mRecordPerPage);

        mInventoryListId = dataBundle.getLong(RecordFragment.sInventoryListIdLabel);
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
        SQLiteOpenHelper helper = new SQLiteDbHelper(MyApplication.getInstance());
        SQLiteDatabase db = null;
        try {
            db = helper.getReadableDatabase();
            String sql = null;
            Cursor cursor = null;

            //取记录总数
            sql = new StringBuffer().append("select count(distinct bin_coding)").append(" from ").append(SQLiteDbHelper.TABLE_INVENTORY_DETAIL)
                    .append(" where pid=?")
                    .toString();
            cursor = db.rawQuery(sql, new String[]{Long.toString(mInventoryListId)});
            if (cursor.moveToFirst()) {
                mRecordCount = cursor.getInt(0);
            } else {
                mRecordCount = 0;
            }
            mPageCount =  (int)Math.ceil((double)mRecordCount/(double)mRecordPerPage);
            cursor.close();

            //取记录明细
            mInventoryTotalList.clear();
            sql = new StringBuffer().append("select bin_coding, count(distinct barcode) as barcodes, sum(quantity) as quantities").append(" from ").append(SQLiteDbHelper.TABLE_INVENTORY_DETAIL)
                    .append(" where pid=?")
                    .append(" group by bin_coding")
                    .append(" order by bin_coding asc")
                    .append(" limit ?,").append(mRecordPerPage)
                    .toString();
            cursor = db.rawQuery(sql, new String[]{Long.toString(mInventoryListId), Integer.toString(mPage*mRecordPerPage)});
            while (cursor.moveToNext()) {
                String bin_coding = cursor.getString(cursor.getColumnIndex("bin_coding"));
                int barcodes = cursor.getInt(cursor.getColumnIndex("barcodes"));
                int quantities = cursor.getInt(cursor.getColumnIndex("quantities"));
                InventoryTotalVo inventoryTotalVo = new InventoryTotalVo(bin_coding, barcodes, quantities);
                mInventoryTotalList.add(inventoryTotalVo);
            }
            cursor.close();
        } catch (Throwable t) {
            Log.e("ERROR", t.getMessage(), t);
            Toast.makeText(MyApplication.getInstance(), "加载数据失败", Toast.LENGTH_SHORT).show();
        } finally {
            db.close();
        }
    }

}
