package com.triman.wifitest;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends Activity {
	
	public static final String EXTRA_NAME = "name";
	
	private EditText etName;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		etName = (EditText) findViewById(R.id.et_name);
	}
	
	public void presideMeeting(View v){
		String name = getName();
		if(name != null){
			Intent intent = new Intent(this, WifiApActivity.class);
			intent.putExtra(EXTRA_NAME, name);
			startActivity(intent);
		}
	}
	
	public void attendMeeting(View v){
		String name = getName();
		if(name != null){
			Intent intent = new Intent(this, WifiConnectActivity.class);
			intent.putExtra(EXTRA_NAME, name);
			startActivity(intent);
		}
	}
	
	private String getName(){
		String name = etName.getText().toString();
		if(name == null || name.trim().equals("")){
			Builder alert = new AlertDialog.Builder(this);
			alert.setTitle(R.string.prompt);
			alert.setMessage(R.string.name_not_null);
			alert.setNeutralButton(R.string.confirm, null);
			alert.show();
			return null;
		} else {
			return name;
		}
	}
}
