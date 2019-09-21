package com.tlg.storehelper.httprequest.net.entity;

import com.nec.lib.android.httprequest.net.revert.BaseResponseEntity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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
    /**购物明细*/
    public List<ShopVo> shopList = new ArrayList<>();

    public static class ShopVo implements Serializable {
        /**日期 yyyy-MM-dd*/
        public String shopDate;
        /**销售单号*/
        public String SalesListCode;
        /**件数*/
        public int quantity;
        /**金额*/
        public int amount;
        /**购物明细*/
        public List<ShopItemVo> shopItemList = new ArrayList<>();
    }

    public static class ShopItemVo implements Serializable {
        /**货号*/
        public String goodsNo;
        /**色尺*/
        public String size;
        /**货名*/
        public String goodsName;
        /**数量*/
        public int quantity;
        /**牌价*/
        public int price;
        /**折扣*/
        public float discount;
        /**结算价*/
        public int realPrice;
        /**金额*/
        public int amount;
        /**导购员*/
        public String sales;
    }
}
