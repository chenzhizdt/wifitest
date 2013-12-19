package com.triman.wifitest;

public class Participant {
	private String name;
	private int connectionId;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getConnectionId() {
		return connectionId;
	}
	public void setConnectionId(int connectionId) {
		this.connectionId = connectionId;
	}
	public Participant(String name, int connectionId) {
		super();
		this.name = name;
		this.connectionId = connectionId;
	}
	@Override
	public String toString() {
		return name;
	}
}
