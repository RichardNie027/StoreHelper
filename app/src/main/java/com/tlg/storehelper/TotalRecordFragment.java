package com.tlg.storehelper;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nec.application.MyApplication;
import com.tlg.storehelper.base.RecycleViewItemClickListener;
import com.tlg.storehelper.loadmoreview.LoadMoreFragment;

public class TotalRecordFragment extends LoadMoreFragment {

    private OnFragmentInteractionListener mListener;
    private String mInventoryListNo;

    private TextView mTvListNo;

    //传参名称
    public static final String sInventoryListIdLabel = "inventory_list_id";
    public static final String sInventoryListNoLabel = "inventory_list_no";

    public TotalRecordFragment() {
        //资源名称
        mLayoutOfFragmentItemList = "fragment_total_record";
        mIdOfSwipeRefreshLayout = "refresh_layout"; //内部资源名称
        mIdOfRecyclerView = "recycle_list";          //内部资源名称
    }

    @Override
    protected void doParamBundle(Bundle bundle) {
        //1、组装mDataBundle
        mDataBundle.putLong(sInventoryListIdLabel, bundle.getLong(sInventoryListIdLabel, -1));
        //2、为页面传参
        mInventoryListNo = bundle.getString(sInventoryListNoLabel, "");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        initView(view);
        return view;
    }

    private void initView(View rootView) {
        // find view
        mTvListNo = rootView.findViewById(R.id.tvListNo);

        // initialize controls
        mTvListNo.setText(mInventoryListNo);

        myRecyclerViewItemAdapter.setOnItemClickListenerAgent(new RecycleViewItemClickListener() {
            @Override
            public void onItemClick(View view, int postion) {
                Log.d(TotalRecordFragment.class.getName(), "click at " + postion);
            }

            @Override
            public boolean onItemLongClick(final View view, int postion) {
                Log.d(TotalRecordFragment.class.getName(), "long-click at " + postion);
                final String bin_coding = view.getTag(R.id.tag_first).toString();
                new AlertDialog.Builder(MyApplication.getInstance())
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setTitle("复盘提示")
                        .setMessage("是否复盘货位：" + bin_coding + "？")
                        .setCancelable(true)
                        .setPositiveButton("复盘", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(mListener != null)
                                    mListener.onInventoryLocatorRedo(bin_coding);
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.d(TotalRecordFragment.class.getName(), "没有复盘");
                            }
                        })
                        .show();
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
         * 货位复盘触发
         * @param bin_coding
         */
        void onInventoryLocatorRedo(String bin_coding);
    }
}
