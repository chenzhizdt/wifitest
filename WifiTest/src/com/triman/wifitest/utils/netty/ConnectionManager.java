package com.triman.wifitest.utils.netty;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.netty.channel.ChannelHandlerContext;

public class ConnectionManager {
	
	private AtomicInteger ID_SEQ = new AtomicInteger(0);
	
	private static final ConnectionManager instance = new ConnectionManager();
	
	private Map<Integer, Connection> connections = new ConcurrentHashMap<Integer, Connection>();
	
	private ConnectionManager(){}
	
	public static final ConnectionManager getInstance(){
		return instance;
	}
	
	public Connection newConnection(ChannelHandlerContext ctx) {
		final Connection c = new Connection(ID_SEQ.incrementAndGet(), ctx);
		connections.put(c.getId(), c);
		return c;
	}
	
	public void addConnection(Connection c) {
		connections.put(c.getId(), c);
	}

	public Connection getConnection(int id) {
		return connections.get(id);
	}

	public void removeConnection(Connection c) {
		Connection old = connections.remove(c.getId());
		if (old != null) {
			old.setAttachment(null);
		} else {
			// XXX: error
		}
	}
	public int getConnectionCount(){
		return connections.size();
	}
}
