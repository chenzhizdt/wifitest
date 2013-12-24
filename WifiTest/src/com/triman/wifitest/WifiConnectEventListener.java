package com.triman.wifitest;

public interface WifiConnectEventListener {
	/**
	 * Wifi启动后，并进入可用状态时触发
	 */
	public void onWifiEnabled();
	/**
	 * Wifi进入关闭状态时触发
	 */
	public void onWifiDisabled();
	/**
	 * Wifi正式连接到特定的Ap时触发
	 */
	public void onWifiConnectEnabled();
	/**
	 * Wifi与Ap之间的连接断开时触发
	 */
	public void onWifiConnectDisabled();
}
