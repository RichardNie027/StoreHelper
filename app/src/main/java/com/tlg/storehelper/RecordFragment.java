package com.tlg.storehelper;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tlg.storehelper.base.RecycleViewItemClickListener;
import com.tlg.storehelper.loadmorerecycler.LoadMoreFragment;

public class RecordFragment extends LoadMoreFragment {

    private OnFragmentInteractionListener mListener;

    public RecordFragment() {
        mLayoutOfFragmentItemList = "fragment_record";
        mIdOfSwipeRefreshLayout = "refresh_layout";
        mIdOfRecycleView = "recycle_list";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mDataBundle.putLong("mInventoryListId", 1); //TODO: need set value
        View view = super.onCreateView(inflater, container, savedInstanceState);
        initView(view);
        return view;
    }

    private void initView(View rootView) {
        // find view

        // initial controls

        myRecyclerViewItemAdapter.setOnItemClickListener(new RecycleViewItemClickListener() {
            @Override
            public void onItemClick(View view, int postion) {
                Log.i("info", "click at " + postion);
            }

            @Override
            public boolean onItemLongClick(View view, int postion) {
                Log.i("info", "long click at " + postion);
                return true;
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        /**
         * 删除扫码记录触发
         * @param id
         */
        void onInventoryDeleteRecord(long id);
    }
}
