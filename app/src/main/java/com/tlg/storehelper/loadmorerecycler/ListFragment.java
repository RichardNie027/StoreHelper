package com.tlg.storehelper.loadmorerecycler;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.nec.utils.TextDrawable;
import com.tlg.storehelper.base.RecycleViewItemClickListener;
import com.tlg.storehelper.utils.ResourceUtil;
import com.tlg.storehelper.utils.UiUtil;

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
 * ListFragment.newInstance(RecordRecyclerViewItemAdapter.class, new SimpleDataRequest(), ListFragment.DisplayMode.LINEAR, 3)
 *
 * @param <TAdapter extends RecyclerViewItemAdapter>
 */
public class ListFragment<TAdapter extends RecyclerViewItemAdapter> extends Fragment {
    ////////////////资源前提 START////////////////
    /**列表Fragmeng布局xml文件名，必要，需要定制布局文件且以文件名对此变量赋值*/
    protected static String sLayoutOfFragmentItemList = "fragment_default_load_more_list";
    //布局xml文件的内部资源ID
    /**布局文件中SwipeRefreshLayout的ID，必要*/
    protected static final String sIdOfSwipeRefreshLayout = "refresh_layout";
    /**布局文件中LoadMoreRecyclerView的ID，必要*/
    protected static final String sIdOfRecycleView = "recycle_list";
    /**切换模式的视图控件，类型为TextView或Button，非必要*/
    protected static final String sIdOfModeSwitchButton = "mode_switch_button";
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
    /**当前页码*/
    protected int mPage = 0;
    /**Item适配器RecyclerViewItemAdapter的子类*/
    private Class<TAdapter> mAdapterClass;

    //private OnListFragmentInteractionListener mListener;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ListFragment() {
    }

    public static ListFragment newInstance(Class clazz, AsynDataRequest asynDataRequest, DisplayMode displayMode) {
        return newInstance(clazz, asynDataRequest, displayMode, 2);
    }

    public static ListFragment newInstance(Class clazz, AsynDataRequest asynDataRequest, DisplayMode displayMode, int columns) {
        ListFragment fragment = new ListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columns);
        args.putInt(ARG_DISPLAY_MODE, displayMode.value);
        args.putSerializable(ASYN_DATA_REQUEST, asynDataRequest);
        fragment.setArguments(args);
        fragment.mAdapterClass = clazz;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
            mDisplayMode = DisplayMode.fromValue(getArguments().getInt(ARG_DISPLAY_MODE));
            mAsynDataRequest = (AsynDataRequest) getArguments().getSerializable(ASYN_DATA_REQUEST);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // find view
        View view = inflater.inflate(ResourceUtil.getLayoutId(sLayoutOfFragmentItemList, getContext()), container, false);
        mRecyclerView = (LoadMoreRecyclerView) view.findViewById(ResourceUtil.getId(sIdOfRecycleView, getContext()));
        int switchControlResId = ResourceUtil.getId(sIdOfModeSwitchButton, getContext());
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(ResourceUtil.getId(sIdOfSwipeRefreshLayout, getContext()));

        // control view
        mRecyclerView.setHasFixedSize(true);
        if(switchControlResId != 0) {
            mSwitchModeView = view.findViewById(switchControlResId);
            mSwitchModeView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    modeAdaptation(view, mDisplayMode==DisplayMode.LINEAR?DisplayMode.STAGGERED:DisplayMode.LINEAR);
                }
            });
        }
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefreshLayout.setRefreshing(false);
                myRecyclerViewItemAdapter.clearData();
                mPage = 0;
                mAsynDataRequest.fetchData(mPage, 2, mHandler); //发起数据异步请求
            }
        });
        if (mDisplayMode == DisplayMode.STAGGERED) {
            mRecyclerView.setLayoutManager(new StrongStaggeredGridLayoutManager(mColumnCount, StrongStaggeredGridLayoutManager.VERTICAL));
        } else {
            mRecyclerView.setLayoutManager(new StrongLinearLayoutManager(getActivity()));
        }

        try {
            //myRecyclerViewItemAdapter = mAdapterClass.newInstance();
            Constructor constructor = mAdapterClass.getDeclaredConstructor();
            myRecyclerViewItemAdapter = (TAdapter) constructor.newInstance();
            modeAdaptation(mSwitchModeView, mDisplayMode);
            mAsynDataRequest.fetchData(mPage, 1, mHandler);     //发起数据异步请求
        } catch (Throwable t) {
            t.printStackTrace();
        }
        mRecyclerView.setAdapter(myRecyclerViewItemAdapter);
        mRecyclerView.setLoadMoreListener(new LoadMoreRecyclerView.LoadMoreListener() {
            @Override
            public void onLoadMore() {
                mRecyclerView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mSwipeRefreshLayout.setRefreshing(false);
                        mAsynDataRequest.fetchData(++mPage, 3, mHandler);   //发起数据异步请求
                    }
                }, 200);
            }
        });
        return view;
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
                    myRecyclerViewItemAdapter.setData(pageContent.datas);
                    //mRecyclerView.setAutoLoadMoreEnable(hasMore);
                    mRecyclerView.notifyMoreFinish(pageContent.hasMore);
                    //myRecyclerViewItemAdapter.notifyDataSetChanged();
                     break;
                case 2: //SwipeRefreshLayout.OnRefreshListener
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
