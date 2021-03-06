package com.tlg.storehelper.vo;

import java.io.Serializable;

public class StatisticInfo implements Serializable {
    public String id;
    public String listNo;
    public int quantity;
    public int totalQuantity;
    public String lastBarcode;
    public String lastBinCoding;

    public StatisticInfo() {
    }

    public StatisticInfo(String id, String listNo, int quantity, int totalQuantity, String lastBarcode, String lastBinCoding) {
        this.id = id;
        this.listNo = listNo;
        this.quantity = quantity;
        this.totalQuantity = totalQuantity;
        this.lastBarcode = lastBarcode;
        this.lastBinCoding = lastBinCoding;
    }
}
