package com.tlg.storehelper.dao;

import com.nec.lib.android.utils.StringUtil;

import java.io.Serializable;
import java.util.Date;

public class Inventory implements Serializable {
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

    public Inventory() {
        id = StringUtil.getUUID();
    }
    public Inventory(String id, String store_code, Date list_date, int idx, String username, String list_no, Date create_time, Date last_time) {
        if(id==null)
            this.id = StringUtil.getUUID();
        else
            this.id = id;
        this.store_code = store_code;
        this.list_date = list_date;
        this.idx = idx;
        this.username = username;
        this.list_no = list_no;
        this.create_time = create_time;
        this.last_time = last_time;
    }
}
