package com.triman.wifitest;

import java.lang.reflect.Method;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

public class WifiApManager {
	
	public static final String WIFI_AP_STATE_CHANGED_ACTION = "android.net.wifi.WIFI_AP_STATE_CHANGED";
	public static final String EXTRA_WIFI_AP_STATE = "wifi_state";

	public static final int WIFI_AP_STATE_DISABLING = 10;
	public static final int WIFI_AP_STATE_DISABLED = 11;
	public static final int WIFI_AP_STATE_ENABLING = 12;
	public static final int WIFI_AP_STATE_ENABLED = 13;
	public static final int WIFI_AP_STATE_FAILED = 14;
	
	private static final String TAG = "WifiApManager";
	
	private WifiManager wifiManager;
	private WifiApBroadcastReceiver receiver;
	private IntentFilter filter;
	private int state = WIFI_AP_STATE_DISABLED;
	private Activity mActivity;
	private StateChangeListener listener;
	private WifiConfiguration apConfig;
	
	public WifiApManager(Activity mActivity){
		this.mActivity = mActivity;
		this.receiver = new WifiApBroadcastReceiver();
		this.filter = new IntentFilter(WIFI_AP_STATE_CHANGED_ACTION);
		wifiManager = (WifiManager) mActivity.getSystemService(Context.WIFI_SERVICE);
		apConfig = new WifiConfiguration();
		apConfig.SSID = "YRCCONNECTION";
	}
	
	public void setStateChangeListener(StateChangeListener listener){
		if(listener != null){
			this.listener = listener;
		}
	}
	
	public void removeStateChangeListener(){
		if(listener != null){
			listener = null;
		}
	}
	
	public boolean setWifiApEnabled() {
		wifiManager.setWifiEnabled(true);
		try {
			Method method = wifiManager.getClass().getMethod(
					"setWifiApEnabled", WifiConfiguration.class, Boolean.TYPE);
			return (Boolean) method.invoke(wifiManager, apConfig, true);
		} catch (Exception e) {
			return false;
		}
	}

	public boolean setWifiApDisabled() {
		wifiManager.setWifiEnabled(false);
		return true;
	}
	
	public void beginStateChangeListen(){
		mActivity.unregisterReceiver(receiver);
	}
	
	public void endStateChangeListen(){
		mActivity.registerReceiver(receiver, filter);
	}
	
	public int getState(){
		return state;
	}
	
	class WifiApBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			int wifiApState = intent.getIntExtra(EXTRA_WIFI_AP_STATE,
					WIFI_AP_STATE_FAILED);
			String str = null;
			switch (wifiApState) {
			case WIFI_AP_STATE_DISABLED:
				str = "已经关闭";
				listener.onDisabled();
				break;
			case WIFI_AP_STATE_DISABLING:
				str = "正在关闭";
				listener.onDisabling();
				break;
			case WIFI_AP_STATE_ENABLED:
				str = "已经打开";
				listener.onEnabled();
				break;
			case WIFI_AP_STATE_ENABLING:
				str = "正在打开";
				listener.onEnabling();
				break;
			case WIFI_AP_STATE_FAILED:
				str = "打开失败";
				listener.onFailed();
				break;
			default:
				break;
			}
			stateChangelog(str);
		}
		
		private void stateChangelog(String message){
			if(message != null){
				Log.v(TAG, "Wifi Ap " + message);
			} else {
				Log.v(TAG, "Message must be not null!");
			}
		}
	}
	
	public interface StateChangeListener {
		/**
		 * WifiAp正式启动时触发该事件
		 */
		public void onEnabled();
		/**
		 * WifiAp正在启动时触发该事件
		 */
		public void onEnabling();
		/**
		 * WifiAp失败时触发该事件
		 */
		public void onFailed();
		/**
		 * WifiAp关闭时触发该事件
		 */
		public void onDisabled();
		/**
		 * WifiAp正在关闭时触发该事件
		 */
		public void onDisabling();
	}
}
