package com.triman.wifitest.utils.netty;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

import android.util.Log;

public class MessageHandler extends SimpleChannelHandler{
	
	private static final String TAG = "MessageHandler";
	
	private String id;
	
	protected String getId(){
		return id;
	}
	
	public MessageHandler(String id){
		this.id = id;
	}
	
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		Message msg = (Message) e.getMessage();
		Log.v(TAG,"I received message: " + msg.getMessage());
		if(msg.getType() == Message.REQUEST){
			e.getChannel().write(new Message(id + " received!", Message.RESPONSE, 0));
		}
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
			throws Exception {
		super.exceptionCaught(ctx, e);
	}
}
