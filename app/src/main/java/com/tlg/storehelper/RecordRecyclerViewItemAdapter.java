package com.tlg.storehelper;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.tlg.storehelper.loadmorerecycler.RecyclerViewItemAdapter;
import com.tlg.storehelper.loadmorerecycler.ListFragment;

import java.util.List;

public class RecordRecyclerViewItemAdapter extends RecyclerViewItemAdapter<DummyItemVo> {

    public RecordRecyclerViewItemAdapter() {
        super();
        reassignLitItemLayout();
        setViewHolderClass(this, MyLinearViewHolder.class, MyStaggeredViewHolder.class);
    }

    public RecordRecyclerViewItemAdapter(List<DummyItemVo> items) {
        super(items);
        reassignLitItemLayout();
        setViewHolderClass(this, RecordRecyclerViewItemAdapter.MyLinearViewHolder.class, RecordRecyclerViewItemAdapter.MyStaggeredViewHolder.class);
    }

    private void reassignLitItemLayout() {
        mLayoutNameOfFragmentItemLinear = "fragment_default_load_more_list_item_linear";
        mLayoutNameOfFragmentItemStagger = "fragment_default_load_more_list_item_stagger";
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (mDisplayMode == ListFragment.DisplayMode.STAGGERED) {
            MyStaggeredViewHolder staggeredViewHolder = (MyStaggeredViewHolder) holder;
            if(staggeredViewHolder.iconView != null)
                staggeredViewHolder.iconView.setVisibility(View.VISIBLE);
            if(staggeredViewHolder.mContentView != null)
                staggeredViewHolder.mContentView.setText(mValues.get(position).details);
        } else if (mDisplayMode == ListFragment.DisplayMode.LINEAR) {
            MyLinearViewHolder mHolder = (MyLinearViewHolder) holder;
            mHolder.mItem = mValues.get(position);
            if(mHolder.mContentView != null)
                mHolder.mContentView.setText(mValues.get(position).content);
            if(mHolder.mIdView != null)
                mHolder.mIdView.setText(mValues.get(position).id);
        } else {
            ;
        }
    }

    public class MyStaggeredViewHolder extends RecyclerViewItemAdapter.StaggeredViewHolder {
        public View iconView;
        public TextView mContentView;

        public MyStaggeredViewHolder(View view) {
            super(view);
            iconView = view.findViewById(R.id.icon);
            mContentView = (TextView) view.findViewById(R.id.content);
        }

        @Override
        public void onClick(View view) {
            super.onClick(view);
            System.out.println("stagger click");
        }

        @Override
        public boolean onLongClick(View view) {
            super.onLongClick(view);
            System.out.println("stagger long click");
            return true;
        }
    }

    public class MyLinearViewHolder extends RecyclerViewItemAdapter.LinearViewHolder {
        public final TextView mIdView;
        public final TextView mContentView;
        public DummyItemVo mItem;

        public MyLinearViewHolder(View view) {
            super(view);
            mIdView = (TextView) view.findViewById(R.id.id);
            mContentView = (TextView) view.findViewById(R.id.content);
        }

        @Override
        public void onClick(View view) {
            super.onClick(view);
            System.out.println("linear click");
        }

        @Override
        public boolean onLongClick(View view) {
            super.onLongClick(view);
            System.out.println("linear long click");
            return true;
        }
        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}
