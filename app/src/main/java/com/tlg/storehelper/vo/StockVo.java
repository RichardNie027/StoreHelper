package com.tlg.storehelper.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class StockVo implements Serializable {
    /**尺码*/
    public String size;
    /**库存*/
    public int stock;
    /**销售*/
    public int sales;
    /**广域库存*/
    public int stocksAll;
    /**有库存店铺*/
    public List<String> storeList = new ArrayList<>();

    public StockVo(String size, int stock, int sales, int stocksAll) {
        this.size = size;
        this.stock = stock;
        this.sales = sales;
        this.stocksAll = stocksAll;
    }
}
