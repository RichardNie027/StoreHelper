package com.tlg.storehelper.httprequest.net.entity;

import com.nec.lib.android.httprequest.net.revert.BaseResponseEntity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class InventoryEntity  implements Serializable {

    public List<DetailBean> detail = new ArrayList<>();

    /**ID，UUID*/
    public String id;
    /**店编*/
    public String store_code;
    /**盘点日期*/
    public Date list_date;
    /**序号*/
    public int idx;
    /**创建用户*/
    public String username;
    /**盘点单号*/
    public String list_no;
    /**创建时间*/
    public Date create_time;
    /**修改时间*/
    public Date last_time;

    public static class DetailBean {
        /**ID，UUID*/
        public String id;
        /**主表ID*/
        public String pid;
        /**序号*/
        public int idx;
        /**货架编码*/
        public String bin_coding;
        /**商品条码*/
        public String barcode;
        /**数量*/
        public int quantity;
    }
}
