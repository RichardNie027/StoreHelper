package com.tlg.storehelper.activity.membership;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.nec.lib.android.loadmoreview.DisplayMode;
import com.nec.lib.android.loadmoreview.RecyclerViewItemAdapter;
import com.tlg.storehelper.R;
import com.tlg.storehelper.vo.ShopHistoryDetailVo;

import java.util.List;

public class MembershipRecyclerViewItemAdapter extends RecyclerViewItemAdapter<ShopHistoryDetailVo> {

    public MembershipRecyclerViewItemAdapter() {
        super();
        itemClickable = true;
        itemLongClickable = false;
        reassignListItemLayout();
        //设置ViewHolder的类，在构造实例后紧接调用！
        setViewHolderClass(this, MyLinearViewHolder.class, MyStaggeredViewHolder.class);
    }

    public MembershipRecyclerViewItemAdapter(List<ShopHistoryDetailVo> items) {
        super(items);
        itemClickable = false;
        itemLongClickable = true;
        reassignListItemLayout();
        //设置ViewHolder的类，在构造实例后紧接调用！
        setViewHolderClass(this, MyLinearViewHolder.class, MyStaggeredViewHolder.class);
    }

    private void reassignListItemLayout() {
        mLayoutNameOfFragmentItemLinear = "activity_membership_load_more_list_item_linear";
        mLayoutNameOfFragmentItemStagger = "activity_membership_load_more_list_item_stagger";
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (mDisplayMode == DisplayMode.STAGGERED) {
            MyStaggeredViewHolder mHolder = (MyStaggeredViewHolder) holder;
            mHolder.mItem = mValues.get(position);
            //mHolder.mView.setTag(mHolder.mItem.barcode);
            if(mHolder.mShopDate != null)
                mHolder.mShopDate.setText(mHolder.mItem.shopDate);
            if(mHolder.mSalesListCode != null)
                mHolder.mSalesListCode.setText(mHolder.mItem.salesListCode);
            if(mHolder.mQuantity != null)
                mHolder.mQuantity.setText(String.valueOf(mHolder.mItem.quantity));
            if(mHolder.mAmount != null)
                mHolder.mAmount.setText(String.valueOf(mHolder.mItem.amount));
        } else if (mDisplayMode == DisplayMode.LINEAR) {
            MyLinearViewHolder mHolder = (MyLinearViewHolder) holder;
            mHolder.mItem = mValues.get(position);
            //mHolder.mView.setTag(R.id.tag_first, mHolder.mItem.barcode);
            if(mHolder.mShopDate != null)
                mHolder.mShopDate.setText(mHolder.mItem.shopDate);
            if(mHolder.mSalesListCode != null)
                mHolder.mSalesListCode.setText(mHolder.mItem.salesListCode);
            if(mHolder.mQuantity != null)
                mHolder.mQuantity.setText(String.valueOf(mHolder.mItem.quantity));
            if(mHolder.mAmount != null)
                mHolder.mAmount.setText(String.valueOf(mHolder.mItem.amount));
        } else {
            ;
        }
    }

    public class MyStaggeredViewHolder extends StaggeredViewHolder {
        public final TextView mShopDate;
        public final TextView mSalesListCode;
        public final TextView mQuantity;
        public final TextView mAmount;
        public ShopHistoryDetailVo mItem;

        public MyStaggeredViewHolder(View view) {
            super(view);
            mShopDate = (TextView) view.findViewById(R.id.tvShopDate);
            mSalesListCode = (TextView) view.findViewById(R.id.tvSalesListCode);
            mQuantity = (TextView) view.findViewById(R.id.tvQuantity);
            mAmount = (TextView) view.findViewById(R.id.tvAmount);
        }

    }

    public class MyLinearViewHolder extends LinearViewHolder {
        public final TextView mShopDate;
        public final TextView mSalesListCode;
        public final TextView mQuantity;
        public final TextView mAmount;
        public ShopHistoryDetailVo mItem;

        public MyLinearViewHolder(View view) {
            super(view);
            mShopDate = (TextView) view.findViewById(R.id.tvShopDate);
            mSalesListCode = (TextView) view.findViewById(R.id.tvSalesListCode);
            mQuantity = (TextView) view.findViewById(R.id.tvQuantity);
            mAmount = (TextView) view.findViewById(R.id.tvAmount);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mShopDate.getText() + ">" + mSalesListCode.getText() + ">" + mQuantity.getText() + ">" + mAmount.getText() + "'";
        }
    }
}
