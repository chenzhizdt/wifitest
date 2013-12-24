package com.triman.wifitest;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

public class WifiConnectManager {
	
	public static final String DEFAULT_SSID = "YRCCONNECTION";
	
	private static final String TAG = "WifiConnectManager";
	
	private Activity mActivity;
	private WifiManager wifiManager;
	private List<String> passableAp = new ArrayList<String>();
	private String connectSSID = "YRCCONNECTION";
	private boolean isConnected = false;
	private boolean isEnabled = false;
	private WifiStateBroadcastReceiver receiver;
	private IntentFilter filter;
	private WifiConnectEventListener listener;
	private String currentSSID;
	private State state = State.WIFI_DISABLED;
	
	private enum State{
		WIFI_ENABLED,
		WIFI_DISABLED,
		CONNECTED,
		DISCONNECTED
	}
	
	public State getState(){
		return state;
	}
	
	public String getCurrentSSID() {
		return currentSSID;
	}
	
	public String getConnectSSID() {
		return connectSSID;
	}



	public void setConnectSSID(String connectSSID) {
		this.connectSSID = connectSSID;
	}

	public WifiConnectManager(Activity mActivity){
		this(mActivity, null);
	}

	public WifiConnectManager(Activity mActivity, String SSID){
		this.mActivity = mActivity;
		wifiManager = (WifiManager) mActivity.getSystemService(Context.WIFI_SERVICE);
		receiver = new WifiStateBroadcastReceiver();
		filter = new IntentFilter();
		filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
		filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
		filter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
		if(SSID == null){
			this.connectSSID = DEFAULT_SSID;
		}
	}

	public void setListener(WifiConnectEventListener listener) {
		this.listener = listener;
	}

	public void setWifiConnectEnabled(boolean enabled){
		isEnabled = enabled;
		wifiManager.setWifiEnabled(enabled);
		if(enabled){
			if(wifiManager.isWifiEnabled() || WifiManager.WIFI_STATE_ENABLING == wifiManager.getWifiState()){
				WifiInfo curConnection = wifiManager.getConnectionInfo();
				if(curConnection != null && connectSSID.equals(curConnection.getSSID())){
					isConnected = true;
					if(listener != null){
						listener.onWifiConnectEnabled();
					}
				} else {
					reconnect();
				}
			} else {
				wifiManager.setWifiEnabled(true);
				reconnect();
			}
		} else {
			isConnected = false;
			currentSSID = null;
			passableAp.clear();
			if(!wifiManager.isWifiEnabled()){
				if(listener != null){
					listener.onWifiDisabled();
				}
			} else {
				wifiManager.setWifiEnabled(false);
			}
		}
	}
	
	public void registerListener(){
		mActivity.registerReceiver(receiver, filter);
	}
	
	public void unregisterListener(){
		mActivity.unregisterReceiver(receiver);
	}
	
	public void reconnect(){
		if(isEnabled){
			passableAp.clear();
			wifiManager.startScan();
		}
	}
	private void onReceiveNewNetworks(List<ScanResult> wifiList) {
		passableAp.clear();
		for (ScanResult result : wifiList) {
			Log.v(TAG, "热点：" + result.SSID);
			if ((result.SSID).contains(connectSSID))
				passableAp.add(result.SSID);
		}
		if(passableAp.size() == 0){
			Log.v(TAG, "未找到匹配的热点，重新扫描热点！");
			reconnect();
		} else {
			connectToAp();
		}
	}
	
	private void connectToAp() {
		WifiConfiguration wifiConfig = setWifiParams(passableAp.get(0));
		wifiManager.disconnect();
		wifiManager.enableNetwork(wifiManager.addNetwork(wifiConfig), true);
		wifiManager.saveConfiguration();
		wifiManager.reconnect();
	}
	
	private WifiConfiguration setWifiParams(String ssid) {
		WifiConfiguration apConfig = new WifiConfiguration();
		apConfig.SSID = "\"" + ssid + "\"";
		apConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
		return apConfig;
	}
	
	private class WifiStateBroadcastReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if(action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)){
				int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
				switch(state){
				case WifiManager.WIFI_STATE_DISABLED:
					if(listener != null){
						listener.onWifiDisabled();
					}
					break;
				case WifiManager.WIFI_STATE_ENABLED:
					if(listener != null){
						listener.onWifiEnabled();
					}
				}
//			} else if(action.equals(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION)){
//				Log.v(TAG, "正在连接热点");
//				WifiInfo info = wifiManager.getConnectionInfo();
//                SupplicantState state = info.getSupplicantState();
//                String str = null;
//                switch(state){
//                case ASSOCIATED :
//                    str = "关联AP完成";
//                    break;
//                case AUTHENTICATING:
//                    str = "正在验证";
//                    break;
//                case ASSOCIATING:
//                    str = "正在关联AP...";
//                    break;
//                case COMPLETED:
//                    str = "已连接";
//                    break;
//                case DISCONNECTED:
//                    str = "已断开";
//                    break;
//                case DORMANT:
//                    str = "暂停活动";
//                    break;
//                case FOUR_WAY_HANDSHAKE:
//                    str = "四路握手中...";
//                    break;
//                case GROUP_HANDSHAKE:
//                    str = "GROUP_HANDSHAKE";
//                    break;
//                case INACTIVE:
//                    str = "休眠中...";
//                    break;
//                case INVALID:
//                    str = "无效";
//                    break;
//                case SCANNING:
//                    str = "扫描中...";
//                    break;
//                case UNINITIALIZED:
//                    str = "未初始化";
//                    break;
//				default:
//					break;
//                }
//                Log.v(TAG, info.getSSID() + "的当前状态：" + str);
			} else if(action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION) && passableAp.size() != 0){
				String str = null;
				NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
				NetworkInfo.State nState = networkInfo.getState();
				switch (nState){
				case CONNECTED:
					str = "网络已连接";
					WifiInfo wifiInfo = wifiManager.getConnectionInfo();
					currentSSID = wifiInfo.getSSID();
					final int ip = wifiInfo.getIpAddress();
					Log.v(TAG, "ip: " + intToIp(ip));
					if(currentSSID.equals(connectSSID)){
						isConnected = true;
						if(listener != null){
							listener.onWifiConnectEnabled();
						}
					}
					break;
				case DISCONNECTED:
					str = "网络已断开";
					if(isConnected && currentSSID != null &&currentSSID.equals(connectSSID)){
						isConnected = false;
						if(listener != null){
							listener.onWifiConnectDisabled();
						}
					}
					break;
//				case CONNECTING:
//					str = "网络连接中";
//					break;
//				case DISCONNECTING:
//					str = "网络正在断开中";
//					break;
//				case SUSPENDED:
//					str = "网络已挂起";
//					break;
//				case UNKNOWN:
//					str = "网络状态未知";
//					break;
					default:break;
				}
				Log.v(TAG, "网络当前状态变更为：" + str);
			} else if(action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)){
				Log.v(TAG, "收到新的扫描结果。");
				if(isEnabled && passableAp.size() == 0){
					onReceiveNewNetworks(wifiManager.getScanResults());
				}
			}
		}
		private String intToIp(int ipAddress){
			String ipString = null;
			if (ipAddress != 0) {
			       ipString = ((ipAddress & 0xff) + "." + (ipAddress >> 8 & 0xff) + "." 
					+ (ipAddress >> 16 & 0xff) + "." + (ipAddress >> 24 & 0xff));
			}
			return ipString;
		}
	}
}
