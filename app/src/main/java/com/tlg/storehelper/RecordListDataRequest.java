package com.tlg.storehelper;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.nec.application.MyApplication;
import com.tlg.storehelper.dao.InventoryDetail;
import com.tlg.storehelper.dao.SQLiteDbHelper;
import com.tlg.storehelper.loadmorerecycler.AsynDataRequest;
import com.tlg.storehelper.loadmorerecycler.PageContent;
import com.tlg.storehelper.vo.InventoryDetailVo;

import java.util.ArrayList;
import java.util.List;

public class RecordListDataRequest implements AsynDataRequest {

    private long mInventoryListId;
    private int mRecordPerPage;
    private int mPage;
    private int mRecordCount;
    private int mPageCount;
    private List<InventoryDetailVo> mInventoryDetailList = new ArrayList<InventoryDetailVo>();

    @Override
    public void fetchData(int page, int what, Handler handler, Bundle bundle) {
        this.mRecordPerPage = 6;
        this.mPage = page;
        PageContent<InventoryDetailVo> pageContent = new PageContent<InventoryDetailVo>(page, mRecordPerPage);

        mInventoryListId = bundle.getLong("mInventoryListId");
        loadData();

        pageContent.hasMore = page < mPageCount-1;
        for (int i = 0; i < mInventoryDetailList.size(); i++) {
            InventoryDetailVo vo = mInventoryDetailList.get(i);
            vo.idx = mPage * mRecordPerPage + mInventoryDetailList.size() - i;
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
        SQLiteOpenHelper helper = new SQLiteDbHelper(MyApplication.getInstance());
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
            System.out.println(t.getMessage());
        } finally {
            db.close();
        }
    }

}
