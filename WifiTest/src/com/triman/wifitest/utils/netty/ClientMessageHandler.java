package com.triman.wifitest.utils.netty;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

import android.util.Log;

public class ClientMessageHandler extends SimpleChannelHandler{
	
	private static final String TAG = "ClientMessageHandler";
	
	private String name;
	private String serverName = "";
	private Integer connectionId = 0;
	
	public ClientMessageHandler(String name){
		this.name = name;
	}
	
	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e)
			throws Exception {
		// TODO Auto-generated method stub
		super.channelConnected(ctx, e);
	}
	
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		Message msg = (Message) e.getMessage();
		if(msg.getType() == Message.NAME_QUERY){
			serverName = msg.getMessage();
			connectionId = msg.getConnectionId();
			Message response = new Message(name, Message.NAME_QUERY_RESPONSE, connectionId);
			ctx.getChannel().write(response);
			Log.v(TAG,"I received message: " + msg.getMessage());
		} else {
			Log.v(TAG,"I received message from " + serverName + ": " + msg.getMessage());
		}
	}
	
	@Override
	public void channelDisconnected(ChannelHandlerContext ctx,
			ChannelStateEvent e) throws Exception {
		// TODO Auto-generated method stub
		super.channelDisconnected(ctx, e);
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
			throws Exception {
		// TODO Auto-generated method stub
		super.exceptionCaught(ctx, e);
	}
}
