package com.triman.wifitest;

public interface WifiApEventListener {
	/**
	 * WifiAp正式启动时触发该事件
	 */
	public void onEnabled();
	/**
	 * WifiAp正在启动时触发该事件
	 */
	public void onEnabling();
	/**
	 * WifiAp失败时触发该事件
	 */
	public void onFailed();
	/**
	 * WifiAp关闭时触发该事件
	 */
	public void onDisabled();
	/**
	 * WifiAp正在关闭时触发该事件
	 */
	public void onDisabling();
}
