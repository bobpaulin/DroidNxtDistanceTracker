package com.bobpaulin.droidnxtdistancetracker;

import java.util.ArrayList;
import java.util.List;

import com.bobpaulin.droidnxtdistancetracker.handleraction.HandlerAction;

import android.os.Handler;
import android.os.Message;

public class NXTHandler extends Handler {
	
	private List<HandlerAction> registeredHandlerActions;
	
	public NXTHandler() {
		registeredHandlerActions = new ArrayList<HandlerAction>();
	}
	
	@Override
	public void dispatchMessage(Message msg) {
		for(HandlerAction currentAction: registeredHandlerActions)
		{
			currentAction.executeHandlerAction(msg);
		}
	}
	
	public void registerHandlerAction(HandlerAction handlerAction)
	{
		registeredHandlerActions.add(handlerAction);
	}
}
