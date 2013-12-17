package com.triman.wifitest.utils.netty;

import java.io.Serializable;

public class Message implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5910155568125389350L;
	
	public static final int REQUEST = 0;
	public static final int RESPONSE = 1;
	
	private String message;
	
	private int type;

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
	
	public Message(String msg, int type){
		this.message = msg;
		this.type = type;
	}
}
