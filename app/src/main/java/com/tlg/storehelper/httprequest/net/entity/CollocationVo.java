package com.tlg.storehelper.httprequest.net.entity;

import com.nec.lib.android.httprequest.net.revert.BaseResponseVo;
import com.tlg.storehelper.vo.GoodsSimpleVo;

import java.util.ArrayList;
import java.util.List;

public class CollocationVo extends BaseResponseVo {

    /**商品*/
    public GoodsSimpleVo goods;

    /**连带商品*/
    public List<GoodsSimpleVo> detail = new ArrayList<>();

}
