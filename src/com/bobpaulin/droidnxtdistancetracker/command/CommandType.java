package com.bobpaulin.droidnxtdistancetracker.command;

import java.util.Arrays;

import org.apache.commons.lang.ArrayUtils;

import com.bobpaulin.droidnxtdistancetracker.NXTConstants;
import com.bobpaulin.droidnxtdistancetracker.NXTHandler;

import android.os.Message;

/**
 * 
 * Types of NXT Responses that might return be returned from the device
 * 
 * @author bpaulin
 *
 */
public enum CommandType {
	
	//Determine how to detect the distance response...
	DISTANCE_RESPONSE(DistanceResponse.class);
	private Class commandClass;
	private byte[] commandPrefix;
	private CommandType(Class commandClass, byte...commandPrefix) {
		this.commandClass = commandClass;
		this.commandPrefix = commandPrefix;
	}
	
	public Class getCommandClass() {
		return commandClass;
	}
	
	public byte[] getCommandPrefix()
	{
		return commandPrefix;
	}
	
	public static void sendCommandToMessage(byte[] command, NXTHandler nxtHandler)
	{
		
		for(CommandType currentCommand: values())
		{
			byte[] commandPrefix = ArrayUtils.subarray(command, 0, currentCommand.getCommandPrefix().length);
			if(Arrays.equals(commandPrefix, currentCommand.getCommandPrefix()))
			{
				Message msg = nxtHandler.obtainMessage(NXTConstants.MESSAGE_NXT_RESPONSE);
				NXTResponse response;
				try {
					response = (NXTResponse) currentCommand.getCommandClass().newInstance();
					response.processResponse(command);
					msg.obj = response;
			        nxtHandler.sendMessage(msg);
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
				
			}
		}
		
	}
}
