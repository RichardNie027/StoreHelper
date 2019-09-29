package com.tlg.storehelper.activity.membership;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.nec.lib.android.base.BaseRxAppCompatActivity;
import com.nec.lib.android.loadmoreview.AsynDataRequest;
import com.nec.lib.android.loadmoreview.PageContent;
import com.tlg.storehelper.httprequest.net.entity.SimplePageListEntity;
import com.tlg.storehelper.httprequest.utils.RequestUtil;
import com.tlg.storehelper.vo.ShopHistoryVo;

import java.util.ArrayList;
import java.util.List;

/**
 * 消费记录的数据请求
 *
 */
public class MembershipListDataRequest implements AsynDataRequest {

    private String mStoreCode;
    private String mMembershipId;

    //分页属性
    private int mPageSize;
    private int mPage;
    private int mRecordCount;
    private int mPageCount;

    private List<ShopHistoryVo> mList = new ArrayList<ShopHistoryVo>();

    @Override
    public void fetchData(int page, int what, Handler handler, Bundle dataBundle, Activity activity) {
        this.mPageSize = 10;   // by server side
        this.mPage = page;
        PageContent<ShopHistoryVo> pageContent = new PageContent<ShopHistoryVo>(page, mPageSize);

        mMembershipId = dataBundle.getString("membership_id");
        mStoreCode = dataBundle.getString("store_code");
        RequestUtil.requestMembershipShopHistory(mMembershipId, mStoreCode, page, (BaseRxAppCompatActivity)activity, new RequestUtil.OnSuccessListener<SimplePageListEntity<ShopHistoryVo>>() {
            @Override
            public void onSuccess(SimplePageListEntity<ShopHistoryVo> response) {
                mPageSize = response.pageSize;
                mRecordCount = response.recordCount;
                mPageCount = response.pageCount;
                mList = response.list;

                pageContent.hasMore = page < mPageCount-1;
                for (int i = 0; i < mList.size(); i++) {
                    ShopHistoryVo vo = mList.get(i);
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
