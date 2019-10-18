package com.tlg.storehelper.activity.membership;

import android.content.Context;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.nec.lib.android.base.RecycleViewItemClickListener;
import com.nec.lib.android.loadmoreview.DisplayMode;
import com.nec.lib.android.loadmoreview.RecyclerViewItemAdapter;
import com.nec.lib.android.utils.AndroidUtil;
import com.nec.lib.android.utils.ResUtil;
import com.tlg.storehelper.R;
import com.tlg.storehelper.httprequest.utils.PicUtil;
import com.tlg.storehelper.vo.ShopHistoryItemVo;
import com.tlg.storehelper.vo.ShopHistoryVo;

import java.text.DecimalFormat;
import java.util.List;

public class MembershipRecyclerViewItemAdapter extends RecyclerViewItemAdapter<ShopHistoryVo> {

    public MembershipRecyclerViewItemAdapter() {
        super();
        itemClickable = false;
        itemLongClickable = false;
        reassignListItemLayout();
        //设置ViewHolder的类，在构造实例后紧接调用！
        setViewHolderClass(this, MyLinearViewHolder.class, MyStaggeredViewHolder.class);
    }

    public MembershipRecyclerViewItemAdapter(List<ShopHistoryVo> items) {
        super(items);
        itemClickable = false;
        itemLongClickable = true;
        reassignListItemLayout();
        //设置ViewHolder的类，在构造实例后紧接调用！
        setViewHolderClass(this, MyLinearViewHolder.class, MyStaggeredViewHolder.class);
    }

    private void reassignListItemLayout() {
        //mLayoutNameOfFragmentItemLinear = "activity_membership_load_more_list_item_linear_p";
        mLayoutNameOfFragmentItemLinear = "activity_membership_load_more_list_item_linear_r";
        mLayoutNameOfFragmentItemStagger = "activity_membership_load_more_list_item_stagger";
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (mDisplayMode == DisplayMode.STAGGERED) {
//            MyStaggeredViewHolder mHolder = (MyStaggeredViewHolder) holder;
//            mHolder.mItem = mValues.get(position);
//            if(mHolder.mShopDate != null)
//                mHolder.mShopDate.setText(mHolder.mItem.shopDate);
//            if(mHolder.mSalesListCode != null)
//                mHolder.mSalesListCode.setText(mHolder.mItem.salesListCode);
//            if(mHolder.mQuantity != null)
//                mHolder.mQuantity.setText(String.valueOf(mHolder.mItem.quantity));
//            if(mHolder.mAmount != null)
//                mHolder.mAmount.setText(new DecimalFormat("￥,###").format(mHolder.mItem.amount));
        } else if (mDisplayMode == DisplayMode.LINEAR) {
            MyLinearViewHolder mHolder = (MyLinearViewHolder) holder;
            mHolder.mItem = mValues.get(position);
            if(mHolder.mShopDate != null) {
                mHolder.mShopDate.setAlpha(mHolder.mItem.shopHistoryItemList.size() > 0 ? 1.0f : 0.5f);
                String dateStr = mHolder.mItem.shopDate;
                if(dateStr.length() == 8)
                    mHolder.mShopDate.setText(dateStr.substring(0,4)+"-"+dateStr.substring(4,6)+"-"+dateStr.substring(6));
                else
                    mHolder.mShopDate.setText(dateStr);
            }
            if(mHolder.mSalesListCode != null) {
                mHolder.mSalesListCode.setAlpha(mHolder.mItem.shopHistoryItemList.size() > 0 ? 1.0f : 0.5f);
                mHolder.mSalesListCode.setText(mHolder.mItem.salesListCode);
            }
            if(mHolder.mQuantity != null) {
                if(mHolder.mItem.quantity > 0) {
                    mHolder.mQuantity.setText(String.valueOf(mHolder.mItem.quantity) + "件");
                    mHolder.mQuantity.setVisibility(View.VISIBLE);
                } else
                    mHolder.mQuantity.setVisibility(View.GONE);
            }
            if(mHolder.mAmount != null) {
                if(mHolder.mItem.amount > 0) {
                    mHolder.mAmount.setText(new DecimalFormat("￥,###").format(mHolder.mItem.amount));
                    mHolder.mAmount.setVisibility(View.VISIBLE);
                } else
                    mHolder.mAmount.setVisibility(View.GONE);
            }
            if(mHolder.mItem.shopHistoryItemList.size() > 0) {
                List<ShopHistoryItemVo> list = mHolder.mItem.shopHistoryItemList;
                if(mHolder.mViewPager != null) {        //inner ViewPager
                    MyAdapter adapter = new MyAdapter(mHolder.mAmount.getRootView().getContext(), list);
                    mHolder.mViewPager.setOffscreenPageLimit(3); // cache 3 pages
                    mHolder.mViewPager.setAdapter(adapter);
                    mHolder.mViewPager.setPageMargin(20);
                    //mHolder.mViewPager.setPageTransformer(false, new ScaleTransformer());
                    mHolder.mViewPager.setVisibility(View.VISIBLE);
                }
                if(mHolder.mRecyclerView != null) {     //inner RecyclerView
                    MyAdapter2 adapter = new MyAdapter2(mHolder.mAmount.getRootView().getContext(), list);
                    mHolder.mRecyclerView.setAdapter(adapter);
                    mHolder.mRecyclerView.setVisibility(View.VISIBLE);
                    adapter.setOnItemClickListener(new RecycleViewItemClickListener() {
                        @Override
                        public void onItemClick(View view, int position) {
                            AndroidUtil.showToast(historyInfo(mHolder.mItem.shopHistoryItemList.get(position)));
                        }

                        @Override
                        public boolean onItemLongClick(View view, int position) {
                            AndroidUtil.copyText(mHolder.mItem.shopHistoryItemList.get(position).goodsNo);
                            AndroidUtil.showToastLong(historyInfo(mHolder.mItem.shopHistoryItemList.get(position)));
                            AndroidUtil.showToast("货号已复制");
                            return true;
                        }
                    });
                }
            } else {
                if (mHolder.mViewPager != null)
                    mHolder.mViewPager.setVisibility(View.GONE);
                if (mHolder.mRecyclerView != null)
                    mHolder.mRecyclerView.setVisibility(View.GONE);
            }
        } else {
            ;
        }
    }

