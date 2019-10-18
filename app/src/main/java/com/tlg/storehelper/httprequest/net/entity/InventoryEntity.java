package com.tlg.storehelper.httprequest.net.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class InventoryEntity  implements Serializable {

    public List<DetailBean> detail = new ArrayList<>();

    /**ID，UUID*/
    public String id;
    /**店编*/
    public String storeCode;
    /**盘点日期*/
    public Date listDate;
    /**序号*/
    public int idx;
    /**创建用户*/
    public String username;
    /**盘点单号*/
    public String listNo;
    /**盘点单状态，I：初始；U：上传；L：锁定*/
    public String status;
    /**创建时间*/
    public Date createTime;
    /**修改时间*/
    public Date lastTime;

    public static class DetailBean {
        /**ID，UUID*/
        public String id;
        /**主表ID*/
        public String pid;
        /**序号*/
        public int idx;
        /**货架编码*/
        public String binCoding;
        /**商品条码*/
        public String barcode;
        /**数量*/
        public int quantity;
    }
}
