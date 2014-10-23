package com.bobpaulin.droidnxtdistancetracker.command;

public class DistanceResponse implements NXTResponse {

	@Override
	public void processResponse(byte[] command) {
		// TODO Pull the correct value from the byte response and place it in the object
		// See Appendex 2 - LSWRITE Command Response AND Appendix 7 - for Ultrasonic sensor we want to read the measurement for byte 0.

	}

}
