package com.triman.wifitest.utils.netty;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;

public class Connection {
	private int id;
	private Object readWriteLock = new Object();
	private ChannelHandlerContext ctx;
	private volatile boolean isKilled = false;
	private Object attachment;
	private ConnectionListener connectionListener;

	public ConnectionListener getConnectionListener() {
		return connectionListener;
	}

	public void setConnectionListener(ConnectionListener connectionListener) {
		this.connectionListener = connectionListener;
	}

	public Object getAttachment() {
		synchronized (readWriteLock) {
			return attachment;
		}
	}

	public void setAttachment(Object attachment) {
		synchronized (readWriteLock) {
			this.attachment = attachment;
		}
		if(connectionListener != null){
			connectionListener.onStableConnection(this);
		}
	}
	
	public boolean isKilled() {
		return isKilled;
	}

	public void kill() {
		if (!isKilled) {
			isKilled = true;
			ctx.getChannel().close();
		}
	}
	
	public boolean isConnected() {
		return ctx.getChannel().isConnected();
	}
	
	public Channel getChannel() {
		return ctx.getChannel();
	}

	public Connection(int id, ChannelHandlerContext ctx) {
		this.id = id;
		this.ctx = ctx;
	}

	public int getId() {
		return id;
	}
}
