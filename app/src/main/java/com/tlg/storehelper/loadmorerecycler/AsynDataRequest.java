package com.tlg.storehelper.loadmorerecycler;

import android.os.Bundle;
import android.os.Handler;

import java.io.Serializable;

/**
 * 异步数据请求接口
 */
public interface AsynDataRequest extends Serializable {

    public final String PAGE_CONTENT = "page_content";

    /**
     * 请求数据，通过消息机制返回结果。
     *
     * 实现步骤：
     * 1、通过mHandler发送(Message)message
     * 2、消息getData()，获取Bundle，Bundle包含成员(PageContent)pageContent

     Message message = handler.obtainMessage();
     message.what = 3;
     Bundle messageBundle = new Bundle();
     messageBundle.putInt("page", ++mPage);
     message.setData(messageBundle);
     handler.sendMessage(message);

     * @param page 页码
     * @param what 数据区分
     * @param handler 消息句柄
     * @param dataBundle 条件参数
     */
    public void fetchData(int page, int what, Handler handler, Bundle dataBundle);

}
