package com.tlg.storehelper.dao;

import com.nec.lib.android.utils.StringUtil;

import java.io.Serializable;

public class InventoryDetail implements Serializable {
    /**ID，UUID*/
    public String id;
    /**主表ID*/
    public String pid;
    /**序号*/
    public int idx;
    /**货架编码*/
    public String binCoding;
    /**商品条码*/
    public String barcode;
    /**数量*/
    public int quantity;

    public InventoryDetail() {
        id = StringUtil.getUUID();
    }

    public InventoryDetail(String id, String pid, int idx, String binCoding, String barcode, int quantity) {
        if(id==null)
            this.id = StringUtil.getUUID();
        else
            this.id = id;
        this.pid = pid;
        this.idx = idx;
        this.binCoding = binCoding;
        this.barcode = barcode;
        this.quantity = quantity;
    }
}
