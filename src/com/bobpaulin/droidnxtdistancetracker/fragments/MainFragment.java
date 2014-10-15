package com.bobpaulin.droidnxtdistancetracker.fragments;

import java.util.Set;

import com.bobpaulin.droidnxtdistancetracker.R;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class MainFragment extends AbstractNxtFragment {
	private BluetoothAdapter mBtAdapter;
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        
        
        final Button connectButton = (Button) rootView.findViewById(R.id.connectButton);
        connectButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	mBtAdapter = BluetoothAdapter.getDefaultAdapter();
                
                Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
                
                BluetoothDevice device = null;
                
                if (pairedDevices.size() > 0) {
                    for (BluetoothDevice currentDevice : pairedDevices) {
                        if ((currentDevice.getBluetoothClass() != null) && (currentDevice.getBluetoothClass().getDeviceClass() == BluetoothClass.Device.TOY_ROBOT)) {
                        	device = mBtAdapter.getRemoteDevice(currentDevice.getAddress());
                        }
                    }
                }
                getTalker().connect(device);
            }
        });
        
        
        return rootView;
    }
	
}
