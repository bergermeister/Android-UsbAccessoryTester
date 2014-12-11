package com.usbaccessorytester;

import java.util.ArrayList;

import com.usbaccessorytester.controllers.UsbAccessoryController;
import com.usbaccessorytester.models.UsbAccessoryArrayAdapter;
import com.usbaccessorytester.models.UsbMessage;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class MainActivity extends Activity
{
	private static final String ACTION_USB_ACCESSORY_PERMISSION = "com.usbaccessorytester.USB_ACCESSORY_PERMISSION";
	private static final String TAG = "MainActivity";
	
	private UsbAccessoryController accCon;
	private UsbAccessoryArrayAdapter adapter;
	private boolean enableThread = false;
	
	private BroadcastReceiver receiver = new BroadcastReceiver() 
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			String action = intent.getAction();
			
			if (action.equals(ACTION_USB_ACCESSORY_PERMISSION))
			{
				synchronized(this)
				{
					UsbAccessory accessory = (UsbAccessory) intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
					if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false))
						openAccessory(accessory);
					else
						Log.d(TAG, "Permission denied for accessory " + accessory);
				}
			}
			else if (action.equals(UsbManager.ACTION_USB_ACCESSORY_DETACHED))
			{
				closeAccessory();
				adapter.clear();
				((Button) findViewById(R.id.openAccessory)).setEnabled(false);
			}
		}
	};
	
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler(this));
        
        setContentView(R.layout.activity_main);
        
        accCon = new UsbAccessoryController(this);
        adapter = new UsbAccessoryArrayAdapter(this, new ArrayList<UsbAccessory>());
        ((Spinner) findViewById(R.id.accessoryList)).setAdapter(adapter);
        
        defineButtons();
    }
    
    @Override
    public void onStart()
    {
    	super.onStart();
    	
    	IntentFilter filter = new IntentFilter(ACTION_USB_ACCESSORY_PERMISSION);
    	filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
    	registerReceiver(receiver, filter);
    }
    
    @Override
    public void onStop()
    {
    	super.onStop();
    	
    	unregisterReceiver(receiver);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) 
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings)
        {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    private void defineButtons()
    {
    	((Button) findViewById(R.id.discoverAccessories)).setOnClickListener(new Button.OnClickListener()
    	{
    		@Override
    		public void onClick(View btn)
    		{
    			UsbManager manager = ((UsbManager) MainActivity.this.getSystemService(Context.USB_SERVICE));
    			UsbAccessory[] list = manager.getAccessoryList();
    			adapter.clear();
    			if (list != null)
    			{
    				for (int i = 0; i < list.length; i++)
    					adapter.add(list[0]);
    				((Button) findViewById(R.id.openAccessory)).setEnabled(true);
    			}
    			else
    			{
    				((Button) findViewById(R.id.openAccessory)).setEnabled(true);
    				Toast.makeText(MainActivity.this, R.string.accessories_not_found, Toast.LENGTH_LONG).show();
    			}
    		}
    	});
    	
    	((Button) findViewById(R.id.openAccessory)).setOnClickListener(new Button.OnClickListener()
    	{
    		@Override
    		public void onClick(View btn)
    		{
    			UsbAccessory accessory = (UsbAccessory)((Spinner)findViewById(R.id.accessoryList)).getSelectedItem();
    			
    			if (accessory == null)
    				Toast.makeText(MainActivity.this, R.string.accessory_not_selected, Toast.LENGTH_LONG).show();
    			else
    			{
    				UsbManager manager = ((UsbManager) getSystemService(Context.USB_SERVICE));
   
    	    		if (manager.hasPermission(accessory))
    					openAccessory(accessory);
    				else
    				{
    					PendingIntent pIntent = PendingIntent.getBroadcast(MainActivity.this, 0, new Intent(ACTION_USB_ACCESSORY_PERMISSION), 0);
    					manager.requestPermission(accessory, pIntent);
    				}
    			}
    		}
    	});
    	
    	((Button) findViewById(R.id.closeAccessory)).setOnClickListener(new Button.OnClickListener()
    	{
    		@Override
    		public void onClick(View btn)
    		{
    			closeAccessory();
    		}
    	});
    	
    	((Button) findViewById(R.id.sendStringMessage)).setOnClickListener(new Button.OnClickListener()
    	{
    		@Override
    		public void onClick(View btn)
    		{
    			String msg = ((EditText)findViewById(R.id.stringMessage)).getText().toString();
    			byte[] data = msg.getBytes();
    			accCon.AddMessage(new UsbMessage(data, data.length));
    			((EditText) findViewById(R.id.log)).getText().insert(0, Utility.getDateTime() + " " + getResources().getString(R.string.sent) + ": " + msg + '\n');
    		}
    	}); 
    	
    	((Button) findViewById(R.id.sendHexMessage)).setOnClickListener(new Button.OnClickListener()
    	{
    		@Override
    		public void onClick(View btn)
    		{
    			try
    			{
    				byte[] data = Utility.getHex(((EditText)findViewById(R.id.hexMessage)).getText().toString());    			
    				accCon.AddMessage(new UsbMessage(data, data.length));
    				((EditText) findViewById(R.id.log)).getText().insert(0, Utility.getDateTime() + " " + getResources().getString(R.string.sent) + ": " + Utility.getHex(data) + '\n');
    			}
    			catch (Exception ex) { Toast.makeText(MainActivity.this, R.string.invalid_hex_string, Toast.LENGTH_LONG).show(); }
    		}
    	});    
    }
    
    private void toggleButtonEnable(boolean accessoryConnected)
    {
    	((Button) findViewById(R.id.openAccessory)).setEnabled(!accessoryConnected);
    	((Button) findViewById(R.id.closeAccessory)).setEnabled(accessoryConnected);
		((Button) findViewById(R.id.sendStringMessage)).setEnabled(accessoryConnected);
		((Button) findViewById(R.id.sendHexMessage)).setEnabled(accessoryConnected);
    }
    
    private void openAccessory(UsbAccessory accessory)
    {
    	int baud = Integer.parseInt((String)((Spinner)findViewById(R.id.accessoryBaudRate)).getSelectedItem());
		byte dataBits = Byte.parseByte((String)((Spinner)findViewById(R.id.accessoryDataBits)).getSelectedItem());
		byte stopBits = Byte.parseByte((String)((Spinner)findViewById(R.id.accessoryStopBits)).getSelectedItem());
		byte parity = (byte)((Spinner)findViewById(R.id.accessoryParity)).getSelectedItemPosition();
		byte flowControl = (byte)((Spinner)findViewById(R.id.accessoryFlowControl)).getSelectedItemPosition();
		    				
		enableThread = true;
		new ReadTask().execute(new Integer[] { });
		
		accCon.OpenAccessory(accessory);
		accCon.SetConfig(baud, dataBits, stopBits, parity, flowControl);
		toggleButtonEnable(true);
		
		Toast.makeText(MainActivity.this, R.string.accessory_opened, Toast.LENGTH_LONG).show();
    }
    
    private void closeAccessory()
    {
    	enableThread = false;
		accCon.CloseAccessory();
		toggleButtonEnable(false);
    }
    
    private class ReadTask extends AsyncTask<Integer, String, Integer>
	{		
    	private EditText log;
    	private String received;
    	
    	@Override
    	protected void onPreExecute()
    	{
    		log = ((EditText) findViewById(R.id.log));
    		log.getText().clear();
    		received = getResources().getString(R.string.received);
    	}
    	
		@Override
		protected Integer doInBackground(Integer... args)
		{
			UsbMessage msg = null;	
			
			while (enableThread)
			{
				synchronized (accCon) { msg = accCon.PollInQueue(); }
				if (msg != null)
				{
					publishProgress(Utility.getHex(msg.getData()));
				}
			}
			return 0;
		}
		
		protected void onProgressUpdate(String... msg)
		{
			log.getText().insert(0, Utility.getDateTime() + " " + received + ": " + msg[0] + '\n');
		}
		
		protected void onPostExecute(Integer result) 
		{
			log = null;
		}
	}
}
