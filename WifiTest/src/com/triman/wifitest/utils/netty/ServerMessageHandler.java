package com.triman.wifitest.utils.netty;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

import android.util.Log;

public class ServerMessageHandler extends SimpleChannelHandler{
	
	private static final String TAG = "ServerMessageHandler";
	private String name;

	public ServerMessageHandler(String name) {
		this.name = name;
	}
	
	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e)
			throws Exception {
		super.channelConnected(ctx, e);
		Connection c = ConnectionManager.getInstance().newConnection(ctx);
		Log.v(TAG, "Client " + c.getId() + " has connected!");
		Message message = new Message(name, Message.NAME_QUERY, c.getId());
		message.setConnectionId(c.getId());
		c.getChannel().write(message);
		c.getChannel().setAttachment(c.getId());
	}
	
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		Message msg = (Message) e.getMessage();
		Connection c = ConnectionManager.getInstance().getConnection(Integer.parseInt(ctx.getChannel().getAttachment().toString()));
		if(msg.getType() == Message.NAME_QUERY_RESPONSE){
			Log.v(TAG,"I received message: " + msg.getMessage());
			c.setAttachment(msg.getMessage());
		} else {
			Log.v(TAG,"I received message from " + c.getAttachment().toString() + ":" + msg.getMessage());
		}
	}
	
	@Override
	public void channelDisconnected(ChannelHandlerContext ctx,
			ChannelStateEvent e) throws Exception {
		Connection c = ConnectionManager.getInstance().getConnection(Integer.parseInt(ctx.getChannel().getAttachment().toString()));
		ConnectionManager.getInstance().removeConnection(c);
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
			throws Exception {
		// TODO Auto-generated method stub
		super.exceptionCaught(ctx, e);
	}
}
