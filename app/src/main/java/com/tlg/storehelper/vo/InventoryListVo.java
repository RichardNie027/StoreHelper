package com.tlg.storehelper.vo;

import java.io.Serializable;

public class InventoryListVo implements Serializable {
    /**ID，自增*/
    public long id;
    /**盘点单号*/
    public String list_no;
    /**数量*/
    public int quantity;

    public InventoryListVo(long id, String list_no, int quantity) {
        this.id = id;
        this.list_no = list_no;
        this.quantity = quantity;
    }
}
