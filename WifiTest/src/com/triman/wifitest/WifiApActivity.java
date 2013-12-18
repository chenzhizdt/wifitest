package com.triman.wifitest;

import org.jboss.netty.channel.ChannelFuture;

import com.triman.wifitest.WifiApManager.StateChangeListener;
import com.triman.wifitest.utils.netty.MessageServer;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.view.View;
import android.widget.Button;

public class WifiApActivity extends Activity implements StateChangeListener{
	
	private Button start;
	private State state;
	private Handler handler = new Handler();
	private MessageServer messageServer;
	private WifiApManager wifiApManager;
	private String name;
	private enum State {
		CLOSED,
		CLOSING,
		OPENED,
		OPENING
	}
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_wifi_ap);
		start = (Button) findViewById(R.id.start_ap);
		state = State.CLOSED;
		messageServer = new MessageServer();
		wifiApManager = new WifiApManager(this);
		wifiApManager.setStateChangeListener(this);
		name = getIntent().getStringExtra(MainActivity.EXTRA_NAME);
		
		start.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				switch(state){
				case CLOSED:
					changeToOpening();
					wifiApManager.setWifiApEnabled(true);
					break;
				case OPENED:
					changeToClosing();
					stopServer();
					wifiApManager.setWifiApEnabled(false);
					break;
				default:
					break;
				}
			}
		});
	}
	
	private void changeToOpening() {
		start.setText("正在启动...");
		start.setEnabled(false);
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
	}
	
	private void changeToClosed() {
		start.setText("启动");
		start.setEnabled(true);
		state = State.CLOSED;
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		wifiApManager.beginStateChangeListen();
		changeToOpening();
		wifiApManager.setWifiApEnabled(true);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		changeToClosed();
		stopServer();
		wifiApManager.setWifiApEnabled(false);
		wifiApManager.endStateChangeListen();
	}
	
	private void startServer(){
		final ChannelFuture channelFuture = messageServer.start(9090, name);
		channelFuture.awaitUninterruptibly();
		if(channelFuture.isSuccess()){
			handler.post(new Runnable() {
				
				@Override
				public void run() {
					Builder dialog = new AlertDialog.Builder(WifiApActivity.this);
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
					Builder dialog = new AlertDialog.Builder(WifiApActivity.this);
					dialog.setTitle("提示");
					dialog.setMessage("启动服务器失败");
					dialog.setNegativeButton("确定", null);
					dialog.show();
					stopServer();
					changeToClosed();
				}
			});
		}
	}
	
	private void stopServer(){
		messageServer.shutdown();
	}

	@Override
	public void onEnabled() {
		if(state == State.OPENING){
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					startServer();
				}
			});
			t.start();
		}
	}

	@Override
	public void onEnabling() {
		
	}

	@Override
	public void onFailed() {
		changeToClosed();
	}

	@Override
	public void onDisabled() {
		changeToClosed();
	}

	@Override
	public void onDisabling() {
		changeToClosing();
		stopServer();
	}
}
