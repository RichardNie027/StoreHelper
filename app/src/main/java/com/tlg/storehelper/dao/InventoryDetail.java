package com.tlg.storehelper.dao;

import java.util.Date;

public class InventoryDetail {
    /**ID，自增*/
    public long id;
    /**主表ID*/
    public long pid;
    /**货架编码*/
    public String bin_coding;
    /**商品条码*/
    public String barcode;
    /**数量*/
    public int quantity;

    public InventoryDetail() {
    }

    public InventoryDetail(long id, long pid, String bin_coding, String barcode, int quantity) {
        this.id = id;
        this.pid = pid;
        this.bin_coding = bin_coding;
        this.barcode = barcode;
        this.quantity = quantity;
    }
}
