package com.triman.wifitest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.util.Log;

public class WifiApBroadcastReceiver extends BroadcastReceiver{
	
	public static final String TAG = "WifiApBroadcastReceiver";
	
	public static final String WIFI_AP_STATE_CHANGED_ACTION = "android.net.wifi.WIFI_AP_STATE_CHANGED";
	public static final String EXTRA_WIFI_AP_STATE = "wifi_state";

	public static final int WIFI_AP_STATE_DISABLING = 0;
	public static final int WIFI_AP_STATE_DISABLED = 1;
	public static final int WIFI_AP_STATE_ENABLING = 2;
	public static final int WIFI_AP_STATE_ENABLED = 3;
	public static final int WIFI_AP_STATE_FAILED = 4;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if(action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)){
			Log.v(TAG, "WIFI_STATE_CHANGED_ACTION");
			int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
			String str = null;
			switch(wifiState){
			case WifiManager.WIFI_STATE_DISABLED:
				str = "WIFI已经关闭";
				break;
			case WifiManager.WIFI_STATE_DISABLING:
				str = "WIFI正在关闭";
				break;
			case WifiManager.WIFI_STATE_ENABLED:
				break;
			case WifiManager.WIFI_STATE_ENABLING:
				str = "WIFI正在打开";
				break;
			case WifiManager.WIFI_STATE_UNKNOWN:
				str = "WIFI状态未知";
				break;
				default:break;
			}
			Log.v(TAG, str);
		} else if (action.equals(WifiManager.NETWORK_IDS_CHANGED_ACTION)){
			Log.v(TAG, "NETWORK_IDS_CHANGED_ACTION");
		} else if (action.equals(WifiManager.RSSI_CHANGED_ACTION)){
			Log.v(TAG, "RSSI_CHANGED_ACTION");
		} else if (action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)){
			Log.v(TAG, "SCAN_RESULTS_AVAILABLE_ACTION");
		} else if (action.equals(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION)){
			Log.v(TAG, "SUPPLICANT_CONNECTION_CHANGE_ACTION");
		} else if (action.equals(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION)){
			Log.v(TAG, "SUPPLICANT_STATE_CHANGED_ACTION");
		} else if(action.equals(WIFI_AP_STATE_CHANGED_ACTION)){
			int wifiApState = intent.getIntExtra(EXTRA_WIFI_AP_STATE, WIFI_AP_STATE_FAILED);
			String str = null;
			switch(wifiApState){
			case WIFI_AP_STATE_DISABLED:
				str = "WIFI AP 已经关闭";
				break;
			case WIFI_AP_STATE_DISABLING:
				str = "WIFI AP 正在关闭";
				break;
			case WIFI_AP_STATE_ENABLED:
				str = "WIFI AP 已经打开";
				break;
			case WIFI_AP_STATE_ENABLING:
				str = "WIFI AP 正在打开";
				break;
			case WIFI_AP_STATE_FAILED:
				str = "WIFI AP 打开失败";
				break;
				default:break;
			}
			Log.v(TAG, str);
		}
	}
}
