package com.usbaccessorytester;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.os.Environment;

public final class Utility 
{
	//private static final String TAG = "Utility";
	public static final String HEXES = "0123456789ABCDEF";	

	public static String getHex( byte [] raw ) 
	{
	    if ( raw == null ) return null;

	    final StringBuilder hex = new StringBuilder( 2 * raw.length );
	    for ( final byte b : raw )
	    {
	    	hex.append(HEXES.charAt((b & 0xF0) >> 4))
	    	   .append(HEXES.charAt((b & 0x0F)));
	    }
	    return hex.toString();
	}
	
	public static byte[] getHex(String raw)
	{
		byte[] hex = new byte[raw.length() / 2];
		
		for (int i = 0; i < raw.length(); i+=2)
		{
			hex[i / 2] = (byte)((HEXES.indexOf(raw.charAt(i)) << 4) +
						 (HEXES.indexOf(raw.charAt(i + 1))));
		}
		
		return hex;
	}
	
	/**
	 * Encrypt
	 * 
	 * Encrypts the given string
	 * 
	 * @param toEncrypt	String to encrypt
	 * @return	Encrypted HEX string
	 */	
	public static final String Encrypt(final String s) 
	{
		try 
		{
			// Create MD5 Hash
	        MessageDigest digest = java.security.MessageDigest
	                .getInstance("MD5");
	        digest.update(s.getBytes());
	        byte messageDigest[] = digest.digest();
 
	        // Create Hex String
	        StringBuffer hexString = new StringBuffer();
	        for (int i = 0; i < messageDigest.length; i++) {
	            String h = Integer.toHexString(0xFF & messageDigest[i]);
	            while (h.length() < 2)
	                h = "0" + h;
	            hexString.append(h);
	        }
	        return hexString.toString();
 
	    } 
		catch (NoSuchAlgorithmException e) 
		{
	        e.printStackTrace();
	    }
	    return "";
	}
	
	public static String getDateTime()
	{
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault());
		Date date = new Date();
		return dateFormat.format(date);
	}
	
	/**
	 * Rounds the given double to 5 decimal places.
	 * @param d		Double value to round
	 * @param num	Number of decimal places
	 * @return		Double value rounded to num decimal places
	 */
	public static double roundDecimals(double d, int num)
	{
		String decimals = "#.########";
		DecimalFormat numDForm = new DecimalFormat(decimals.substring(0, num + 2));
		return Double.valueOf(numDForm.format(d));
	}
					
	public static int LogDMESG()
	{
		File file = new File(Environment.getExternalStorageDirectory().toString() + "/Documents/dmesg_" + getDateTime() + ".log");
		String cmd = "dmesg";
		BufferedWriter fw = null;
		InputStream in = null;
		
		try 
		{ 
			// Create a new file from the given path
			if (!file.createNewFile())
				return -1;					// Return -1 for failure
			
			// Create a buffered writer to the file
			fw = new BufferedWriter(new FileWriter(file));
			
			// Create a process for the command
			Process proc = Runtime.getRuntime().exec(cmd); 
			
			// Get the process's input stream
			in = proc.getInputStream();
			
			// Read the output of the process and log it to the file
			byte[] buffer = new byte[1024];
			while (in.read(buffer, 0, 1024) != -1)
				fw.write(new String(buffer));
		} 
		catch (IOException e) { e.printStackTrace(); }
		finally
		{
			if (fw != null)
			{
				try { fw.close(); }
				catch (IOException e) { e.printStackTrace(); }
			}
			
			if (in != null)
			{
				try { in.close(); }
				catch (IOException e) { e.printStackTrace(); }
			}
		}
		
		return 0;	// Return 0 for success
	}

	public static int LogReport(String reportName, String report)
	{
		File file = new File(Environment.getExternalStorageDirectory().toString() + "/Documents/" + reportName + "_" + getDateTime() + ".log");
		BufferedWriter fw = null;

		try 
		{ 
			// Create a new file from the given path
			if (!file.createNewFile())
				return -1;					// Return -1 for failure
			
			// Create a buffered writer to the file
			fw = new BufferedWriter(new FileWriter(file));
			
			fw.write(report);
		} 
		catch (IOException e) { e.printStackTrace(); }
		finally
		{
			if (fw != null)
			{
				try { fw.close(); }
				catch (IOException e) { e.printStackTrace(); }
			}
		}
		
		return 0;	// Return 0 for success
	}
	
	public static int LogException(String reportName, Exception exception)
	{
		if (exception == null) return -1;
		
		try
		{
			Writer writer = new StringWriter();
			PrintWriter printWriter = new PrintWriter(writer);
			exception.printStackTrace(printWriter);
			Utility.LogReport(reportName, writer.toString());
		}
		catch (Exception ex) { /* What just happened? */ }
		
		return 0;	// Return 0 for success
	}
}
