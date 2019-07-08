package com.tlg.storehelper.stickheaderview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.nec.utils.UiUtil;

/**
 * 自定义装饰器（实现分组+吸顶效果）
 */
public class StickHeaderDecoration extends RecyclerView.ItemDecoration {

    //头部的高
    private int mItemHeaderHeight;
    private int mTextPaddingLeft;

    //画笔，绘制头部和分割线
    private Paint mItemHeaderPaint;
    private Paint mTextPaint;
    private Paint mLinePaint;

    private Rect mTextRect;

    public StickHeaderDecoration(Context context, int headerHeight, int textPaddingLeft, int textSizeinSp, int textColor, int backgroundColor, int dividerColor) {
        mItemHeaderHeight = UiUtil.dp2px(context, headerHeight);
        mTextPaddingLeft = UiUtil.dp2px(context, textPaddingLeft);

        mTextRect = new Rect();

        mItemHeaderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mItemHeaderPaint.setColor(backgroundColor);

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextSize(UiUtil.sp2px(textSizeinSp));
        mTextPaint.setColor(textColor);

        mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinePaint.setColor(dividerColor);

    }

    /**
     * 绘制Item的分割线和组头
     *
     * @param c
     * @param parent
     * @param state
     */
    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        if (parent.getAdapter() instanceof StickHeaderRecyclerViewAdapter) {
            StickHeaderRecyclerViewAdapter adapter = (StickHeaderRecyclerViewAdapter) parent.getAdapter();
            int count = parent.getChildCount();//获取可见范围内Item的总数
            for (int i = 0; i < count; i++) {
                View view = parent.getChildAt(i);
                int position = parent.getChildLayoutPosition(view);
                boolean isHeader = adapter.isItemHeader(position);
                int left = parent.getPaddingLeft();
                int right = parent.getWidth() - parent.getPaddingRight();
                if (isHeader) {
                    c.drawRect(left, view.getTop() - mItemHeaderHeight, right, view.getTop(), mItemHeaderPaint);
                    mTextPaint.getTextBounds(adapter.getGroupName(position), 0, adapter.getGroupName(position).length(), mTextRect);
                    c.drawText(adapter.getGroupName(position), left + mTextPaddingLeft, (view.getTop() - mItemHeaderHeight) + mItemHeaderHeight / 2 + mTextRect.height() / 2, mTextPaint);
                } else {
                    c.drawRect(left, view.getTop() - 1, right, view.getTop(), mLinePaint);
                }
            }
        }
    }

    /**
     * 绘制Item的顶部布局（吸顶效果）
     *
     * @param c
     * @param parent
     * @param state
     */
    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        if (parent.getAdapter() instanceof StickHeaderRecyclerViewAdapter) {
            StickHeaderRecyclerViewAdapter adapter = (StickHeaderRecyclerViewAdapter) parent.getAdapter();
            int position = ((LinearLayoutManager) (parent.getLayoutManager())).findFirstVisibleItemPosition();
            View view = parent.findViewHolderForAdapterPosition(position).itemView;
            boolean isHeader = adapter.isItemHeader(position + 1);
            int top = parent.getPaddingTop();
            int left = parent.getPaddingLeft();
            int right = parent.getWidth() - parent.getPaddingRight();
            if (isHeader) {
                int bottom = Math.min(mItemHeaderHeight, view.getBottom());
                c.drawRect(left, top + view.getTop() - mItemHeaderHeight, right, top + bottom, mItemHeaderPaint);
                mTextPaint.getTextBounds(adapter.getGroupName(position), 0, adapter.getGroupName(position).length(), mTextRect);
                c.drawText(adapter.getGroupName(position), left + mTextPaddingLeft, top + mItemHeaderHeight / 2 + mTextRect.height() / 2 - (mItemHeaderHeight - bottom), mTextPaint);
            } else {
                c.drawRect(left, top, right, top + mItemHeaderHeight, mItemHeaderPaint);
                mTextPaint.getTextBounds(adapter.getGroupName(position), 0, adapter.getGroupName(position).length(), mTextRect);
                c.drawText(adapter.getGroupName(position), left + mTextPaddingLeft, top + mItemHeaderHeight / 2 + mTextRect.height() / 2, mTextPaint);
            }
            c.save();
        }
    }

    /**
     * 设置Item的间距
     *
     * @param outRect
     * @param view
     * @param parent
     * @param state
     */
    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        if (parent.getAdapter() instanceof StickHeaderRecyclerViewAdapter) {
            StickHeaderRecyclerViewAdapter adapter = (StickHeaderRecyclerViewAdapter) parent.getAdapter();
            int position = parent.getChildLayoutPosition(view);
            boolean isHeader = adapter.isItemHeader(position);
            if (isHeader) {
                outRect.top = mItemHeaderHeight;
            } else {
                outRect.top = 1;
            }
        }
    }

}
