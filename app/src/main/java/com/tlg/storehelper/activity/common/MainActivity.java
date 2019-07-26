package com.tlg.storehelper.activity.common;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;

import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nec.lib.android.boost.CommonProgressDialog;
import com.nec.lib.android.grantor.PermissionListener;
import com.nec.lib.android.grantor.PermissionsUtil;
import com.nec.lib.android.base.BaseRxAppCompatActivity;
import com.nec.lib.android.utils.ResUtil;
import com.nec.lib.android.utils.StringUtil;
import com.tlg.storehelper.MyApp;
import com.tlg.storehelper.R;
import com.tlg.storehelper.comm.GlobalVars;
import com.nec.lib.android.utils.UiUtil;
import com.tlg.storehelper.httprequest.net.entity.SimpleEntity;
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

public class MainActivity extends BaseRxAppCompatActivity {

    private EditText mEditTextName;
    private EditText mEditTextPwd;
    private Button mBtnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mFullScreen = true; //super前
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void requestAccessNetworkAndStorage() {
        PermissionsUtil.requestPermission(getApplication(), new PermissionListener() {
            @Override
            public void permissionGranted(@NonNull String[] permissions) {
                GlobalVars.permissionOfNetworkAndStroage = true;
                afterGrantPermission();
            }

            @Override
            public void permissionDenied(@NonNull String[] permissions) {
                Toast.makeText(MyApp.getInstance(), "访问网络和读写文件被拒绝", Toast.LENGTH_SHORT).show();
            }
        }, Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    private void initView() {
        // find view
        mEditTextName = findViewById(R.id.etUsername);
        mEditTextPwd = findViewById(R.id.etPwd);
        mBtnLogin = findViewById(R.id.btnLogin);

        // initialize controls
        setOnFocusChangeListener(mEditTextName, mEditTextPwd);
        //setHideInputViews(mEditTextName, mEditTextPwd);

        // 初始化系统的屏幕尺寸信息
        UiUtil.getAndroiodScreenProperty(this);

        //load data
        beforeGrantPermission();
        requestAccessNetworkAndStorage();

    }

    private void beforeGrantPermission() {
        mBtnLogin.setVisibility(View.GONE);
    }

    private void afterGrantPermission() {
        if(GlobalVars.permissionOfNetworkAndStroage) {
            mBtnLogin.setVisibility(View.VISIBLE);
            getVersion(localVersion());
        }
    }

    public void btnLoginClick(View v) {
        String username = mEditTextName.getText().toString().toUpperCase();
        String password = mEditTextPwd.getText().toString();
        if(username.equals("")) {
            Toast.makeText(MyApp.getInstance(), "用户名不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        RequestUtil.requestLogin(username, password, this, new RequestUtil.OnSuccessListener<SimpleEntity<String>>() {
            @Override
            public void onSuccess(SimpleEntity<String> response) {
                Intent intent = new Intent(_this, HomeActivity.class);
                String[] array = response.result_list.toArray(new String[response.result_list.size()]);
                intent.putExtra("storeCodes", array);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
    }

    //****************************************************************//
    //                          升级                                  //
    //****************************************************************//

    private static final String DOWNLOAD_DIR_NAME = "Download";     // 下载在服务端的路径名
    private static final String DOWNLOAD_NAME = "StoreHelper.apk";  // S下载在服务端的文件名
    private static final String DOWNLOAD_URL = "http://192.168.1.176:8080/pre_api/downloadApk"; //安装包下载地址

    private CommonProgressDialog pBar;

    public static int localVersion() {
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
                if (newVersionCode != vision)
                    if (vision < newVersionCode) {
                        // 版本号不同
                        ShowDialog(vision, newVersion, content, DOWNLOAD_URL);
                    }
            }
        });
    }

    private void ShowDialog(int vision, String newversion, String content, final String url) {
        new android.app.AlertDialog.Builder(this)
                .setTitle("版本更新")
                .setMessage(content)
                .setPositiveButton("更新", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        pBar = new CommonProgressDialog(MainActivity.this);
                        pBar.setCanceledOnTouchOutside(false);
                        pBar.setTitle("正在下载");
                        //动态构造对话框Layout
                        View rootView = getWindow().getDecorView().getRootView();
                        LinearLayout mLinearLayout = new LinearLayout(rootView.getContext());
                        mLinearLayout.setGravity(Gravity.CENTER);
                        mLinearLayout.setOrientation(LinearLayout.VERTICAL);
                        LinearLayout.LayoutParams mLayoutParams1 = new LinearLayout.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.MATCH_PARENT);
                        mLinearLayout.setLayoutParams(mLayoutParams1);
                        TextView textView = new TextView(rootView.getContext());
                        LinearLayout.LayoutParams mLayoutParams2 = new LinearLayout.LayoutParams(RecyclerView.LayoutParams.WRAP_CONTENT, RecyclerView.LayoutParams.WRAP_CONTENT);
                        textView.setTextColor(Color.WHITE);
                        textView.setTextSize(14);
                        textView.setText("正在下载");
                        mLinearLayout.addView(textView);
                        //设置对话框
                        pBar.setCustomTitle(mLinearLayout);
                        pBar.setMessage("正在下载");
                        pBar.setIndeterminate(true);
                        pBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                        pBar.setCancelable(true);
                        // downFile(URLData.DOWNLOAD_URL);
                        final DownloadTask downloadTask = new DownloadTask(MainActivity.this);
                        downloadTask.execute(url);
                        pBar.setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                downloadTask.cancel(true);
                            }
                        });
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }


    class DownloadTask extends AsyncTask<String, Integer, String> {

        private Context context;
        private PowerManager.WakeLock mWakeLock;

        public DownloadTask(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(String... sUrl) {
            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            File file = null;
            try {
                URL url = new URL(sUrl[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                // expect HTTP 200 OK, so we don't mistakenly save error
                // report
                // instead of the file
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return "Server returned HTTP "
                            + connection.getResponseCode() + " "
                            + connection.getResponseMessage();
                }
                // this will be useful to display download percentage
                // might be -1: server did not report the length
                int fileLength = connection.getContentLength();
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    file = new File(Environment.getExternalStorageDirectory(), DOWNLOAD_DIR_NAME);
                    if (!file.exists()) {
                        file.mkdirs();
                    }
                    if (!file.exists()) {
                        // 判断父文件夹是否存在
                        if (!file.getParentFile().exists()) {
                            file.getParentFile().mkdirs();
                        }
                    }
                    file = new File(file, DOWNLOAD_NAME);
                } else {
                    Toast.makeText(MainActivity.this, "存储未挂载", Toast.LENGTH_LONG).show();
                }
                input = connection.getInputStream();
                output = new FileOutputStream(file);
                byte data[] = new byte[4096];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    // allow canceling with back button
                    if (isCancelled()) {
                        input.close();
                        return null;
                    }
                    total += count;
                    // publishing the progress....
                    if (fileLength > 0) // only if total length is known
                        publishProgress((int) (total * 100 / fileLength));
                    output.write(data, 0, count);

                }
            } catch (Exception e) {
                System.out.println(e.toString());
                return e.toString();

            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException ignored) {
                    ignored.toString();
                }
                if (connection != null)
                    connection.disconnect();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // take CPU lock to prevent CPU from going off if the user
            // presses the power button during download
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
            mWakeLock.acquire();
            pBar.show();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            // if we get here, length is known, now set indeterminate to false
            pBar.setIndeterminate(false);
            pBar.setMax(100);
            pBar.setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            mWakeLock.release();
            pBar.dismiss();
            if (result != null) {
                Log.d(this.getClass().getName(), result);
                Toast.makeText(context, "失败：" + result, Toast.LENGTH_LONG).show();
            } else {
                update();
            }
        }
    }

    //----------------------------------权限回调处理----------------------------------//

//    private static final int REQUEST_CODE_SETTING = 300;
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        switch (requestCode) {
//            case REQUEST_CODE_SETTING: {
//                Toast.makeText(this, "已由设置返回", Toast.LENGTH_LONG).show();
//                //设置成功，再次请求更新
//                getVersion(localVersion());
//                break;
//            }
//        }
//    }

    private void update() {
        //安装应用
        Intent intent = new Intent(Intent.ACTION_VIEW);
        File file = new File(Environment.getExternalStorageDirectory(), DOWNLOAD_DIR_NAME+"/"+DOWNLOAD_NAME);
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
