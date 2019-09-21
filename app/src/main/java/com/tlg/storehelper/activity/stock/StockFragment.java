package com.tlg.storehelper.activity.stock;

import android.content.Context;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.flexbox.AlignItems;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.nec.lib.android.boost.BottomDialogFragment;
import com.tlg.storehelper.R;

import java.util.List;

public class StockFragment extends BottomDialogFragment<String> {

    private RecyclerView mRecyclerView;

    @Override
    protected int setLayoutResourceID() {
        return R.layout.fragment_stock;
    }

    @Override
    protected void initView() {
        //设置RecyclerView
        mRecyclerView = mRootView.findViewById(R.id.recyclerView);
        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(this.getContext());
        layoutManager.setFlexWrap(FlexWrap.WRAP); //设置是否换行
        layoutManager.setFlexDirection(FlexDirection.ROW); // 设置主轴排列方式
        layoutManager.setAlignItems(AlignItems.STRETCH);
        layoutManager.setJustifyContent(JustifyContent.FLEX_START);
        mRecyclerView.setLayoutManager(layoutManager);

        RecyclerViewAdapter recyclerViewAdapter = new RecyclerViewAdapter(this.getContext(), mDatas);
        mRecyclerView.setAdapter(recyclerViewAdapter);
    }

    public static StockFragment newInstance(List datas) {
        StockFragment fragment = new StockFragment();
        fragment.setDataList(datas);
        return fragment;
    }

    ///自定义类继承RecycleView.Adapter类作为数据适配器
    class RecyclerViewAdapter extends RecyclerView.Adapter {

        private Context mContext;
        private List<String> mDatas;

        public RecyclerViewAdapter(Context context, List<String> datas) {
            this.mContext = context;
            this.mDatas = datas;
        }

        @Override
        public int getItemCount() {
            return mDatas.size();
        }

        ///对控件赋值
        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            RecyclerViewHolder recyclerViewHolder = (RecyclerViewHolder) holder;
            recyclerViewHolder.tvItem.setText(mDatas.get(position).toString());

            //定义Flexbox布局的元素特征
            ViewGroup.LayoutParams lp = recyclerViewHolder.layoutRecylerviewItem.getLayoutParams();
            if (lp instanceof FlexboxLayoutManager.LayoutParams) {
                FlexboxLayoutManager.LayoutParams flexboxLp = (FlexboxLayoutManager.LayoutParams) lp;
                flexboxLp.setFlexGrow(0.0f);
                flexboxLp.setAlignSelf(AlignItems.FLEX_END);
            }
        }

        @Override
        public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_stock_recyclerview, parent, false);
            RecyclerViewHolder recyclerViewHolder = new RecyclerViewHolder(view);
            return recyclerViewHolder;
        }

        ///适配器中的自定义内部类，其中的子对象用于呈现数据
        class RecyclerViewHolder extends RecyclerView.ViewHolder {
            TextView tvItem;
            ConstraintLayout layoutRecylerviewItem;

            public RecyclerViewHolder(View view) {
                super(view);
                //实例化自定义对象
                tvItem = view.findViewById(R.id.tvItem);
                layoutRecylerviewItem = view.findViewById(R.id.layoutRecylerviewItem);
            }
        }
    }

}
