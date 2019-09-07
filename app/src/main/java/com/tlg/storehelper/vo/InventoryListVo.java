package com.tlg.storehelper.vo;

import java.io.Serializable;

public class InventoryListVo implements Serializable {
    /**ID，UUID*/
    public String id;
    /**盘点单号*/
    public String listNo;
    /**盘点状态*/
    public String status;
    /**数量*/
    public int quantity;

    public InventoryListVo(String id, String listNo, String status, int quantity) {
        this.id = id;
        this.listNo = listNo;
        this.status = status;
        this.quantity = quantity;
    }
}
