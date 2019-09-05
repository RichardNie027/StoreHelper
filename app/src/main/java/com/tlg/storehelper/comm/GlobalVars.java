package com.tlg.storehelper.comm;

import android.os.Environment;

public final class GlobalVars {

    /** APK下载本地存储路径 */
    public static final String DL_PATH = Environment.getExternalStorageDirectory().getAbsoluteFile() + "/Download/";
    /** APK文件名 */
    public static final String APK_FILENAME = "StoreHelper.apk";

    /** 店编 */
    public static String storeCode = "";

    /** 当前用户 */
    public static String username = "";

    /** 网络访问令牌 */
    public static String token = "";

}
