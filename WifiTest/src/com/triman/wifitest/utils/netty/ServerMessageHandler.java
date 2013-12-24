package com.triman.wifitest.utils.netty;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

import com.triman.wifitest.Participant;

import android.util.Log;

public class ServerMessageHandler extends SimpleChannelHandler{
	
	private static final String TAG = "ServerMessageHandler";
	private String name;

	public ServerMessageHandler(String name) {
		this.name = name;
	}
	
	@Override
	public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e)
			throws Exception {
		Connection c = ConnectionManager.getInstance().getConnection(Integer.parseInt(ctx.getChannel().getAttachment().toString()));
		ConnectionManager.getInstance().removeConnection(c);
		Log.v(TAG, "Channel closed");
	}
	
	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e)
			throws Exception {
		Connection c = ConnectionManager.getInstance().newConnection(ctx);
		
		Message message = new Message(name, Message.NAME_QUERY, c.getId());
		message.setConnectionId(c.getId());
		c.getChannel().write(message);
		
		c.getChannel().setAttachment(c.getId());
		Log.v(TAG, "New channel connected");
	}
	
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		Message msg = (Message) e.getMessage();
		Connection c = ConnectionManager.getInstance().getConnection(Integer.parseInt(ctx.getChannel().getAttachment().toString()));
		if(msg.getType() == Message.NAME_QUERY_RESPONSE){
			Log.v(TAG,"I received message: " + msg.getMessage());
			Participant participant = new Participant(msg.getMessage(), c.getId());
			c.setParticipant(participant);
		} else {
			Log.v(TAG,"I received message from " + c.getParticipant().getName() + ":" + msg.getMessage());
		}
		Log.v(TAG, "Channel received a message");
	}
	
	@Override
	public void channelDisconnected(ChannelHandlerContext ctx,
			ChannelStateEvent e) throws Exception {
//		Connection c = ConnectionManager.getInstance().getConnection(Integer.parseInt(ctx.getChannel().getAttachment().toString()));
//		ConnectionManager.getInstance().removeConnection(c);
		Log.v(TAG, "Channel disconnected");
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
			throws Exception {
		// TODO Auto-generated method stub
		super.exceptionCaught(ctx, e);
		Log.v(TAG, "Channel get a exception");
	}
}
