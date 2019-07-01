package com.tlg.storehelper.loadmorerecycler;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.nec.application.MyApplication;
import com.nec.utils.TextDrawable;
import com.nec.utils.ResourceUtil;

import java.lang.reflect.Constructor;

import static com.tlg.storehelper.loadmorerecycler.AsynDataRequest.PAGE_CONTENT;

/* fragment_default_load_more_list.xml 默认配置
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:layout_marginLeft="0dp"
    android:layout_marginRight="0dp"
    android:orientation="vertical">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal">

        <TextView
            android:id="@+id/mode_switch_button"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="显示模式"
            android:textSize="14dp" />
    </LinearLayout>

    <android.support.v4.widget.SwipeRefreshLayout
        android:id="@+id/refresh_layout"
        android:layout_width="match_parent"
        android:layout_height="match_parent">

        <com.tlg.storehelper.loadmorerecycler.LoadMoreRecyclerView
            android:id="@+id/recycle_list"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            app:layoutManager="LinearLayoutManager"
            tools:listitem="@layout/fragment_default_load_more_list_item" />

    </android.support.v4.widget.SwipeRefreshLayout>
</LinearLayout>
 */

/**
 * 无限加载Fragment
 * 定制布局文件，对应更新子类的sLayoutOfFragmentItemList
 *
 * 调用示例：
 * RecordFragment.newInstance(RecordFragment.class, RecordRecyclerViewItemAdapter.class, new RecordListDataRequest(), dataBundle, LoadMoreFragment.DisplayMode.LINEAR, 3);
 *
 * @param <TAdapter extends RecyclerViewItemAdapter>
 */
public abstract class LoadMoreFragment<TAdapter extends RecyclerViewItemAdapter> extends Fragment {
    ////////////////资源前提 START////////////////
    /**列表Fragmeng布局xml文件名，必要，需要定制布局文件且以文件名对此变量赋值*/
    protected String mLayoutOfFragmentItemList = "fragment_default_load_more_list";
    //布局xml文件的内部资源ID
    /**布局文件中SwipeRefreshLayout的ID，必要*/
    protected String mIdOfSwipeRefreshLayout = "refresh_layout";
    /**布局文件中LoadMoreRecyclerView的ID，必要*/
    protected String mIdOfRecycleView = "recycle_list";
    /**切换模式的视图控件，类型为TextView或Button，非必要*/
    protected String mIdOfModeSwitchButton = "mode_switch_button";
    ////////////////资源前提 END////////////////

    /**
     * 列表显示模式：行、瀑布
     */
    public enum DisplayMode {
        LINEAR(1), STAGGERED(2);
        public int value;
        DisplayMode(int value) {
            this.value = value;
        }
        public int getValue() {
            return value;
        }
        public static DisplayMode fromValue(int value) {
            switch (value) {
                case 1:
                    return LINEAR;
                case 2:
                    return STAGGERED;
                default:
                    return null;
            }
        }
    }

    /**内部使用的列数关键字名称，Staggered模式下的列数*/
    private static String ARG_COLUMN_COUNT = "column-count";
    /**内部使用的显示模式关键字名称*/
    private static String ARG_DISPLAY_MODE = "display-mode";
    /**内部使用的异步数据请求接口AsynDataRequest关键字名称*/
    private static String ASYN_DATA_REQUEST = "asyn-data-request";
    /**显示模式*/
    protected DisplayMode mDisplayMode = DisplayMode.LINEAR;
    /**Staggered模式列数*/
    protected int mColumnCount = 1;
    /**异步数据请求对象*/
    protected AsynDataRequest mAsynDataRequest;

