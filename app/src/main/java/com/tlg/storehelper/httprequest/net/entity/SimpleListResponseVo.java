package com.tlg.storehelper.httprequest.net.entity;

import com.nec.lib.android.httprequest.net.revert.BaseResponseVo;

import java.util.ArrayList;
import java.util.List;

public class SimpleListResponseVo<T> extends BaseResponseVo {

    public List<T> list = new ArrayList<>();

}
