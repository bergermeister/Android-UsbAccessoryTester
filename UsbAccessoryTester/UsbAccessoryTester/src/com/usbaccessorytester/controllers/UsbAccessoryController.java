package com.usbaccessorytester.controllers;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;

import com.usbaccessorytester.Utility;
import com.usbaccessorytester.models.UsbMessage;

import android.content.Context;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.ParcelFileDescriptor;
import android.util.Log;

public class UsbAccessoryController 
{
	private Context context;
	private ParcelFileDescriptor fileDescriptor;
	private FileInputStream inputStream;
	private FileOutputStream outputStream;    
	private AccessoryReadThread readThread;
	private AccessoryWriteThread writeThread;
	private boolean enableThread;
	private ArrayBlockingQueue<UsbMessage> outQueue;
	private ArrayBlockingQueue<UsbMessage> inQueue;
	
	/**
	 * Constructor
	 * 
	 * @param context	Application context needed to broadcast messages
	 */
	public UsbAccessoryController(Context context)
	{
		this.context = context;
		fileDescriptor = null;
		inputStream = null;
		outputStream = null;
		readThread = null;
		writeThread = null;
		enableThread = false;
		this.outQueue = new ArrayBlockingQueue<UsbMessage>(1024, true);
		this.inQueue = new ArrayBlockingQueue<UsbMessage>(1024, true);
	}
	
	/**
	 * Flag to determine if an accessory is connected
	 */
	public boolean IsAccessoryConnected()
	{
		return enableThread;
	}
    
	/**
	 * Public method for adding a CANMessage to the queue
	 */
	public void AddMessage(UsbMessage msg)
	{
		outQueue.add(msg);
	}
	
	/**
	 * Polls the in queue for a CAN Message
	 */
	public UsbMessage PollInQueue()
	{
		return this.inQueue.poll();
	}
	
	/**
	 * Public method for opening a connected usb accessory
	 */
	public void OpenAccessory(UsbAccessory accessory)
    {
		if (!enableThread)
		{
	    	fileDescriptor = ((UsbManager) context.getSystemService(Context.USB_SERVICE)).openAccessory(accessory);
	    	if (fileDescriptor != null)
	    	{
	    		FileDescriptor fd = fileDescriptor.getFileDescriptor();
	    		inputStream = new FileInputStream(fd);
	    		outputStream = new FileOutputStream(fd);
	    		
	    		if (inputStream == null || outputStream == null)
	    			return;
	    		
    			enableThread = true;
    			readThread = new AccessoryReadThread(inputStream);
    			readThread.start();
    			
    			writeThread = new AccessoryWriteThread(outputStream);
    			writeThread.start();
	    	}
		}
    }
	
	/**
	 * Public method for closing a connected accessory
	 */
    public void CloseAccessory()
    {
    	outQueue.clear();
    	enableThread = false;
    	try { Thread.sleep(10); }
    	catch (InterruptedException ie) { ie.printStackTrace(); }

    	try
    	{
    		if (fileDescriptor != null)
    			fileDescriptor.close();
    	}
    	catch (IOException ex) { ex.printStackTrace(); }
    	
    	try
    	{
    		if (outputStream != null)
    			outputStream.close();
    	}
    	catch (IOException ex) { ex.printStackTrace(); }
    	
    	try
    	{
    		if (inputStream != null)
    			inputStream.close();
    	}
    	catch (IOException ex) { ex.printStackTrace(); }
    	
    	fileDescriptor = null;
    	outputStream = null;
    	inputStream = null;   	
    }

    /**
     * Configures the connected usb accessory
     */
    public void SetConfig(int baud, byte dataBits, byte stopBits, byte parity, byte flowControl)
    {
    	byte[] buffer = new byte[8];
    	
    	// Prepare the baud rate buffer
    	buffer[0] = (byte) baud;
    	buffer[1] = (byte)(baud >> 8);
    	buffer[2] = (byte)(baud >> 16);
    	buffer[3] = (byte)(baud >> 24);
    	
    	buffer[4] = dataBits;
    	buffer[5] = stopBits;
    	buffer[6] = parity;
    	buffer[7] = flowControl;
    	
    	outQueue.add(new UsbMessage(buffer, 8));
    }

    /**
     * Background thread that continuously reads from the
     * connected usb accessory
     */
	private class AccessoryReadThread extends Thread
    {
    	private static final String TAG = "AccessoryReadThread";
    	private FileInputStream inStream;
    	
    	public AccessoryReadThread(FileInputStream stream) { this.inStream = stream; }
    	
    	@Override
    	public void run()
    	{    		
    		Log.d(TAG, "AccessoryThread Running");
    	
    		byte[] buffer = new byte[1024];
    	
    		while (enableThread)
    		{
    			try 
    			{
    				if (inStream!= null)
    				{
	    				int read = inStream.read(buffer, 0, 1024);
	    				inQueue.add(new UsbMessage(Arrays.copyOf(buffer, read), read));
    				}
				} 
    			catch (IOException e) 
    			{ 
    				e.printStackTrace(); 
    			}	
    		}
    	}
    }

	/*
	 * Background thread that continuously sends messages stored in the queue to the
	 * connected usb accessory
	 */
	private class AccessoryWriteThread extends Thread
	{
		private static final String TAG = "AccessoryWriteThread";
		private FileOutputStream outStream;
		
		public AccessoryWriteThread(FileOutputStream outputStream)
		{
			this.outStream = outputStream;
		}
		
		public void run()
		{
			UsbMessage msg;
			while (enableThread)
			{
				try
				{
					if (outStream != null)
					{
						msg = outQueue.poll();
						if (msg != null)
						{
							sendPacket(msg.getData(), 0, msg.getLength());
							msg = null;
							try { Thread.sleep(1); }
							catch (InterruptedException ie) { ie.printStackTrace(); }
						}
					}
					else
						outQueue.clear();
				}
				catch (Exception ex) { ex.printStackTrace(); }
			}
		}
		
		private void sendPacket(byte[] buffer, int offset, int length)
	    {
	    	try
	    	{    		
	    		if (outStream != null)
	    		{
	    			outStream.write(buffer, offset, length);
		    		Log.d(TAG, "Sent: " + Utility.getHex(buffer));
		    	}
	    	}
	    	catch (IOException ex) 
	    	{ 
	    		ex.printStackTrace(); 
	    	}
	    }
	}
}
