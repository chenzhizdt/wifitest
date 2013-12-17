package com.triman.wifitest.utils.netty;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;

public class Connection {
	private int id;
	private Object readWriteLock = new Object();
	private ChannelHandlerContext ctx;
	private volatile boolean isKilled = false;
	private Object attachment;

	public Object getAttachment() {
		synchronized (readWriteLock) {
			return attachment;
		}
	}

	public void setAttachment(Object attachment) {
		synchronized (readWriteLock) {
			this.attachment = attachment;
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
