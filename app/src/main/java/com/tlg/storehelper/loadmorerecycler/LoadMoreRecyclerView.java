package com.tlg.storehelper.loadmorerecycler;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tlg.storehelper.utils.ResourceUtil;
import com.tlg.storehelper.utils.UiUtil;

/* 加载更多list_footer_loading.xml 默认布局文件，与代码实现对等
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:background="@color/colorSecond"
    android:gravity="center"
    android:orientation="horizontal"
    android:padding="10dp" >
    <ProgressBar
        android:layout_width="25dp"
        android:layout_height="25dp" >
    </ProgressBar>
    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginLeft="10dp"
        android:textSize="14dp"
        android:textColor="@color/colorPrimaryLight"
        android:text="加载中" >
    </TextView>
</LinearLayout>
 */

/**
 * 支持上拉加载更多的RecyclerView
 */
public class LoadMoreRecyclerView extends RecyclerView {
    ///资源前提
    //Layout xml文件名
    /**加载更多的View布局，缺失则运行时按默认构建*/
    protected static String sLayoutOfFooterLoading = "list_footer_loading";

    /**
     * item 类型
     */
    public final static int TYPE_NORMAL = 0;    //item
    public final static int TYPE_HEADER = 1;    //头部--支持头部增加一个headerView
    public final static int TYPE_FOOTER = 2;    //底部--往往是loading_more
    public final static int TYPE_LIST = 3;      //代表item展示的模式是list模式
    public final static int TYPE_STAGGER = 4;   //代码item展示模式是网格模式

    private boolean mIsFooterEnable = false;    //是否允许加载更多

    /**
     * 自定义实现了头部和底部加载更多的adapter
     */
    private AutoLoadAdapter mAutoLoadAdapter;
    /**
     * 标记是否正在加载更多，防止再次调用加载更多接口
     */
    private boolean mIsLoadingMore;
    /**
     * 标记加载更多的position
     */
    private int mLoadMorePosition;
    /**
     * 加载更多的监听-业务需要实现加载数据
     */
    private LoadMoreListener mListener;

    public LoadMoreRecyclerView(Context context) {
        super(context);
        init();
    }

