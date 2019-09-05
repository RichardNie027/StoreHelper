package com.tlg.storehelper.dao;

import java.io.Serializable;

public class GoodsBarcode implements Serializable {
    /**商品条码*/
    public String barcode;
    /**商品货号*/
    public String goodsNo;

    public GoodsBarcode() {

    }

    public GoodsBarcode(String barcode, String goodsNo) {
        this.barcode = barcode;
        this.goodsNo = goodsNo;
    }
}
