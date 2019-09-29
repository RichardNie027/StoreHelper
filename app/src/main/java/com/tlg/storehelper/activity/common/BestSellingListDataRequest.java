package com.tlg.storehelper.activity.common;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.nec.lib.android.base.BaseRxAppCompatActivity;
import com.nec.lib.android.loadmoreview.AsynDataRequest;
import com.nec.lib.android.loadmoreview.PageContent;
import com.tlg.storehelper.httprequest.net.entity.SimplePageListEntity;
import com.tlg.storehelper.httprequest.utils.RequestUtil;
import com.tlg.storehelper.vo.GoodsSimpleVo;

import java.util.ArrayList;
import java.util.List;

/**
 * 畅销款的数据请求
 */
public class BestSellingListDataRequest implements AsynDataRequest {

    private String mStoreCode;
    private String mDimension;

    //分页属性
    private int mPageSize;
    private int mPage;
    private int mRecordCount;
    private int mPageCount;

    private List<GoodsSimpleVo> mGoodsList = new ArrayList<GoodsSimpleVo>();

    @Override
    public void fetchData(int page, int what, Handler handler, Bundle dataBundle, Activity activity) {
        this.mPageSize = 10;   // by server side
        this.mPage = page;
        PageContent<GoodsSimpleVo> pageContent = new PageContent<GoodsSimpleVo>(page, mPageSize);

        mStoreCode = dataBundle.getString(BestSellingFragment.sStoreCodeLabel);
        mDimension = dataBundle.getString(BestSellingFragment.sDimensionLabel);
        RequestUtil.requestBestSelling(mStoreCode, mDimension, page, (BaseRxAppCompatActivity)activity, new RequestUtil.OnSuccessListener<SimplePageListEntity<GoodsSimpleVo>>() {
            @Override
            public void onSuccess(SimplePageListEntity<GoodsSimpleVo> response) {
                mPageSize = response.pageSize;
                mRecordCount = response.recordCount;
                mPageCount = response.pageCount;
                mGoodsList = response.list;

                pageContent.hasMore = page < mPageCount-1;
                for (int i = 0; i < mGoodsList.size(); i++) {
                    GoodsSimpleVo vo = mGoodsList.get(i);
                    pageContent.datas.add(vo);
                }

                Message message = handler.obtainMessage();
                message.what = what;
                Bundle messageBundle = message.getData();
                messageBundle.putSerializable(PAGE_CONTENT, pageContent);
                message.setData(messageBundle);
                handler.sendMessage(message);
            }
        });
    }

}
