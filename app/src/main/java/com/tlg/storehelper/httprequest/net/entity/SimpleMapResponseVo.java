package com.tlg.storehelper.httprequest.net.entity;

import com.nec.lib.android.httprequest.net.revert.BaseResponseVo;

import java.util.LinkedHashMap;

public class SimpleMapResponseVo extends BaseResponseVo {

    public LinkedHashMap<String, Object> map = new LinkedHashMap<>();

}
