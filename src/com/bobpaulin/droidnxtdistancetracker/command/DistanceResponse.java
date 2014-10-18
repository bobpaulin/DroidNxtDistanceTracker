package com.bobpaulin.droidnxtdistancetracker.command;

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
