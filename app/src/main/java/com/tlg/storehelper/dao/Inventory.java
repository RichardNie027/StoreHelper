package com.tlg.storehelper.dao;

import com.nec.lib.android.utils.StringUtil;

import java.io.Serializable;
import java.util.Date;

public class Inventory implements Serializable {
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

    public Inventory() {
        id = StringUtil.getUUID();
        this.listNo = "I";
    }
    public Inventory(String id, String storeCode, Date listDate, int idx, String username, String listNo, String status, Date createTime, Date lastTime) {
        if(id==null)
            this.id = StringUtil.getUUID();
        else
            this.id = id;
        this.storeCode = storeCode;
        this.listDate = listDate;
        this.idx = idx;
        this.username = username;
        this.listNo = listNo;
        this.status = status != null && status.equalsIgnoreCase("U") ? "U" : "I";
        this.createTime = createTime;
        this.lastTime = lastTime;
    }
}
