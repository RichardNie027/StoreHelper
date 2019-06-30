package com.tlg.storehelper.loadmorerecycler;

import android.graphics.drawable.Drawable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tlg.storehelper.base.RecycleViewItemClickListener;
import com.tlg.storehelper.utils.ResourceUtil;
import com.tlg.storehelper.utils.UiUtil;

import java.lang.reflect.Constructor;
import java.util.List;

/* fragment_default_load_more_list_item_linear.xml 默认布局
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="30dp">

    <TextView
        android:id="@+id/id"
        android:layout_width="60dp"
        android:layout_height="match_parent"
        android:gravity="center|center_horizontal"
        android:textAppearance="?attr/textAppearanceListItem"
        android:textSize="14sp" />

    <TextView
        android:id="@+id/content"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:layout_marginLeft="10dp"
        android:gravity="center|start"
        android:textAppearance="?attr/textAppearanceListItem"
        android:textSize="14sp" />

</LinearLayout>
 */
/* fragment_default_load_more_list_item_stagger.xml 默认布局
<?xml version="1.0" encoding="utf-8"?>
<android.support.v7.widget.CardView xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:layout_margin="3dp"
    android:orientation="horizontal">

    <ImageView
        android:id="@+id/icon"
        android:layout_width="50dp"
        android:layout_height="50dp"
        android:src="@mipmap/ic_launcher" />

    <TextView
        android:id="@+id/content"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="50dp"/>
</android.support.v7.widget.CardView>
 */

/**
 * RecyclerView的Adapter适配器，子类必须：
 * 1、新建布局文件，文件名赋值给类变量mLayoutNameOfFragmentItemLinear/mLayoutNameOfFragmentItemStagger
 * 2、两个构造函数中调用：super();setViewHolderClass(this, MyLinearViewHolder.class, MyStaggeredViewHolder.class);
 * 3、重写：onBindViewHolder
 * 4、定义LinearViewHolder、StaggeredViewHolder的内部子类。
 *
 * 子类若以无参构造函数创建实例，需马上调用：setData();
 *
 * @param <T> 数据集合的元素类
 */
