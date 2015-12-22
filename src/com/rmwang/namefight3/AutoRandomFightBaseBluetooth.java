package com.rmwang.namefight3;



import java.io.UnsupportedEncodingException;

import android.R.integer;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
public class AutoRandomFightBaseBluetooth extends Activity {
	private String myDeviceName;
	 //UI��Ϣ��ʾ�豸����
		public static final String DeviceName = null;
	//������������������
		private BluetoothAdapter btAdapter = null;
		private BluetoothService myService = null;
	//������������handler��Ϣ���̼߳�ͨ��
    public static final int Message = 1;
    public static final int State_Change = 96;
    public static final int Device_Name = 4;
    //
    public static final int starttimer=501;
	//��Ϸbutton��
    private ImageButton Skillbutton1;
    private ImageButton Skillbutton2;
    private ImageButton Skillbutton3;
    private ImageButton Skillbutton4;
    private ImageButton Skillbutton5;
    private ImageButton Skillbutton6;
    private Button startGameButton;
    private Thread fightThread;
	//�������豸
	private static final int Enable_Bluetooth = 2;
	//ս�������
	private Fighters fighters=null;
	@Override
	    protected void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.activity_autorandomfightbasebluetooth);
	        btAdapter = BluetoothAdapter.getDefaultAdapter();
	        if(btAdapter == null) {
	        	Toast.makeText(this, R.string.bt_not_available, Toast.LENGTH_LONG).show();
	        	return;
	        }
	        Skillbutton1=(ImageButton)findViewById(R.id.imageButton1);
	        Skillbutton2=(ImageButton)findViewById(R.id.imageButton2);
	        Skillbutton3=(ImageButton)findViewById(R.id.imageButton3);
	        Skillbutton4=(ImageButton)findViewById(R.id.imageButton4);
	        Skillbutton5=(ImageButton)findViewById(R.id.imageButton5);
	        Skillbutton6=(ImageButton)findViewById(R.id.imageButton6);
	        startGameButton=(Button)findViewById(R.id.startgame1);
	        //����button����¼���
		    Skillbutton1.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					// TODO �Զ����ɵķ������
					
				}
			}); 
		    Skillbutton2.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					// TODO �Զ����ɵķ������
					
				}
			});
		    Skillbutton3.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					// TODO �Զ����ɵķ������
					
				}
			});
		    Skillbutton4.setOnClickListener(new OnClickListener() {
	
		    	@Override
		    	public void onClick(View arg0) {
		    		// TODO �Զ����ɵķ������
		
		    	}
		    });
		    Skillbutton5.setOnClickListener(new OnClickListener() {
	
		    	@Override
		    	public void onClick(View arg0) {
		    		// TODO �Զ����ɵķ������
		
		    	}
		    });
		    Skillbutton6.setOnClickListener(new OnClickListener() {
	
		    	@Override
		    	public void onClick(View arg0) {
		    		// TODO �Զ����ɵķ������
		
		    	}
		    });
		    
		}


	    @Override
	    public boolean onCreateOptionsMenu(Menu menu) {
	        // Inflate the menu; this adds items to the action bar if it is present.
	        getMenuInflater().inflate(R.menu.fightbasebluetooth, menu);
	        return true;
	    }
	    @Override
	    public void onStart() {
	    	super.onStart();
	    	
	    	if(!btAdapter.isEnabled()) {
	    		Intent btintent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
	    		startActivityForResult(btintent, Enable_Bluetooth);
	    	}
	    		else if(myService == null) {
	    			myService = new BluetoothService(this, mHandler);
	    		}
	    	final EditText inputServer = new EditText(this);
	    	inputServer.setText("");
	        AlertDialog.Builder builder = new AlertDialog.Builder(this);
	        builder.setTitle("��������������").setIcon(android.R.drawable.ic_dialog_info).setView(inputServer)
	                .setNegativeButton("Cancel", null);
	        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

	            public void onClick(DialogInterface dialog, int which) {
	               inputServer.getText().toString();
	             }
	        });
	        while(inputServer.getText().toString().length()==0)
	        {
	        	builder.show();
	        }
	        final String nameString=inputServer.getText().toString();
	        Toast.makeText(getApplicationContext(), nameString, Toast.LENGTH_SHORT).show();
	        
	    	startGameButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					// TODO �Զ����ɵķ������
					if(myService.getState()==BluetoothService.STATE_CONNECTED)//��������
					{
						fightThread=new Thread(fighters,"ս�����߳�");
						fightThread.start();
						if(myService.getServerState()==false)//���Ƿ�������
						{
							
							mySendMessage(nameString.getBytes());//
						}
						else{//����������
							fighters=new Fighters(nameString, mHandler);
						}
					}
					else{
						Toast.makeText(getApplicationContext(), "��δ���ӵ��豸", Toast.LENGTH_SHORT).show();
					}
				}
			});
	    }
	    @Override
	    public synchronized void onResume() {
	    	super.onResume();
	    	if(myService != null) {
	    		if(myService.getState() == BluetoothService.STATE_NONE) {
	    			myService.start();
	    		}
	    	}
	    }
	    @Override
	    protected void onPause() {
	        super.onPause();
	        if(btAdapter.isEnabled()) {
	        	//sendMessage((byte)0x00);//.........151104
	        }
	    }
	    @Override
	    public void onDestroy() {
	        super.onDestroy();
	        if (myService != null) myService.stop();
	    }
	    public void mySendMessage(byte []b) {
			if (myService.getState() != BluetoothService.STATE_CONNECTED) {
				Toast.makeText(getApplicationContext(), "��δ���ӵ��豸"
	                    , Toast.LENGTH_SHORT).show();
	            return;
	        }
	        myService.write(b);
		}
	    
	  //���������¼������ؽ��
		public void onActivityResult(int requestCode, int resultCode, Intent data) {
			switch (requestCode) {//requestCode��ʶ���ĸ�Activity��ת����Activity ��startActivityForResult�е�requestCode���Ӧ 
			case 1://1���������豸  //resultCode��ʾ����ֵ״̬ ����Activityͨ����setResult()��������       data�����˷�������
				// ����豸�б�Activity����һ�����ӵ��豸
				if (resultCode == Activity.RESULT_OK) {
					// ��ȡ�豸��MAC��ַ
					String address = data.getExtras().getString(
							MyDeviceListActivity.EXTRA_DEVICE_ADDRESS);
					// ��ȡBLuetoothDevice����
					BluetoothDevice device = btAdapter.getRemoteDevice(address);
					myService.connect(device);// ���Ӹ��豸
				}
				break;
			case Enable_Bluetooth:
	          if (resultCode == Activity.RESULT_OK) {
	        	  myService = new BluetoothService(this, mHandler);
	        	  // ��ʼ��BluetoothService��ִ����������
	              //Handler����mHandler���������ݵĽ������߳�֮���ͨ��
	             } 
	          else {
	            Toast.makeText(this, R.string.bt_not_enable, Toast.LENGTH_SHORT).show();
	            return;
	            }
			}
		}
		@Override
		public boolean onPrepareOptionsMenu(Menu menu) {
			// �����豸�б�Activity
			Intent serverIntent = new Intent(this, MyDeviceListActivity.class);
			startActivityForResult(serverIntent, 1);//1�뷽��onActivityResult�е�1���Ӧ
			return true;
		}
		 //���������̴߳�������Ϣ��
		@SuppressLint("HandlerLeak") private final Handler mHandler = new Handler() {
				@SuppressLint("HandlerLeak") @Override
				public void handleMessage(android.os.Message msg) {
					 switch (msg.what){
			            case Device_Name:
			                myDeviceName = msg.getData().getString(DeviceName);
			                Toast.makeText(getApplicationContext(), "�����ӵ� "
			                               + myDeviceName, Toast.LENGTH_SHORT).show();
			                break;
			            case Message:if(myService.getServerState()==true)//���������ˣ���������������
			            			{
			            				byte[] a;
			            				a=(byte[])msg.obj;
			            				try {
											String aString=new String(a,0,msg.arg1,"UTF-8");
											fighters.fighterHandler.obtainMessage(1,0,0,aString);
										} catch (UnsupportedEncodingException e) {
											// TODO �Զ����ɵ� catch ��
											Toast.makeText(getApplicationContext(), "byte����ת��String����", Toast.LENGTH_SHORT).show();
											e.printStackTrace();
										}
			            			}
			            			else{//���ͻ��ˣ����յ�����fightdescription
			            			
					 				}
			            	break;
							default: /*Toast.makeText(getApplicationContext(), "�Զ�����ָ�����"+msg.what+" "+msg.arg1+" "+msg.arg2
		                            , Toast.LENGTH_SHORT).show();*/
								break;
			            }
				}
		    	
		    };
		
}//�����

