package com.tlg.storehelper.vo;

import java.io.Serializable;

public class InventoryListVo implements Serializable {
    /**ID，UUID*/
    public String id;
    /**盘点单号*/
    public String list_no;
    /**数量*/
    public int quantity;

    public InventoryListVo(String id, String list_no, int quantity) {
        this.id = id;
        this.list_no = list_no;
        this.quantity = quantity;
    }
}
