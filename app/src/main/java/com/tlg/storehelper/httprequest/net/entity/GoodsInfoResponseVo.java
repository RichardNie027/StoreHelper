package com.tlg.storehelper.httprequest.net.entity;

import com.nec.lib.android.httprequest.net.revert.BaseResponseVo;

import java.util.ArrayList;
import java.util.List;

public class GoodsInfoResponseVo extends BaseResponseVo {

    public String lastModDate;

    public List<String> goodsList = new ArrayList<>();

    public List<String> goodsBarcodeList = new ArrayList<>();

}