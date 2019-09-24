package com.tlg.storehelper.activity.membership;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.nec.lib.android.base.BaseRxAppCompatActivity;
import com.nec.lib.android.loadmoreview.AsynDataRequest;
import com.nec.lib.android.loadmoreview.PageContent;
import com.tlg.storehelper.httprequest.net.entity.SimpleListPageEntity;
import com.tlg.storehelper.httprequest.utils.RequestUtil;
import com.tlg.storehelper.vo.ShopHistoryDetailVo;

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
    private int mRecordPerPage;
    private int mPage;
    private int mRecordCount;
    private int mPageCount;

    private List<ShopHistoryDetailVo> mList = new ArrayList<ShopHistoryDetailVo>();

    @Override
    public void fetchData(int page, int what, Handler handler, Bundle dataBundle, Activity activity) {
        this.mRecordPerPage = 10;   // by server side
        this.mPage = page;
        PageContent<ShopHistoryDetailVo> pageContent = new PageContent<ShopHistoryDetailVo>(page, mRecordPerPage);

        mMembershipId = dataBundle.getString("membership_id");
        mStoreCode = dataBundle.getString("store_code");
        RequestUtil.requestMembershipShopHistoryDeltail(mMembershipId, mStoreCode, page, (BaseRxAppCompatActivity)activity, new RequestUtil.OnSuccessListener<SimpleListPageEntity<ShopHistoryDetailVo>>() {
            @Override
            public void onSuccess(SimpleListPageEntity<ShopHistoryDetailVo> response) {
                mRecordPerPage = response.recordPerPage;
                mRecordCount = response.recordCount;
                mPageCount = response.pageCount;
                mList = response.result;

                pageContent.hasMore = page < mPageCount-1;
                for (int i = 0; i < mList.size(); i++) {
                    ShopHistoryDetailVo vo = mList.get(i);
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