    private String historyInfo(ShopHistoryItemVo vo) {
        StringBuffer sb = new StringBuffer();
        sb.append("货号："+vo.goodsNo).append("\n");
        sb.append("尺码："+String.valueOf(vo.size)).append("\n");
        sb.append("名称："+vo.goodsName).append("\n");
        sb.append("牌价："+new DecimalFormat("￥,###").format(vo.price));
        sb.append("×"+String.valueOf(vo.quantity)+"件");
        sb.append("×"+String.valueOf(vo.discount)+"折").append("\n");
        sb.append("金额："+new DecimalFormat("￥,###").format(vo.amount)).append("\n");
        sb.append("导购："+vo.sales);
        return sb.toString();
    }

    //RecyclerView Staggered Item ViewHolder
    public class MyStaggeredViewHolder extends StaggeredViewHolder {
        public final TextView mShopDate;
        public final TextView mSalesListCode;
        public final TextView mQuantity;
        public final TextView mAmount;
        public final ViewPager mViewPager;
        public ShopHistoryVo mItem;

        public MyStaggeredViewHolder(View view) {
            super(view);
            mShopDate = (TextView) view.findViewById(R.id.tvShopDate);
            mSalesListCode = (TextView) view.findViewById(R.id.tvSalesListCode);
            mQuantity = (TextView) view.findViewById(R.id.tvQuantity);
            mAmount = (TextView) view.findViewById(R.id.tvAmount);
            mViewPager = view.findViewById(R.id.viewpager);
        }

    }

    //RecyclerView Line Item ViewHolder
    public class MyLinearViewHolder extends LinearViewHolder {
        public final TextView mShopDate;
        public final TextView mSalesListCode;
        public final TextView mQuantity;
        public final TextView mAmount;
        public final ViewPager mViewPager;
        public final RecyclerView mRecyclerView;
        public ShopHistoryVo mItem;

        public MyLinearViewHolder(View view) {
            super(view);
            mShopDate = (TextView) view.findViewById(R.id.tvShopDate);
            mSalesListCode = (TextView) view.findViewById(R.id.tvSalesListCode);
            mQuantity = (TextView) view.findViewById(R.id.tvQuantity);
            mAmount = (TextView) view.findViewById(R.id.tvAmount);
            mViewPager = view.findViewById(R.id.viewpager);
            mRecyclerView = view.findViewById(R.id.recyclerView);
            if(mRecyclerView != null && mAmount != null)
                mRecyclerView.setLayoutManager(new LinearLayoutManager(mAmount.getRootView().getContext(), RecyclerView.HORIZONTAL, false));
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mShopDate.getText() + ">" + mSalesListCode.getText() + ">" + mQuantity.getText() + ">" + mAmount.getText() + "'";
        }
    }

