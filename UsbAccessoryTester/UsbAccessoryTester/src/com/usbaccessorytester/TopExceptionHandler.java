package com.usbaccessorytester;

import android.app.Activity;

public class TopExceptionHandler implements Thread.UncaughtExceptionHandler
{
	private Thread.UncaughtExceptionHandler defaultUEH;
	//private Activity app = null;
	
	public TopExceptionHandler(Activity app)
	{
		this.defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
		//this.app = app;
	}
	
	public void uncaughtException(Thread t, Throwable e)
	{
		StackTraceElement[] arr = e.getStackTrace();
		String report = e.toString() + "\n\n";
		report += "--------- Stack trace ---------\n\n";
		for (int i = 0; i < arr.length; i++)
			report += "    " + arr[i].toString() + "\n";
		report += "-------------------------------\n\n";
		
		// If the exception was thrown in a background thread inside
		// AsyncTask, then the actual exception can be found with getCause
		report += "------------ Cause ------------\n\n";
		Throwable cause = e.getCause();
		if (cause != null)
		{
			report += cause.toString() + "\n\n";
			arr = cause.getStackTrace();
			for (int i = 0; i < arr.length; i++)
				report += "    " + arr[i].toString() + "\n";
		}
		report += "-------------------------------\n\n";
		Utility.LogReport("stack-trace", report);
		
		defaultUEH.uncaughtException(t,  e);
	}
}
