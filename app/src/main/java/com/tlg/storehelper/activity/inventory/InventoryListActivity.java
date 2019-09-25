package com.tlg.storehelper.activity.inventory;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.nec.lib.android.base.BaseRxAppCompatActivity;
import com.nec.lib.android.boost.DatetimePickerFragment;
import com.nec.lib.android.utils.AndroidUtil;
import com.tlg.storehelper.MyApp;
import com.tlg.storehelper.R;
import com.tlg.storehelper.comm.GlobalVars;
import com.nec.lib.android.utils.DateUtil;
import com.nec.lib.android.utils.SQLiteUtil;
import com.tlg.storehelper.dao.Inventory;
import com.tlg.storehelper.dao.SQLiteDbHelper;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class InventoryListActivity extends BaseRxAppCompatActivity implements DatetimePickerFragment.DatetimePickerResultInterface {

    private EditText mEtListNo;
    private TextView mTvListNo;
    private TextView mTvStoreCode;
    private TextView mTvDate;
    private TextView mTvIndex;
    private TextView mTvUsername;
    private Calendar mCalendar;

    private String mNewId = null;    // 新盘点单记录的ID

    @Override
    protected void beforeCreate(Bundle savedInstanceState) {
        mFullScreen = true;
    }

    @Override
    protected int setToolbarResourceID() {
        return R.id.toolbar;
    }

    @Override
    protected void initView() {
        // find view
        mTvListNo = findViewById(R.id.tvListNo);
        mEtListNo = findViewById(R.id.etListNo);
        mTvStoreCode = findViewById(R.id.tvStoreCode);
        mTvDate = findViewById(R.id.tvDate);
        mTvIndex = findViewById(R.id.tvIndex);
        mTvUsername = findViewById(R.id.tvUsername);

        hideKeyboard(true);

        //setup view
        mCalendar = Calendar.getInstance(Locale.CHINA);
        processDatetimePickerResult(mCalendar);
        updateUi();
    }

    @Override
    protected int setLayoutResourceID() {
        return R.layout.activity_inventory_list;
    }

    private void updateUi() {
        mTvStoreCode.setText(GlobalVars.storeCode);
        mTvUsername.setText(GlobalVars.username);
        mTvDate.setText(DateUtil.toStr(mCalendar).substring(0,10));
        String idx = calulateNewIdx();
        mTvIndex.setText(idx);
        String dateStr = android.text.format.DateFormat.format("yyyyMMdd", mCalendar.getTimeInMillis()).toString();
        String inventoryNo1 = new StringBuffer().append(GlobalVars.storeCode).append("-").append(dateStr).append("-").toString();
        mTvListNo.setText(inventoryNo1);
        String inventoryNo2 = new StringBuffer().append(idx).append("-").append(GlobalVars.username).toString();
        mEtListNo.setText(inventoryNo2);
    }

    private String calulateNewIdx() {
        String result = "01";
        SQLiteOpenHelper helper = new SQLiteDbHelper(getApplicationContext());
        SQLiteDatabase db = null;
        try {
            db = helper.getReadableDatabase();
            String sql = new StringBuffer().append("select max(idx) as idx").append(" from ").append(SQLiteDbHelper.TABLE_INVENTORY)
                    .append(" where storeCode=? and listDate like ?").toString();
            Cursor cursor = db.rawQuery(sql, new String[] {GlobalVars.storeCode, mTvDate.getText().toString()+"%"});
            if(cursor.moveToFirst()) {
                int idx = cursor.getInt(cursor.getColumnIndex("idx")) + 1;
                if(idx > 0 && idx < 10)
                    result = "0" + idx;
                else
                    result = String.valueOf(idx);
            }
            cursor.close();
        } catch (Throwable t) {
            Log.e(this.getClass().getName(), t.getMessage(), t);
            AndroidUtil.showToast("记录序号生成错误");
            result = "01";
        } finally {
            db.close();
        }
        return result;
    }

    private boolean saveNewInventory() {
        boolean result = false;
        SQLiteOpenHelper helper = new SQLiteDbHelper(getApplicationContext());
        SQLiteDatabase db = null;
        try {
            db = helper.getWritableDatabase();
            db.beginTransaction();
            ///取值
            String storeCode = mTvStoreCode.getText().toString();
            Date listDate = DateUtil.fromStr(mTvDate.getText().toString());
            int idx = Integer.parseInt(mTvIndex.getText().toString());
            String username = mTvUsername.getText().toString();
            String listNo = mTvListNo.getText().toString() + mEtListNo.getText().toString();
            Date create_date = new Date();
            ///生成对象并插入
            Inventory inventory = new Inventory(null, storeCode, listDate, idx, username, listNo, "I", create_date, create_date);
            ContentValues contentValues = SQLiteUtil.toContentValues(inventory);
            long ret = db.insert(SQLiteDbHelper.TABLE_INVENTORY, null, contentValues);
            if(ret == -1)
                throw new Exception("保存盘点单失败");
            mNewId = inventory.id;
            db.setTransactionSuccessful();
            result = true;
        } catch (Throwable t) {
            Log.e(this.getClass().getName(), t.getMessage(), t);
            AndroidUtil.showToast("保存盘点单失败");
        } finally {
            if (db != null) {
                db.endTransaction();
                db.close();
            }
        }
        return result;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.inventory_list_toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_save:
                if(!saveNewInventory()) {
                    AndroidUtil.showToast("盘点单保存失败");
                    break;
                }
                //AndroidUtil.showToast("新盘点单");
                Intent intent = new Intent(this, InventoryActivity.class);
                intent.putExtra("list_id", mNewId);
                startActivityForResult(intent, 3);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==3 && resultCode == 4 && data != null) {
            finish();
        }
    }

    public void btnDateClick(View v) {
        DatetimePickerFragment dateFragment = new DatetimePickerFragment();
        dateFragment.datetimePickerResult = this;
        dateFragment.datePickerMode = true;
        dateFragment.setCalendar(mCalendar);
        dateFragment.show(getSupportFragmentManager(),"date picker");
    }

    @Override
    public void processDatetimePickerResult(Calendar cal) {
        this.mCalendar = cal;
        updateUi();
    }
}
