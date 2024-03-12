package com.gameshock.generateaiimage.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Const {

//    public static String ak = "c0657ac3-20e8-4e3d-aa49-a76f5121c7e0";
    public static String ak = "dd3d472b-7c76-46da-9e3b-2c666a0d36fa";
    public static String gurl = "https://cloud.leonardo.ai/api/rest/v1/generations";

    //
    public static String adUnitIdRewarded = "Rewarded_Android";
    public static String unityGameID = "5483168";
    public static Boolean testMode = true;

    //
    public static Boolean isResultBack = false;
    public static Boolean isGeneratePicButton = false;

    //internet permission
    public static class InternetConnection {

        /**
         * CHECK WHETHER INTERNET CONNECTION IS AVAILABLE OR NOT
         */
        public static boolean checkConnection(Context context) {
            final ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            if (connMgr != null) {
                NetworkInfo activeNetworkInfo = connMgr.getActiveNetworkInfo();

                if (activeNetworkInfo != null) { // connected to the internet
                    // connected to the mobile provider's data plan
                    if (activeNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                        // connected to wifi
                        return true;
                    } else return activeNetworkInfo.getType() == ConnectivityManager.TYPE_MOBILE;
                }
            }
            return false;
        }
    }
}
