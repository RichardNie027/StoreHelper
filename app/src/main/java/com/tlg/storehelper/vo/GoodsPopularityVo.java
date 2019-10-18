package com.tlg.storehelper.vo;

import java.io.Serializable;

/**
 * 商品热度值
 */
public class GoodsPopularityVo implements Serializable {
    /**货号*/
    public String goodsNo;
    /**热度值*/
    public int popularity;

    public GoodsPopularityVo(String goodsNo, int popularity) {
        this.goodsNo = goodsNo;
        this.popularity = popularity;
    }
}
