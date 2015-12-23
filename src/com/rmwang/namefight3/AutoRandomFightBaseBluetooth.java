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
import android.widget.TextView;
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
    public static final int serverWaitingName=501;//�Ѿ���ʼ���ȴ��Է���������
    public static final int serverComputing=502;//���ڼ���ս����
    public static final int serverDenyMessage=503;//��δ��ʼ
    public static final int clientWaitingStartSignal=504;//���������֣��ȴ���������֪���յ�
    public static final int clientWaitingFightDescription=505;//�ȴ�ս���������˽׶����ΰ���
    public static final int clientHavenotStarted=506;//�ͻ�����δ��ʼ��Ϸ
	//��Ϸbutton��
    private ImageButton Skillbutton1;
    private ImageButton Skillbutton2;
    private ImageButton Skillbutton3;
    private ImageButton Skillbutton4;
    private ImageButton Skillbutton5;
    private ImageButton Skillbutton6;
    private Button inputNameButton;
    private Button startGameButton;
    private Thread fightThread;
    private StringBuffer resultBuffer=null;
    private int activityState;
    private EditText inputServer = null;
    TextView resultTextView=null;
    String nameInput=null;
	//�������豸
	private static final int Enable_Bluetooth = 2;
	//ս�������
	private Fighters p1=null;
	@Override
	    protected void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.activity_autorandomfightbasebluetooth);
	        btAdapter = BluetoothAdapter.getDefaultAdapter();
	        if(btAdapter == null) {
	        	Toast.makeText(this, R.string.bt_not_available, Toast.LENGTH_LONG).show();
	        	return;
	        }
	        resultTextView=(TextView)findViewById(R.id.autorandombluetoothresultTextView);
	        resultBuffer=new StringBuffer();
	        Skillbutton1=(ImageButton)findViewById(R.id.imageButton1);
	        Skillbutton2=(ImageButton)findViewById(R.id.imageButton2);
	        Skillbutton3=(ImageButton)findViewById(R.id.imageButton3);
	        Skillbutton4=(ImageButton)findViewById(R.id.imageButton4);
	        Skillbutton5=(ImageButton)findViewById(R.id.imageButton5);
	        Skillbutton6=(ImageButton)findViewById(R.id.imageButton6);
	        startGameButton=(Button)findViewById(R.id.startgame1);
	        inputNameButton=(Button)findViewById(R.id.inputname);
	        inputServer=new EditText(this);
	        inputNameButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					// TODO �Զ����ɵķ������
					showDialogtoInput();
					if(nameInput==null||nameInput.length()==0)
					{
						nameInput=null;
						Toast.makeText(getApplicationContext(), "���벻�Ϸ�������������", Toast.LENGTH_SHORT).show();
					}
				}
			});
	        startGameButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					// TODO �Զ����ɵķ������
					if(myService.getState()==BluetoothService.STATE_CONNECTED)//��������
					{
						
						if(myService.getServerState()==false)//���Ƿ�������
						{
							if(activityState!=clientWaitingFightDescription)
							{
								if(nameInput==null){
									Toast.makeText(getApplicationContext(), "������������", Toast.LENGTH_SHORT).show();
								}
								else{
									mySendMessage(nameInput.getBytes());//�������֣�Ȼ��ȴ����ڼ�Ӧ�����ΰ�������δ������ι���
									setactivityState(clientWaitingStartSignal);
								}
							}
						}
						else{//����������
							if(activityState!=serverComputing)
							{
								if(nameInput==null){
									Toast.makeText(getApplicationContext(), "������������", Toast.LENGTH_SHORT).show();
								}
								else{
									p1=new Fighters(nameInput, mHandler);
									fightThread=new Thread(p1,"ս�����߳�");
									fightThread.start();
									setactivityState(serverWaitingName);
								}
							}
						}
					}
					else{
						Toast.makeText(getApplicationContext(), "��δ���ӵ��豸", Toast.LENGTH_SHORT).show();
					}
				}
			});
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

		public void showDialogtoInput()
		{
			inputServer.setText("");
	        AlertDialog.Builder builder = new AlertDialog.Builder(this);
	        builder.setTitle("��������������").setIcon(android.R.drawable.ic_dialog_info).setView(inputServer)
	                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int arg1) {
							// TODO �Զ����ɵķ������
							nameInput=null;
							dialog.dismiss();
						}
					});
	        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

	            public void onClick(DialogInterface dialog, int which) {
	               nameInput=inputServer.getText().toString();
	               Toast.makeText(getApplicationContext(), nameInput, Toast.LENGTH_SHORT).show();
	               dialog.dismiss();
	             }
	        });
	        builder.create().show();
	        //final String nameString=inputServer.getText().toString();
	        //Toast.makeText(getApplicationContext(), nameString, Toast.LENGTH_SHORT).show();
	        //return nameString;
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
			            case Device_Name://device_name=4����ʾ���ӵ������豸
			                myDeviceName = msg.getData().getString(DeviceName);
			                Toast.makeText(getApplicationContext(), "�����ӵ� "
			                               + myDeviceName, Toast.LENGTH_SHORT).show();
			                if(myService.getServerState()==true)//�Ƿ�����
			    	        {
			    	        	setactivityState(serverDenyMessage);
			    	        }
			    	        else {
			    	        	setactivityState(clientHavenotStarted);
			    	        }
			                break;
			            case Message:if(myService.getServerState()==true)//���������ˣ���������������//message=1,������������Ϣ
			            			{
			            				if(activityState==serverWaitingName)
			            				{	byte[] a;
			            					String aString="";
			            					a=(byte[])msg.obj;
			            					try {
			            						aString=new String(a,0,msg.arg1,"UTF-8");
											
			            					} catch (UnsupportedEncodingException e) {
			            						// TODO �Զ����ɵ� catch ��
			            						Toast.makeText(getApplicationContext(), "byte����ת��String����", Toast.LENGTH_SHORT).show();
			            						e.printStackTrace();
			            					}
			            					p1.fighterHandler.obtainMessage(1,0,0,aString).sendToTarget();
			            					byte[] s=new byte[]{-1};
			            					mySendMessage(s);
			            				}
			            				else if(activityState==serverDenyMessage)
			            				{
			            					byte[] s=new byte[]{-2};
			            					mySendMessage(s);
			            				}
			            				else{
			            					byte[] s=new byte[]{-3};
			            					mySendMessage(s);
			            				}
			            			}
			            			else{//���ͻ��ˣ����յ�����fightdescription
			            				byte[] a;
			            				String aString="";
			            				a=(byte[])msg.obj;
			            				try {
											aString=new String(a,0,msg.arg1,"UTF-8");
											
										} catch (UnsupportedEncodingException e) {
											// TODO �Զ����ɵ� catch ��
											Toast.makeText(getApplicationContext(), "byte����ת��String����", Toast.LENGTH_SHORT).show();
											e.printStackTrace();
										}
			            				resultBuffer.append(aString);
			            				resultTextView.setText(resultBuffer);
					 				}
			            	break;
			            case 2://��ʾ����fighter���ս��������Ϣ,���������˻��������֧
			            	mySendMessage((byte[])msg.obj);//��������
			            	if(msg.arg1==1){//ս������Ҫ����һЩ״̬�任
			            		//�����������
			            		setactivityState(clientHavenotStarted);
			            	}
			            	break;
			            case 10://��fighter�������̴߳��صı���ս����Ϣ
			            	resultBuffer=(StringBuffer)msg.obj;
			            	resultTextView.setText(resultBuffer);
			            	setactivityState(serverDenyMessage);//���һ�Σ��ȴ��ٴο�ʼ
			            	break;
			            case -1:Toast.makeText(getApplicationContext(), "�������ѽ�����Ϣ", Toast.LENGTH_SHORT).show();
			            	setactivityState(clientWaitingFightDescription);
			            	break;
			            case -2:Toast.makeText(getApplicationContext(), "��������δ��ʼ", Toast.LENGTH_SHORT).show();
			            	break;
			            case -3:Toast.makeText(getApplicationContext(), "�������ѽ�����Ϣ�����������У����Ժ�", Toast.LENGTH_SHORT).show();
			            	break;
						default: 
							break;
			            }
				}
		    	
		    };
		public void setactivityState(int a)
		{
			activityState=a;
		}
		public int getactivityState()
		{
			return activityState;
		}
}//�����

