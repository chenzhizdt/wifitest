package com.triman.wifitest;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class WifiConnectActivity extends Activity {
	
	private static final String TAG = "WifiConnectActivity";
	
	private Button connect;
	
	private WifiBroadcastReceiver receiver;
	private boolean isConnected = false;
	private boolean isConnecting = false;
	private WifiLock wifiLock;
	private boolean isScan = false;
	private WifiManager wifiManager;
	private List<ScanResult> wifiList;
	private List<String> passableAp;
	private int wcgId;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wifi_connect);
		
		wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		
		connect = (Button) findViewById(R.id.start_connect_ap);
		connect.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(isScan){
					isScan = false;
					isConnected = false;
					isConnecting = false;
					connect.setText(R.string.start_connect_ap);
					wifiManager.setWifiEnabled(false);
					if(wifiLock != null){
						wifiLock.release();
						wifiLock = null;
					}
				} else {
					isScan = true;
					connect.setText(R.string.close_connect_ap);
					wifiManager.setWifiEnabled(true);
					wifiManager.startScan();
					Log.v(TAG, "启动热点扫描");
					wifiLock = wifiManager.createWifiLock(TAG);
					wifiLock.acquire();
				}
			}
		});
		
		receiver = new WifiBroadcastReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
		filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
		filter.addAction(WifiManager.RSSI_CHANGED_ACTION);
		filter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
		filter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
		filter.addAction(WifiManager.NETWORK_IDS_CHANGED_ACTION);
		registerReceiver(receiver, filter);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(receiver);
		if(wifiLock != null){
			wifiLock.release();
		}
	}
	
	private void onReceiveNewNetworks(List<ScanResult> wifiList) {
		passableAp = new ArrayList<String>();
		for (ScanResult result : wifiList) {
			Log.v(TAG, "热点：" + result.SSID);
			if ((result.SSID).contains("YRCCONNECTION"))
				passableAp.add(result.SSID);
		}
		synchronized (this) {
			connectToAp();
		}
	}
	
	private void connectToAp() {
		if (passableAp == null || passableAp.size() == 0){
			Log.v(TAG, "未找到匹配的热点，重新扫描热点！");
			wifiManager.startScan();
			return;
		}
		WifiConfiguration wifiConfig = setWifiParams(passableAp.get(0));
		wcgId = wifiManager.addNetwork(wifiConfig);
		boolean flag = wifiManager.enableNetwork(wcgId, true);
		wifiManager.saveConfiguration();
		wifiManager.reconnect();
		isConnecting = flag;
		Log.v(TAG, "连接成功？ " + flag);
	}
	
	private WifiConfiguration setWifiParams(String ssid) {
		WifiConfiguration apConfig = new WifiConfiguration();
		apConfig.SSID = "\"" + ssid + "\"";
		apConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
		return apConfig;
	}
	
	private final class WifiBroadcastReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if(isScan && !isConnecting){
				if(action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)){
					Log.v(TAG, "收到新的扫描结果。");
					wifiList = wifiManager.getScanResults();
					int wifiState = wifiManager.getWifiState();
					Log.v(TAG, "当前状态为：" + wifiState);
					onReceiveNewNetworks(wifiList);
				}
			} else if(isScan && isConnecting){
				if(action.equals(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION)){
					Log.v(TAG, "正在连接热点");
					WifiInfo info = wifiManager.getConnectionInfo();
	                SupplicantState state = info.getSupplicantState();
	                String str = null;
	                if (state == SupplicantState.ASSOCIATED){
	                    str = "关联AP完成";
	                } else if(state.toString().equals("AUTHENTICATING")){
	                    str = "正在验证";
	                } else if (state == SupplicantState.ASSOCIATING){
	                    str = "正在关联AP...";
	                } else if (state == SupplicantState.COMPLETED){
	                    str = "已连接";
	                } else if (state == SupplicantState.DISCONNECTED){
	                    str = "已断开";
	                } else if (state == SupplicantState.DORMANT){
	                    str = "暂停活动";
	                } else if (state == SupplicantState.FOUR_WAY_HANDSHAKE){
	                    str = "四路握手中...";
	                } else if (state == SupplicantState.GROUP_HANDSHAKE){
	                    str = "GROUP_HANDSHAKE";
	                } else if (state == SupplicantState.INACTIVE){
	                    str = "休眠中...";
	                } else if (state == SupplicantState.INVALID){
	                    str = "无效";
	                } else if (state == SupplicantState.SCANNING){
	                    str = "扫描中...";
	                } else if (state == SupplicantState.UNINITIALIZED){
	                    str = "未初始化";
	                }
	                Log.v(TAG, info.getSSID() + "的当前状态：" + str);
				}
			}
		}
	}
}
