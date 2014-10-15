package com.bobpaulin.droidnxtdistancetracker;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class NXTTalker {

    public static final int STATE_NONE = 0;
    public static final int STATE_CONNECTING = 1;
    public static final int STATE_CONNECTED = 2;
    
    private int mState;
    private List<Handler> mHandlerList;
    private BluetoothAdapter mAdapter;
    
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    
    public NXTTalker() {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mHandlerList = new ArrayList<Handler>();
        setState(STATE_NONE);
    }
    
    public void addHandler(Handler handler)
    {
    	mHandlerList.add(handler);
    }

    private synchronized void setState(int state) {
        mState = state;
        if (mHandlerList != null) {
        	for(Handler currentHandler: mHandlerList)
        	{
        		currentHandler.obtainMessage(NXTConstants.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
        	}
            
        } else {
            //XXX
        }
    }
    
    public synchronized int getState() {
        return mState;
    }
    
    private void toast(String text) {
        if (mHandlerList != null) {
        	for(Handler currentHandler: mHandlerList)
        	{
        		Message msg = currentHandler.obtainMessage(NXTConstants.MESSAGE_TOAST);
                Bundle bundle = new Bundle();
                bundle.putString(NXTConstants.TOAST, text);
                msg.setData(bundle);
                currentHandler.sendMessage(msg);
        	}
            
        } else {
            //XXX
        }
    }
    
    private void log(String text) {
        if (mHandlerList != null) {
        	for(Handler currentHandler: mHandlerList)
        	{
        		Message msg = currentHandler.obtainMessage(NXTConstants.MESSAGE_EDIT_TEXT);
                Bundle bundle = new Bundle();
                bundle.putString(NXTConstants.LOG_MESSAGE, text);
                msg.setData(bundle);
                currentHandler.sendMessage(msg);
        	}
            
        } else {
            //XXX
        }
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
        
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();
        
        DistanceThread distThread = new DistanceThread();
        distThread.start();
        
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
                        0x0c, 0x00, (byte) 0x80, 0x04, 0x01, 0x32, 0x07, 0x00, 0x00, 0x20, 0x00, 0x00, 0x00, 0x00,
                        0x0c, 0x00, (byte) 0x00, 0x05, 0x03, 0x0a, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                        0x0c, 0x00, (byte) 0x00, 0x07, 0x03, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00}; //also ping distance
        
        //Log.i("NXT", "motors: " + Byte.toString(l) + ", " + Byte.toString(r));
        
        data[5] = l;
        data[19] = r;
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
    
    public void motor(int motor, byte power, boolean speedReg, boolean motorSync) {
        byte[] data = { 0x0c, 0x00, (byte) 0x80, 0x04, 0x02, 0x32, 0x07, 0x00, 0x00, 0x20, 0x00, 0x00, 0x00, 0x00};//Also request Distance
        
        //Log.i("NXT", "motor: " + Integer.toString(motor) + ", " + Byte.toString(power));
        
        if (motor == 0) {
            data[4] = 0x02;
        } else {
            data[4] = 0x01;
        }
        data[5] = power;
        if (speedReg) {
            data[7] |= 0x01;
        }
        if (motorSync) {
            data[7] |= 0x02;
        }
        write(data);
    }
    
    public void motors3(byte l, byte r, byte action, boolean speedReg, boolean motorSync) {
        byte[] data = { 0x0c, 0x00, (byte) 0x80, 0x04, 0x02, 0x32, 0x07, 0x00, 0x00, 0x20, 0x00, 0x00, 0x00, 0x00,
                        0x0c, 0x00, (byte) 0x80, 0x04, 0x01, 0x32, 0x07, 0x00, 0x00, 0x20, 0x00, 0x00, 0x00, 0x00,
                        0x0c, 0x00, (byte) 0x80, 0x04, 0x00, 0x32, 0x07, 0x00, 0x00, 0x20, 0x00, 0x00, 0x00, 0x00 };
        
        //Log.i("NXT", "motors3: " + Byte.toString(l) + ", " + Byte.toString(r) + ", " + Byte.toString(action));
        
        data[5] = l;
        data[19] = r;
        data[33] = action;
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
    	log("Init Ultrasonic");
    	byte[] data = {0x08, 0x00, 0x00, 0x0f, 0x03, 0x03, 0x00, 0x02, 0x41, 0x02,
    			0x05, 0x00, (byte) 0x00, 0x05, 0x03, 0x0b, 0x00};
    	write(data);
    }
    
    public void requestDistance()
    {
    	log("Request Distance");
    	byte[] data = {0x07, 0x00, (byte) 0x00, 0x0F, 0x03, 0x02, 0x01, 0x02, 0x42};
    	write(data);
    }
    
    public void requestStatus()
    {
    	log("Request Status");
    	byte[] data = {0x3, 0x00, 0x00, 0x0e, 0x03};
    	write(data);
    }
    
    public void readDistance()
    {
    	log("Read Distance");
    	byte[] data = {0x3, 0x00, 0x00, 0x10, 0x03};
    	write(data);
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
                        	for(int i = startPosition; i < endPosition; i++)
                            {
                            	message = message + " " + Byte.toString(buffer[i]);
                            }
                            
                            log(message);
                            
                            	msDelay(1000);
							
                            currentPosition = endPosition;
                            startPosition = endPosition;
                            endPosition = startPosition + buffer[startPosition] + 2;
                        }
                        
                	
                    
                    //log(Integer.toString(bytes) + " bytes read from device");
                } catch (IOException e) {
                    e.printStackTrace();
                    connectionLost();
                    log("Boom");
                    break;
                }
            }
        }
        
        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);
            } catch (IOException e) {
                e.printStackTrace();
                log("Write failed");
                // XXX?
            }
        }
        
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    private class DistanceThread extends Thread {
    	public void run() {
    			msDelay(1000);
    		initUltrasonic();
    		while(true)
    		{
    			requestDistance();
    			msDelay(1000);
        		requestStatus();
        		msDelay(1000);
        		readDistance();
        		msDelay(2000);
    		
    		}
    		
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
