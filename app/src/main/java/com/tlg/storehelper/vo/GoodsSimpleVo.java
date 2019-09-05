package com.tlg.storehelper.vo;

import java.io.Serializable;

/**
 * 用于显示带图片的商品基本信息
 */
public class GoodsSimpleVo implements Serializable {
    /**货号*/
    public String goodsNo;
    /**名称*/
    public String goodsName;
    /**价格*/
    public int price;
    /**其它信息*/
    public String info;
    /**商品图片*/
    public String pic;

    public GoodsSimpleVo(String goodsNo, String goodsName, int price, String info, String pic) {
        this.goodsNo = goodsNo;
        this.goodsName = goodsName;
        this.price = price;
        this.info = info;
        this.pic = pic;
    }
}
