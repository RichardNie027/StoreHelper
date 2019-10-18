package com.tlg.storehelper.httprequest.net.entity;

import com.nec.lib.android.httprequest.net.revert.BaseResponseVo;

import java.util.ArrayList;
import java.util.List;

public class CollocationResponseVo extends BaseResponseVo {

    public List<DetailBean> detail = new ArrayList<>();

    /**商品编号*/
    public String goodsNo;
    /**商品名称*/
    public String goodsName;
    /**吊牌价*/
    public int price;
    /**商品图片*/
    public String pic;

    public static class DetailBean {
        /**商品编号*/
        public String goodsNo;
        /**信息*/
        public String info;
        /**连带次数*/
        public int frequency;
        /**商品图片*/
        public String pic;
    }
}
