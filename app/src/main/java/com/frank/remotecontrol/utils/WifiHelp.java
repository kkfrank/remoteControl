package com.frank.remotecontrol.utils;//package com.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;

public class WifiHelp {

    public static boolean isWifiConnect(Context mContext) {
        ConnectivityManager connManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifiInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return mWifiInfo!=null && mWifiInfo.isConnected();
    }

    public static String getWIFILocalIpAdress(Context mContext) {
        WifiManager mWifiManager = (WifiManager)mContext.getSystemService(Context.WIFI_SERVICE);
        if(!mWifiManager.isWifiEnabled()) {
            return null;
        }
        WifiInfo mWifiInfo = mWifiManager.getConnectionInfo();

        int ipAddress = mWifiInfo.getIpAddress();
        return formatIpAddress(ipAddress);
    }
    public static String getWifiRouteIPAddress(Context mContext) {
        WifiManager mWifiManager = (WifiManager)mContext.getSystemService(Context.WIFI_SERVICE);
        if(!mWifiManager.isWifiEnabled()) {
            return null;
        }
        DhcpInfo dhcpInfo = mWifiManager.getDhcpInfo();
        String routeIp = Formatter.formatIpAddress(dhcpInfo.gateway);

        return routeIp;
    }


    private static String formatIpAddress(int ipAdress) {
        return (ipAdress & 0xFF ) + "." +
                ((ipAdress >> 8 ) & 0xFF) + "." +
                ((ipAdress >> 16 ) & 0xFF) + "." +
                ( ipAdress >> 24 & 0xFF) ;
    }

}
