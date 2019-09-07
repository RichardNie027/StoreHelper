package com.tlg.storehelper.activity.common;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.nec.lib.android.base.RecycleViewItemClickListener;
import com.nec.lib.android.loadmoreview.LoadMoreFragment;
import com.tlg.storehelper.MyApp;
import com.tlg.storehelper.R;
import com.tlg.storehelper.activity.inventory.RecordFragment;
import com.tlg.storehelper.comm.GlobalVars;

public class BestSellingFragment extends LoadMoreFragment {

    private OnFragmentInteractionListener mListener;

    //传参名称
    public static final String sStoreCodeLabel = "storeCode";
    public static final String sDimensionLabel = "dimension";

    public BestSellingFragment() {
        //资源名称
        mLayoutOfFragmentItemList = "fragment_best_selling";
        mIdOfSwipeRefreshLayout = "refresh_layout";     //内部资源名称
        mIdOfRecyclerView = "recycler_list";            //内部资源名称
    }

    @Override
    protected void doParamBundle(Bundle bundle) {
        //1、组装mDataBundle
        mDataBundle.putString(sStoreCodeLabel, GlobalVars.storeCode);
        mDataBundle.putString(sDimensionLabel, bundle.getString(sDimensionLabel));
        //2、为页面传参

    }

    public void updateBundle(Bundle bundle, boolean reload) {
        doParamBundle(bundle);
        if(reload)
            doRefreshOnRecyclerView();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        initView(view);
        return view;
    }

    private void initView(View rootView) {
        myRecyclerViewItemAdapter.setOnItemClickListenerAgent(new RecycleViewItemClickListener() {
            @Override
            public void onItemClick(View view, int postion) {
                Log.d(this.getClass().getName(), "click at " + postion);
                if(mListener != null) {
                    final String goodsNo = view.getTag().toString();
                    mListener.onClickGoodsItem(goodsNo);
                }
            }

            @Override
            public boolean onItemLongClick(final View view, int postion) {
                Log.d(this.getClass().getName(), "long-click at " + postion);
                return true;
            }
        });
        mRecyclerView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(hasFocus) {
                    if(mListener != null) {
                        mListener.onRecyclerViewHasFocus();
                    }
                }
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
         * 点击触发
         * @param goodsNo
         */
        void onClickGoodsItem(String goodsNo);

        void onRecyclerViewHasFocus();
    }
}
