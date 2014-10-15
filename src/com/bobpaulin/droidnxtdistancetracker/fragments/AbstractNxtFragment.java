package com.bobpaulin.droidnxtdistancetracker.fragments;

import android.support.v4.app.Fragment;

import com.bobpaulin.droidnxtdistancetracker.NXTTalker;

public abstract class AbstractNxtFragment extends Fragment {
	private NXTTalker talker;
	
	public void setTalker(NXTTalker talker) {
		this.talker = talker;
	}
	
	public NXTTalker getTalker() {
		return talker;
	}
}