public abstract class RecyclerViewItemAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    ////////////////资源前提 START////////////////
    //列表Fragmeng的行项目Layout xml文件名
    /**Linear布局xml文件*/
    protected String mLayoutNameOfFragmentItemLinear = "fragment_default_load_more_list_item_linear";
    /**Staggered布局xml文件*/
    protected String mLayoutNameOfFragmentItemStagger = "fragment_default_load_more_list_item_stagger";
    ////////////////资源前提 END////////////////

    /**Item的数据集*/
    protected List<T> mValues;
    /**显示模式*/
    protected LoadMoreFragment.DisplayMode mDisplayMode;
    /**Stagger模式的列数*/
    protected int mColumnCount = 1;

    /**Item是否可以点击，子类构造函数里或外部尽早设置*/
    public boolean itemClickable = true;
    /**Item是否可以长按点击，可点击是长按的前提，子类构造函数里或外部尽早设置*/
    public boolean itemLongClickable = true;
    /**Item点击可视化，默认打开，子类构造函数里或外部尽早设置*/
    public boolean itemClickVisible = true;

    /**ItemRecyclerViewAdapter之类的实例，用于调取内部类*/
    private RecyclerViewItemAdapter mViewHolderOuter;
    /**行显示的ViewHolder类*/
    private Class mLinearViewHolderClass;
    /**瀑布显示的ViewHolder类*/
    private Class mStaggeredViewHolderClass;
    /**Item的Click监听器的代理，通过 setOnItemClickListener 方法设置*/
    protected RecycleViewItemClickListener mClickListenerAgent = null;

    /**
     * 构造函数，之类中必须调用setViewHolderClass函数：
     * super();
     * setViewHolderClass(this, MyLinearViewHolder.class, MyStaggeredViewHolder.class);
     */
    public RecyclerViewItemAdapter() {
        super();
    }

    /**
     * 构造函数，之类中必须调用setViewHolderClass函数：
     * super(items);
     * setViewHolderClass(this, MyLinearViewHolder.class, MyStaggeredViewHolder.class);
     */
    public RecyclerViewItemAdapter(List<T> items) {
        mValues = items;
    }

    /**
     * 设置Item点击监听器的代理
     * @param listenerAgent
     */
    public void setOnItemClickListenerAgent(RecycleViewItemClickListener listenerAgent) {
        this.mClickListenerAgent = listenerAgent;
    }

    /**
     * 设置ViewHolder的类，在构造实例后紧接调用！
     * demo in constructure of subclass:
     * setViewHolderClass(this, MyLinearViewHolder.class, MyStaggeredViewHolder.class);
     *
     * @param outerInstance ItemRecyclerViewAdapter之类的实例，用于调取内部类
     * @param clazz_linear 行显示的ViewHolder类
     * @param clazz_stagger 瀑布显示的ViewHolder类
     * @param <T_Linear> RecyclerViewItemAdapter.LinearViewHolder子类
     * @param <T_Stagger> RecyclerViewItemAdapter.StaggeredViewHolder子类
     */
    public <T_Linear extends LinearViewHolder, T_Stagger extends StaggeredViewHolder> void setViewHolderClass(RecyclerViewItemAdapter outerInstance, Class<T_Linear> clazz_linear, Class<T_Stagger> clazz_stagger) {
        mViewHolderOuter = outerInstance;
        mLinearViewHolderClass = clazz_linear;
        mStaggeredViewHolderClass = clazz_stagger;
    }

    /**
     *
     * @param clazz
     * @param view
     * @return
     */
    private RecyclerView.ViewHolder createViewHolder(Class clazz, View view) {
        RecyclerView.ViewHolder obj = null;
        try {
            Constructor constructor = clazz.getConstructors()[0];
            obj = (RecyclerView.ViewHolder) constructor.newInstance(mViewHolderOuter, view);
        } catch (Exception e) {
            obj = null;
        }
        return obj;
    }

    public void switchMode(LoadMoreFragment.DisplayMode displayMode) {
        switchMode(displayMode, mColumnCount);
    }

    public void switchMode(LoadMoreFragment.DisplayMode displayMode, int columnCount) {
        this.mDisplayMode = displayMode;
        this.mColumnCount = columnCount<1 || columnCount>9 ? 1 : columnCount;
    }

    public void clearData() {
        mValues.clear();
    }

    public void setData(List<T> datas) {
        mValues = datas;
    }

    public int getDataSize() {
        return mValues.size();
    }

    public void addDatas(List<T> datas) {
        mValues.addAll(datas);
    }

    @Override
    public int getItemCount() {
        if(mValues == null)
            return 0;
        else {
            return layoutMissing() ? 1 : mValues.size();
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == LoadMoreRecyclerView.TYPE_STAGGER) {
            int resId = ResourceUtil.getLayoutId(mLayoutNameOfFragmentItemStagger, parent.getContext());
            View listItemStaggerView = null;
            if(resId != 0) {
                listItemStaggerView = LayoutInflater.from(parent.getContext()).inflate(resId, parent, false);
                listItemStaggerView.setClickable(itemClickable);
            } else {
                listItemStaggerView = loadDefaultListItemStaggerLayout(parent);
            }
            ViewGroup.LayoutParams layoutParams = listItemStaggerView.getLayoutParams();
            layoutParams.width = UiUtil.dip2px(parent.getContext(), UiUtil.sScreenWidthInDp/mColumnCount);
            return createViewHolder(mStaggeredViewHolderClass, listItemStaggerView);
        } else {
            int resId = ResourceUtil.getLayoutId(mLayoutNameOfFragmentItemLinear, parent.getContext());
            View listItemLinearView = null;
            if(resId != 0) {
                listItemLinearView = LayoutInflater.from(parent.getContext()).inflate(resId, parent, false);
                listItemLinearView.setClickable(itemClickable);
                if(itemClickable && itemClickVisible) {
                    int rippleeId = ResourceUtil.getDrawableId("recycle_view_touch_ripple", parent.getContext());
                    Drawable drawable = parent.getContext().getResources().getDrawable(rippleeId);
                    listItemLinearView.setBackground(drawable);
                }
            } else {
                listItemLinearView = loadDefaultListItemLinearLayout(parent);
            }
            return createViewHolder(mLinearViewHolderClass, listItemLinearView);
        }
    }

    private boolean missingLinearLayout = false;
    private boolean missingStaggerLayout = false;

    /**判断是否加载了默认布局*/
    public boolean layoutMissing() {
        return (mDisplayMode == LoadMoreFragment.DisplayMode.STAGGERED && missingStaggerLayout)
                || (mDisplayMode == LoadMoreFragment.DisplayMode.LINEAR && missingLinearLayout);
    }

    /**加载默认的fragment_load_more_list_item_linear*/
    private View loadDefaultListItemLinearLayout(ViewGroup parent) {
        missingLinearLayout = true;
        LinearLayout mLinearLayout = new LinearLayout(parent.getContext());
        LinearLayout.LayoutParams mLayoutParams1 = new LinearLayout.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, UiUtil.dip2px(parent.getContext(),30));
        mLinearLayout.setLayoutParams(mLayoutParams1);
        parent.addView(mLinearLayout);

        TextView textView = new TextView(parent.getContext());
        textView.setTextColor(ResourceUtil.getColor("colorPrimary", parent.getContext()));
        textView.setTextSize(14);
        textView.setText("未实现具体布局");
        LinearLayout.LayoutParams mLayoutParams2 = new LinearLayout.LayoutParams(RecyclerView.LayoutParams.WRAP_CONTENT, RecyclerView.LayoutParams.WRAP_CONTENT);
        mLayoutParams2.setMarginStart(UiUtil.dip2px(parent.getContext(),10));
        textView.setLayoutParams(mLayoutParams2);
        mLinearLayout.addView(textView);
        return mLinearLayout;
    }

    /**加载默认的fragment_load_more_list_item_stagger*/
    private View loadDefaultListItemStaggerLayout(ViewGroup parent) {
        missingStaggerLayout = true;
        CardView mCardView = new CardView(parent.getContext());
        LinearLayout.LayoutParams mLayoutParams1 = new LinearLayout.LayoutParams(RecyclerView.LayoutParams.WRAP_CONTENT, RecyclerView.LayoutParams.WRAP_CONTENT);
        int margin1 = UiUtil.dip2px(parent.getContext(),3);
        mLayoutParams1.setMargins(margin1, margin1, margin1, margin1);
        mCardView.setLayoutParams(mLayoutParams1);
        parent.addView(mCardView);

        TextView textView = new TextView(parent.getContext());
        textView.setTextColor(ResourceUtil.getColor("colorPrimary", parent.getContext()));
        textView.setTextSize(14);
        textView.setText("未实现具体布局");
        LinearLayout.LayoutParams mLayoutParams2 = new LinearLayout.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT);
        mLayoutParams2.setMargins(0, UiUtil.dip2px(parent.getContext(),50), 0, 0);
        textView.setLayoutParams(mLayoutParams2);
        mCardView.addView(textView);
        return mCardView;
    }

    /**
     * 控件赋值，子类中必须重写
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (mDisplayMode == LoadMoreFragment.DisplayMode.STAGGERED) {
            StaggeredViewHolder staggeredViewHolder = (StaggeredViewHolder) holder;
            /* assign view controls
            staggeredViewHolder.iconView.setVisibility(View.VISIBLE);
            staggeredViewHolder.mContentView.setText(mValues.get(position).details);
            */
        } else {
            LinearViewHolder mHolder = (LinearViewHolder) holder;
            /* assign view controls
            mHolder.mItem = mValues.get(position);
            mHolder.mContentView.setText(mValues.get(position).content);
            mHolder.mIdView.setText(mValues.get(position).id);
            */
        }
    }

    /**
     * 必须定义新的子类
     */
    public class StaggeredViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        public View mView;
        /* demo of view controls
        public View iconView;
        public TextView mContentView;
        */
        public StaggeredViewHolder(View view) {
            super(view);
            mView = view;
            //监听ItemView点击事件
            if(itemClickable) {
                view.setOnClickListener(this);
                if(itemLongClickable)
                    view.setOnLongClickListener(this);
            }
            //实例化自定义对象
            /* demo
            iconView = view.findViewById(R.id.icon);
            mContentView = (TextView) view.findViewById(R.id.content);
            */
        }

        @Override
        public void onClick(View view) {
            if(mClickListenerAgent != null)
                mClickListenerAgent.onItemClick(view, getPosition());
        }

        @Override
        public boolean onLongClick(View view) {
            if(!itemLongClickable)
                return false;
            if(mClickListenerAgent != null)
                return mClickListenerAgent.onItemLongClick(view, getPosition());
            else
                return false;
        }
    }

    /**
     * 必须定义新的子类
     */
    public class LinearViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        public final View mView;
        /* demo of view controls
        public TextView mIdView;
        public TextView mContentView;
        public DummyItem mItem;
        */
        public LinearViewHolder(View view) {
            super(view);
            mView = view;
            //监听ItemView点击事件
            if(itemClickable) {
                view.setOnClickListener(this);
                if(itemLongClickable)
                    view.setOnLongClickListener(this);
            }
            //实例化自定义对象
            /* demo
            mIdView = (TextView) view.findViewById(R.id.id);
            mContentView = (TextView) view.findViewById(R.id.content);
            */
        }

        @Override
        public void onClick(View view) {
            if(mClickListenerAgent != null)
                mClickListenerAgent.onItemClick(view, getPosition());
        }

        @Override
        public boolean onLongClick(View view) {
            if(!itemLongClickable)
                return false;
            if(mClickListenerAgent != null)
                return mClickListenerAgent.onItemLongClick(view, getPosition());
            else
                return false;
        }
    }

}