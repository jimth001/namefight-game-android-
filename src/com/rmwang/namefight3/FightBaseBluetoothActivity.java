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
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
public class FightBaseBluetoothActivity extends Activity {
	private String myDeviceName;
	 //UI��Ϣ��ʾ�豸����
		public static final String DeviceName = null;
	//������������������
		private BluetoothAdapter btAdapter = null;
		private BluetoothService myService = null;
		
	//������������handler��Ϣ���̼߳�ͨ��:
   public static final int Message = 1;
   public static final int State_Change = 96;
   public static final int Device_Name = 4;
   //��Ϸģʽ��
   public static final int autoRandomMode=1000;
   public static final int controlMode=1001;
   //��activity״̬���ԣ�
   public static final int serverWaitingName=501;//�Ѿ���ʼ���ȴ��Է���������
   public static final int serverComputing=502;//���ڼ���ս����
   public static final int serverDenyMessage=503;//��δ��ʼ
   public static final int serverWaitingOptionCmd=507;//�ȴ��Է��Ĳ�����Ϣ
   public static final int serverMakeCmd=509;//�ֵ��������˳���
   public static final int clientWaitingStartSignal=504;//���������֣��ȴ���������֪���յ�
   public static final int clientWaitingFightDescription=505;//�ȴ�ս���������˽׶����ΰ���
   public static final int clientHavenotStarted=506;//�ͻ�����δ��ʼ��Ϸ
   public static final int clientMakeCmd=508;//�ͻ���������״̬����״̬����imagebutton������Ӧ
	//��Ϸbutton��
   private ImageButton Skillbutton1;
   private ImageButton Skillbutton2;
   private ImageButton Skillbutton3;
   private ImageButton Skillbutton4;
   private ImageButton Skillbutton5;
   private ImageButton Skillbutton6;
   private Button inputNameButton;
   private Button startGameButton;
   private Button linkButton;
   private StringBuffer resultBuffer=null;
   public static  int gameMode;
   public static int activityState;//activity��ǰ״̬
   private EditText inputServer = null;
   private TextView resultTextView=null;
   private TextView stateTextView;
   private ScrollView mScrollView_showMessages;
   private ScrollView stateScrollView;
   private Button changeModeButton;
   String nameInput=null;
   
