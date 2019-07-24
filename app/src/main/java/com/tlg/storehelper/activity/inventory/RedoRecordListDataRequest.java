package com.tlg.storehelper.activity.inventory;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.nec.lib.android.loadmoreview.AsynDataRequest;
import com.nec.lib.android.loadmoreview.PageContent;
import com.tlg.storehelper.vo.InventoryRedoVo;

import java.util.List;

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
