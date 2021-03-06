package com.triman.wifitest.utils.netty;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.netty.channel.ChannelHandlerContext;

public class ConnectionManager {
	
	private AtomicInteger ID_SEQ = new AtomicInteger(0);
	
	private static final ConnectionManager instance = new ConnectionManager();
	
	private Map<Integer, Connection> connections = new ConcurrentHashMap<Integer, Connection>();
	
	private ConnectionListener connectionListener;
	
	public ConnectionListener getConnectionListener() {
		return connectionListener;
	}

	public void setConnectionListener(ConnectionListener connectionListener) {
		this.connectionListener = connectionListener;
	}

	private ConnectionManager(){}
	
	public static final ConnectionManager getInstance(){
		return instance;
	}
	
	public Connection newConnection(ChannelHandlerContext ctx) {
		final Connection c = new Connection(ID_SEQ.incrementAndGet(), ctx);
		c.setConnectionListener(connectionListener);
		connections.put(c.getId(), c);
		if(connectionListener != null){
			connectionListener.onNewConnection(c);
		}
		return c;
	}
	
	public void addConnection(Connection c) {
		connections.put(c.getId(), c);
	}

	public Connection getConnection(int id) {
		return connections.get(id);
	}

	public void removeConnection(Connection c) {
		this.removeConnection(c.getId());
	}
	
	public void removeConnection(int id){
		Connection old = connections.remove(id);
		if (old != null) {
			if(connectionListener != null){
				connectionListener.onRemoveConnection(old);
			}
		} else {
			// XXX: error
		}
	}
	
	public int getConnectionCount(){
		return connections.size();
	}
}
