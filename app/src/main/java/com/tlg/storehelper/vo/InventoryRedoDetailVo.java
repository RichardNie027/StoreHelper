package com.tlg.storehelper.vo;

import java.io.Serializable;

/**
 * 用于记录盘点单货位复盘明细记录
 */
public class InventoryRedoDetailVo implements Serializable {
    /**序号*/
    public int idx;
    /**商品条码*/
    public String barcode;

    public InventoryRedoDetailVo(int idx, String barcode) {
        this.idx = idx;
        this.barcode = barcode;
    }

}
