package com.triman.wifitest.utils.netty;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;

import com.triman.wifitest.Participant;

public class Connection {
	private int id;
	private ChannelHandlerContext ctx;
	private volatile boolean isKilled = false;
	private ConnectionListener connectionListener;
	private Participant participant;

	public ConnectionListener getConnectionListener() {
		return connectionListener;
	}

	public void setConnectionListener(ConnectionListener connectionListener) {
		this.connectionListener = connectionListener;
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
		this.ctx = ctx;
	}
	
	public void setParticipant(Participant participant) {
		this.participant = participant;
		if(connectionListener != null){
			connectionListener.onStableConnection(this);
		}
	}

	public Participant getParticipant() {
		return participant;
	}
	
	public int getId(){
		return id;
	}
}
