package com.usbaccessorytester.models;

import java.util.ArrayList;

import com.usbaccessorytester.R;

import android.content.Context;
import android.hardware.usb.UsbAccessory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class UsbAccessoryArrayAdapter extends ArrayAdapter<UsbAccessory>
{
	private static class ViewHolder
	{
		private TextView serialNumber;
		private TextView modelNumber;
		private TextView manufacturer;
		
		private ViewHolder(View rootView)
		{
			serialNumber = (TextView) rootView.findViewById(R.id.accessorySerial);
			modelNumber = (TextView) rootView.findViewById(R.id.accessoryModel);
			manufacturer = (TextView) rootView.findViewById(R.id.accessoryManufacturer);
		}
	}
	
	public UsbAccessoryArrayAdapter(Context context, ArrayList<UsbAccessory> arrayList)
	{
		super(context, R.layout.list_item_usb_accessory, arrayList);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		final ViewHolder vh;
	
		if (convertView == null)
		{
			convertView = LayoutInflater.from(this.getContext()).inflate(R.layout.list_item_usb_accessory, parent, false);
			vh = new ViewHolder(convertView);
			convertView.setTag(vh);
		}
		else
			vh = (ViewHolder)convertView.getTag();
	
		UsbAccessory item = getItem(position);
		if (item != null)
		{
			vh.manufacturer.setText(item.getManufacturer());
			vh.modelNumber.setText(item.getModel() + ",");
			vh.serialNumber.setText(item.getSerial() + ",");
		}
		
		return convertView;
	}
	
	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent)
	{
		final ViewHolder vh;
	
		if (convertView == null)
		{
			convertView = LayoutInflater.from(this.getContext()).inflate(R.layout.list_item_usb_accessory, parent, false);
			vh = new ViewHolder(convertView);
			convertView.setTag(vh);
		}
		else
			vh = (ViewHolder)convertView.getTag();
	
		UsbAccessory item = getItem(position);
		if (item != null)
		{
			vh.manufacturer.setText(item.getManufacturer());
			vh.modelNumber.setText(item.getModel() + ",");
			vh.serialNumber.setText(item.getSerial() + ",");
		}
		
		return convertView;
	}
}
