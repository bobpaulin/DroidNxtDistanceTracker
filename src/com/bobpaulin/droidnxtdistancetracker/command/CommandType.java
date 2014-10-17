package com.bobpaulin.droidnxtdistancetracker.command;

import java.util.Arrays;

import org.apache.commons.lang.ArrayUtils;

import com.bobpaulin.droidnxtdistancetracker.NXTConstants;
import com.bobpaulin.droidnxtdistancetracker.NXTHandler;

import android.os.Message;

public enum CommandType {
	DISTANCE_RESPONSE(DistanceResponse.class, (byte)0x02, (byte)0x10, (byte)0x00);
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
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		}
		
	}
}
