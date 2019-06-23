package com.tlg.storehelper;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.tlg.storehelper.loadmorerecycler.RecyclerViewItemAdapter;
import com.tlg.storehelper.loadmorerecycler.LoadMoreFragment;
import com.tlg.storehelper.vo.InventoryDetailVo;

import java.util.List;

public class RecordRecyclerViewItemAdapter extends RecyclerViewItemAdapter<InventoryDetailVo> {

    public RecordRecyclerViewItemAdapter() {
        super();
        reassignLitItemLayout();
        setViewHolderClass(this, MyLinearViewHolder.class, MyStaggeredViewHolder.class);
    }

    public RecordRecyclerViewItemAdapter(List<InventoryDetailVo> items) {
        super(items);
        reassignLitItemLayout();
        setViewHolderClass(this, RecordRecyclerViewItemAdapter.MyLinearViewHolder.class, RecordRecyclerViewItemAdapter.MyStaggeredViewHolder.class);
    }

    private void reassignLitItemLayout() {
        mLayoutNameOfFragmentItemLinear = "fragment_record_load_more_list_item_linear";
        mLayoutNameOfFragmentItemStagger = "fragment_record_load_more_list_item_stagger";
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (mDisplayMode == LoadMoreFragment.DisplayMode.STAGGERED) {
            MyStaggeredViewHolder mHolder = (MyStaggeredViewHolder) holder;
            if(mHolder.iconView != null)
                mHolder.iconView.setVisibility(View.VISIBLE);
            mHolder.mItem = mValues.get(position);
            if(mHolder.mIdxView != null)
                mHolder.mIdxView.setText(String.valueOf(mValues.get(position).idx));
            if(mHolder.mBinCodingView != null)
                mHolder.mBinCodingView.setText(mValues.get(position).bin_coding);
            if(mHolder.mBarCodeView != null)
                mHolder.mBarCodeView.setText(mValues.get(position).barcode);
            if(mHolder.mQuantityView != null)
                mHolder.mQuantityView.setText(String.valueOf(mValues.get(position).quantity));
        } else if (mDisplayMode == LoadMoreFragment.DisplayMode.LINEAR) {
            MyLinearViewHolder mHolder = (MyLinearViewHolder) holder;
            mHolder.mItem = mValues.get(position);
            if(mHolder.mIdxView != null)
                mHolder.mIdxView.setText(String.valueOf(mValues.get(position).idx));
            if(mHolder.mBinCodingView != null)
                mHolder.mBinCodingView.setText(mValues.get(position).bin_coding);
            if(mHolder.mBarCodeView != null)
                mHolder.mBarCodeView.setText(mValues.get(position).barcode);
            if(mHolder.mQuantityView != null)
                mHolder.mQuantityView.setText(String.valueOf(mValues.get(position).quantity));
        } else {
            ;
        }
    }

    public class MyStaggeredViewHolder extends RecyclerViewItemAdapter.StaggeredViewHolder {
        public View iconView;
        public final TextView mIdxView;
        public final TextView mBinCodingView;
        public final TextView mBarCodeView;
        public final TextView mQuantityView;
        public InventoryDetailVo mItem;

        public MyStaggeredViewHolder(View view) {
            super(view);
            iconView = view.findViewById(R.id.icon);
            mIdxView = (TextView) view.findViewById(R.id.tvIdx);
            mBinCodingView = (TextView) view.findViewById(R.id.tvBinCoding);
            mBarCodeView = (TextView) view.findViewById(R.id.tvBarCode);
            mQuantityView = (TextView) view.findViewById(R.id.tvQuantity);
        }

    }

    public class MyLinearViewHolder extends RecyclerViewItemAdapter.LinearViewHolder {
        public final TextView mIdxView;
        public final TextView mBinCodingView;
        public final TextView mBarCodeView;
        public final TextView mQuantityView;
        public InventoryDetailVo mItem;

        public MyLinearViewHolder(View view) {
            super(view);
            mIdxView = (TextView) view.findViewById(R.id.tvIdx);
            mBinCodingView = (TextView) view.findViewById(R.id.tvBinCoding);
            mBarCodeView = (TextView) view.findViewById(R.id.tvBarCode);
            mQuantityView = (TextView) view.findViewById(R.id.tvQuantity);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mIdxView.getText() + ">" + mBinCodingView.getText() + ">" + mBarCodeView.getText() + ">" + mQuantityView.getText() + "'";
        }
    }
}
