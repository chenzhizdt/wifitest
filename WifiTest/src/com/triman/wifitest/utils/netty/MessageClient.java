package com.triman.wifitest.utils.netty;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.serialization.ClassResolvers;
import org.jboss.netty.handler.codec.serialization.ObjectDecoder;
import org.jboss.netty.handler.codec.serialization.ObjectEncoder;

public class MessageClient {
	private ClientBootstrap bootstrap;
	private Channel channel;
	
	public ChannelFuture start(String host, int port){
		ChannelFactory factory = new NioClientSocketChannelFactory(
				Executors.newCachedThreadPool(),
				Executors.newCachedThreadPool());
		bootstrap = new ClientBootstrap(factory);
		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
			@Override
			public ChannelPipeline getPipeline() throws Exception {
				return Channels.pipeline(
						new ObjectDecoder(ClassResolvers.cacheDisabled(this
		                .getClass().getClassLoader())),
		                new ObjectEncoder(),
		                new MessageHandler("Client"));
			}
		});
		ChannelFuture channelFuture = bootstrap.connect(new InetSocketAddress(host, port));
		return channelFuture;
	}
	
	public void sendMessage(Message msg){
		channel.write(msg);
	}
	
	public void shutdown(){
		if(bootstrap != null){
			bootstrap.releaseExternalResources();
			bootstrap.shutdown();
			bootstrap = null;
		}
	}
}
