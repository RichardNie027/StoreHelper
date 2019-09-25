package com.tlg.storehelper.activity.membership;

import android.content.Context;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.nec.lib.android.loadmoreview.DisplayMode;
import com.nec.lib.android.loadmoreview.RecyclerViewItemAdapter;
import com.tlg.storehelper.R;
import com.tlg.storehelper.httprequest.utils.PicUtil;
import com.tlg.storehelper.vo.ShopHistoryDetailVo;

import org.w3c.dom.Text;

import java.text.DecimalFormat;
import java.util.List;

public class MembershipRecyclerViewItemAdapter extends RecyclerViewItemAdapter<ShopHistoryDetailVo> {

    public MembershipRecyclerViewItemAdapter() {
        super();
        itemClickable = false;
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
            if(mHolder.mShopDate != null)
                mHolder.mShopDate.setText(mHolder.mItem.shopDate);
            if(mHolder.mSalesListCode != null)
                mHolder.mSalesListCode.setText(mHolder.mItem.salesListCode);
            if(mHolder.mQuantity != null)
                mHolder.mQuantity.setText(String.valueOf(mHolder.mItem.quantity));
            if(mHolder.mAmount != null)
                mHolder.mAmount.setText(new DecimalFormat("￥,###").format(mHolder.mItem.amount));
        } else if (mDisplayMode == DisplayMode.LINEAR) {
            MyLinearViewHolder mHolder = (MyLinearViewHolder) holder;
            mHolder.mItem = mValues.get(position);
            if(mHolder.mShopDate != null)
                mHolder.mShopDate.setText(mHolder.mItem.shopDate);
            if(mHolder.mSalesListCode != null)
                mHolder.mSalesListCode.setText(mHolder.mItem.salesListCode);
            if(mHolder.mQuantity != null)
                mHolder.mQuantity.setText(String.valueOf(mHolder.mItem.quantity)+"件");
            if(mHolder.mAmount != null)
                mHolder.mAmount.setText(new DecimalFormat("￥,###").format(mHolder.mItem.amount));
            //ViewPager
            List<ShopHistoryDetailVo.ShopItemVo> list = mHolder.mItem.shopItemList;
            MyAdapter adapter = new MyAdapter(mHolder.mAmount.getRootView().getContext(), list);
            mHolder.mViewPager.setOffscreenPageLimit(3); // cache 3 pages
            mHolder.mViewPager.setAdapter(adapter);
            mHolder.mViewPager.setPageMargin(20);
            mHolder.mViewPager.setPageTransformer(false, new ScaleTransformer());
        } else {
            ;
        }
    }

    public class MyStaggeredViewHolder extends StaggeredViewHolder {
        public final TextView mShopDate;
        public final TextView mSalesListCode;
        public final TextView mQuantity;
        public final TextView mAmount;
        public final ViewPager mViewPager;
        public ShopHistoryDetailVo mItem;

        public MyStaggeredViewHolder(View view) {
            super(view);
            mShopDate = (TextView) view.findViewById(R.id.tvShopDate);
            mSalesListCode = (TextView) view.findViewById(R.id.tvSalesListCode);
            mQuantity = (TextView) view.findViewById(R.id.tvQuantity);
            mAmount = (TextView) view.findViewById(R.id.tvAmount);
            mViewPager = view.findViewById(R.id.viewpager);
        }

    }

    public class MyLinearViewHolder extends LinearViewHolder {
        public final TextView mShopDate;
        public final TextView mSalesListCode;
        public final TextView mQuantity;
        public final TextView mAmount;
        public final ViewPager mViewPager;
        public ShopHistoryDetailVo mItem;

        public MyLinearViewHolder(View view) {
            super(view);
            mShopDate = (TextView) view.findViewById(R.id.tvShopDate);
            mSalesListCode = (TextView) view.findViewById(R.id.tvSalesListCode);
            mQuantity = (TextView) view.findViewById(R.id.tvQuantity);
            mAmount = (TextView) view.findViewById(R.id.tvAmount);
            mViewPager = view.findViewById(R.id.viewpager);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mShopDate.getText() + ">" + mSalesListCode.getText() + ">" + mQuantity.getText() + ">" + mAmount.getText() + "'";
        }
    }

    //CardView Adapter
    public class MyAdapter extends PagerAdapter {
        private List<ShopHistoryDetailVo.ShopItemVo> mList;
        private Context context;
        private LayoutInflater inflater;
        private String localPicPath = Environment.getExternalStorageDirectory().getAbsoluteFile() + "/StoreHelper/pic/";

        public MyAdapter(Context context, List<ShopHistoryDetailVo.ShopItemVo> list) {
            this.context = context;
            this.mList = list;
            inflater = LayoutInflater.from(context);
        }
        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = inflater.inflate(R.layout.activity_membership_viewpager_item, container, false);
            //find view
            ImageView ivPic = view.findViewById(R.id.ivPic);
            TextView tvGoodsNo = view.findViewById(R.id.tvGoodsNo);
            TextView tvSize = view.findViewById(R.id.tvSize);
            TextView tvGoodsName = view.findViewById(R.id.tvGoodsName);
            TextView tvPrice = view.findViewById(R.id.tvPrice);
            TextView tvQuantity = view.findViewById(R.id.tvQuantity);
            TextView tvDiscount = view.findViewById(R.id.tvDiscount);
            TextView tvAmount = view.findViewById(R.id.tvAmount);
            TextView tvSales = view.findViewById(R.id.tvSales);
            //setup view
            tvGoodsNo.setText("货号："+mList.get(position).goodsNo);
            tvSize.setText("尺码："+String.valueOf(mList.get(position).size));
            tvGoodsName.setText("名称："+mList.get(position).goodsName);
            tvPrice.setText("牌价："+new DecimalFormat("￥,###").format(mList.get(position).price));
            tvQuantity.setText("×"+String.valueOf(mList.get(position).quantity)+"件");
            tvDiscount.setText("×"+String.valueOf(mList.get(position).discount)+"折");
            tvAmount.setText("金额："+new DecimalFormat("￥,###").format(mList.get(position).amount));
            tvSales.setText("导购："+mList.get(position).sales);
            PicUtil.loadPic(ivPic, localPicPath, mList.get(position).goodsNo, 2);
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }

    public class ScaleTransformer implements ViewPager.PageTransformer {
        private static final float MIN_SCALE = 0.9f;

        @Override
        public void transformPage(View view, float position) {

             //过滤那些 <-1 或 >1 的值，使它区于【-1，1】之间
            if (position < -1) {
                position = -1;
            } else if (position > 1) {
                position = 1;
            }
             // 判断是前一页 1 + position ，右滑 pos -> -1 变 0
             // 判断是后一页 1 - position ，左滑 pos -> 1 变 0
            float tempScale = position < 0 ? 1 + position : 1 - position; // [0,1]
            float scaleValue = MIN_SCALE + tempScale * 0.1f; // [0,1]
            view.setScaleX(scaleValue);
            view.setScaleY(scaleValue);
        }
    }

}
