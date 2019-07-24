package com.tlg.storehelper.activity.inventory;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.tlg.storehelper.MyApp;
import com.tlg.storehelper.dao.SQLiteDbHelper;
import com.nec.lib.android.loadmoreview.AsynDataRequest;
import com.nec.lib.android.loadmoreview.PageContent;
import com.tlg.storehelper.vo.InventoryDetailVo;

import java.util.ArrayList;
import java.util.List;

/**
 * 明细记录的数据请求
 * 没有使用InventoryActivity的属性mInventoryDetailList（无序号），而是重新分批加载数据
 */
public class RecordListDataRequest implements AsynDataRequest {

    private long mInventoryListId;

    //分页属性
    private int mRecordPerPage;
    private int mPage;
    private int mRecordCount;
    private int mPageCount;

    private List<InventoryDetailVo> mInventoryDetailList = new ArrayList<InventoryDetailVo>();

    @Override
    public void fetchData(int page, int what, Handler handler, Bundle dataBundle) {
        this.mRecordPerPage = 20;
        this.mPage = page;
        PageContent<InventoryDetailVo> pageContent = new PageContent<InventoryDetailVo>(page, mRecordPerPage);

        mInventoryListId = dataBundle.getLong(RecordFragment.sInventoryListIdLabel);
        loadData();

        pageContent.hasMore = page < mPageCount-1;
        for (int i = 0; i < mInventoryDetailList.size(); i++) {
            InventoryDetailVo vo = mInventoryDetailList.get(i);
            vo.idx = mRecordCount - i - (mPage * mRecordPerPage);
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
        SQLiteOpenHelper helper = new SQLiteDbHelper(MyApp.getInstance());
        SQLiteDatabase db = null;
        try {
            db = helper.getReadableDatabase();
            String sql = null;
            Cursor cursor = null;

            //取记录总数
            sql = new StringBuffer().append("select count(*)").append(" from ").append(SQLiteDbHelper.TABLE_INVENTORY_DETAIL)
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
            mInventoryDetailList.clear();
            sql = new StringBuffer().append("select *").append(" from ").append(SQLiteDbHelper.TABLE_INVENTORY_DETAIL)
                    .append(" where pid=?")
                    .append(" order by id desc")
                    .append(" limit ?,").append(mRecordPerPage)
                    .toString();
            cursor = db.rawQuery(sql, new String[]{Long.toString(mInventoryListId), Integer.toString(mPage*mRecordPerPage)});
            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndex("id"));
                int idx = mRecordCount;
                String bin_coding = cursor.getString(cursor.getColumnIndex("bin_coding"));
                String barcode = cursor.getString(cursor.getColumnIndex("barcode"));
                int quantity = cursor.getInt(cursor.getColumnIndex("quantity"));
                InventoryDetailVo inventoryDetailVo = new InventoryDetailVo(id, idx, bin_coding, barcode, quantity);
                mInventoryDetailList.add(inventoryDetailVo);
            }
            cursor.close();
        } catch (Throwable t) {
            Log.e(this.getClass().getName(), t.getMessage(), t);
            Toast.makeText(MyApp.getInstance(), "加载数据失败", Toast.LENGTH_SHORT).show();
        } finally {
            db.close();
        }
    }

}
