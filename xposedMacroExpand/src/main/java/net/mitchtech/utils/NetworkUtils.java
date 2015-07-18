package net.mitchtech.utils;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.util.List;

public class NetworkUtils {

    private static final String TAG = NetworkUtils.class.getSimpleName();

    public static String getMacAddress(Context context) {
        WifiManager wifiMan = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInf = wifiMan.getConnectionInfo();
        String macAddress = wifiInf.getMacAddress();
        return macAddress;
    }

    public static String getIpAddress(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ip = wifiInfo.getIpAddress();

        String ipAddress = String.format("%d.%d.%d.%d",
                (ip & 0xff),
                (ip >> 8 & 0xff),
                (ip >> 16 & 0xff),
                (ip >> 24 & 0xff));
        return ipAddress;
    }

    public static String getPublicIpAddress() {
        try {
            HttpClient httpclient = new DefaultHttpClient();
            // HttpGet httpget = new HttpGet("http://ip2country.sourceforge.net/ip2c.php?format=JSON");
            // HttpGet httpget = new HttpGet("http://whatismyip.com.au/");
            HttpGet httpget = new HttpGet("http://www.whatismyip.org/");

            HttpResponse response;
            response = httpclient.execute(httpget);
            HttpEntity entity = response.getEntity();
            entity.getContentLength();

            String ipAdd = EntityUtils.toString(entity);
            JSONObject json_data = new JSONObject(ipAdd);
            String ipAddress = json_data.getString("ip");
            return ipAddress;
        } catch (Exception ex) {
            Log.e(TAG, ex.toString());
        }
        return null;
    }

    public static String getWifiSsid(Context context) {
        WifiManager wifiMgr = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
        String wifiSsid = wifiInfo.getSSID().replace("\"", "").trim();
        return wifiSsid;
    }

    public static String getSignalStrength(Context context) {
        WifiManager wifiMgr = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
        String signalStrength = String.valueOf(wifiInfo.getRssi());
        return signalStrength;
    }

    public static String getLinkSpeed(Context context) {
        WifiManager wifiMgr = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
        String linkSpeed = String.valueOf(wifiInfo.getLinkSpeed()) + " Mbps";
        return linkSpeed;
    }

    public static String getWifiSecurityType(Context context) {
        WifiManager wifiMgr = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        List<ScanResult> networkList = wifiMgr.getScanResults();
        WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
        String ssid = wifiInfo.getSSID().replaceAll("^\"|\"$", "");
        if (networkList != null) {
            for (ScanResult network : networkList) {
                if (ssid.contentEquals(network.SSID)) {
                    String capabilities = network.capabilities;
//                    Log.i(TAG, "Security:[" + capabilities + "]");
//                    if (capabilities.contains("WPA")) {
//                        // WPA encryption
//                    } else if (capabilities.contains("WEP")) {
//                        // WEP encryption
//                    } else {
//                        // open wifi, captive portal, other
//                    }
                    return capabilities;
                }
            }
        }
        return null;
    }

}
