package com.tlg.storehelper.activity.inventory;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.nec.lib.android.base.RecycleViewItemClickListener;
import com.nec.lib.android.loadmoreview.LoadMoreFragment;
import com.tlg.storehelper.MyApp;
import com.tlg.storehelper.R;

public class TotalRecordFragment extends LoadMoreFragment {

    private OnFragmentInteractionListener mListener;
    private String mInventoryListNo;

    private TextView mTvListNo;

    //传参名称
    public static final String sInventoryListIdLabel = "inventory_list_id";
    public static final String sInventoryListNoLabel = "inventory_list_no";

    @Override
    protected void doParamBundle(Bundle bundle) {
        //1、组装mDataBundle
        mDataBundle.putString(sInventoryListIdLabel, bundle.getString(sInventoryListIdLabel));
        //2、为页面传参
        mInventoryListNo = bundle.getString(sInventoryListNoLabel, "");

    }

    @Override
    protected void initView(View rootView, Bundle bundle) {
        // find view
        mTvListNo = rootView.findViewById(R.id.tvListNo);

        // initialize controls
        mTvListNo.setText(mInventoryListNo);

        myRecyclerViewItemAdapter.setOnItemClickListenerAgent(new RecycleViewItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Log.d(TotalRecordFragment.class.getName(), "click at " + position);
            }

            @Override
            public boolean onItemLongClick(final View view, int position) {
                Log.d(TotalRecordFragment.class.getName(), "long-click at " + position);
                final String binCoding = view.getTag(R.id.tag_first).toString();
                new AlertDialog.Builder(MyApp.getInstance())
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setTitle("复盘提示")
                        .setMessage("是否复盘货位：" + binCoding + "？")
                        .setCancelable(true)
                        .setPositiveButton("复盘", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(mListener != null)
                                    mListener.onInventoryLocatorRedo(binCoding);
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
    protected int setLayoutResourceID() {
        return R.layout.fragment_total_record;
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
         * @param binCoding
         */
        void onInventoryLocatorRedo(String binCoding);
    }
}
