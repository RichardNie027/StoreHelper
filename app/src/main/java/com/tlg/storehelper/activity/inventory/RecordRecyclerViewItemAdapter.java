package com.tlg.storehelper.activity.inventory;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.nec.lib.android.loadmoreview.DisplayMode;
import com.nec.lib.android.loadmoreview.RecyclerViewItemAdapter;
import com.tlg.storehelper.R;
import com.tlg.storehelper.vo.InventoryDetailVo;

import java.util.List;

public class RecordRecyclerViewItemAdapter extends RecyclerViewItemAdapter<InventoryDetailVo> {

    public RecordRecyclerViewItemAdapter() {
        super();
        itemClickable = true;
        itemLongClickable = true;
        reassignListItemLayout();
        //设置ViewHolder的类，在构造实例后紧接调用！
        setViewHolderClass(this, MyLinearViewHolder.class, MyStaggeredViewHolder.class);
    }

    public RecordRecyclerViewItemAdapter(List<InventoryDetailVo> items) {
        super(items);
        itemClickable = true;
        itemLongClickable = true;
        reassignListItemLayout();
        //设置ViewHolder的类，在构造实例后紧接调用！
        setViewHolderClass(this, MyLinearViewHolder.class, MyStaggeredViewHolder.class);
    }

    private void reassignListItemLayout() {
        mLayoutNameOfFragmentItemLinear = "fragment_record_load_more_list_item_linear";
        mLayoutNameOfFragmentItemStagger = "fragment_record_load_more_list_item_stagger";
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (mDisplayMode == DisplayMode.STAGGERED) {
            MyStaggeredViewHolder mHolder = (MyStaggeredViewHolder) holder;
            if(mHolder.iconView != null)
                mHolder.iconView.setVisibility(View.VISIBLE);
            mHolder.mItem = mValues.get(position);
            mHolder.mView.setTag(R.id.tag_first, mHolder.mItem.id);  //tag1:id
            mHolder.mView.setTag(R.id.tag_second, mHolder.mItem.idx);  //tag2:idx
            if(mHolder.mIdxView != null)
                mHolder.mIdxView.setText(String.valueOf(mHolder.mItem.idx));
            if(mHolder.mBinCodingView != null)
                mHolder.mBinCodingView.setText(mHolder.mItem.binCoding);
            if(mHolder.mBarcodeView != null)
                mHolder.mBarcodeView.setText(mHolder.mItem.barcode);
            if(mHolder.mQuantityView != null)
                mHolder.mQuantityView.setText(String.valueOf(mHolder.mItem.quantity));
        } else if (mDisplayMode == DisplayMode.LINEAR) {
            MyLinearViewHolder mHolder = (MyLinearViewHolder) holder;
            mHolder.mItem = mValues.get(position);
            mHolder.mView.setTag(R.id.tag_first, mHolder.mItem.id);  //tag1:id
            mHolder.mView.setTag(R.id.tag_second, mHolder.mItem.idx);  //tag2:idx
            if(mHolder.mIdxView != null)
                mHolder.mIdxView.setText(String.valueOf(mHolder.mItem.idx));
            if(mHolder.mBinCodingView != null)
                mHolder.mBinCodingView.setText(mHolder.mItem.binCoding);
            if(mHolder.mBarcodeView != null)
                mHolder.mBarcodeView.setText(mHolder.mItem.barcode);
            if(mHolder.mQuantityView != null)
                mHolder.mQuantityView.setText(String.valueOf(mHolder.mItem.quantity));
        } else {
            ;
        }
    }

    public class MyStaggeredViewHolder extends RecyclerViewItemAdapter.StaggeredViewHolder {
        public View iconView;
        public final TextView mIdxView;
        public final TextView mBinCodingView;
        public final TextView mBarcodeView;
        public final TextView mQuantityView;
        public InventoryDetailVo mItem;

        public MyStaggeredViewHolder(View view) {
            super(view);
            iconView = view.findViewById(R.id.icon);
            mIdxView = (TextView) view.findViewById(R.id.tvIdx);
            mBinCodingView = (TextView) view.findViewById(R.id.tvBinCoding);
            mBarcodeView = (TextView) view.findViewById(R.id.tvBarcode);
            mQuantityView = (TextView) view.findViewById(R.id.tvQuantity);
        }

    }

    public class MyLinearViewHolder extends RecyclerViewItemAdapter.LinearViewHolder {
        public final TextView mIdxView;
        public final TextView mBinCodingView;
        public final TextView mBarcodeView;
        public final TextView mQuantityView;
        public InventoryDetailVo mItem;

        public MyLinearViewHolder(View view) {
            super(view);
            mIdxView = (TextView) view.findViewById(R.id.tvIdx);
            mBinCodingView = (TextView) view.findViewById(R.id.tvBinCoding);
            mBarcodeView = (TextView) view.findViewById(R.id.tvBarcode);
            mQuantityView = (TextView) view.findViewById(R.id.tvQuantity);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mIdxView.getText() + ">" + mBinCodingView.getText() + ">" + mBarcodeView.getText() + ">" + mQuantityView.getText() + "'";
        }
    }
}
