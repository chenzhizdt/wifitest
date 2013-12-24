package com.triman.wifitest.utils.netty;

public interface ConnectionListener {
	public void onNewConnection(Connection c);
	public void onRemoveConnection(Connection c);
	public void onStableConnection(Connection c);
}
