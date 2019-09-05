package com.tlg.storehelper.vo;

import java.io.Serializable;

/**
 * 用于显示盘点单货位统计记录
 */
public class InventoryTotalVo implements Serializable {
    /**货架编码*/
    public String binCoding;
    /**款码统计*/
    public int sizeQuantity;
    /**商品数量*/
    public int quantity;

    public InventoryTotalVo(String binCoding, int sizeQuantity, int quantity) {
        this.binCoding = binCoding;
        this.sizeQuantity = sizeQuantity;
        this.quantity = quantity;
    }
}
