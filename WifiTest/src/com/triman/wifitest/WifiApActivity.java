package com.triman.wifitest;

import java.util.ArrayList;

import org.jboss.netty.channel.ChannelFuture;

import com.triman.wifitest.WifiApManager.StateChangeListener;
import com.triman.wifitest.utils.netty.Connection;
import com.triman.wifitest.utils.netty.ConnectionListener;
import com.triman.wifitest.utils.netty.ConnectionManager;
import com.triman.wifitest.utils.netty.MessageServer;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

public class WifiApActivity extends Activity implements StateChangeListener, ConnectionListener{
	
	private static final String TAG = "WifiApActivity";
	
	private Button start;
	private ArrayList<Participant> participants;
	private ArrayAdapter<Participant> adapter;
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
		start = (Button) findViewById(R.id.btn_start_ap);
		ListView lvParticipants = (ListView) findViewById(R.id.lv_participants);
		participants = new ArrayList<Participant>();
		adapter = new ArrayAdapter<Participant>(this, android.R.layout.simple_list_item_1, participants);
		lvParticipants.setAdapter(adapter);
		state = State.CLOSED;
		messageServer = new MessageServer();
		wifiApManager = new WifiApManager(this);
		wifiApManager.setStateChangeListener(this);
		ConnectionManager.getInstance().setConnectionListener(this);
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
	
	private void removeParticipant(int connectionId){
		Participant old = null;
		for(Participant p : participants){
			if(p.getConnectionId() == connectionId){
				old = p;
				break;
			}
		}
		if(old != null){
			participants.remove(old);
			adapter.notifyDataSetChanged();
		}
	}
	
	private void addParticipant(Participant p){
		this.participants.add(p);
		adapter.notifyDataSetChanged();
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
		if(state == State.CLOSING)
			changeToClosed();
	}

	@Override
	public void onDisabling() {
		if(state == State.OPENED){
			changeToClosing();
			stopServer();
		}
	}

	@Override
	public void onNewConnection(Connection c) {
		
	}

	@Override
	public void onRemoveConnection(final Connection c) {
		handler.post(new Runnable() {
			
			@Override
			public void run() {
				removeParticipant(c.getId());
				Log.v(TAG, "remove " + c.getAttachment().toString());
			}
		});
	}

	@Override
	public void onStableConnection(final Connection c) {
		final Participant p = new Participant(c.getAttachment().toString(), c.getId());
		handler.post(new Runnable() {
			
			@Override
			public void run() {
				addParticipant(p);
				Log.v(TAG, "add " + c.getAttachment().toString());
			}
		});
	}
}
