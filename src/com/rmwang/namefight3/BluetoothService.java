package com.rmwang.namefight3;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
//���ڹ������ӵ�Service
public class BluetoothService {
    // ��Ӧ�õ�Ψһ UUID
	 private static final UUID MY_UUID = UUID.
	    fromString("00001101-0000-1000-8000-00805F9B34FB");
    // ��Ա����
    private final BluetoothAdapter btAdapter;
    private final Handler myHandler;
    private AcceptThread myAcceptThread;
    private ConnectThread myConnectThread;
    private ConnectedThread myConnectedThread;
    private int myState;
    // ��ʾ��ǰ����״̬�ĳ���
    public static final int STATE_NONE = 1;       // ʲôҲû��
    public static final int STATE_LISTEN = 2;     // ���ڼ�������
    public static final int STATE_CONNECTING = 3; // ��������
    public static final int STATE_CONNECTED = 4;  // �����ӵ��豸
    private boolean isServer=false;
	public void setServerState(boolean a)
	{
		isServer=a;
	}
	public boolean getServerState()
	{
		return isServer;
	}
    // ������
    public BluetoothService(Context context, Handler handler) {
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        myState = STATE_NONE;
        myHandler = handler;
        isServer = false;
    }
    //���õ�ǰ����״̬�ķ���
    private synchronized void setState(int state) {
        myState = state;
        myHandler.obtainMessage(FightBaseBluetoothActivity.State_Change, state, -1).sendToTarget();
    }
    //��ȡ��ǰ����״̬�ķ���
    public synchronized int getState() {
        return myState;
    }
    //����service�ķ���
    public synchronized void start() {
        // �رղ���Ҫ���߳�
        if (myConnectThread != null) {myConnectThread.cancel(); myConnectThread = null;}
        if (myConnectedThread != null) {myConnectedThread.cancel(); myConnectedThread = null;}
        if (myAcceptThread == null) {// �����̼߳�������
            myAcceptThread = new AcceptThread();
            myAcceptThread.start();
        }
        setState(STATE_LISTEN);
    }
    //�����豸�ķ���
    public synchronized void connect(BluetoothDevice device) {
    	// �رղ���Ҫ���߳�
        if (myState == STATE_CONNECTING) {
            if (myConnectThread != null) {myConnectThread.cancel(); myConnectThread = null;}
        }
        if (myConnectedThread != null) {myConnectedThread.cancel(); myConnectedThread = null;}
        // �����߳������豸
        myConnectThread = new ConnectThread(device);
        myConnectThread.start();
        setState(STATE_CONNECTING);
    }
    //��������������ӵ��豸��ͨ�����̵߳ķ���
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device,int callType) {//1��server��0��client
        // �رղ���Ҫ���߳�
        if (myConnectThread != null) {myConnectThread.cancel(); myConnectThread = null;}
        if (myConnectedThread != null) {myConnectedThread.cancel(); myConnectedThread = null;}
        if (myAcceptThread != null) {myAcceptThread.cancel(); myAcceptThread = null;}
        // ����������ConnectedThread
        myConnectedThread = new ConnectedThread(socket);
        myConnectedThread.start();
        // ���������ӵ��豸���Ƶ�������Activity
        Message msg = myHandler.obtainMessage(FightBaseBluetoothActivity.Device_Name);
        Bundle bundle = new Bundle();
        bundle.putString(FightBaseBluetoothActivity.DeviceName, device.getName());
        msg.setData(bundle);
        myHandler.sendMessage(msg);
        setState(STATE_CONNECTED);
        if(callType==1) isServer=true;
    }
    public synchronized void stop() {//ֹͣ�����̵߳ķ���
        if (myConnectThread != null) {myConnectThread.cancel(); myConnectThread = null;}
        if (myConnectedThread != null) {myConnectedThread.cancel(); myConnectedThread = null;}
        if (myAcceptThread != null) {myAcceptThread.cancel(); myAcceptThread = null;}
        setState(STATE_NONE);
    }
    public void write(byte []out) {//��ConnectedThreadд�����ݵķ���
        ConnectedThread tmpCt;// ������ʱ��������
        synchronized (this) {// ����ConnectedThread
            if (myState != STATE_CONNECTED) return;
            tmpCt = myConnectedThread;
        }
        tmpCt.write(out);// д������
    }
    private class AcceptThread extends Thread {//���ڼ������ӵ��߳�
        // ���ط�������ServerSocket
        private final BluetoothServerSocket mmServerSocket;
        public AcceptThread() {
            BluetoothServerSocket tmpSS = null;
            try {// �������ڼ����ķ�������ServerSocket
                tmpSS = btAdapter.listenUsingRfcommWithServiceRecord("BluetoothChat", MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mmServerSocket = tmpSS;
        }
        public void run() {
            setName("AcceptThread");
            BluetoothSocket socket = null;
            while (myState != STATE_CONNECTED) {//���û�����ӵ��豸
                try {
                    socket = mmServerSocket.accept();//��ȡ���ӵ�Socket
                } catch (IOException e) {
                	e.printStackTrace();
                    break;
                }
                if (socket != null) {// ������ӳɹ�
                    synchronized (BluetoothService.this) {
                        switch (myState) {
                        case STATE_LISTEN:
                        case STATE_CONNECTING:
                            // �����������Ӻ����ݽ������߳�
                            connected(socket, socket.getRemoteDevice(),1);
                            break;
                        case STATE_NONE:
                        case STATE_CONNECTED:
                            try {// �ر���Socket
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
    //���ڳ������������豸���߳�
    private class ConnectThread extends Thread {
        private final BluetoothSocket myBtSocket;
        private final BluetoothDevice mmDevice;
        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;
            // ͨ���������ӵ��豸��ȡBluetoothSocket
            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
               e.printStackTrace();
            }
            myBtSocket = tmp;
        }
        public void run() {
            setName("ConnectThread");
            btAdapter.cancelDiscovery();// ȡ�������豸
            try {// ���ӵ�BluetoothSocket
                myBtSocket.connect();//��������
            } catch (IOException e) {
            	setState(STATE_LISTEN);//���ӶϿ�������״̬Ϊ���ڼ���
                try {// �ر�socket
                    myBtSocket.close();
                } catch (IOException e2) {
                    e.printStackTrace();
                }
                BluetoothService.this.start();//������Ӳ��ɹ������¿���service
                return;
            }
            synchronized (BluetoothService.this) {// ��ConnectThread�߳��ÿ�
                myConnectThread = null;
            }
            connected(myBtSocket, mmDevice,0);// �����������Ӻ����ݽ������߳�
        }
        public void cancel() {
            try {
                myBtSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    //���ڹ������Ӻ����ݽ������߳�
    private class ConnectedThread extends Thread {
        private final BluetoothSocket myBtSocket;
        private final InputStream mmInStream;
        private final OutputStream myOs;
        public ConnectedThread(BluetoothSocket socket) {
            myBtSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            // ��ȡBluetoothSocket�����������
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mmInStream = tmpIn;
            myOs = tmpOut;
        }
        /*Լ������ҿ��Ƴ���ʱ�Ĳ����淶
         * ��һ���ֽڵĺ���
         * 1.����������shenfa
         * 2.�������˺�
         * 3.��ս�������������ĸ��ֽڹ���int����ս��������btye����
         * 4.�������˺�
         * 5.���״̬����//�Ƴ�״̬�ļ������Լ����
         * 
         */
        public void run() {
            byte[] buffer = new byte[8192];
            int bytes;
            
            while (true) {//��
                try {
                	bytes = mmInStream.read(buffer);
                	if(bytes==1)//������Ϣ��
                    {
                    	switch (buffer[0]) {
						case -1://���������ѽ��յ�����
							myHandler.obtainMessage(-1).sendToTarget();
							break;
						case -2://��������δ��ʼ
							myHandler.obtainMessage(-2).sendToTarget();
							break;
						case -3://���������ڼ���
							myHandler.obtainMessage(-3).sendToTarget();
							break;
						case -4://��������֪�ͻ���ս������
							myHandler.obtainMessage(-4).sendToTarget();
							break;
						default:
							break;
						}
                    }
                	else{
                    myHandler.obtainMessage(AutoRandomFightBaseBluetooth.Message, bytes, -1, buffer)//�����ݳ��Ⱥ����ݷ��͸����߳�
                    .sendToTarget();
                	}
                    
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }
        //���������д�����ݵķ���
        public void write(byte[] out) {
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