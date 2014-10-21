package com.bobpaulin.droidnxtdistancetracker.command;

/**
 * 
 * Interface for parsing NXT Byte Array Responses into Objects
 * 
 * @author bpaulin
 *
 */
public interface NXTResponse {
	
	public void processResponse(byte[] command);
	
}
