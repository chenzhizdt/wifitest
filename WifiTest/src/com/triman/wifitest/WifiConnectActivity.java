package com.triman.wifitest;

import org.jboss.netty.channel.ChannelFuture;

import com.triman.wifitest.utils.netty.MessageClient;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class WifiConnectActivity extends Activity{
	
	private static final String TAG = "WifiConnectActivity";
	
	private enum State {
		CLOSED,
		CLOSING,
		OPENED,
		OPENING
	}
	
	private Button connect;
	private WifiConnectManager wifiConnectManager;
	private MessageClient messageClient;
	private Handler handler = new Handler();
	private String name;
	private State state = State.CLOSED;
	private State prePauseState;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wifi_connect);
		
		wifiConnectManager = new WifiConnectManager(this);
		wifiConnectManager.setListener(new ConnectEventListener());
		
		connect = (Button) findViewById(R.id.connect);
		
		messageClient = new MessageClient();
		
		name = getIntent().getStringExtra(MainActivity.EXTRA_NAME);
		
		connect.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				switch(state){
				case CLOSED:
					changeToOpening();
					wifiConnectManager.setWifiConnectEnabled(true);
					break;
				case OPENED:
					changeToClosing();
					stopClient();
					wifiConnectManager.setWifiConnectEnabled(false);
					break;
				default:
					break;
				}
			}
		});
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		wifiConnectManager.registerListener();
		if(prePauseState == null || prePauseState == State.OPENED || prePauseState == State.OPENING){
			changeToOpening();
			wifiConnectManager.setWifiConnectEnabled(true);
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		prePauseState = state;
		wifiConnectManager.unregisterListener();
		wifiConnectManager.setWifiConnectEnabled(false);
		stopClient();
		changeToClosed();
	}
	
	private void changeToOpening() {
		connect.setText("正在连接...");
		connect.setEnabled(false);
		state = State.OPENING;
	}
	
	private void changeToOpened() {
		connect.setText("关闭");
		connect.setEnabled(true);
		state = State.OPENED;
	}
	
	private void changeToClosing() {
		connect.setText("正在关闭...");
		connect.setEnabled(false);
		state = State.CLOSING;
	}
	
	private void changeToClosed() {
		connect.setText("连接");
		connect.setEnabled(true);
		state = State.CLOSED;
	}
	
	private void startClient(){
		final ChannelFuture channelFuture = messageClient.start("192.168.43.1", 9090, name);
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
					changeToOpened();
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
					changeToClosed();
					stopClient();
					wifiConnectManager.setWifiConnectEnabled(false);
				}
			});
		}
	}
	
	private void stopClient(){
		messageClient.shutdown();
	}

	private class ConnectEventListener extends BaseWifiConnectEventListener{
		@Override
		public void onWifiEnabled() {
			Log.v(TAG, "WifiEnabled");
		}

		@Override
		public void onWifiDisabled() {
			if(state == State.CLOSING){
				changeToClosed();
			}
			Log.v(TAG, "WifiDisabled");
		}

		@Override
		public void onWifiConnectEnabled() {
			if(state == State.OPENING){
				Thread t = new Thread(new Runnable() {
					@Override
					public void run() {
						startClient();
					}
				});
				t.start();
			}
			Log.v(TAG, "WifiConnectEnabled");
		}

		@Override
		public void onWifiConnectDisabled() {
			if(state == State.OPENED){
				stopClient();
				changeToClosing();
				wifiConnectManager.setWifiConnectEnabled(false);
			}
			Log.v(TAG, "WifiConnectDisabled");
		}
	}
}