    public LoadMoreRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LoadMoreRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    /**
     * 初始化-添加滚动监听
     * <p/>
     * 回调加载更多方法，前提是
     * <pre>
     *    1、有监听并且支持加载更多：null != mListener && mIsFooterEnable
     *    2、目前没有在加载，正在上拉（dy>0），当前最后一条可见的view是否是当前数据列表的最后一条（加载更多）
     * </pre>
     */
    private void init() {
        super.addOnScrollListener(new OnScrollListener() {

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (null != mListener && mIsFooterEnable && !mIsLoadingMore && dy > 0) {
                    loadMoreIfPossible();
                }
            }
        });
    }

    /**
     * 如有必要，加载更多数据
     * @return 已触发加载
     */
    public void loadMoreIfPossible() {
        if (null != mListener && mIsFooterEnable && !mIsLoadingMore) {
            int lastVisiblePosition = getLastVisiblePosition();
            if (lastVisiblePosition + 1 == mAutoLoadAdapter.getItemCount()) {
                setLoadingMore(true);
                mLoadMorePosition = lastVisiblePosition;
                mListener.onLoadMore();
            }
        }
    }

    /**
     * 设置加载更多的监听
     *
     * @param listener
     */
    public void setLoadMoreListener(LoadMoreListener listener) {
        mListener = listener;
    }

    /**
     * 设置正在加载更多
     *
     * @param loadingMore
     */
    public void setLoadingMore(boolean loadingMore) {
        this.mIsLoadingMore = loadingMore;
    }

    /**
     * 加载更多监听
     */
    public interface LoadMoreListener {
        /**
         * 加载更多
         */
        void onLoadMore();
    }

    /**
     * RecyclerView的适配器，ViewHolder是Header、Footer、ListItem
     */
    public class AutoLoadAdapter extends Adapter<ViewHolder> {

        /**
         * 数据adapter
         */
        private RecyclerViewItemAdapter mInternalAdapter;

        private boolean mIsHeaderEnable;
        private int mHeaderResId;

        /**
         * 构造函数传入主体Item的适配器
         * @param adapter
         */
        public AutoLoadAdapter(RecyclerViewItemAdapter adapter) {
            mInternalAdapter = adapter;
            mIsHeaderEnable = false;
        }

        public RecyclerViewItemAdapter getInternalAdapter() {
            return mInternalAdapter;
        }

        @Override
        public int getItemViewType(int position) {
            int headerPosition = 0;
            int footerPosition = getItemCount() - 1;

            if (headerPosition == position && mIsHeaderEnable && mHeaderResId > 0) {
                return TYPE_HEADER;
            }
            if (footerPosition == position && mIsFooterEnable) {
                return TYPE_FOOTER;
            }
            /**
             * 这么做保证layoutManager切换之后能及时的刷新上对的布局
             */
            if (getLayoutManager() instanceof StrongLinearLayoutManager) {
                return TYPE_LIST;
            } else if (getLayoutManager() instanceof StaggeredGridLayoutManager) {
                return TYPE_STAGGER;
            } else {
                return TYPE_NORMAL;
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == TYPE_HEADER) {
                return new HeaderViewHolder(LayoutInflater.from(parent.getContext()).inflate(mHeaderResId, parent, false));
            }
            if (viewType == TYPE_FOOTER) {
                int footerResId = ResourceUtil.getLayoutId(sLayoutOfFooterLoading, parent.getContext());
                View footerView = null;
                if(footerResId != 0) {
                    footerView = LayoutInflater.from(parent.getContext()).inflate(footerResId , parent, false);
                } else {
                    footerView = loadDefaultFooterLayout(parent);
                }
                return new FooterViewHolder(footerView);
            } else { // type normal
                return mInternalAdapter.onCreateViewHolder(parent, viewType);
            }
        }

        /**加载默认的FooterBar*/
        private View loadDefaultFooterLayout(ViewGroup parent) {
            LinearLayout mLinearLayout = new LinearLayout(parent.getContext());
            mLinearLayout.setBackgroundColor(ResourceUtil.getColor("colorSecond", parent.getContext()));
            mLinearLayout.setGravity(Gravity.CENTER);
            mLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
            int mPadding = UiUtil.dip2px(parent.getContext(),10);
            mLinearLayout.setPadding(mPadding, mPadding, mPadding, mPadding);
            LinearLayout.LayoutParams mLayoutParams1 = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            mLinearLayout.setLayoutParams(mLayoutParams1);
            parent.addView(mLinearLayout);

            ProgressBar progressBar = new ProgressBar(parent.getContext());
            int mProgressBarSize = UiUtil.dip2px(parent.getContext(),25);
            LinearLayout.LayoutParams mLayoutParams2 = new LinearLayout.LayoutParams(mProgressBarSize, mProgressBarSize);
            progressBar.setLayoutParams(mLayoutParams2);
            mLinearLayout.addView(progressBar);

            TextView textView = new TextView(parent.getContext());
            textView.setTextColor(ResourceUtil.getColor("colorPrimaryLight", parent.getContext()));
            textView.setTextSize(14);
            textView.setText("加载中");
            LinearLayout.LayoutParams mLayoutParams3 = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            mLayoutParams3.setMarginStart(UiUtil.dip2px(parent.getContext(),10));
            textView.setLayoutParams(mLayoutParams3);
            mLinearLayout.addView(textView);
            return mLinearLayout;
        }

        public class FooterViewHolder extends ViewHolder {

            public FooterViewHolder(View itemView) {
                super(itemView);
            }
        }

        public class HeaderViewHolder extends ViewHolder {
            public HeaderViewHolder(View itemView) {
                super(itemView);
            }
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            int type = getItemViewType(position);
            if (type != TYPE_FOOTER && type != TYPE_HEADER) {
                mInternalAdapter.onBindViewHolder(holder, position);
            }
        }

        /**
         * 需要计算上加载更多和添加的头部俩个
         *
         * @return
         */
        @Override
        public int getItemCount() {
            int count = mInternalAdapter.getItemCount();
            if (mIsFooterEnable) count++;
            if (mIsHeaderEnable) count++;

            return count;
        }

        public void setHeaderEnable(boolean enable) {
            mIsHeaderEnable = enable;
        }

        public boolean getHeaderEnable() {
            return mIsHeaderEnable;
        }

        public void addHeaderView(int resId) {
            mHeaderResId = resId;
        }
    }

    @Override
    public void setAdapter(Adapter adapter) {
        if (adapter != null) {
            mAutoLoadAdapter = new AutoLoadAdapter((RecyclerViewItemAdapter)adapter);
        }
        super.swapAdapter(mAutoLoadAdapter, true);
    }

    /**
     * 切换layoutManager
     *
     * 为了保证切换之后页面上还是停留在当前展示的位置，记录下切换之前的第一条展示位置，切换完成之后滚动到该位置
     * 另外切换之后必须要重新刷新下当前已经缓存的itemView，否则会出现布局错乱（俩种模式下的item布局不同），
     * RecyclerView提供了swapAdapter来进行切换adapter并清理老的itemView cache
     *
     * @param layoutManager
     */
    public void switchLayoutManager(LayoutManager layoutManager) {
        int firstVisiblePosition = getFirstVisiblePosition();
        //getLayoutManager().removeAllViews();
        setLayoutManager(layoutManager);
        //super.swapAdapter(mAutoLoadAdapter, true);
        getLayoutManager().scrollToPosition(firstVisiblePosition);
    }

    /**
     * 获取第一条展示的位置
     *
     * @return
     */
    private int getFirstVisiblePosition() {
        int position;
        if (getLayoutManager() instanceof StrongLinearLayoutManager) {
            position = ((StrongLinearLayoutManager) getLayoutManager()).findFirstVisibleItemPosition();
        } else if (getLayoutManager() instanceof GridLayoutManager) {
            position = ((GridLayoutManager) getLayoutManager()).findFirstVisibleItemPosition();
        } else if (getLayoutManager() instanceof StaggeredGridLayoutManager) {
            StaggeredGridLayoutManager layoutManager = (StaggeredGridLayoutManager) getLayoutManager();
            int[] lastPositions = layoutManager.findFirstVisibleItemPositions(new int[layoutManager.getSpanCount()]);
            position = getMinPositions(lastPositions);
        } else {
            position = 0;
        }
        return position;
    }

    /**
     * 获得当前展示最小的position
     *
     * @param positions
     * @return
     */
    private int getMinPositions(int[] positions) {
        int size = positions.length;
        int minPosition = Integer.MAX_VALUE;
        for (int i = 0; i < size; i++) {
            minPosition = Math.min(minPosition, positions[i]);
        }
        return minPosition;
    }

    /**
     * 获取最后一条展示的位置
     *
     * @return
     */
    private int getLastVisiblePosition() {
        int position;
        LayoutManager layoutManager = getLayoutManager();
        if (layoutManager instanceof StrongLinearLayoutManager) {
            position = ((StrongLinearLayoutManager) layoutManager).findLastVisibleItemPosition();
        } else if (layoutManager instanceof GridLayoutManager) {
            position = ((GridLayoutManager) layoutManager).findLastVisibleItemPosition();
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            StaggeredGridLayoutManager _layoutManager = (StaggeredGridLayoutManager) layoutManager;
            int[] lastPositions = _layoutManager.findLastVisibleItemPositions(new int[_layoutManager.getSpanCount()]);
            position = getMaxPosition(lastPositions);
        } else {
            position = layoutManager.getItemCount() - 1;
        }
        return position;
    }

    /**
     * 获得最大的位置
     *
     * @param positions
     * @return
     */
    private int getMaxPosition(int[] positions) {
        int size = positions.length;
        int maxPosition = Integer.MIN_VALUE;
        for (int i = 0; i < size; i++) {
            maxPosition = Math.max(maxPosition, positions[i]);
        }
        return maxPosition;
    }

    /**
     * 添加头部view
     *
     * @param resId
     */
    public void addHeaderView(int resId) {
        mAutoLoadAdapter.addHeaderView(resId);
    }

    /**
     * 设置头部view是否展示
     * @param enable
     */
    public void setHeaderEnable(boolean enable) {
        mAutoLoadAdapter.setHeaderEnable(enable);
    }

    /**
     * 设置是否支持自动加载更多
     *
     * @param autoLoadMore
     */
    public void setAutoLoadMoreEnable(boolean autoLoadMore) {
        mIsFooterEnable = autoLoadMore;
    }

    /**
     * 通知更多的数据已经加载
     *
     * 每次加载完成之后添加了Data数据，用notifyItemRemoved来刷新列表展示，
     * 而不是用notifyDataSetChanged来刷新列表
     *
     * @param hasMore
     */
    public void notifyMoreFinish(boolean hasMore) {
        setAutoLoadMoreEnable(hasMore);
        setLoadingMore(false);
        if(!hasMore) {
            getAdapter().notifyItemRemoved(mLoadMorePosition);
        }
        int itemCount = mAutoLoadAdapter.getInternalAdapter().getDataSize() - mLoadMorePosition;
        if (mIsFooterEnable) itemCount++;
        if (mAutoLoadAdapter.getHeaderEnable()) itemCount++;
        getAdapter().notifyItemRangeChanged(mLoadMorePosition, itemCount);
    }
}