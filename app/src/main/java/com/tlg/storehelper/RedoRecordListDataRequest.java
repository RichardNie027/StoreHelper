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
 * （已经变更为从外部获取数据，此类存在是为了不改结构）
 */
public class RedoRecordListDataRequest implements AsynDataRequest {

    //分页属性
    private int mRecordPerPage;
    private int mPage;
    private int mRecordCount;
    private int mPageCount;

    private RequireRedoDataListener mListener;

    @Override
    public void fetchData(int page, int what, Handler handler, Bundle dataBundle) {
        this.mRecordPerPage = Integer.MAX_VALUE;
        this.mPageCount = 1;
        this.mPage = page;
        PageContent<InventoryRedoVo> pageContent = new PageContent<InventoryRedoVo>(page, Integer.MAX_VALUE);

        if(this.mListener != null)
            pageContent.datas.addAll(this.mListener.onNeedData());
        this.mRecordCount = pageContent.datas.size();

        Message message = handler.obtainMessage();
        message.what = what;
        Bundle messageBundle = message.getData();
        messageBundle.putSerializable(PAGE_CONTENT, pageContent);
        message.setData(messageBundle);
        handler.sendMessage(message);

    }

    public void setmListener(RequireRedoDataListener mListener) {
        this.mListener = mListener;
    }

    /**
     * 复盘数量
     */
    public interface RequireRedoDataListener {
        /**
         * 需要获得外部数据
         */
        List<InventoryRedoVo> onNeedData();

    }

}
