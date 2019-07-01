package com.tlg.storehelper.vo;

import java.io.Serializable;

/**
 * 用于显示盘点单货位统计记录
 */
public class InventoryTotalVo implements Serializable {
    /**货架编码*/
    public String bin_coding;
    /**款码统计*/
    public int sizeQuantity;
    /**商品数量*/
    public int quantity;

    public InventoryTotalVo(String bin_coding, int sizeQuantity, int quantity) {
        this.bin_coding = bin_coding;
        this.sizeQuantity = sizeQuantity;
        this.quantity = quantity;
    }
}
