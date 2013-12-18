package com.triman.wifitest.utils.netty;

import java.io.Serializable;

public class Message implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5910155568125389350L;
	
	public static final int NAME_QUERY = 0;
	public static final int NAME_QUERY_RESPONSE = 1;
	public static final int NORMAL_MESSAGE = 2;
	public static final int REQUEST = 3;
	public static final int RESPONSE = 4;
	
	private String message;
	
	private int type;
	
	private int connectionId;

	public int getConnectionId() {
		return connectionId;
	}

	public void setConnectionId(int connectionId) {
		this.connectionId = connectionId;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	public Message(String msg, int type, int connectionId){
		this.message = msg;
		this.type = type;
	}
}
