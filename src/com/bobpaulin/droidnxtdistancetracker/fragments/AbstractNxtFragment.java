package com.bobpaulin.droidnxtdistancetracker.fragments;

import android.support.v4.app.Fragment;

import com.bobpaulin.droidnxtdistancetracker.NXTHandler;
import com.bobpaulin.droidnxtdistancetracker.NXTTalker;

/**
 * 
 * Abstract UI Fragment for screens that interact with NXT
 * 
 * @author bpaulin
 *
 */
public abstract class AbstractNxtFragment extends Fragment {
	private NXTTalker talker;
	
	private NXTHandler handler;
	
	public void setTalker(NXTTalker talker) {
		this.talker = talker;
	}
	
	public NXTTalker getTalker() {
		return talker;
	}
	
	public NXTHandler getHandler() {
		return handler;
	}
	
	public void setHandler(NXTHandler handler) {
		this.handler = handler;
	}
}
