package com.tlg.storehelper.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GoodsPsiVo implements Serializable {
    /**商品货号*/
    public String goodsNo;
    /**商品名称*/
    public String goodsName;
    /**商品款号*/
    public String styleNo;
    /**商品颜色值*/
    public String colorDesc;
    /**各尺码进销存*/
    public List<GoodsSizePsiVo> goodsSizePsiList = new ArrayList<>();

    public GoodsPsiVo(String goodsNo, String goodsName, String styleNo, String colorDesc) {
        this.goodsNo = goodsNo;
        this.goodsName = goodsName;
        this.styleNo = styleNo;
        this.colorDesc = colorDesc;
    }
}
