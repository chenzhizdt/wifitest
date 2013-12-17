package com.triman.wifitest;

import java.lang.reflect.Method;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;

import com.triman.wifitest.utils.netty.MessageServer;

import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {
	
	private static final String TAG = "MainActivity";

	public static final String WIFI_AP_STATE_CHANGED_ACTION = "android.net.wifi.WIFI_AP_STATE_CHANGED";
	public static final String EXTRA_WIFI_AP_STATE = "wifi_state";

	public static final int WIFI_AP_STATE_DISABLING = 10;
	public static final int WIFI_AP_STATE_DISABLED = 11;
	public static final int WIFI_AP_STATE_ENABLING = 12;
	public static final int WIFI_AP_STATE_ENABLED = 13;
	public static final int WIFI_AP_STATE_FAILED = 14;
	
	private WifiManager wifiManager;
	private Button start;
	private State state;
	private Button connect;
	private WifiApBroadcastReceiver receiver;
	private IntentFilter filter;
	private Handler handler = new Handler();
	private MessageServer messageServer;
	private Channel channel;
	private enum State {
		CLOSED,
		CLOSING,
		OPENED,
		OPENING
	}
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
		wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		start = (Button) findViewById(R.id.start_ap);
		connect = (Button) findViewById(R.id.connect_ap);
		state = State.CLOSED;
		receiver = new WifiApBroadcastReceiver();
		filter = new IntentFilter();
		filter.addAction(WIFI_AP_STATE_CHANGED_ACTION);
		messageServer = new MessageServer();
		
		changeToClosed();
		start.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				switch(state){
				case CLOSED:
					changeToOpening();
					break;
				case OPENED:
					changeToClosing();
					break;
				default:
					break;
				}
			}
		});
		connect.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, WifiConnectActivity.class);
				startActivity(intent);
			}
		});
	}
	
	private void changeToOpening() {
		start.setText("正在启动...");
		start.setEnabled(false);
		setWifiApEnabled(true);
		state = State.OPENING;
	}
	
	private void changeToOpened() {
		start.setText("关闭");
		start.setEnabled(true);
		state = State.OPENED;
	}
	
	private void changeToClosing() {
		start.setText("正在关闭...");
		start.setEnabled(false);
		state = State.CLOSING;
		setWifiApEnabled(false);
		stopServer();
	}
	
	private void changeToClosed() {
		start.setText("启动");
		start.setEnabled(true);
		state = State.CLOSED;
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(receiver, filter);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(receiver);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		setWifiApEnabled(false);
	}
	
	private boolean setWifiApEnabled(boolean enabled) {
		if (enabled) {
			wifiManager.setWifiEnabled(false);
		}
		try {
			WifiConfiguration apConfig = new WifiConfiguration();
			apConfig.SSID = "YRCCONNECTION";
			Method method = wifiManager.getClass().getMethod(
					"setWifiApEnabled", WifiConfiguration.class, Boolean.TYPE);
			return (Boolean) method.invoke(wifiManager, apConfig, enabled);
		} catch (Exception e) {
			return false;
		}
	}
	
	private void startServer(){
		final ChannelFuture channelFuture = messageServer.start(9090);
		channelFuture.awaitUninterruptibly();
		if(channelFuture.isSuccess()){
			handler.post(new Runnable() {
				
				@Override
				public void run() {
					Builder dialog = new AlertDialog.Builder(MainActivity.this);
					dialog.setTitle("提示");
					dialog.setMessage("成功启动服务器");
					dialog.setNegativeButton("确定", null);
					dialog.show();
					changeToOpened();
					channel = channelFuture.getChannel();
				}
			});
		} else {
			handler.post(new Runnable() {
				@Override
				public void run() {
					Builder dialog = new AlertDialog.Builder(MainActivity.this);
					dialog.setTitle("提示");
					dialog.setMessage("启动服务器失败");
					dialog.setNegativeButton("确定", null);
					dialog.show();
					stopServer();
				}
			});
		}
	}
	
	private void stopServer(){
		channel = null;
		messageServer.shutdown();
	}
	
	class WifiApBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			int wifiApState = intent.getIntExtra(EXTRA_WIFI_AP_STATE,
					WIFI_AP_STATE_FAILED);
			String str = "WIFI AP 默认消息";
			switch (wifiApState) {
			case WIFI_AP_STATE_DISABLED:
				str = "WIFI AP 已经关闭";
				changeToClosed();
				break;
			case WIFI_AP_STATE_DISABLING:
				str = "WIFI AP 正在关闭";
				break;
			case WIFI_AP_STATE_ENABLED:
				str = "WIFI AP 已经打开";
				if(state ==State.OPENING){
					Thread t = new Thread(new Runnable() {
						@Override
						public void run() {
							Log.v(TAG, "正在启动服务器");
							startServer();
						}
					});
					t.start();
				}
				break;
			case WIFI_AP_STATE_ENABLING:
				str = "WIFI AP 正在打开";
				break;
			case WIFI_AP_STATE_FAILED:
				str = "WIFI AP 打开失败";
				break;
			default:
				break;
			}
			Log.v(TAG, str);
		}
	}
}
