package com.tlg.storehelper.activity.inventory;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.nec.lib.android.loadmoreview.LoadMoreFragment;
import com.nec.lib.android.loadmoreview.RecyclerViewItemAdapter;
import com.tlg.storehelper.R;
import com.tlg.storehelper.vo.InventoryRedoVo;

import java.util.List;

public class RedoRecordRecyclerViewItemAdapter extends RecyclerViewItemAdapter<InventoryRedoVo> {

    public RedoRecordRecyclerViewItemAdapter() {
        super();
        itemClickable = false;
        itemLongClickable = true;
        reassignListItemLayout();
        //设置ViewHolder的类，在构造实例后紧接调用！
        setViewHolderClass(this, MyLinearViewHolder.class, MyStaggeredViewHolder.class);
    }

    public RedoRecordRecyclerViewItemAdapter(List<InventoryRedoVo> items) {
        super(items);
        itemClickable = false;
        itemLongClickable = true;
        reassignListItemLayout();
        //设置ViewHolder的类，在构造实例后紧接调用！
        setViewHolderClass(this, RedoRecordRecyclerViewItemAdapter.MyLinearViewHolder.class, RedoRecordRecyclerViewItemAdapter.MyStaggeredViewHolder.class);
    }

    private void reassignListItemLayout() {
        mLayoutNameOfFragmentItemLinear = "fragment_redo_record_load_more_list_item_linear";
        mLayoutNameOfFragmentItemStagger = "fragment_redo_record_load_more_list_item_stagger";
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (mDisplayMode == LoadMoreFragment.DisplayMode.STAGGERED) {
            MyStaggeredViewHolder mHolder = (MyStaggeredViewHolder) holder;
            if(mHolder.iconView != null)
                mHolder.iconView.setVisibility(View.VISIBLE);
            mHolder.mItem = mValues.get(position);
            mHolder.mView.setTag(R.id.tag_first, mHolder.mItem.barcode);  //tag1:barcode
            if(mHolder.mBarcodeView != null)
                mHolder.mBarcodeView.setText(mHolder.mItem.barcode);
            if(mHolder.mQuantity1View != null)
                mHolder.mQuantity1View.setText(String.valueOf(mHolder.mItem.quantity1));
            if(mHolder.mQuantity2View != null)
                mHolder.mQuantity2View.setText(String.valueOf(mHolder.mItem.quantity2));
            if(mHolder.mQuantity3View != null)
                mHolder.mQuantity3View.setText(String.valueOf(mHolder.mItem.quantity3));
        } else if (mDisplayMode == LoadMoreFragment.DisplayMode.LINEAR) {
            MyLinearViewHolder mHolder = (MyLinearViewHolder) holder;
            mHolder.mItem = mValues.get(position);
            mHolder.mView.setTag(R.id.tag_first, mHolder.mItem.barcode);  //tag1:barcode
            if(mHolder.mBarcodeView != null)
                mHolder.mBarcodeView.setText(mHolder.mItem.barcode);
            if(mHolder.mQuantity1View != null)
                mHolder.mQuantity1View.setText(String.valueOf(mHolder.mItem.quantity1));
            if(mHolder.mQuantity2View != null)
                mHolder.mQuantity2View.setText(String.valueOf(mHolder.mItem.quantity2));
            if(mHolder.mQuantity3View != null)
                mHolder.mQuantity3View.setText(String.valueOf(mHolder.mItem.quantity3));
            if (mHolder.mItem.quantity1 == 0 && mHolder.mItem.quantity2 > 0)
                holder.itemView.setBackgroundColor(holder.itemView.getResources().getColor(R.color.lightskyblue));
            else if (mHolder.mItem.quantity2 == 0)
                holder.itemView.setBackgroundColor(holder.itemView.getResources().getColor(R.color.snow));
            else if (mHolder.mItem.quantity3 == 0)
                holder.itemView.setBackgroundColor(holder.itemView.getResources().getColor(R.color.grassgreen));
            else if (mHolder.mItem.quantity3 < 0)
                holder.itemView.setBackgroundColor(holder.itemView.getResources().getColor(R.color.yellow));
            else
                holder.itemView.setBackgroundColor(holder.itemView.getResources().getColor(R.color.lightyellow));
        } else {
            ;
        }
    }

    public class MyStaggeredViewHolder extends StaggeredViewHolder {
        public View iconView;
        public final TextView mBarcodeView;
        public final TextView mQuantity1View;
        public final TextView mQuantity2View;
        public final TextView mQuantity3View;
        public InventoryRedoVo mItem;

        public MyStaggeredViewHolder(View view) {
            super(view);
            iconView = view.findViewById(R.id.icon);
            mBarcodeView = (TextView) view.findViewById(R.id.tvBarcode);
            mQuantity1View = (TextView) view.findViewById(R.id.tvQuantity1);
            mQuantity2View = (TextView) view.findViewById(R.id.tvQuantity2);
            mQuantity3View = (TextView) view.findViewById(R.id.tvQuantity3);
        }

    }

    public class MyLinearViewHolder extends LinearViewHolder {
        public final TextView mBarcodeView;
        public final TextView mQuantity1View;
        public final TextView mQuantity2View;
        public final TextView mQuantity3View;
        public InventoryRedoVo mItem;

        public MyLinearViewHolder(View view) {
            super(view);
            mBarcodeView = (TextView) view.findViewById(R.id.tvBarcode);
            mQuantity1View = (TextView) view.findViewById(R.id.tvQuantity1);
            mQuantity2View = (TextView) view.findViewById(R.id.tvQuantity2);
            mQuantity3View = (TextView) view.findViewById(R.id.tvQuantity3);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mBarcodeView.getText() + ">" + mQuantity1View.getText() + ">" + mQuantity2View.getText() + ">" + mQuantity3View.getText() + "'";
        }
    }
}
