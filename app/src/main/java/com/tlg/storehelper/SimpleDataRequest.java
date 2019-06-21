package com.tlg.storehelper;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.tlg.storehelper.loadmorerecycler.AsynDataRequest;
import com.tlg.storehelper.loadmorerecycler.PageContent;

public class SimpleDataRequest implements AsynDataRequest {
    @Override
    public void fetchData(int page, int what, Handler mHandler) {
        Message message = mHandler.obtainMessage();
        message.what = what;
        Bundle bundle = message.getData();
        PageContent<DummyItemVo> pageContent = new PageContent<DummyItemVo>(page, 25);

        int perPage = 6;
        int maxPage = 5;
        pageContent.hasMore = page < maxPage-1;
        int start = page * perPage;
        int end = maxPage-1 <= page ? maxPage * perPage : start + perPage;
        for (int i = start; i < end; i++) {
            pageContent.datas.add(new DummyItemVo(i+"", "Text of id " + i, makeDetails(i)));
        }

        bundle.putSerializable(PAGE_CONTENT, pageContent);
        message.setData(bundle);
        mHandler.sendMessage(message);

    }


    private String makeDetails(int position) {
        StringBuilder builder = new StringBuilder();
        builder.append("Details about Item: ").append(position);
        int count = position % 3;
        for (int i = 0; i < count; i++) {
            builder.append("\nMore details information here.");
        }
        return builder.toString();
    }

}
