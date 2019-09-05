package com.tlg.storehelper.vo;

import java.io.Serializable;

/**
 * 用于显示盘点单明细记录
 */
public class InventoryDetailVo implements Serializable {
    /**ID，UUID*/
    public String id;
    /**序号*/
    public int idx;
    /**货架编码*/
    public String binCoding;
    /**商品条码*/
    public String barcode;
    /**数量*/
    public int quantity;

    public InventoryDetailVo(String id, int idx, String binCoding, String barcode, int quantity) {
        this.id = id;
        this.idx = idx;
        this.binCoding = binCoding;
        this.barcode = barcode;
        this.quantity = quantity;
    }
}
