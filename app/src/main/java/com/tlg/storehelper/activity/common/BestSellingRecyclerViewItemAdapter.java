package com.tlg.storehelper.activity.common;

import android.os.Environment;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.nec.lib.android.loadmoreview.LoadMoreFragment;
import com.nec.lib.android.loadmoreview.RecyclerViewItemAdapter;
import com.tlg.storehelper.R;
import com.tlg.storehelper.httprequest.utils.PicUtil;
import com.tlg.storehelper.vo.GoodsSimpleVo;

import java.text.DecimalFormat;
import java.util.List;

public class BestSellingRecyclerViewItemAdapter extends RecyclerViewItemAdapter<GoodsSimpleVo> {

    private String localPicPath = Environment.getExternalStorageDirectory().getAbsoluteFile() + "/StoreHelper/pic/";

    public BestSellingRecyclerViewItemAdapter() {
        super();
        itemClickable = true;
        itemLongClickable = true;
        reassignListItemLayout();
        //设置ViewHolder的类，在构造实例后紧接调用！
        setViewHolderClass(this, MyLinearViewHolder.class, MyStaggeredViewHolder.class);
    }

    public BestSellingRecyclerViewItemAdapter(List<GoodsSimpleVo> items) {
        super(items);
        itemClickable = true;
        itemLongClickable = true;
        reassignListItemLayout();
        //设置ViewHolder的类，在构造实例后紧接调用！
        setViewHolderClass(this, MyLinearViewHolder.class, MyStaggeredViewHolder.class);
    }

    private void reassignListItemLayout() {
        mLayoutNameOfFragmentItemLinear = "fragment_best_selling_load_more_list_item_linear";
        mLayoutNameOfFragmentItemStagger = "fragment_best_selling_load_more_list_item_stagger";
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (mDisplayMode == LoadMoreFragment.DisplayMode.STAGGERED) {
            MyStaggeredViewHolder mHolder = (MyStaggeredViewHolder) holder;
            mHolder.mItem = mValues.get(position);
            mHolder.mView.setTag(mHolder.mItem.goodsNo);
            if(mHolder.ivPicView != null) {
                mHolder.ivPicView.setVisibility(View.VISIBLE);
                PicUtil.loadPic(mHolder.ivPicView, localPicPath, mHolder.mItem.pic, 2);
            }
            if(mHolder.tvGoodsNoView != null)
                mHolder.tvGoodsNoView.setText(String.valueOf(mHolder.mItem.goodsNo));
            if(mHolder.tvInfoView != null)
                mHolder.tvInfoView.setText(new DecimalFormat("￥,###").format(mHolder.mItem.price) + " [" + mHolder.mItem.info + "]");
        } else if (mDisplayMode == LoadMoreFragment.DisplayMode.LINEAR) {
            MyLinearViewHolder mHolder = (MyLinearViewHolder) holder;
            mHolder.mItem = mValues.get(position);
            mHolder.mView.setTag(mHolder.mItem.goodsNo);
            if(mHolder.tvGoodsNoView != null)
                mHolder.tvGoodsNoView.setText(String.valueOf(mHolder.mItem.goodsNo));
            if(mHolder.tvInfoView != null)
                mHolder.tvInfoView.setText(mHolder.mItem.price + " [" + mHolder.mItem.info + "]");
        } else {
            ;
        }
    }

    public class MyStaggeredViewHolder extends RecyclerViewItemAdapter.StaggeredViewHolder {
        TextView tvGoodsNoView, tvInfoView;
        ImageView ivPicView;
        GoodsSimpleVo mItem;

        public MyStaggeredViewHolder(View view) {
            super(view);
            tvGoodsNoView = view.findViewById(R.id.tvGoodsNo);
            tvInfoView = view.findViewById(R.id.tvInfo);
            ivPicView = view.findViewById(R.id.ivPic);
        }

    }

    public class MyLinearViewHolder extends RecyclerViewItemAdapter.LinearViewHolder {
        TextView tvGoodsNoView, tvInfoView;
        GoodsSimpleVo mItem;

        public MyLinearViewHolder(View view) {
            super(view);
            tvGoodsNoView = view.findViewById(R.id.tvGoodsNo);
            tvInfoView = view.findViewById(R.id.tvInfo);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + tvGoodsNoView.getText() + ">" + tvInfoView.getText() + "'";
        }
    }
}
