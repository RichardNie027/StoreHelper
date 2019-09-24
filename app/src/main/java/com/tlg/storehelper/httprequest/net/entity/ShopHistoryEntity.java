package com.tlg.storehelper.httprequest.net.entity;

import com.nec.lib.android.httprequest.net.revert.BaseResponseEntity;

public class ShopHistoryEntity extends BaseResponseEntity {
    /**卡号*/
    public String membershipCardId;
    /**姓名*/
    public String name;
    /**手机号*/
    public String mobile;
    /**本年消费额*/
    public int yearExpenditure;
    /**总消费额*/
    public int totalExpenditure;

}
