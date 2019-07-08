package com.tlg.storehelper;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.nec.application.MyApplication;
import com.tlg.storehelper.base.BaseAppCompatActivity;
import com.tlg.storehelper.comm.GlobalVars;

public class HomeActivity extends BaseAppCompatActivity {

    private String[] mStoreCodes = null;
    private Spinner mSpinner = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        initView();
    }

    private void initView() {
        // find view
        TextView tvUsername = findViewById(R.id.tvUsername);
        mSpinner = findViewById(R.id.spinner);

        ///获取用户可用门店集合
        mStoreCodes = new String[]{"M04", "704", "204", "202", "M02", "M59"};

        //Bundle extras = getIntent().getExtras();
        //String username = extras.getString("username");
        tvUsername.setText("用户：" + GlobalVars.username);

        //填充店铺列表
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item_select, mStoreCodes);
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_item_drop);
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        spinner.setAdapter(spinnerAdapter);
        if(GlobalVars.storeCode.equals("") && mStoreCodes.length > 0)
            GlobalVars.storeCode = mStoreCodes[0];

        spinner.setOnItemSelectedListener(new StoreCodeOnItemSelectedListener());

    }

    @Override
    public void finish() {
        new AlertDialog.Builder(MyApplication.getInstance())
                .setIcon(android.R.drawable.ic_dialog_info)
                .setTitle("温馨提示")
                .setMessage("是否退出本程序？")
                .setCancelable(false)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        System.exit(0);
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(MyApplication.getInstance(), "欢迎继续使用", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    private class StoreCodeOnItemSelectedListener implements AdapterView.OnItemSelectedListener{
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
            String item  = adapterView.getItemAtPosition(position).toString();
            GlobalVars.storeCode = mStoreCodes[position];
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    }

    public void ivInventoryClick(View v) {
        Intent intent = new Intent(this, InventoryListsActivity.class);
        startActivity(intent);
    }

    public void ivSettingsClick(View v) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    public void ivSearchClick(View v) {
        Toast.makeText(MyApplication.getInstance(), "待实现", Toast.LENGTH_SHORT).show();
    }

    public void btnExitClick(View v) {
        finish();
    }
}
