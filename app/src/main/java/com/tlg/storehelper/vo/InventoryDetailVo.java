package com.tlg.storehelper.vo;

import java.io.Serializable;

/**
 * 用于显示盘点单明细记录
 */
public class InventoryDetailVo implements Serializable {
    /**ID，自增*/
    public long id;
    /**序号*/
    public int idx;
    /**货架编码*/
    public String bin_coding;
    /**商品条码*/
    public String barcode;
    /**数量*/
    public int quantity;

    public InventoryDetailVo(long id, int idx, String bin_coding, String barcode, int quantity) {
        this.id = id;
        this.idx = idx;
        this.bin_coding = bin_coding;
        this.barcode = barcode;
        this.quantity = quantity;
    }
}
