package com.bobpaulin.droidnxtdistancetracker.fragments;

import com.bobpaulin.droidnxtdistancetracker.NXTConstants;
import com.bobpaulin.droidnxtdistancetracker.R;
import com.bobpaulin.droidnxtdistancetracker.handleraction.HandlerAction;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

/**
 * 
 * UI Fragment that logs messages from NXT
 * 
 * @author bpaulin
 *
 */
public class LogFragment extends AbstractNxtFragment {
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_log, container, false);
        
        final EditText editText = (EditText) rootView.findViewById(R.id.editText1);
        
        getHandler().registerHandlerAction(new HandlerAction() {
			
			@Override
			public void executeHandlerAction(Message msg) {
				switch (msg.what) {
            	case NXTConstants.MESSAGE_EDIT_TEXT:
            		editText.getText().append(msg.getData().getString(NXTConstants.LOG_MESSAGE) + "\n");
            		break;
            }
				
			}
		});
        
        return rootView;
    }
	
	

}
