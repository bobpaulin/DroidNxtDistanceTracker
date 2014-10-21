package com.bobpaulin.droidnxtdistancetracker.handleraction;

import android.os.Message;
/**
 * 
 * Interface for Actions that may be registered with handlers.
 * 
 * @author bpaulin
 *
 */
public interface HandlerAction {
	
	public void executeHandlerAction(Message message);

}
