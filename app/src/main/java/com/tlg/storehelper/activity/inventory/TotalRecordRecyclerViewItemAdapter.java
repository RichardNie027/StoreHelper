package com.tlg.storehelper.activity.inventory;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.nec.lib.android.loadmoreview.LoadMoreFragment;
import com.nec.lib.android.loadmoreview.RecyclerViewItemAdapter;
import com.tlg.storehelper.R;
import com.tlg.storehelper.vo.InventoryTotalVo;

import java.util.List;

public class TotalRecordRecyclerViewItemAdapter extends RecyclerViewItemAdapter<InventoryTotalVo> {

    public TotalRecordRecyclerViewItemAdapter() {
        super();
        itemClickable = true;
        itemLongClickable = true;
        reassignListItemLayout();
        //设置ViewHolder的类，在构造实例后紧接调用！
        setViewHolderClass(this, MyLinearViewHolder.class, MyStaggeredViewHolder.class);
    }

    public TotalRecordRecyclerViewItemAdapter(List<InventoryTotalVo> items) {
        super(items);
        itemClickable = true;
        itemLongClickable = true;
        reassignListItemLayout();
        //设置ViewHolder的类，在构造实例后紧接调用！
        setViewHolderClass(this, TotalRecordRecyclerViewItemAdapter.MyLinearViewHolder.class, TotalRecordRecyclerViewItemAdapter.MyStaggeredViewHolder.class);
    }

    private void reassignListItemLayout() {
        mLayoutNameOfFragmentItemLinear = "fragment_total_record_load_more_list_item_linear";
        mLayoutNameOfFragmentItemStagger = "fragment_total_record_load_more_list_item_stagger";
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (mDisplayMode == LoadMoreFragment.DisplayMode.STAGGERED) {
            MyStaggeredViewHolder mHolder = (MyStaggeredViewHolder) holder;
            if(mHolder.iconView != null)
                mHolder.iconView.setVisibility(View.VISIBLE);
            mHolder.mItem = mValues.get(position);
            mHolder.mView.setTag(R.id.tag_first, mHolder.mItem.binCoding);  //tag1:binCoding
            if(mHolder.mBinCodingView != null)
                mHolder.mBinCodingView.setText(mHolder.mItem.binCoding);
            if(mHolder.mSizeQuantityView != null)
                mHolder.mSizeQuantityView.setText(String.valueOf(mHolder.mItem.sizeQuantity));
            if(mHolder.mQuantityView != null)
                mHolder.mQuantityView.setText(String.valueOf(mHolder.mItem.quantity));
        } else if (mDisplayMode == LoadMoreFragment.DisplayMode.LINEAR) {
            MyLinearViewHolder mHolder = (MyLinearViewHolder) holder;
            mHolder.mItem = mValues.get(position);
            mHolder.mView.setTag(R.id.tag_first, mHolder.mItem.binCoding);  //tag1:binCoding
            if(mHolder.mBinCodingView != null)
                mHolder.mBinCodingView.setText(mHolder.mItem.binCoding);
            if(mHolder.mSizeQuantityView != null)
                mHolder.mSizeQuantityView.setText(String.valueOf(mHolder.mItem.sizeQuantity));
            if(mHolder.mQuantityView != null)
                mHolder.mQuantityView.setText(String.valueOf(mHolder.mItem.quantity));
        } else {
            ;
        }
    }

    public class MyStaggeredViewHolder extends StaggeredViewHolder {
        public View iconView;
        public final TextView mBinCodingView;
        public final TextView mSizeQuantityView;
        public final TextView mQuantityView;
        public InventoryTotalVo mItem;

        public MyStaggeredViewHolder(View view) {
            super(view);
            iconView = view.findViewById(R.id.icon);
            mBinCodingView = (TextView) view.findViewById(R.id.tvBinCoding);
            mSizeQuantityView = (TextView) view.findViewById(R.id.tvSizeQuantity);
            mQuantityView = (TextView) view.findViewById(R.id.tvQuantity);
        }

    }

    public class MyLinearViewHolder extends LinearViewHolder {
        public final TextView mBinCodingView;
        public final TextView mSizeQuantityView;
        public final TextView mQuantityView;
        public InventoryTotalVo mItem;

        public MyLinearViewHolder(View view) {
            super(view);
            mBinCodingView = (TextView) view.findViewById(R.id.tvBinCoding);
            mSizeQuantityView = (TextView) view.findViewById(R.id.tvSizeQuantity);
            mQuantityView = (TextView) view.findViewById(R.id.tvQuantity);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mBinCodingView.getText() + ">" + mSizeQuantityView.getText() + ">" + mQuantityView.getText() + "'";
        }
    }
}
