package com.tlg.storehelper.vo;

import java.io.Serializable;

/**
 * 用于显示盘点单货位复盘汇总结果
 */
public class InventoryRedoVo implements Serializable {
    /**货架编码*/
    public String barcode;
    /**原数量*/
    public int quantity1;
    /**已完数量*/
    public int quantity2;
    /**未完数量*/
    public int quantity3;

    public InventoryRedoVo(String barcode, int quantity1, int quantity2, int quantity3) {
        this.barcode = barcode;
        this.quantity1 = quantity1;
        this.quantity2 = quantity2;
        this.quantity3 = quantity3;
    }
}
