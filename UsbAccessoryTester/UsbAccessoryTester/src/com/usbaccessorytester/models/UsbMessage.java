package com.usbaccessorytester.models;

public class UsbMessage 
{
	private byte[] data;
	private int length;
	
	public UsbMessage(byte[] data, int length)
	{
		this.data = data;
		this.length = length;
	}
	
	public byte[] getData()
	{
		return this.data;
	}
	
	public int getLength()
	{
		return this.length;
	}
}
