package com.tlg.storehelper.dao;

import java.io.Serializable;

public class Goods implements Serializable {
    /**商品货号*/
    public String goodsNo;
    /**商品款码*/
    public String styleNo;
    /**商品颜色值*/
    public String colorDesc;
    /**商品名称*/
    public String goodsName;
    /**商品吊牌价*/
    public int price;
    /**商品品牌*/
    public String brand;
    /**商品对应的店编首集合*/
    public String storeHeaders;
    /**创建日期 yyyy-MM-dd*/
    public String createTime;

    public Goods() {

    }

    public Goods(String goodsNo, String styleNo, String colorDesc, String goodsName, int price, String brand, String storeHeaders, String createTime) {
        this.goodsNo = goodsNo;
        this.styleNo = styleNo;
        this.colorDesc = colorDesc;
        this.goodsName = goodsName;
        this.price = price;
        this.brand = brand;
        this.storeHeaders = storeHeaders;
        this.createTime = createTime;
    }
}
