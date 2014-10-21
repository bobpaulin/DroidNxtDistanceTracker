package com.bobpaulin.droidnxtdistancetracker.command;

/**
 * 
 * NXT response for the command to read the first distance 
 * register.
 * 
 * @author bpaulin
 *
 */
public class DistanceResponse implements NXTResponse {
	private int distance;
	
	
	@Override
	public void processResponse(byte[] command) {
		distance = command[4];
	}
	
	public int getDistance() {
		return distance;
	}
}
