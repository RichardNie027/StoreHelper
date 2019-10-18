package com.tlg.storehelper.activity.common;

import com.nec.lib.android.boost.BottomFlexboxDialogFragment;
import com.tlg.storehelper.R;

import java.util.LinkedHashMap;

/**货品选择，底部弹窗*/
public class GoodsSearchingFragment extends BottomFlexboxDialogFragment {

    @Override
    protected int setLayoutResourceID() {
        return R.layout.fragment_goods_searching;
    }

    @Override
    protected int setRecyclerViewResourceID() {
        return R.id.recyclerView;
    }

    public GoodsSearchingFragment(LinkedHashMap<String, String> datas) {
        super();
        this.setDataList(datas);
    }

}
