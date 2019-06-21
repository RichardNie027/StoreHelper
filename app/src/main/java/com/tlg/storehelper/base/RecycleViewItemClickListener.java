package com.tlg.storehelper.base;

import android.view.View;

/**
 * RecycleView的Item的Click事件监听器接口
 */
public interface RecycleViewItemClickListener {
    public void onItemClick(View view, int postion);
    public boolean onItemLongClick(View view, int postion);
}
