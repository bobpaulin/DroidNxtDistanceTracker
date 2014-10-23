package com.bobpaulin.droidnxtdistancetracker;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.ArrayUtils;

import com.bobpaulin.droidnxtdistancetracker.command.CommandType;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

/**
 * 
 * Class that holds logic for managing state and communicating with the NXT over bluetooth.
 * 
 * @author bpaulin
 *
 */
public class NXTTalker {

    public static final int STATE_NONE = 0;
    public static final int STATE_CONNECTING = 1;
    public static final int STATE_CONNECTED = 2;
    
    private int mState;
    private NXTHandler nxtHandler;
    private BluetoothAdapter mAdapter;
    
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private DistanceThread mDistanceThread;
    
    public NXTTalker(NXTHandler nxtHandler) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        this.nxtHandler = nxtHandler;
        setState(STATE_NONE);
    }

    private synchronized void setState(int state) {
        mState = state;
     
        nxtHandler.obtainMessage(NXTConstants.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
       
    }
    
    public synchronized int getState() {
        return mState;
    }
    
    private void toast(String text) {

        Message msg = nxtHandler.obtainMessage(NXTConstants.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(NXTConstants.TOAST, text);
        msg.setData(bundle);
        nxtHandler.sendMessage(msg);
       
    }
    
    private void log(String text) {
        
        Message msg = nxtHandler.obtainMessage(NXTConstants.MESSAGE_EDIT_TEXT);
        Bundle bundle = new Bundle();
        bundle.putString(NXTConstants.LOG_MESSAGE, text);
        msg.setData(bundle);
        nxtHandler.sendMessage(msg);
    }

    public synchronized void connect(BluetoothDevice device) {
        //Log.i("NXT", "NXTTalker.connect()");
        
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }
        
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }
    
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        
        if (mDistanceThread != null) {
        	mDistanceThread.cancel();
        	mDistanceThread = null;
        }
        
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();
        
        mDistanceThread = new DistanceThread();
        mDistanceThread.start();
        
        
        
        log("Connected to " + device.getName());
        
        setState(STATE_CONNECTED);
    }
    
    public synchronized void stop() {
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        
        if(mDistanceThread != null) {
        	mDistanceThread.cancel();
        	mDistanceThread = null;
        }
        setState(STATE_NONE);
    }
    
    private void connectionFailed() {
        setState(STATE_NONE);
        log("Connection failed");
    }
    
    private void connectionLost() {
        setState(STATE_NONE);
        log("Connection lost");
    }
    
    public void motors(byte l, byte r, boolean speedReg, boolean motorSync) {
        byte[] data = { 0x0c, 0x00, (byte) 0x80, 0x04, 0x02, 0x32, 0x07, 0x00, 0x00, 0x20, 0x00, 0x00, 0x00, 0x00,
                        0x0c, 0x00, (byte) 0x80, 0x04, 0x01, 0x32, 0x07, 0x00, 0x00, 0x20, 0x00, 0x00, 0x00, 0x00};
        
        //Log.i("NXT", "motors: " + Byte.toString(l) + ", " + Byte.toString(r));
        // TODO send the correct data to the motors based on the l and r parameters
        //See Appendix 2 SETOUTPUTSTATE
       
        if (speedReg) {
            data[7] |= 0x01;
            data[21] |= 0x01;
        }
        if (motorSync) {
            data[7] |= 0x02;
            data[21] |= 0x02;
        }
        write(data);
    }
    
    public void initUltrasonic()
    {
    	//See Appendex 2 - LS Write Command  AND Appendix 7 - for Ultrasonic sensor
    	log("Init Ultrasonic");
    	byte[] data = {0x08, 0x00, 0x00, 0x0f, 0x03, 0x03, 0x00, 0x02, 0x41, 0x02,
    			0x05, 0x00, (byte) 0x00, 0x05, 0x03, 0x0b, 0x00};
    	write(data);
    }
    
    public void requestDistance()
    {
    	//See Appendex 2 - LSWRITE Command AND Appendix 7 - for Ultrasonic sensor we want to read the measurement for byte 0.
    	log("Request Distance");
    	// TODO Create command for Requesting Distance from PORT 4
    }
    
    public void requestStatus()
    {
    	//See Appendex 2 - LSGETSTATUS Command
    	log("Request Status");
    	// TODO Create command for Requesting Status from PORT 4
    }
    
    public void readDistance()
    {
    	//See Appendex 2 - LSREAD Command AND Appendix 7 - for Ultrasonic sensor we want to read the measurement for byte 0.
    	log("Read Distance");
    	// TODO Create Command for Reading the Distance value from PORT 4
    }
    
    private void write(byte[] out) {
        ConnectedThread r;
        synchronized (this) {
            if (mState != STATE_CONNECTED) {
                return;
            }
            r = mConnectedThread;
        }
        r.write(out);
        msDelay(500);
    }
    
    private class ConnectThread extends Thread {
        private BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        
        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
        }
        
        public void run() {
            setName("ConnectThread");
            mAdapter.cancelDiscovery();
            
            try {
                mmSocket = mmDevice.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
                mmSocket.connect();
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    // This is a workaround that reportedly helps on some older devices like HTC Desire, where using
                    // the standard createRfcommSocketToServiceRecord() method always causes connect() to fail.
                    Method method = mmDevice.getClass().getMethod("createRfcommSocket", new Class[] { int.class });
                    mmSocket = (BluetoothSocket) method.invoke(mmDevice, Integer.valueOf(1));
                    mmSocket.connect();
                } catch (Exception e1) {
                    e1.printStackTrace();
                    connectionFailed();
                    try {
                        mmSocket.close();
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    }
                    return;
                }
            }
            
            synchronized (NXTTalker.this) {
                mConnectThread = null;
            }
            
            connected(mmSocket, mmDevice);
        }
        
        public void cancel() {
            try {
                if (mmSocket != null) {
                    mmSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        
        
        
        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }
        
        private void processMessage(byte[] message)
        {
        	byte[] commandMessage = ArrayUtils.subarray(message, 2, message.length);
        	CommandType.sendCommandToMessage(commandMessage, nxtHandler);
        }
        
        public void run() {
            byte[] buffer = new byte[1024];
            
            while (true) {
                try {
                	
                	log("Starting Read");
                	int bytes;
                    int currentPosition = 0;
                    int endPosition = 0;
                    int startPosition = 0;
                		bytes = mmInStream.read(buffer);
                		log("Read Size: " + bytes);
                		endPosition = startPosition + buffer[startPosition] + 2;
                        
                        while(currentPosition < bytes -1)
                        {
                        	String message = "Message: ";
                        	int messageLength = (endPosition + 1) - startPosition;
                        	byte[] byteMessage = new byte[messageLength];
                        	for(int i = startPosition; i < endPosition; i++)
                            {
                            	message = message + " " + Byte.toString(buffer[i]);
                            	byteMessage[i-startPosition] = buffer[i];
                            }
                            processMessage(byteMessage);
                            log(message);
                            
							
                            currentPosition = endPosition;
                            startPosition = endPosition;
                            endPosition = startPosition + buffer[startPosition] + 2;
                        }
                        
                	
                    
                    //log(Integer.toString(bytes) + " bytes read from device");
                } catch (IOException e) {
                    e.printStackTrace();
                    cancel();
                    connectionLost();
                    break;
                }
            }
        }
        
        public synchronized void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);
            } catch (IOException e) {
                e.printStackTrace();
                log("Write failed");
            }
        }
        
        public void cancel() {
            try {
            	mmInStream.close();
            	mmOutStream.close();
                mmSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    private class DistanceThread extends Thread {
    	private volatile boolean runThread = true;
    	public void run() {
    			msDelay(1000);
    		initUltrasonic();
    		while(runThread)
    		{
    			requestDistance();
        		requestStatus();
        		readDistance();
    		
    		}
    		
    	}
    	public void cancel()
    	{
    		runThread = false;
    	}
    }
    public static void msDelay(long period)
    {
        if (period <= 0) return;
        long end = System.currentTimeMillis() + period;
        boolean interrupted = false;
        do {
            try {
                Thread.sleep(period);
            } catch (InterruptedException ie)
            {
                interrupted = true;
            }
            period = end - System.currentTimeMillis();
        } while (period > 0);
        if (interrupted)
            Thread.currentThread().interrupt();
    }
}
