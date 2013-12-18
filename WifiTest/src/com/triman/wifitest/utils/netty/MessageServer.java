package com.triman.wifitest.utils.netty;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.serialization.ClassResolvers;
import org.jboss.netty.handler.codec.serialization.ObjectDecoder;
import org.jboss.netty.handler.codec.serialization.ObjectEncoder;

public class MessageServer {
	
	private ServerBootstrap bootstrap;
	private ConnectionManager connectionManager = ConnectionManager.getInstance();
	
	public ChannelFuture start(int port, final String name){
		ChannelFactory factory = new NioServerSocketChannelFactory(
				Executors.newCachedThreadPool(),
				Executors.newCachedThreadPool());
		bootstrap = new ServerBootstrap(factory);
		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
			@Override
			public ChannelPipeline getPipeline() throws Exception {
				return Channels.pipeline(
						new ObjectDecoder(ClassResolvers.cacheDisabled(this
		                .getClass().getClassLoader())),
		                new ObjectEncoder(),
		                new ServerMessageHandler(name));
			}
		});
		return bootstrap.bindAsync(new InetSocketAddress(port));
	}
	
	public void sendBroadcastMessage(Message msg){
		int n = connectionManager.getConnectionCount();
		while(n > 0){
			ConnectionManager.getInstance().getConnection(n).getChannel().write(msg);
			n--;
		}
	}
	
	public void shutdown(){
		if(bootstrap != null){
			bootstrap.releaseExternalResources();
			bootstrap.shutdown();
			bootstrap = null;
		}
	}
	
	public boolean isOpened(){
		return bootstrap == null ? false : true;
	}
}
