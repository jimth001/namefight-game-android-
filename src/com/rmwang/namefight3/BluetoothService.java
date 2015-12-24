package com.rmwang.namefight3;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.UUID;

import android.R.integer;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;
//用于管理连接的Service
public class BluetoothService {
    // 本应用的唯一 UUID
	 private static final UUID MY_UUID = UUID.
	    fromString("00001101-0000-1000-8000-00805F9B34FB");
    // 成员变量
    private final BluetoothAdapter btAdapter;
    private final Handler myHandler;
    private AcceptThread myAcceptThread;
    private ConnectThread myConnectThread;
    private ConnectedThread myConnectedThread;
    private int myState;
    // 表示当前连接状态的常量
    public static final int STATE_NONE = 1;       // 什么也没做
    public static final int STATE_LISTEN = 2;     // 正在监听连接
    public static final int STATE_CONNECTING = 3; // 正在连接
    public static final int STATE_CONNECTED = 4;  // 已连接到设备
    private boolean isServer=false;
	public void setServerState(boolean a)
	{
		isServer=a;
	}
	public boolean getServerState()
	{
		return isServer;
	}
    // 构造器
    public BluetoothService(Context context, Handler handler) {
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        myState = STATE_NONE;
        myHandler = handler;
        isServer = false;
    }
    //设置当前连接状态的方法
    private synchronized void setState(int state) {
        myState = state;
        myHandler.obtainMessage(FightBaseBluetoothActivity.State_Change, state, -1).sendToTarget();
    }
    //获取当前连接状态的方法
    public synchronized int getState() {
        return myState;
    }
    //开启service的方法
    public synchronized void start() {
        // 关闭不必要的线程
        if (myConnectThread != null) {myConnectThread.cancel(); myConnectThread = null;}
        if (myConnectedThread != null) {myConnectedThread.cancel(); myConnectedThread = null;}
        if (myAcceptThread == null) {// 开启线程监听连接
            myAcceptThread = new AcceptThread();
            myAcceptThread.start();
        }
        setState(STATE_LISTEN);
    }
    //连接设备的方法
    public synchronized void connect(BluetoothDevice device) {
    	// 关闭不必要的线程
        if (myState == STATE_CONNECTING) {
            if (myConnectThread != null) {myConnectThread.cancel(); myConnectThread = null;}
        }
        if (myConnectedThread != null) {myConnectedThread.cancel(); myConnectedThread = null;}
        // 开启线程连接设备
        myConnectThread = new ConnectThread(device);
        myConnectThread.start();
        setState(STATE_CONNECTING);
    }
    //开启管理和已连接的设备间通话的线程的方法
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device,int callType) {//1是server，0是client
        // 关闭不必要的线程
        if (myConnectThread != null) {myConnectThread.cancel(); myConnectThread = null;}
        if (myConnectedThread != null) {myConnectedThread.cancel(); myConnectedThread = null;}
        if (myAcceptThread != null) {myAcceptThread.cancel(); myAcceptThread = null;}
        // 创建并启动ConnectedThread
        myConnectedThread = new ConnectedThread(socket);
        myConnectedThread.start();
        // 发送已连接的设备名称到主界面Activity
        Message msg = myHandler.obtainMessage(FightBaseBluetoothActivity.Device_Name);
        Bundle bundle = new Bundle();
        bundle.putString(FightBaseBluetoothActivity.DeviceName, device.getName());
        msg.setData(bundle);
        myHandler.sendMessage(msg);
        setState(STATE_CONNECTED);
        if(callType==1) isServer=true;
    }
    public synchronized void stop() {//停止所有线程的方法
        if (myConnectThread != null) {myConnectThread.cancel(); myConnectThread = null;}
        if (myConnectedThread != null) {myConnectedThread.cancel(); myConnectedThread = null;}
        if (myAcceptThread != null) {myAcceptThread.cancel(); myAcceptThread = null;}
        setState(STATE_NONE);
    }
    public void write(byte []out) {//向ConnectedThread写入数据的方法
        ConnectedThread tmpCt;// 创建临时对象引用
        synchronized (this) {// 锁定ConnectedThread
            if (myState != STATE_CONNECTED) return;
            tmpCt = myConnectedThread;
        }
        tmpCt.write(out);// 写入数据
    }
    private class AcceptThread extends Thread {//用于监听连接的线程
        // 本地服务器端ServerSocket
        private final BluetoothServerSocket mmServerSocket;
        
        public AcceptThread() {
            BluetoothServerSocket tmpSS = null;
            
            try {// 创建用于监听的服务器端ServerSocket
                tmpSS = btAdapter.listenUsingRfcommWithServiceRecord("BluetoothChat", MY_UUID);
                
            } catch (IOException e) {
                e.printStackTrace();
            }
            mmServerSocket = tmpSS;
            
        }
        public void run() {
            setName("AcceptThread");
            BluetoothSocket socket = null;
            while (myState != STATE_CONNECTED) {//如果没有连接到设备
                try {
                    socket = mmServerSocket.accept();//获取连接的Socket
                } catch (IOException e) {
                	e.printStackTrace();
                    break;
                }
                if (socket != null) {// 如果连接成功
                    synchronized (BluetoothService.this) {
                        switch (myState) {
                        case STATE_LISTEN:
                        case STATE_CONNECTING:
                            // 开启管理连接后数据交流的线程
                            connected(socket,socket.getRemoteDevice(),1);
                            break;
                        case STATE_NONE:
                        case STATE_CONNECTED:
                            try {// 关闭新Socket
                                socket.close();
                            } catch (IOException e) {
                            	e.printStackTrace();
                            }
                            break;
                        }
                    }
                }
            }
        }
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) {
               e.printStackTrace();
            }
        }
    }
    //用于尝试连接其他设备的线程
    private class ConnectThread extends Thread {
        private final BluetoothSocket myBtSocket;
        
        private final BluetoothDevice mmDevice;
        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;
            BluetoothSocket tmp2= null;
            // 通过正在连接的设备获取BluetoothSocket
            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
                tmp2=device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
               e.printStackTrace();
            }
            myBtSocket = tmp;
           
        }
        public void run() {
            setName("ConnectThread");
            btAdapter.cancelDiscovery();// 取消搜索设备
            try {// 连接到BluetoothSocket
                myBtSocket.connect();//尝试连接
                
            } catch (IOException e) {
            	setState(STATE_LISTEN);//连接断开后设置状态为正在监听
            	myHandler.obtainMessage(999,0,1).sendToTarget();
                try {// 关闭socket
                    myBtSocket.close();
                } catch (IOException e2) {
                    e.printStackTrace();
                }
                BluetoothService.this.start();//如果连接不成功，重新开启service
                return;
            }
            
            	
            synchronized (BluetoothService.this) {// 将ConnectThread线程置空
                myConnectThread = null;
            }
            connected(myBtSocket,mmDevice,0);// 开启管理连接后数据交流的线程
        }
        public void cancel() {
            try {
                myBtSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    //用于管理连接后数据交流的线程
    private class ConnectedThread extends Thread {
        private final BluetoothSocket myBtSocket;
       
        private final InputStream mmInStream;
        private final OutputStream myOs;
       
        public ConnectedThread(BluetoothSocket socket) {
            myBtSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            
           
            // 获取BluetoothSocket的输入输出流
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
                
            } catch (IOException e) {
                e.printStackTrace();
            }
            mmInStream = tmpIn;
            myOs = tmpOut;
            
            
        }
        public void sendtoHandler(Handler m,int what,int arg1,int arg2,Object obj)
        {
        	 Message msg = new Message();
			 
			 msg.what = what;
			 msg.obj=obj;
			 msg.arg1=arg1;
			 msg.arg2=arg2;
			 m.sendMessage(msg);
        }
        public void  run() {
            byte[] buffer = new byte[8192];
            
            int bytes;
            
            while (true) {//读
            	
            		synchronized (mmInStream) {
            			try {
	                    	byte[]len=new byte[4];
	                    	mmInStream.read(len,0,4);
	                    	bytes=FightBaseBluetoothActivity.byteArrayToInt(len);
	                    	if(buffer.length>=bytes)
	                    	{
	                    		mmInStream.read(buffer, 0, bytes);
	                    		//myHandler.obtainMessage(999,5,bytes).sendToTarget();
	                    	}
	                    	else{
	                    		//myHandler.obtainMessage(999,4,bytes).sendToTarget();
	                    		//continue;
	                    	}
	                    	if(bytes==1)//控制信息流
	                    	{
                    		
	                    		switch (buffer[0]) {
	                    		case -1://服务器端已接收到名字,
	                    			sendtoHandler(myHandler, -1, 0, 0, null);
	                    			//myHandler.obtainMessage(-1).sendToTarget();
	                    			break;
	    						case -2://服务器尚未开始
	    							sendtoHandler(myHandler, -2, 0, 0, null);
	    							//myHandler.obtainMessage(-2).sendToTarget();
	    							break;
	    						case -3://服务器正在计算或轮到服务器出招
	    							sendtoHandler(myHandler, -3, 0, 0, null);
	    							//myHandler.obtainMessage(-3).sendToTarget();
	    							break;
	    						case -4://服务器告知客户端战斗结束
	    							sendtoHandler(myHandler, -4, 0, 0, null);
	    							//myHandler.obtainMessage(-4).sendToTarget();
	    							break;
	    						case -5://服务器告知客户端可以出招
	    							sendtoHandler(myHandler, 12, 0, 0, null);
	    							//myHandler.obtainMessage(12).sendToTarget();
	    							break;
	    						case -6://服务器端通知客户端修改游戏模式
	    							sendtoHandler(myHandler, -6, 0, 0, null);
	    							//myHandler.obtainMessage(-6).sendToTarget();
	    							break;
	    						case -15:sendtoHandler(myHandler, 11, 0, 0, null);break;
	    						case -14:sendtoHandler(myHandler, 11, 1, 0, null);break;
	    						case -13:sendtoHandler(myHandler, 11, 2, 0, null);break;
	    						case -12:sendtoHandler(myHandler, 11, 3, 0, null);break;
	    						case -11:sendtoHandler(myHandler, 11, 4, 0, null);break;
	    						case -10:sendtoHandler(myHandler, 11, 5, 0, null);break;
	    						default://sendtoHandler(myHandler,11,(int)(15+buffer[0]), 0, null);
	    							//myHandler.obtainMessage(11,(int)(15+buffer[0])).sendToTarget();
	    							break;
	                    		}
	                    	}
	                    	else
	                    	{
	                    		String aString="";
	                    		try{
	                    			aString=new String(buffer,0,bytes,"UTF-8");
    							
	                    		} catch (UnsupportedEncodingException e) {
	                    			// TODO 自动生成的 catch 块
    							
	                    		}
	                    		Message msg = new Message();
	                    		Bundle data = new Bundle();
	            				 data.putString("fightdsp",aString);
	            				 msg.what = FightBaseBluetoothActivity.Message;
	            				 msg.setData(data);
	            				 myHandler.sendMessage(msg);
	                    		//myHandler.obtainMessage(FightBaseBluetoothActivity.Message, bytes, -1, buffer)//把数据长度和数据发送给主线程
	                            //.sendToTarget();
	                    	}
                        
                    	
                        
		                    } catch (IOException e) {
		                        e.printStackTrace();
		                        break;
		                    }
            			}
            	}
                
            
        }
        //向输出流中写入数据的方法
        public synchronized void write(byte[] out) {
        		try {
        			
        			myOs.write(out);
        		} catch (IOException e) {
        			e.printStackTrace();
        		}
        }
        
        public void cancel() {
            try {
                myBtSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}