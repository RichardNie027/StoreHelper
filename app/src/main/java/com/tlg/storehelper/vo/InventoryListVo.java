package com.tlg.storehelper.vo;

import java.io.Serializable;

public class InventoryListVo implements Serializable {
    /**ID，UUID*/
    public String id;
    /**盘点单号*/
    public String listNo;
    /**数量*/
    public int quantity;

    public InventoryListVo(String id, String listNo, int quantity) {
        this.id = id;
        this.listNo = listNo;
        this.quantity = quantity;
    }
}
