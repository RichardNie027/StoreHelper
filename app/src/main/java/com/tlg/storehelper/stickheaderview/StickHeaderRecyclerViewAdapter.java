package com.tlg.storehelper.stickheaderview;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nec.utils.ResourceUtil;

import java.lang.reflect.Constructor;
import java.util.List;

/**
 * 子类继承要做的事：
 * 1、定义时指定泛型
 * 2、定义layout资源xml的名称
 * 3、子类中必须重写onBindViewHolder
 * 4、必须定义内部类iewHolder的新子类
 */
public abstract class StickHeaderRecyclerViewAdapter<T extends StickHeaderViewGroupData> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    ////////////////资源前提 START////////////////
    //列表行项目Layout xml文件名
    protected static String sLayoutOfRecyclerView = "stick_header_recyclerview";
    ////////////////资源前提 END////////////////

    /**Item的数据集*/
    protected List<T> mValues;

    /**RecyclerViewAdapter之类的实例，用于调取内部类*/
    private StickHeaderRecyclerViewAdapter mViewHolderOuter;
    /**行显示的ViewHolder类*/
    private Class mViewHolderClass;

    public StickHeaderRecyclerViewAdapter(List<T> list) {
        this.mValues = list;
    }

    public void setViewHolderClass(StickHeaderRecyclerViewAdapter outerInstance, Class clazz_holder) {
        mViewHolderOuter = outerInstance;
        mViewHolderClass = clazz_holder;
    }

    /**
     * 构造指定类的Holder
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

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int resId = ResourceUtil.getLayoutId(sLayoutOfRecyclerView, parent.getContext());
        View itemView = null;
        if(resId != 0) {
            int itemViewResId = ResourceUtil.getLayoutId(sLayoutOfRecyclerView, parent.getContext());
            if(itemViewResId != 0) {
                itemView = LayoutInflater.from(parent.getContext()).inflate(itemViewResId , parent, false);
            }
        }
        if(itemView != null)
            return createViewHolder(mViewHolderClass, itemView);
        else
            return new StickHeaderViewHolder(itemView);
    }

    /**
     * 控件赋值，子类中必须重写
     */
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        /* assign view controls
        String text = mValues.get(position).getText();
        holder.mTextView.setText(text);
        */
    }


    @Override
    public int getItemCount() {
        return mValues == null ? 0 : mValues.size();
    }

    /**
     * 判断position对应的Item是否是组的第一项
     *
     * @param position
     * @return
     */
    public boolean isItemHeader(int position) {
        if (position == 0) {
            return true;
        } else {
            String lastGroupName = mValues.get(position - 1).groupName;
            String currentGroupName = mValues.get(position).groupName;
            //判断上一个数据的组别和下一个数据的组别是否一致，如果不一致则是不同组，也就是为第一项（头部）
            if (lastGroupName.equals(currentGroupName)) {
                return false;
            } else {
                return true;
            }
        }
    }

    /**
     * 获取position对应的Item组名
     *
     * @param position
     * @return
     */
    public String getGroupName(int position) {
        return mValues.get(position).groupName;
    }


    /**
     * 必须定义新的子类
     */
    public class StickHeaderViewHolder extends RecyclerView.ViewHolder {
        public View mView;
        /* demo of view controls
        TextView mTextView;
        */
        public StickHeaderViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            //实例化自定义对象
            /* demo
            mTextView = itemView.findViewById(R.id.tvItemText);
            */
        }
    }
}