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
	/**
	 * 默认的Ap广播名称
	 */
	public static final String DEFAULT_SSID = "YRCCONNECTION";
	
	private static final String TAG = "WifiApManager";
	
	private WifiManager wifiManager;
	private WifiApBroadcastReceiver receiver;
	private IntentFilter filter;
	private int state = WIFI_AP_STATE_DISABLED;
	private Activity mActivity;
	private WifiApEventListener listener;
	private WifiConfiguration apConfig;
	
	public WifiApManager(Activity mActivity){
		this(mActivity, null);
	}
	
	public WifiApManager(Activity mActivity, WifiConfiguration apConfig){
		this.mActivity = mActivity;
		this.receiver = new WifiApBroadcastReceiver();
		this.filter = new IntentFilter(WIFI_AP_STATE_CHANGED_ACTION);
		wifiManager = (WifiManager) mActivity.getSystemService(Context.WIFI_SERVICE);
		if(apConfig == null){
			this.apConfig = new WifiConfiguration();
			this.apConfig.SSID = "YRCCONNECTION";
		}
	}
	
	public WifiConfiguration getApConfig() {
		return apConfig;
	}

	public void setApConfig(WifiConfiguration apConfig) {
		if(apConfig != null)
			this.apConfig = apConfig;
	}

	public void setListener(WifiApEventListener listener){
		this.listener = listener;
	}
	
	public boolean setWifiApEnabled(boolean enabled) {
		if (enabled) {
			wifiManager.setWifiEnabled(false);
		}
		try {
			//由于api中没有导出设置WifiAp启动的方法，所以需要利用java的反射机制去获取该方法
			Method method = wifiManager.getClass().getMethod(
					"setWifiApEnabled", WifiConfiguration.class, Boolean.TYPE);
			return (Boolean) method.invoke(wifiManager, apConfig, enabled);
		} catch (Exception e) {
			return false;
		}
	}
	/**
	 * 状态变化事件的监听需要手动注册
	 */
	public void registerListener(){
		mActivity.registerReceiver(receiver, filter);
	}
	
	public void unregisterListener(){
		mActivity.unregisterReceiver(receiver);
	}
	
	public int getState(){
		return state;
	}
	
	private class WifiApBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			int wifiApState = intent.getIntExtra(EXTRA_WIFI_AP_STATE,
					WIFI_AP_STATE_FAILED);
			String str = null;
			switch (wifiApState) {
			case WIFI_AP_STATE_DISABLED:
				str = "已经关闭";
				state = WIFI_AP_STATE_DISABLED;
				if(listener != null)
					listener.onDisabled();
				break;
			case WIFI_AP_STATE_DISABLING:
				str = "正在关闭";
				state = WIFI_AP_STATE_DISABLING;
				if(listener != null)
					listener.onDisabling();
				break;
			case WIFI_AP_STATE_ENABLED:
				str = "已经打开";
				state = WIFI_AP_STATE_ENABLED;
				if(listener != null)
					listener.onEnabled();
				break;
			case WIFI_AP_STATE_ENABLING:
				str = "正在打开";
				state = WIFI_AP_STATE_ENABLING;
				if(listener != null)
					listener.onEnabling();
				break;
			case WIFI_AP_STATE_FAILED:
				str = "打开失败";
				state = WIFI_AP_STATE_FAILED;
				if(listener != null)
					listener.onFailed();
				break;
			default:
				break;
			}
			stateChangelog(str);
		}
		
		private void stateChangelog(String message){
			if(message != null){
				Log.v(TAG, "热点：" + apConfig.SSID +" " + message);
			} else {
				Log.v(TAG, "Message must be not null!");
			}
		}
	}
}