    /**主体Item适配器*/
    protected TAdapter myRecyclerViewItemAdapter;
    /**切换模式的控件,TEXTVIEW或BUTTON*/
    protected View mSwitchModeView = null;
    /**支持加载更多的RecyclerView*/
    protected LoadMoreRecyclerView mRecyclerView;
    /**被嵌套的SwipeRefreshLayout布局对象*/
    protected SwipeRefreshLayout mSwipeRefreshLayout;
    /**当前页码,zero-base*/
    protected int mPage = 0;
    /** 数据获取条件
     * 在三处使用：
     * 1、进入页面初始装载数据onCreateView：子类中设置在onCreateView事件前
     * 2、刷新数据SwipeRefreshLayout.OnRefreshListener：下拉刷新
     * 3、加载更多LoadMoreRecyclerView.LoadMoreListener：上拉加载（初始加载的首批数据不足一屏时，自动触发）*/
    protected Bundle mDataBundle = new Bundle();
    /**Item适配器RecyclerViewItemAdapter的子类*/
    protected Class<TAdapter> mAdapterClass;

    //private OnListFragmentInteractionListener mListener;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public LoadMoreFragment() {
    }

    /**
     * 默认地创建Linear显示模式的实例
     * @param fragmentClass 子类
     * @param itemAdapterClass 行适配器的类
     * @param asynDataRequest 异步数据请求
     * @param paramBundle 传递参数（数据请求条件、页面参数）
     * @return
     */
    public static <T extends LoadMoreFragment> T newInstance(Class<? extends T> fragmentClass,
                                                             Class<? extends RecyclerViewItemAdapter> itemAdapterClass,
                                                             AsynDataRequest asynDataRequest,
                                                             Bundle paramBundle) {
        return newInstance(fragmentClass, itemAdapterClass, asynDataRequest, paramBundle, DisplayMode.LINEAR, 2);
    }

    /**
     * 创建Linear/Staggered(默认2列)显示模式的实例
     * @param fragmentClass 子类
     * @param itemAdapterClass 行适配器的类
     * @param asynDataRequest 异步数据请求
     * @param paramBundle 传递参数（数据请求条件、页面参数）
     * @param displayMode 显示模式
     * @return
     */
    public static <T extends LoadMoreFragment> T newInstance(Class<? extends T> fragmentClass,
                                                             Class<? extends RecyclerViewItemAdapter> itemAdapterClass,
                                                             AsynDataRequest asynDataRequest,
                                                             Bundle paramBundle,
                                                             DisplayMode displayMode) {
        return newInstance(fragmentClass, itemAdapterClass, asynDataRequest, paramBundle, displayMode, 2);
    }

    /**
     * 创建Linear/Staggered显示模式的实例
     * @param fragmentClass 子类
     * @param itemAdapterClass 行适配器的类
     * @param asynDataRequest 异步数据请求
     * @param paramBundle 传递参数（数据请求条件、页面参数）
     * @param displayMode 显示模式
     * @param columns Staggered显示列数
     * @return
     */
    public static <T extends LoadMoreFragment> T newInstance(Class<? extends T> fragmentClass,
                                                             Class<? extends RecyclerViewItemAdapter> itemAdapterClass,
                                                             AsynDataRequest asynDataRequest,
                                                             Bundle paramBundle,
                                                             DisplayMode displayMode,
                                                             int columns) {
        T fragment = null;
        try {
            Class clazz = fragmentClass.asSubclass(LoadMoreFragment.class);
            fragment = (T) clazz.newInstance();
        } catch (ClassCastException | java.lang.InstantiationException | IllegalAccessException e) {
            return null;
        }
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columns);
        args.putInt(ARG_DISPLAY_MODE, displayMode.value);
        args.putSerializable(ASYN_DATA_REQUEST, asynDataRequest);
        if(paramBundle != null && !paramBundle.isEmpty())
            args.putAll(paramBundle);    //包含数据请求条件、页面参数
        fragment.setArguments(args);
        fragment.mAdapterClass = itemAdapterClass;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        if (bundle != null && !bundle.isEmpty()) {
            mColumnCount = bundle.getInt(ARG_COLUMN_COUNT);
            mDisplayMode = DisplayMode.fromValue(bundle.getInt(ARG_DISPLAY_MODE));
            mAsynDataRequest = (AsynDataRequest) bundle.getSerializable(ASYN_DATA_REQUEST);
            doParamBundle(bundle);
        }
    }

    /**
     * ##子类需要重写方法##
     * 解析Bundle参数：
     * 1、组装mDataBundle（数据获取AsynDataRequest需要）；
     * 2、为页面传参，需要在LoadMoreFragment子类定义新的属性。
     * @param bundle 通过newInstance方法传入的参数集
     */
    protected void doParamBundle(Bundle bundle) {
        //1、组装mDataBundle
        //mDataBundle.putLong("dataParam1", bundle.getLong("dataParam1"));
        //mDataBundle.putInt("dataParam1", bundle.getInt("dataParam1"));
        //mDataBundle.putFloat("dataParam1", bundle.getFloat("dataParam1"));
        //mDataBundle.putString("dataParam1", bundle.getString("dataParam1"));
        //mDataBundle.putBoolean("dataParam1", bundle.getBoolean("dataParam1"));
        //mDataBundle.putSerializable("dataParam1", bundle.getSerializable("dataParam1"));

        //2、为页面传参
        //mParam1 = bundle.getString("dataParam1"); //需要定义类属性mParam1
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // find view
        View view = inflater.inflate(ResourceUtil.getLayoutId(mLayoutOfFragmentItemList, getContext()), container, false);
        mRecyclerView = (LoadMoreRecyclerView) view.findViewById(ResourceUtil.getId(mIdOfRecycleView, getContext()));
        int switchControlResId = ResourceUtil.getId(mIdOfModeSwitchButton, getContext());
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(ResourceUtil.getId(mIdOfSwipeRefreshLayout, getContext()));

        // control view
        mRecyclerView.setHasFixedSize(true);
        if(switchControlResId != 0) {
            mSwitchModeView = view.findViewById(switchControlResId);
            if(mSwitchModeView != null) {
                mSwitchModeView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        modeAdaptation(view, mDisplayMode == DisplayMode.LINEAR ? DisplayMode.STAGGERED : DisplayMode.LINEAR);
                    }
                });
            }
        }
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                doRefreshOnRecyclerView();
            }
        });
        if (mDisplayMode == DisplayMode.STAGGERED) {
            mRecyclerView.setLayoutManager(new StrongStaggeredGridLayoutManager(mColumnCount, StrongStaggeredGridLayoutManager.VERTICAL));
        } else {
            mRecyclerView.setLayoutManager(new StrongLinearLayoutManager(getActivity()));
        }

        //自动加载初始数据
        try {
            //myRecyclerViewItemAdapter = mAdapterClass.newInstance();
            Constructor constructor = mAdapterClass.getDeclaredConstructor();
            myRecyclerViewItemAdapter = (TAdapter) constructor.newInstance();
            modeAdaptation(mSwitchModeView, mDisplayMode);
            mAsynDataRequest.fetchData(mPage, 1, mHandler, mDataBundle);     //发起数据异步请求
        } catch (Throwable t) {
            Log.e("ERROR", t.getMessage(), t);
            Toast.makeText(MyApplication.getInstance(), "加载数据失败", Toast.LENGTH_SHORT).show();
        }
        mRecyclerView.setAdapter(myRecyclerViewItemAdapter);
        mRecyclerView.setLoadMoreListener(new LoadMoreRecyclerView.LoadMoreListener() {
            @Override
            public void onLoadMore() {
                mRecyclerView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mSwipeRefreshLayout.setRefreshing(false);
                        mAsynDataRequest.fetchData(++mPage, 3, mHandler, mDataBundle);   //发起数据异步请求
                    }
                }, 200);
            }
        });
        return view;
    }

    /**刷新RecyclerView*/
    public void doRefreshOnRecyclerView() {
        mSwipeRefreshLayout.setRefreshing(false);
        myRecyclerViewItemAdapter.clearData();
        mPage = 0;
        mAsynDataRequest.fetchData(mPage, 2, mHandler, mDataBundle); //发起数据异步请求
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message message) {
            PageContent<String> pageContent = (PageContent) message.getData().getSerializable(PAGE_CONTENT);
            boolean hasMore = pageContent.hasMore && !myRecyclerViewItemAdapter.layoutMissing();
            switch (message.what){
                case 1: //init
                    mRecyclerView.notifyDataReset();
                    myRecyclerViewItemAdapter.setData(pageContent.datas);
                    //mRecyclerView.setAutoLoadMoreEnable(hasMore);
                    mRecyclerView.notifyMoreFinish(pageContent.hasMore);
                    //myRecyclerViewItemAdapter.notifyDataSetChanged();
                     break;
                case 2: //SwipeRefreshLayout.OnRefreshListener
                    mRecyclerView.notifyDataReset();
                    myRecyclerViewItemAdapter.setData(pageContent.datas);
                    //mRecyclerView.setAutoLoadMoreEnable(hasMore);
                    mRecyclerView.notifyMoreFinish(hasMore);
                    //myRecyclerViewItemAdapter.notifyDataSetChanged();
                    break;
                case 3: //LoadMoreRecyclerView.LoadMoreListener
                    myRecyclerViewItemAdapter.addDatas(pageContent.datas);
                    //mRecyclerView.setAutoLoadMoreEnable(hasMore);
                    mRecyclerView.notifyMoreFinish(hasMore);
                    break;
                default:
                    break;
            }
            if(pageContent.hasMore)
                loadMoreDelayIfPossible();
            //空白页效果
            if(myRecyclerViewItemAdapter.getDataSize() == 0) {
                TextDrawable textDrawable = new TextDrawable(getContext());
                textDrawable.setText("\n【没有发现任何数据】");
                textDrawable.setTextColor(Color.LTGRAY);
                textDrawable.setTextAlign(Layout.Alignment.ALIGN_CENTER);
                mRecyclerView.setBackground(textDrawable);

            } else
                mRecyclerView.setBackground(null);
        }
    };

    private void loadMoreDelayIfPossible() {
        mRecyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mRecyclerView.loadMoreIfPossible();     //尝试加载更多
            }
        }, 200);
    }

    private void modeAdaptation(View view, DisplayMode displayMode) {
        mDisplayMode = displayMode;
        if (mDisplayMode == DisplayMode.STAGGERED) {
            mColumnCount = (mColumnCount<1 || mColumnCount>9) ? 1 : mColumnCount;
            if(view != null) {
                if(TextView.class.isAssignableFrom(view.getClass()))
                    ((TextView) view).setText("瀑布模式");
                else if(Button.class.isAssignableFrom(view.getClass()))
                    ((Button) view).setText("瀑布模式");
            }
            myRecyclerViewItemAdapter.switchMode(mDisplayMode, mColumnCount);
            mRecyclerView.switchLayoutManager(new StrongStaggeredGridLayoutManager(mColumnCount, StrongStaggeredGridLayoutManager.VERTICAL));
        } else {
            if(view != null) {
                if(TextView.class.isAssignableFrom(view.getClass()))
                    ((TextView) view).setText("列表模式");
                else if(Button.class.isAssignableFrom(view.getClass()))
                    ((Button) view).setText("列表模式");
            }
            myRecyclerViewItemAdapter.switchMode(mDisplayMode);
            mRecyclerView.switchLayoutManager(new StrongLinearLayoutManager(getActivity()));
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
//        if (context instanceof OnListFragmentInteractionListener) {
//            mListener = (OnListFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnListFragmentInteractionListener");
//        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
//        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
//    public interface OnListFragmentInteractionListener {
//        public void onListFragmentInteraction();
//    }
}
