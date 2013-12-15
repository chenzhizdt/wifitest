package com.triman.wifitest;

import java.lang.reflect.Method;

import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {
	
	private WifiManager wifiManager;
	private Button start;
	private boolean flag;
	private Button connect;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		start = (Button) findViewById(R.id.start_ap);
		start.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				flag = !flag;
				setWifiApEnabled(flag);
			}
		});
		connect = (Button) findViewById(R.id.connect_ap);
		connect.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, WifiConnectActivity.class);
				startActivity(intent);
			}
		});
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
			// 热点的配置类
			WifiConfiguration apConfig = new WifiConfiguration();
			// 配置热点的名称(可以在名字后面加点随机数什么的)
			apConfig.SSID = "YRCCONNECTION";
			// 配置热点的密码
			// 通过反射调用设置热点
			Method method = wifiManager.getClass().getMethod(
					"setWifiApEnabled", WifiConfiguration.class, Boolean.TYPE);
			// 返回热点打开状态
			return (Boolean) method.invoke(wifiManager, apConfig, enabled);
		} catch (Exception e) {
			return false;
		}
	}
}
