package com.tlg.storehelper.dao;

import java.io.Serializable;

public class GoodsBarcode implements Serializable {
    /**ID*/
    public String id;
    /**商品ID*/
    public String goods_id;
    /**尺码ID*/
    public String size_id;
    /**尺码显示*/
    public String size_desc;
    /**商品条码*/
    public String barcode;
}
