package com.triman.wifitest;

import java.util.ArrayList;
import java.util.List;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;

import com.triman.wifitest.utils.netty.MessageClient;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
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
import android.net.wifi.WifiManager.WifiLock;
import android.os.Bundle;
import android.os.Handler;
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
	private MessageClient messageClient;
	private Channel channel;
	private Handler handler = new Handler();
	private String name;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wifi_connect);
		
		wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		
		connect = (Button) findViewById(R.id.start_connect_ap);
		
		messageClient = new MessageClient();
		
		name = getIntent().getStringExtra(MainActivity.EXTRA_NAME);
		
		connect.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(isScan){
					isScan = false;
					isConnected = false;
					isConnecting = false;
					connect.setText(R.string.start_connect_ap);
					wifiManager.setWifiEnabled(false);
					stopClient();
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
	
	private void startClient(String host, int port){
		final ChannelFuture channelFuture = messageClient.start(host, port, name);
		channelFuture.awaitUninterruptibly();
		if(channelFuture.isSuccess()){
			handler.post(new Runnable() {
				
				@Override
				public void run() {
					Builder dialog = new AlertDialog.Builder(WifiConnectActivity.this);
					dialog.setTitle("提示");
					dialog.setMessage("成功启动服务器");
					dialog.setNegativeButton("确定", null);
					dialog.show();
					channel = channelFuture.getChannel();
				}
			});
		} else {
			handler.post(new Runnable() {
				@Override
				public void run() {
					Builder dialog = new AlertDialog.Builder(WifiConnectActivity.this);
					dialog.setTitle("提示");
					dialog.setMessage("启动服务器失败");
					dialog.setNegativeButton("确定", null);
					dialog.show();
					stopClient();
				}
			});
		}
	}
	
	private void stopClient(){
		channel = null;
		messageClient.shutdown();
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
				} else if(action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)){
					String str = null;
					NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
					NetworkInfo.State nState = networkInfo.getState();
					switch (nState){
					case CONNECTED:
						str = "网络已连接";
						WifiInfo wifiInfo = wifiManager.getConnectionInfo();
						final int ip = wifiInfo.getIpAddress();
						Log.v(TAG, "ip: " + intToIp(ip));
						Thread t = new Thread(new Runnable() {
							
							@Override
							public void run() {
								Log.v(TAG, "正在启动客户端");
								startClient("192.168.43.1", 9090);
							}
						});
						t.start();
						break;
					case DISCONNECTED:
						str = "网络已断开";
						break;
					case CONNECTING:
						str = "网络连接中";
						break;
					case DISCONNECTING:
						str = "网络正在断开中";
						break;
					case SUSPENDED:
						str = "网络已挂起";
						break;
					case UNKNOWN:
						str = "网络状态未知";
						break;
						default:break;
					}
					Log.v(TAG, "网络当前状态变更为：" + str);
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
