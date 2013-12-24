package com.triman.wifitest;
/**
 * {@link WifiConnectEventListener}的简单实现，对其中所有的事件不做任何操作
 * @author Chen
 *
 */
public class BaseWifiConnectEventListener implements WifiConnectEventListener{

	@Override
	public void onWifiEnabled() {
		return;
	}

	@Override
	public void onWifiDisabled() {
		return;
	}

	@Override
	public void onWifiConnectEnabled() {
		return;
	}

	@Override
	public void onWifiConnectDisabled() {
		return;
	}
	
}
