package com.tlg.storehelper.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ShopHistoryVo implements Serializable {
    /**日期 yyyy-MM-dd*/
    public String shopDate;
    /**销售单号*/
    public String salesListCode;
    /**件数*/
    public int quantity;
    /**金额*/
    public int amount;
    /**购物明细*/
    public List<ShopHistoryItemVo> shopHistoryItemList = new ArrayList<>();

}
