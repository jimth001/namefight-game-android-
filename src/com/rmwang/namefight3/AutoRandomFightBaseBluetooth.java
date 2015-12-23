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
	 //UI消息显示设备名称
		public static final String DeviceName = null;
	//蓝牙适配器蓝牙服务
		private BluetoothAdapter btAdapter = null;
		private BluetoothService myService = null;
	//蓝牙服务连接handler消息，线程间通信
    public static final int Message = 1;
    public static final int State_Change = 96;
    public static final int Device_Name = 4;
    //
    public static final int serverWaitingName=501;//已经开始，等待对方发送名字
    public static final int serverComputing=502;//正在计算战斗中
    public static final int serverDenyMessage=503;//尚未开始
    public static final int clientWaitingStartSignal=504;//发送了名字，等待服务器告知已收到
    public static final int clientWaitingFightDescription=505;//等待战斗描述，此阶段屏蔽按键
    public static final int clientHavenotStarted=506;//客户端尚未开始游戏
	//游戏button：
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
	//打开蓝牙设备
	private static final int Enable_Bluetooth = 2;
	//战斗类对象：
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
					// TODO 自动生成的方法存根
					showDialogtoInput();
					if(nameInput==null||nameInput.length()==0)
					{
						nameInput=null;
						Toast.makeText(getApplicationContext(), "输入不合法，请重新输入", Toast.LENGTH_SHORT).show();
					}
				}
			});
	        startGameButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					// TODO 自动生成的方法存根
					if(myService.getState()==BluetoothService.STATE_CONNECTED)//已连接上
					{
						
						if(myService.getServerState()==false)//不是服务器端
						{
							if(activityState!=clientWaitingFightDescription)
							{
								if(nameInput==null){
									Toast.makeText(getApplicationContext(), "请先输入姓名", Toast.LENGTH_SHORT).show();
								}
								else{
									mySendMessage(nameInput.getBytes());//发送名字，然后等待，期间应该屏蔽按键，尚未完成屏蔽功能
									setactivityState(clientWaitingStartSignal);
								}
							}
						}
						else{//做服务器端
							if(activityState!=serverComputing)
							{
								if(nameInput==null){
									Toast.makeText(getApplicationContext(), "请先输入姓名", Toast.LENGTH_SHORT).show();
								}
								else{
									p1=new Fighters(nameInput, mHandler);
									fightThread=new Thread(p1,"战斗类线程");
									fightThread.start();
									setactivityState(serverWaitingName);
								}
							}
						}
					}
					else{
						Toast.makeText(getApplicationContext(), "尚未连接到设备", Toast.LENGTH_SHORT).show();
					}
				}
			});
	        //技能button点击事件：
		    Skillbutton1.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					// TODO 自动生成的方法存根
					
				}
			}); 
		    Skillbutton2.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					// TODO 自动生成的方法存根
					
				}
			});
		    Skillbutton3.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					// TODO 自动生成的方法存根
					
				}
			});
		    Skillbutton4.setOnClickListener(new OnClickListener() {
	
		    	@Override
		    	public void onClick(View arg0) {
		    		// TODO 自动生成的方法存根
		
		    	}
		    });
		    Skillbutton5.setOnClickListener(new OnClickListener() {
	
		    	@Override
		    	public void onClick(View arg0) {
		    		// TODO 自动生成的方法存根
		
		    	}
		    });
		    Skillbutton6.setOnClickListener(new OnClickListener() {
	
		    	@Override
		    	public void onClick(View arg0) {
		    		// TODO 自动生成的方法存根
		
		    	}
		    });
		    
		}

		public void showDialogtoInput()
		{
			inputServer.setText("");
	        AlertDialog.Builder builder = new AlertDialog.Builder(this);
	        builder.setTitle("请输入人物名称").setIcon(android.R.drawable.ic_dialog_info).setView(inputServer)
	                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int arg1) {
							// TODO 自动生成的方法存根
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
				Toast.makeText(getApplicationContext(), "尚未连接到设备"
	                    , Toast.LENGTH_SHORT).show();
	            return;
	        }
	        myService.write(b);
		}
	    
	  //交互界面事件处理返回结果
		public void onActivityResult(int requestCode, int resultCode, Intent data) {
			switch (requestCode) {//requestCode标识从哪个Activity跳转到该Activity 和startActivityForResult中的requestCode相对应 
			case 1://1代表连接设备  //resultCode表示返回值状态 由子Activity通过其setResult()方法返回       data包含了返回数据
				// 如果设备列表Activity返回一个连接的设备
				if (resultCode == Activity.RESULT_OK) {
					// 获取设备的MAC地址
					String address = data.getExtras().getString(
							MyDeviceListActivity.EXTRA_DEVICE_ADDRESS);
					// 获取BLuetoothDevice对象
					BluetoothDevice device = btAdapter.getRemoteDevice(address);
					myService.connect(device);// 连接该设备
				}
				break;
			case Enable_Bluetooth:
	          if (resultCode == Activity.RESULT_OK) {
	        	  myService = new BluetoothService(this, mHandler);
	        	  // 初始化BluetoothService并执行蓝牙连接
	              //Handler对象mHandler来负责数据的交换和线程之间的通信
	             } 
	          else {
	            Toast.makeText(this, R.string.bt_not_enable, Toast.LENGTH_SHORT).show();
	            return;
	            }
			}
		}
		@Override
		public boolean onPrepareOptionsMenu(Menu menu) {
			// 启动设备列表Activity
			Intent serverIntent = new Intent(this, MyDeviceListActivity.class);
			startActivityForResult(serverIntent, 1);//1与方法onActivityResult中的1相对应
			return true;
		}
		 //处理其他线程传来的消息：
		@SuppressLint("HandlerLeak") private final Handler mHandler = new Handler() {
				@SuppressLint("HandlerLeak") @Override
				public void handleMessage(android.os.Message msg) {
					 switch (msg.what){
			            case Device_Name://device_name=4，表示连接到蓝牙设备
			                myDeviceName = msg.getData().getString(DeviceName);
			                Toast.makeText(getApplicationContext(), "已连接到 "
			                               + myDeviceName, Toast.LENGTH_SHORT).show();
			                if(myService.getServerState()==true)//是服务器
			    	        {
			    	        	setactivityState(serverDenyMessage);
			    	        }
			    	        else {
			    	        	setactivityState(clientHavenotStarted);
			    	        }
			                break;
			            case Message:if(myService.getServerState()==true)//做服务器端，传过来的是名字//message=1,来自蓝牙的消息
			            			{
			            				if(activityState==serverWaitingName)
			            				{	byte[] a;
			            					String aString="";
			            					a=(byte[])msg.obj;
			            					try {
			            						aString=new String(a,0,msg.arg1,"UTF-8");
											
			            					} catch (UnsupportedEncodingException e) {
			            						// TODO 自动生成的 catch 块
			            						Toast.makeText(getApplicationContext(), "byte数据转换String错误", Toast.LENGTH_SHORT).show();
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
			            			else{//做客户端，接收到的是fightdescription
			            				byte[] a;
			            				String aString="";
			            				a=(byte[])msg.obj;
			            				try {
											aString=new String(a,0,msg.arg1,"UTF-8");
											
										} catch (UnsupportedEncodingException e) {
											// TODO 自动生成的 catch 块
											Toast.makeText(getApplicationContext(), "byte数据转换String错误", Toast.LENGTH_SHORT).show();
											e.printStackTrace();
										}
			            				resultBuffer.append(aString);
			            				resultTextView.setText(resultBuffer);
					 				}
			            	break;
			            case 2://表示来自fighter类的战斗描述信息,仅服务器端会走这个分支
			            	mySendMessage((byte[])msg.obj);//发送描述
			            	if(msg.arg1==1){//战斗结束要做的一些状态变换
			            		//解除按键屏蔽
			            		setactivityState(clientHavenotStarted);
			            	}
			            	break;
			            case 10://从fighter开启的线程传回的本地战斗信息
			            	resultBuffer=(StringBuffer)msg.obj;
			            	resultTextView.setText(resultBuffer);
			            	setactivityState(serverDenyMessage);//完成一次，等待再次开始
			            	break;
			            case -1:Toast.makeText(getApplicationContext(), "服务器已接收信息", Toast.LENGTH_SHORT).show();
			            	setactivityState(clientWaitingFightDescription);
			            	break;
			            case -2:Toast.makeText(getApplicationContext(), "服务器尚未开始", Toast.LENGTH_SHORT).show();
			            	break;
			            case -3:Toast.makeText(getApplicationContext(), "服务器已接收消息，正在运行中，请稍后", Toast.LENGTH_SHORT).show();
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
}//类结束

