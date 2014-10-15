package com.bobpaulin.droidnxtdistancetracker.fragments;

import com.bobpaulin.droidnxtdistancetracker.NXTConstants;
import com.bobpaulin.droidnxtdistancetracker.R;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

public class LogFragment extends AbstractNxtFragment {
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_log, container, false);
        
        final EditText editText = (EditText) rootView.findViewById(R.id.editText1);
        
        getTalker().addHandler(new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                case NXTConstants.MESSAGE_EDIT_TEXT:
                	editText.getText().append(msg.getData().getString(NXTConstants.TOAST) + "\n");
                	break;
                }
            }});
        
        return rootView;
    }
	
	

}