    //Inner CardView Adapter
    public class MyAdapter extends PagerAdapter {
        private List<ShopHistoryItemVo> mList;
        private Context mContext;
        private LayoutInflater inflater;
        private String localPicPath = Environment.getExternalStorageDirectory().getAbsoluteFile() + "/StoreHelper/pic/";

        public MyAdapter(Context context, List<ShopHistoryItemVo> list) {
            this.mContext = context;
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
            View view = inflater.inflate(R.layout.activity_membership_inner_viewpager_item, container, false);
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
            if(mList.get(position).quantity < 0)
                tvGoodsNo.setTextColor(ResUtil.getColor("colorSecondDark"));
            else
                tvGoodsNo.setTextColor(ResUtil.getColor("colorPrimary"));
            tvSize.setTextColor(tvGoodsNo.getCurrentTextColor());
            tvGoodsName.setTextColor(tvGoodsNo.getCurrentTextColor());
            tvPrice.setTextColor(tvGoodsNo.getCurrentTextColor());
            tvQuantity.setTextColor(tvGoodsNo.getCurrentTextColor());
            tvDiscount.setTextColor(tvGoodsNo.getCurrentTextColor());
            tvAmount.setTextColor(tvGoodsNo.getCurrentTextColor());
            tvSales.setTextColor(tvGoodsNo.getCurrentTextColor());
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

    //Inner RecycleView Adapter
    class MyAdapter2 extends RecyclerView.Adapter {
        private List<ShopHistoryItemVo> mList;
        private Context mContext;
        private LayoutInflater mInflater;
        private String localPicPath = Environment.getExternalStorageDirectory().getAbsoluteFile() + "/StoreHelper/pic/";
        private RecycleViewItemClickListener mClickListener;

        public MyAdapter2(Context context, List<ShopHistoryItemVo> list) {
            this.mContext = context;
            this.mList = list;
            this.mInflater = LayoutInflater.from(context);
        }

        @Override
        public int getItemCount() {
            return mList.size();
        }

        ///对控件赋值
        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            RecyclerViewHolder recyclerViewHolder = (RecyclerViewHolder) holder;
            recyclerViewHolder.itemView.setTag(mList.get(position).goodsNo);
            PicUtil.loadPic(recyclerViewHolder.ivPic, localPicPath, mList.get(position).goodsNo, 2);
            if(recyclerViewHolder.ivPicLayout != null)
                if(mList.get(position).quantity <= 0)
                    recyclerViewHolder.ivPicLayout.setBackgroundResource(R.color.colorPrimary);
                else
                    recyclerViewHolder.ivPicLayout.setBackgroundResource(R.color.white);
        }

        @Override
        public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = mInflater.inflate(R.layout.activity_membership_inner_recyclerview_item, parent, false);
            RecyclerViewHolder recyclerViewHolder = new RecyclerViewHolder(view, mClickListener);
            return recyclerViewHolder;
        }

        public void setOnItemClickListener(RecycleViewItemClickListener listener) {
            this.mClickListener = listener;
        }

        ///适配器中的自定义内部类，其中的子对象用于呈现数据
        class RecyclerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
            ImageView ivPic;
            ViewGroup ivPicLayout;

            private RecycleViewItemClickListener mListener;

            public RecyclerViewHolder(View view, RecycleViewItemClickListener listener) {
                super(view);
                mListener = listener;
                //为ItemView添加点击事件
                view.setOnClickListener(this);
                view.setOnLongClickListener(this);
                //实例化自定义对象
                ivPic = view.findViewById(R.id.ivPic);
                ivPicLayout = view.findViewById(R.id.ivPicLayout);
            }

            @Override
            public void onClick(View view) {
                if(mListener != null)
                    mListener.onItemClick(view, getPosition());
            }

            @Override
            public boolean onLongClick(View view) {
                if(mListener != null)
                    return mListener.onItemLongClick(view, getPosition());
                else
                    return true;
            }
        }
    }

}
