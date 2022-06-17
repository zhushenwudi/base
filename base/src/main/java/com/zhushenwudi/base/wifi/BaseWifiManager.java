package com.zhushenwudi.base.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public abstract class BaseWifiManager implements IWifiManager {

    static final int WIFI_STATE_DISABLED = 1;
    static final int WIFI_STATE_DISABLING = 2;
    static final int WIFI_STATE_ENABLING = 3;
    static final int WIFI_STATE_ENABLED = 4;
    static final int WIFI_STATE_UNKNOWN = 5;
    static final int WIFI_STATE_MODIFY = 6;
    static final int WIFI_STATE_CONNECTED = 7;
    static final int WIFI_STATE_UNCONNECTED = 8;

    String TAG = "wifiState";

    WifiManager manager;
    List<IWifi> wifis;
    OnWifiChangeListener onWifiChangeListener;
    OnWifiConnectListener onWifiConnectListener;
    OnWifiStateChangeListener onWifiStateChangeListener;
    WifiReceiver wifiReceiver;
    Context context;
    Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WIFI_STATE_DISABLED:
                    if (onWifiStateChangeListener != null)
                        onWifiStateChangeListener.onStateChanged(State.DISABLED);
                    break;
                case WIFI_STATE_DISABLING:
                    if (onWifiStateChangeListener != null)
                        onWifiStateChangeListener.onStateChanged(State.DISABLING);
                    break;
                case WIFI_STATE_ENABLING:
                    if (onWifiStateChangeListener != null)
                        onWifiStateChangeListener.onStateChanged(State.ENABLING);
                    break;
                case WIFI_STATE_ENABLED:
                    if (onWifiStateChangeListener != null)
                        onWifiStateChangeListener.onStateChanged(State.ENABLED);
                    break;
                case WIFI_STATE_UNKNOWN:
                    if (onWifiStateChangeListener != null)
                        onWifiStateChangeListener.onStateChanged(State.UNKNOWN);
                    break;
                case WIFI_STATE_MODIFY:
                    if (onWifiChangeListener != null)
                        onWifiChangeListener.onWifiChanged(wifis);
                    break;
                case WIFI_STATE_CONNECTED:
                    if (onWifiConnectListener != null) {
                        Bundle b = msg.getData();
                        String ssid = b.getString("ssid");
                        onWifiConnectListener.onConnectChanged(true, ssid);
                    }
                    break;
                case WIFI_STATE_UNCONNECTED:
                    if (onWifiConnectListener != null)
                        onWifiConnectListener.onConnectChanged(false, null);
                    break;
            }
        }
    };

    BaseWifiManager(Context context) {
        this.context = context;
        manager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifis = new ArrayList<>();
        wifiReceiver = new WifiReceiver();

        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);//监听wifi是开关变化的状态
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);//监听wifiwifi连接状态广播
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);//监听wifi列表变化（开启一个热点或者关闭一个热点）
        filter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        context.registerReceiver(wifiReceiver, filter);
    }

    @Override
    public void destroy() {
        try {
            context.unregisterReceiver(wifiReceiver);
        } catch (Exception ignored) {
        }
        handler.removeCallbacksAndMessages(null);
        manager = null;
        wifis = null;
        context = null;
    }

    @Override
    public void setOnWifiChangeListener(OnWifiChangeListener onWifiChangeListener) {
        this.onWifiChangeListener = onWifiChangeListener;
    }

    @Override
    public void setOnWifiConnectListener(OnWifiConnectListener onWifiConnectListener) {
        this.onWifiConnectListener = onWifiConnectListener;
    }

    @Override
    public void setOnWifiStateChangeListener(OnWifiStateChangeListener onWifiStateChangeListener) {
        this.onWifiStateChangeListener = onWifiStateChangeListener;
    }

    public class WifiReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // 监听wifi的打开与关闭，与wifi的连接无关
            String action = intent.getAction();

            if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
                int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
                int what = 0;
                switch (state) {
                    case WifiManager.WIFI_STATE_DISABLED: {
                        Log.i(TAG, "已经关闭");
                        what = WIFI_STATE_DISABLED;
                        break;
                    }
                    case WifiManager.WIFI_STATE_DISABLING: {
                        what = WIFI_STATE_DISABLING;
                        Log.i(TAG, "正在关闭");
                        break;
                    }
                    case WifiManager.WIFI_STATE_ENABLED: {
                        what = WIFI_STATE_ENABLED;
                        Log.i(TAG, "已经打开");
//                        Log.i("wifiState","CONNECTED--已经打开");
                        scanWifi();
                        break;
                    }
                    case WifiManager.WIFI_STATE_ENABLING: {
                        Log.i(TAG, "正在打开");
                        what = WIFI_STATE_ENABLING;
                        break;
                    }
                    case WifiManager.WIFI_STATE_UNKNOWN: {
                        Log.i(TAG, "未知状态");
                        what = WIFI_STATE_UNKNOWN;
                        break;
                    }
                }
                handler.sendEmptyMessage(what);
            }

            if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
                NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (info == null) return;
                NetworkInfo.State myState = info.getState();
                if (myState == null) return;
                WifiInfo wifiInfo = manager.getConnectionInfo();
                if (wifiInfo == null) return;
                String SSID = wifiInfo.getSSID();
                if (TextUtils.isEmpty(SSID)) return;
                SSID = SSID.replace("\"", "");//去除双引号

                if (NetworkInfo.State.DISCONNECTED == info.getState()) {//wifi没连接上
                    handler.sendEmptyMessage(WIFI_STATE_UNCONNECTED);
                    modifyList(SSID, "已断开", false);
                } else if (NetworkInfo.State.CONNECTED == info.getState()) {//wifi连接上了
                    Bundle b = new Bundle();
                    b.putString("ssid", SSID);
                    Message message = new Message();
                    message.setData(b);
                    message.what = WIFI_STATE_CONNECTED;
                    handler.sendMessage(message);
                    modifyList(SSID, "已连接", true);
//                    Log.i("wifiState","CONNECTED");
                    scanWifi();
                } else if (NetworkInfo.State.CONNECTING == info.getState()) {//正在连接
                    modifyList(SSID, "连接中...", false);
                } else if (NetworkInfo.State.DISCONNECTING == info.getState()) {
                    modifyList(SSID, "断开中...", false);
                }
            }

            if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    boolean isUpdated = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);
                    if (isUpdated)
                        updateWifiList();
                } else {
                    updateWifiList();
                }
            }
        }
    }

    protected void updateWifiList() {
        synchronized (wifis) {
            try {
                List<ScanResult> results = manager.getScanResults();
                List<IWifi> wifiList = new LinkedList<>();
                List<IWifi> mergeList = new ArrayList<>();
                List<WifiConfiguration> configurations = manager.getConfiguredNetworks();
                String connectedSSID = manager.getConnectionInfo().getSSID();
                int ipAddress = manager.getConnectionInfo().getIpAddress();
                for (ScanResult result : results) {
                    IWifi mergeObj = Wifi.create(result, configurations, connectedSSID, ipAddress);
                    if (mergeObj == null) continue;
                    mergeList.add(mergeObj);
                }
                mergeList = WifiHelper.removeDuplicate(mergeList);
                for (IWifi merge : mergeList) {
                    boolean isMerge = false;
                    for (IWifi wifi : wifis) {
                        if (wifi.equals(merge)) {
                            wifiList.add(wifi.merge(merge));
                            isMerge = true;
                        }
                    }
                    if (!isMerge)
                        wifiList.add(merge);
                }
                wifis.clear();
                wifis.addAll(wifiList);
                handler.sendEmptyMessage(WIFI_STATE_MODIFY);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    protected void modifyList(String SSID, String state, boolean isConnected) {
        synchronized (wifis) {
            try {
//                Log.i("wifiState", "SSID=="+SSID);
                List<IWifi> wifiList = new ArrayList<>();
                for (IWifi wifi : wifis) {
                    if (SSID.equals(wifi.SSID())) {
                        wifi.state(state);
                        wifi.setConnected(true);
                        wifiList.add(0, wifi);
                    } else {
                        wifi.state(null);
                        wifi.setConnected(false);
                        wifiList.add(wifi);
                    }
                }
                wifis.clear();
                wifis.addAll(wifiList);
                handler.sendEmptyMessage(WIFI_STATE_MODIFY);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
