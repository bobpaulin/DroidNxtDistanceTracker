package com.bobpaulin.droidnxtdistancetracker.fragments;

import java.util.Set;

import com.bobpaulin.droidnxtdistancetracker.NXTConstants;
import com.bobpaulin.droidnxtdistancetracker.NXTTalker;
import com.bobpaulin.droidnxtdistancetracker.R;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

public class MainFragment extends AbstractNxtFragment {
	private BluetoothAdapter mBtAdapter;
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        
        final Button exitButton = (Button) rootView.findViewById(R.id.exitButton);
        
        final Button disconnectButton = (Button) rootView.findViewById(R.id.disconnectButton);
        
        disconnectButton.setVisibility(View.INVISIBLE);
        
        disconnectButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				getTalker().stop();
			}
		});
        
        exitButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				getActivity().finish();
				System.exit(0);
				
			}
		});
        
        
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
        getTalker().addHandler(new Handler(){
        	@Override
        	public void dispatchMessage(Message msg) {
        		if(msg.what == NXTConstants.MESSAGE_STATE_CHANGE)
        		{
        			switch(msg.arg1){
        				case NXTTalker.STATE_NONE:
        					connectButton.setVisibility(View.VISIBLE);
        					disconnectButton.setVisibility(View.INVISIBLE);
        					break;
        				case NXTTalker.STATE_CONNECTED:
        				case NXTTalker.STATE_CONNECTING:
        					disconnectButton.setVisibility(View.VISIBLE);
        					connectButton.setVisibility(View.INVISIBLE);
        					break;
        			}
        		}
        	}
        });
        return rootView;
    }
	
}
