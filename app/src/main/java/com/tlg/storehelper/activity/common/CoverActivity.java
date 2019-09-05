package com.tlg.storehelper.activity.common;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.nec.lib.android.base.BaseRxAppCompatActivity;
import com.nec.lib.android.boost.CommonProgressDialog;
import com.nec.lib.android.utils.AndroidUtil;
import com.nec.lib.android.utils.DownloadUtil;
import com.nec.lib.android.utils.StringUtil;
import com.nec.lib.android.utils.UiUtil;
import com.tlg.storehelper.MyApp;
import com.tlg.storehelper.R;
import com.tlg.storehelper.comm.GlobalVars;
import com.tlg.storehelper.httprequest.net.entity.SimpleMapEntity;
import com.tlg.storehelper.httprequest.utils.RequestUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class CoverActivity extends BaseRxAppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mFullScreen = true; //super前
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cover);
        initView();
    }

    private void initView() {
        // find view
        Button btnEnter = findViewById(R.id.btnEnter);

        // initialize controls
        hideKeyboard(true);
        CoverActivity coverActivity = this;
        btnEnter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CoverActivityPermissionsDispatcher.enterAppWithPermissionCheck(coverActivity);
            }
        });

        // 初始化系统的屏幕尺寸信息
        UiUtil.getAndroiodScreenProperty(this);
    }


    @NeedsPermission({Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    public void enterApp() {
        getVersion(localVersion());
    }

    private void enterAppComeOn() {
        Intent intent = new Intent(_this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @SuppressLint("NeedOnRequestPermissionsResult")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        CoverActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @OnShowRationale({Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void showRationale(final PermissionRequest request) {
        new AlertDialog.Builder(this)
                .setPositiveButton("好的", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        request.proceed();
                    }
                })
                .setNegativeButton("不给", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        request.cancel();
                    }
                })
                .setCancelable(false)
                .setMessage("系统涉及网络和文件读写，需要申请权限")
                .show();
    }

    @OnPermissionDenied({Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void permissionDenied() {
        AndroidUtil.showToast("权限不足，无法进入系统");
    }

    @OnNeverAskAgain({Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void neverAskAgain() {
        new AlertDialog.Builder(this)
                .setPositiveButton("好的", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startAppDetailSetting();
                        dialog.cancel();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setCancelable(false)
                .setMessage("您已经禁止了必要权限,是否现在去开启")
                .show();
    }

    /**
     * 跳转到权限设置界面
     */
    private void startAppDetailSetting(){
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if(Build.VERSION.SDK_INT >= 9){
            intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            intent.setData(Uri.fromParts("package", getPackageName(), null));
        } else if(Build.VERSION.SDK_INT <= 8){
            intent.setAction(Intent.ACTION_VIEW);
            intent.setClassName("com.android.settings","com.android.settings.InstalledAppDetails");
            intent.putExtra("com.android.settings.ApplicationPkgName", getPackageName());
        }
        startActivity(intent);
    }

    //****************************************************************//
    //                          升级                                  //
    //****************************************************************//

    private int localVersion() {
        try {
            PackageManager manager = MyApp.getInstance().getPackageManager();
            PackageInfo info = manager.getPackageInfo(MyApp.getInstance().getPackageName(),0);
            String version = info.versionName;
            int versioncode = info.versionCode;
            return versioncode;
        } catch (Exception e) {
            Log.e(MyApp.getCurrentActivity().getClass().getName(), e.getMessage(), e);
        }
        return 0;
    }

    private void getVersion(final int vision) {
        RequestUtil.requestAppVersion(this, new RequestUtil.OnSuccessListener<SimpleMapEntity>() {

            @Override
            public void onSuccess(SimpleMapEntity response) {
                String newVersion = response.result.get("versionCode").toString();      //网络版本号
                int newVersionCode = new Double(StringUtil.parseDouble(newVersion, 0)).intValue();
                String content = response.msg;
                if (vision < newVersionCode) {
                    // 版本号不同
                    new DownloadUtil(GlobalVars.DL_PATH, GlobalVars.APK_FILENAME, MyApp.baseUrl + "pre_api/downloadApk", true, new DownloadUtil.DownloadListener() {
                        @Override
                        public void onDownloadFail(String localFilePath, String msg) {
                            AndroidUtil.showAlertDialog("更新失败", "请联系信息部或稍后再试\n" + msg);
                        }
                        @Override
                        public void onDownloaded(String localFilePath) {
                            updateApk(localFilePath);
                        }
                    }).showDialog("版本更新", response.msg, "更新");

                } else
                    enterAppComeOn();   //进入登录界面
            }
        });
    }

    private void updateApk(String filePath) {
        //安装应用
        Intent intent = new Intent(Intent.ACTION_VIEW);
        File file = new File(filePath);
        if(file.length() > 100) {
            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
            startActivity(intent);
        } else {
            StringBuffer lines = new StringBuffer();
            try (FileReader reader = new FileReader(file);
                 BufferedReader br = new BufferedReader(reader)
            ) {
                String line;
                while ((line = br.readLine()) != null) {
                    lines.append(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            new android.app.AlertDialog.Builder(this)
                    .setTitle("更新失败")
                    .setMessage(lines.toString())
                    .setPositiveButton("确定", null)
                    .show();
        }
    }

}
