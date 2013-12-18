package com.triman.wifitest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class MainActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}
	
	public void presideMeeting(){
		Intent intent = new Intent(this, WifiApActivity.class);
		startActivity(intent);
	}
	
	public void attendMeeting(){
		Intent intent = new Intent(this, WifiConnectActivity.class);
		startActivity(intent);
	}
}
