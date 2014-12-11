package com.usbaccessorytester;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ConfigureAccessoryFragment extends Fragment
{
	private View view;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		if (container == null)
			return null;
		
		view = inflater.inflate(R.layout.fragment_configure_accessory, container, false);
		
		return view;
	}
}
