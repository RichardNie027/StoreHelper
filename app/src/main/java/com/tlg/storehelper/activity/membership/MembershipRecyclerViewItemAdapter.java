package com.tlg.storehelper.activity.membership;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

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
            if(mHolder.mShopDate != null)
                mHolder.mShopDate.setText(mHolder.mItem.shopDate);
            if(mHolder.mSalesListCode != null)
                mHolder.mSalesListCode.setText(mHolder.mItem.salesListCode);
            if(mHolder.mQuantity != null)
                mHolder.mQuantity.setText(String.valueOf(mHolder.mItem.quantity));
            if(mHolder.mAmount != null)
                mHolder.mAmount.setText(String.valueOf(mHolder.mItem.amount));
            //ViewPager
            List<ShopHistoryDetailVo.ShopItemVo> list = mHolder.mItem.shopItemList;
            MyAdapter adapter = new MyAdapter(mHolder.mAmount.getRootView().getContext(), list);
            mHolder.mViewPager.setAdapter(adapter);
            mHolder.mViewPager.setPageMargin((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    48, mHolder.mAmount.getRootView().getResources().getDisplayMetrics()));
            mHolder.mViewPager.setPageTransformer(false, new ScaleTransformer(mHolder.mAmount.getRootView().getContext()));
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
        private List<ShopHistoryDetailVo.ShopItemVo> list;
        private Context context;
        private LayoutInflater inflater;

        public MyAdapter(Context context, List<ShopHistoryDetailVo.ShopItemVo> list) {
            this.context = context;
            this.list = list;
            inflater = LayoutInflater.from(context);
        }
        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = inflater.inflate(R.layout.activity_membership_viewpager_item, container, false);
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }

    public class ScaleTransformer implements ViewPager.PageTransformer {
        private Context context;
        private float elevation;

        public ScaleTransformer(Context context) {
            this.context = context;
            elevation = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    20, context.getResources().getDisplayMetrics());
        }

        @Override
        public void transformPage(View page, float position) {
            if (position < -1 || position > 1) {

            } else {
                if (position < 0) {
                    ((CardView) page).setCardElevation((1 + position) * elevation);
                } else {
                    ((CardView) page).setCardElevation((1 - position) * elevation);
                }
            }
        }
    }

}