	//�������豸
	private static final int Enable_Bluetooth = 2;
	//ս���̣߳�
	FightThread fightThread=null;
	@Override
	    protected void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.activity_fightbasebluetooth);
	        btAdapter = BluetoothAdapter.getDefaultAdapter();
	        if(btAdapter == null) {
	        	Toast.makeText(this, R.string.bt_not_available, Toast.LENGTH_LONG).show();
	        	return;
	        }
	        gameMode=autoRandomMode;
	        resultTextView=(TextView)findViewById(R.id.bluetoothresultTextView);
	        resultBuffer=new StringBuffer();
	        Skillbutton1=(ImageButton)findViewById(R.id.imageButton1);
	        Skillbutton2=(ImageButton)findViewById(R.id.imageButton2);
	        Skillbutton3=(ImageButton)findViewById(R.id.imageButton3);
	        Skillbutton4=(ImageButton)findViewById(R.id.imageButton4);
	        Skillbutton5=(ImageButton)findViewById(R.id.imageButton5);
	        Skillbutton6=(ImageButton)findViewById(R.id.imageButton6);
	        startGameButton=(Button)findViewById(R.id.startgame1);
	        inputNameButton=(Button)findViewById(R.id.inputname);
	        linkButton=(Button)findViewById(R.id.linkbluetooth);
	        changeModeButton=(Button)findViewById(R.id.changemode);
	        mScrollView_showMessages=(ScrollView) findViewById(R.id.scrollView_showMessages);
	        stateScrollView=(ScrollView)findViewById(R.id.statescrollview);
	        stateTextView=(TextView)findViewById(R.id.statetextview);
	        inputServer=new EditText(this);
	        changeModeButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					// TODO �Զ����ɵķ������
					
					if(myService!=null){
						if(myService.getState()==BluetoothService.STATE_CONNECTED)
						{
							if(myService.getServerState()==true)
							{
								
								if(changeGameMode()==true)
								{
								byte []s=new byte[]{-6};
								mySendMessage(s);
								}
							}
							else {
								Toast.makeText(getApplicationContext(), "���ڷ��������޸���Ϸģʽ", Toast.LENGTH_SHORT).show();
							}
						}
						else{
							Toast.makeText(getApplicationContext(), "�������ӵ��豸�����޸���Ϸģʽ", Toast.LENGTH_SHORT).show();
						}
					}
					else {
						Toast.makeText(getApplicationContext(), "��������ģ��BluetoothService��δ���������Ժ�����", Toast.LENGTH_SHORT).show();
					}
				}
			});
	        inputNameButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					// TODO �Զ����ɵķ������
					showDialogtoInput();
					
				}
			});
	        linkButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					// TODO �Զ����ɵķ������
					// �����豸�б�Activity
					Intent serverIntent = new Intent(FightBaseBluetoothActivity.this, MyDeviceListActivity.class);
					startActivityForResult(serverIntent, 1);//1�뷽��onActivityResult�е�1���Ӧ
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
							if(activityState==clientHavenotStarted)
							{
								if(nameInput==null){
									Toast.makeText(getApplicationContext(), "������������", Toast.LENGTH_SHORT).show();
								}
								else{
									//mScrollView_showMessages.scrollTo(0, resultTextView.getTop());
									mySendMessage(nameInput.getBytes());//�������֣�Ȼ��ȴ����ڼ�Ӧ�����ΰ���
									setactivityState(clientWaitingStartSignal);
									resultBuffer.setLength(0);
								}
							}
							else {
								Toast.makeText(getApplicationContext(), "���ڵȴ�������������Ϣ�����Ժ�", Toast.LENGTH_SHORT).show();
							}
						}
						else{//����������
							if(activityState==serverDenyMessage)
							{
								if(nameInput==null){
									Toast.makeText(getApplicationContext(), "������������", Toast.LENGTH_SHORT).show();
								}
								else{
									//mScrollView_showMessages.scrollTo(0, resultTextView.getTop());
									Fighters p1=new Fighters(nameInput);
									if(fightThread==null)
									{	fightThread=new FightThread(mHandler);
										
										fightThread.start();
									}
									fightThread.setP1(p1);
									setactivityState(serverWaitingName);
									Toast.makeText(getApplicationContext(), "�������ѿ���", Toast.LENGTH_SHORT).show();
									resultBuffer.setLength(0);
								}
							}
							else{
								Toast.makeText(getApplicationContext(), "������������У����Ժ�", Toast.LENGTH_SHORT).show();
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
					if(gameMode==controlMode)
						sendCmd(0);
				}
			}); 
		    Skillbutton2.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					// TODO �Զ����ɵķ������
					if(gameMode==controlMode)
						sendCmd(1);
				}
			});
		    Skillbutton3.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					// TODO �Զ����ɵķ������
					if(gameMode==controlMode)
						sendCmd(2);
				}
			});
		    Skillbutton4.setOnClickListener(new OnClickListener() {
	
		    	@Override
		    	public void onClick(View arg0) {
		    		// TODO �Զ����ɵķ������
		    		if(gameMode==controlMode)
		    			sendCmd(3);
		    	}
		    });
		    Skillbutton5.setOnClickListener(new OnClickListener() {
	
		    	@Override
		    	public void onClick(View arg0) {
		    		// TODO �Զ����ɵķ������
		    		if(gameMode==controlMode)
		    			sendCmd(4);
		    	}
		    });
		    Skillbutton6.setOnClickListener(new OnClickListener() {
	
		    	@Override
		    	public void onClick(View arg0) {
		    		// TODO �Զ����ɵķ������
		    		if(gameMode==controlMode)
		    			sendCmd(5);
		    	}
		    });
		    
		}
		public void updateStateView(){//����updateStateView
			StringBuffer tmp=new StringBuffer("");
			if(gameMode==autoRandomMode)
			{
				tmp.append("��Ϸģʽ��autoRandomMode"+'\n');
			}
			else {
				tmp.append("��Ϸģʽ��controlMode"+'\n');
			}
			if(myService!=null&&myService.getState()==BluetoothService.STATE_CONNECTED)
			{
				if(myService.getServerState()==true)
				{
					tmp.append("��������"+'\n');
				}
				else {
					tmp.append("�ͻ���"+'\n');
				}
			}
			if(activityState==serverMakeCmd||activityState==clientMakeCmd)
			{
				tmp.append("��Ļغϣ������"+'\n');
			}
			stateTextView.setText(tmp);
		}
		public boolean changeGameMode()
		{
			if(activityState==serverDenyMessage||activityState==clientHavenotStarted)
			{
			if(gameMode==autoRandomMode)
			{
				gameMode=controlMode;
				Toast.makeText(getApplicationContext(), "��Ϸģʽ�Ѹ���ΪcontrolMode", Toast.LENGTH_SHORT).show();
				
			}
			else {
				gameMode=autoRandomMode;
				Toast.makeText(getApplicationContext(), "��Ϸģʽ�Ѹ���ΪautoRandomMode", Toast.LENGTH_SHORT).show();
				
			}
			updateStateView();
			return true;
			}
			else {
				Toast.makeText(getApplicationContext(), "���������������У����ܸ�����Ϸģʽ", Toast.LENGTH_SHORT).show();
				return false;
			}
		}
		public void sendCmd(int cmd){
			if(activityState==serverMakeCmd)
			{
				fightThread.fighterHandler.obtainMessage(2,1,cmd).sendToTarget();
				setactivityState(serverComputing);
			}
			else if(activityState==clientMakeCmd){
				cmd-=15;
				byte[] s=new byte []{0};
				s[0]=(byte)cmd;
				//byte[]s=Integer.valueOf(cmd).toString().getBytes();
				mySendMessage(s);
				setactivityState(clientWaitingFightDescription);
			}
		}
		public void showDialogtoInput()
		{
			
			try {
				ViewGroup parent=(ViewGroup)inputServer.getParent();
		        parent.removeView(inputServer);
		        
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
			
			inputServer.setText("");
	        AlertDialog.Builder builder = new AlertDialog.Builder(this);
	        builder.setTitle("��������������").setIcon(android.R.drawable.ic_dialog_info).setView(inputServer)
	                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int arg1) {
							// TODO �Զ����ɵķ������
							nameInput=null;
							dialog.dismiss();
							ViewGroup parent=(ViewGroup)inputServer.getParent();
					        parent.removeView(inputServer);
						}
					});
	        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

	            public void onClick(DialogInterface dialog, int which) {
	               nameInput=inputServer.getText().toString();
	               if(nameInput.length()==0)
					{
						Toast.makeText(getApplicationContext(), "���벻�Ϸ�������������", Toast.LENGTH_SHORT).show();
						nameInput=null;
					}
	               else Toast.makeText(getApplicationContext(), nameInput, Toast.LENGTH_SHORT).show();
	               dialog.dismiss();
	               ViewGroup parent=(ViewGroup)inputServer.getParent();
	   	           parent.removeView(inputServer);
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
	    public synchronized void mySendMessage(byte []b) {
			if (myService.getState() != BluetoothService.STATE_CONNECTED) {
				Toast.makeText(getApplicationContext(), "��δ���ӵ��豸"
	                    , Toast.LENGTH_SHORT).show();
	            return;
	        }
			int l=b.length;
			
			byte[] len=new byte[4];
			len=intToByteArray(l);
			myService.write(len);
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
		/*@Override
		public boolean onPrepareOptionsMenu(Menu menu) {
			// �����豸�б�Activity
			Intent serverIntent = new Intent(this, MyDeviceListActivity.class);
			startActivityForResult(serverIntent, 1);//1�뷽��onActivityResult�е�1���Ӧ
			return true;
		}*/
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
			                updateStateView();
			                break;
			            case Message:synchronized (this) {
			            	if(myService.getServerState()==true)//���������ˣ���������������//message=1,������������Ϣ
	            			{
	            				if(activityState==serverWaitingName)
	            				{	/*byte[] a;
	            					String aString="";
	            					a=(byte[])msg.obj;
	            					try {
	            						aString=new String(a,0,msg.arg1,"UTF-8");
									
	            					} catch (UnsupportedEncodingException e) {
	            						// TODO �Զ����ɵ� catch ��
	            						Toast.makeText(getApplicationContext(), "byte����ת��String����", Toast.LENGTH_SHORT).show();
	            						e.printStackTrace();
	            					}*/
	            					Bundle data = msg.getData();
		            				String aString = data.getString("fightdsp");
	            					if(gameMode==controlMode)
	            						fightThread.fighterHandler.obtainMessage(2,3,0,aString).sendToTarget();
	            					else 
	            						fightThread.fighterHandler.obtainMessage(1,0,0,aString).sendToTarget();
	            					byte[] s=new byte[]{-1};
	            					mySendMessage(s);
	            				}
	            				else if(activityState==serverDenyMessage)
	            				{
	            					byte[] s=new byte[]{-2};
	            					mySendMessage(s);
	            				}
	            				else if(activityState==serverComputing||activityState==serverMakeCmd){
	            					byte[] s=new byte[]{-3};
	            					mySendMessage(s);
	            				}
	            				
	            			}
	            			else{//���ͻ��ˣ����յ�����fightdescription
	            				/*byte[] a;
	            				String aString="";
	            				a=(byte[])msg.obj;
	            				try {
									aString=new String(a,0,msg.arg1,"UTF-8");
									
								} catch (UnsupportedEncodingException e) {
									// TODO �Զ����ɵ� catch ��
									Toast.makeText(getApplicationContext(), "byte����ת��String����", Toast.LENGTH_SHORT).show();
									e.printStackTrace();
								}*/
	            				Bundle data = msg.getData();
	            				String readMessage = data.getString("fightdsp");
	            				resultBuffer.append(readMessage);
	            				
	            				resultTextView.setText(resultBuffer);
	            				//Toast.makeText(getApplicationContext(), "resultbuffer����:"+readMessage.length(), Toast.LENGTH_SHORT).show();//����bug
			 				}
						}
			            	break;
			            case 2://��ʾ����fightThread���ս��������Ϣ,���������˻��������֧
			            	byte[] aa=(byte[])msg.obj;
			            	mySendMessage(aa);//��������
			            	byte[] tmp1;
            				String tmpString="";
            				tmp1=(byte[])msg.obj;
            				try {
								tmpString=new String(tmp1,"UTF-8");
								
							} catch (UnsupportedEncodingException e) {
								// TODO �Զ����ɵ� catch ��
								Toast.makeText(getApplicationContext(), "byte����ת��String����", Toast.LENGTH_SHORT).show();
								e.printStackTrace();
							}
            				resultBuffer.append(tmpString);
            				resultTextView.setText(resultBuffer);
			            	//Toast.makeText(getApplicationContext(), "test000000000", Toast.LENGTH_SHORT).show();
			            	break;
			            case 10://��fightThread�̴߳��صı���ս����Ϣ����autorandomģʽ��
			            	//�����ã�autorandom��controlģʽ�ķ�������ս������������ֲ��case2�У�
			            	//resultBuffer=(StringBuffer)msg.obj;
			            	//resultTextView.setText(resultBuffer);
			            	//ս����ɣ���ͻ��˷�����źţ�
			            	//���һ�Σ��ȴ��ٴο�ʼ
			            	if(msg.arg1==1){//ս������Ҫ����һЩ״̬�任
			            		//�����������
			            		setactivityState(serverDenyMessage);
			            		byte[] ss=new byte[]{-4};
				            	mySendMessage(ss);
				            	Toast.makeText(getApplicationContext(), "ս���ѽ��������Ե����ʼ��Ϸ���¿�ʼ", Toast.LENGTH_SHORT).show();
			            	}
			            	//Toast.makeText(getApplicationContext(), "ս���ѽ��������Ե����ʼ��Ϸ���¿�ʼ", Toast.LENGTH_SHORT).show();
			            	
			            	
			            	//mySendMessage(resultBuffer.toString().getBytes());
			            	break;
			            case 11://�������ˣ����յ��ͻ��˵ļ�������
			            	if(activityState==serverWaitingOptionCmd)
			            	{
			            		/*byte[] a;
			            		String aString="";
			            		a=(byte[])msg.obj;
			            		try {
			            			aString=new String(a,0,msg.arg1,"UTF-8");
								
			            		} catch (UnsupportedEncodingException e) {
			            			// TODO �Զ����ɵ� catch ��
			            			Toast.makeText(getApplicationContext(), "byte����ת��String����", Toast.LENGTH_SHORT).show();
			            			e.printStackTrace();
			            		}
			            		int cmd=Integer.valueOf(aString).intValue(); */
			            		int cmd=msg.arg1;
			            		fightThread.fighterHandler.obtainMessage(2,2,cmd).sendToTarget();
			            		setactivityState(serverComputing);
			            	}
			            	else {
			            		//����Ϣ����Ϳͻ��˰������Ʋ����ִ��󣬲�Ӧ������˷�֧
			            		Toast.makeText(getApplicationContext(), "wrong branch"+msg.arg1+ "  "+msg.arg2, Toast.LENGTH_SHORT).show();//����bug
			            	}
			            	break;
			            case 12://�ͻ��ˣ����յ��˷������˵�������Է���ָ����
			            	setactivityState(clientMakeCmd);//ֻ�д�״̬�¿ͻ��˲ſ��Գ���
			            	break;
			            case 13://�������ˣ����յ���fightThread�ĳ��������ʱ���Գ���
			            	setactivityState(serverMakeCmd);
			            	break;
			            case 14://�������˽ӵ���fightThread�Ŀͻ��˿��Գ��е�����
			            	byte[] s2=new byte[]{-5};
        					mySendMessage(s2);
        					setactivityState(serverWaitingOptionCmd);//����ȴ�����������״̬
			            	break;
			            case 15://�������ˣ�controlģʽ�£�fightThread����ս����������ʾ
			            	synchronized (this) {
			            		/*byte[] a;
	            				String aString="";
	            				a=(byte[])msg.obj;
	            				try {
									aString=new String(a,"UTF-8");
									
								} catch (UnsupportedEncodingException e) {
									// TODO �Զ����ɵ� catch ��
									Toast.makeText(getApplicationContext(), "byte����ת��String����", Toast.LENGTH_SHORT).show();
									e.printStackTrace();
								}
	            				resultBuffer.append(aString);
	            				resultTextView.setText(resultBuffer);
	            				//resultTextView.setText(resultBuffer);
	            				Toast.makeText(getApplicationContext(), "00000000000000", Toast.LENGTH_SHORT).show();*/
				            	if(msg.arg1==1){//ս������Ҫ����һЩ״̬�任
				            		//�����������
				            		setactivityState(serverDenyMessage);
				            		byte[] ss=new byte[]{-4};
					            	mySendMessage(ss);
					            	Toast.makeText(getApplicationContext(), "ս���ѽ��������Ե����ʼ��Ϸ���¿�ʼ", Toast.LENGTH_SHORT).show();
				            	}
							}
			            	break;
			            case -1:synchronized (this) {
			            	Toast.makeText(getApplicationContext(), "�������ѽ�����Ϣ", Toast.LENGTH_SHORT).show();
			            	setactivityState(clientWaitingFightDescription);
						}
			            	break;
			            case -2:Toast.makeText(getApplicationContext(), "��������δ��ʼ", Toast.LENGTH_SHORT).show();
			            	setactivityState(clientHavenotStarted);
			            	break;
			            case -3:Toast.makeText(getApplicationContext(), "���������������У����Ժ�", Toast.LENGTH_SHORT).show();
			            	break;
			            case -4:setactivityState(clientHavenotStarted);
			            Toast.makeText(getApplicationContext(), "ս���ѽ��������Ե����ʼ��Ϸ���¿�ʼ", Toast.LENGTH_SHORT).show();
			            	break;
			            case -6://�ͻ����޸���Ϸģʽ
			            	changeGameMode();
			            	break;
			            case 999://�����ã�
			            	if(msg.arg1==0){
			            		if(msg.arg2==1)
			            		Toast.makeText(getApplicationContext(), "��������ʧ�ܣ�������", Toast.LENGTH_SHORT).show();
			            		else if(msg.arg2==2)
			            			Toast.makeText(getApplicationContext(), "XXXXXXXXX222", Toast.LENGTH_SHORT).show();
			            	}
			            	if(msg.arg1==1)Toast.makeText(getApplicationContext(), "TTTTTTTTT", Toast.LENGTH_SHORT).show();
			            	if(msg.arg1==4)Toast.makeText(getApplicationContext(), "wrongbytes:"+msg.arg2, Toast.LENGTH_SHORT).show();
			            	if(msg.arg1==5)Toast.makeText(getApplicationContext(), "bytes:"+msg.arg2, Toast.LENGTH_SHORT).show();
			            	break;
						default: 
							break;
			            }
				}
		    	
		    };
		public void setactivityState(int a)
		{
			activityState=a;
			updateStateView();
		}
		public int getactivityState()
		{
			return activityState;
		}
		public static byte[] intToByteArray(int i) {   
	          byte[] result = new byte[4];   
	          //�ɸ�λ����λ
	          result[0] = (byte)((i >> 24) & 0xFF);
	          result[1] = (byte)((i >> 16) & 0xFF);
	          result[2] = (byte)((i >> 8) & 0xFF); 
	          result[3] = (byte)(i & 0xFF);
	          return result;
	        }
	 
	        /**
	         * byte[]תint
	         * @param bytes
	         * @return
	         */
	        public static int byteArrayToInt(byte[] bytes) {
	               int value= 0;
	               //�ɸ�λ����λ
	               for (int i = 0; i < 4; i++) {
	                   int shift= (4 - 1 - i) * 8;
	                   value +=(bytes[i] & 0x000000FF) << shift;//����λ��
	               }
	               return value;
	         }

}//�����
