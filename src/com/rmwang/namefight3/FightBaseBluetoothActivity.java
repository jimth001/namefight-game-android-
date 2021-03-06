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
	 //UI消息显示设备名称
		public static final String DeviceName = null;
	//蓝牙适配器蓝牙服务
		private BluetoothAdapter btAdapter = null;
		private BluetoothService myService = null;
		
	//蓝牙服务连接handler消息，线程间通信:
   public static final int Message = 1;
   public static final int State_Change = 96;
   public static final int Device_Name = 4;
   //游戏模式：
   public static final int autoRandomMode=1000;
   public static final int controlMode=1001;
   //此activity状态属性：
   public static final int serverWaitingName=501;//已经开始，等待对方发送名字
   public static final int serverComputing=502;//正在计算战斗中
   public static final int serverDenyMessage=503;//尚未开始
   public static final int serverWaitingOptionCmd=507;//等待对方的操作信息
   public static final int serverMakeCmd=509;//轮到服务器端出招
   public static final int clientWaitingStartSignal=504;//发送了名字，等待服务器告知已收到
   public static final int clientWaitingFightDescription=505;//等待战斗描述，此阶段屏蔽按键
   public static final int clientHavenotStarted=506;//客户端尚未开始游戏
   public static final int clientMakeCmd=508;//客户端做决定状态，此状态按下imagebutton才有响应
	//游戏button：
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
   public static int activityState;//activity当前状态
   private EditText inputServer = null;
   private TextView resultTextView=null;
   private TextView stateTextView;
   private ScrollView mScrollView_showMessages;
   private ScrollView stateScrollView;
   private Button changeModeButton;
   String nameInput=null;
   
	//打开蓝牙设备
	private static final int Enable_Bluetooth = 2;
	//战斗线程：
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
					// TODO 自动生成的方法存根
					
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
								Toast.makeText(getApplicationContext(), "请在服务器端修改游戏模式", Toast.LENGTH_SHORT).show();
							}
						}
						else{
							Toast.makeText(getApplicationContext(), "请先连接到设备，再修改游戏模式", Toast.LENGTH_SHORT).show();
						}
					}
					else {
						Toast.makeText(getApplicationContext(), "蓝牙服务模块BluetoothService尚未开启，请稍后再试", Toast.LENGTH_SHORT).show();
					}
				}
			});
	        inputNameButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					// TODO 自动生成的方法存根
					showDialogtoInput();
					
				}
			});
	        linkButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					// TODO 自动生成的方法存根
					// 启动设备列表Activity
					Intent serverIntent = new Intent(FightBaseBluetoothActivity.this, MyDeviceListActivity.class);
					startActivityForResult(serverIntent, 1);//1与方法onActivityResult中的1相对应
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
							if(activityState==clientHavenotStarted)
							{
								if(nameInput==null){
									Toast.makeText(getApplicationContext(), "请先输入姓名", Toast.LENGTH_SHORT).show();
								}
								else{
									//mScrollView_showMessages.scrollTo(0, resultTextView.getTop());
									mySendMessage(nameInput.getBytes());//发送名字，然后等待，期间应该屏蔽按键
									setactivityState(clientWaitingStartSignal);
									resultBuffer.setLength(0);
								}
							}
							else {
								Toast.makeText(getApplicationContext(), "正在等待服务器返回信息，请稍后", Toast.LENGTH_SHORT).show();
							}
						}
						else{//做服务器端
							if(activityState==serverDenyMessage)
							{
								if(nameInput==null){
									Toast.makeText(getApplicationContext(), "请先输入姓名", Toast.LENGTH_SHORT).show();
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
									Toast.makeText(getApplicationContext(), "服务器已开启", Toast.LENGTH_SHORT).show();
									resultBuffer.setLength(0);
								}
							}
							else{
								Toast.makeText(getApplicationContext(), "服务端正在运行，请稍后", Toast.LENGTH_SHORT).show();
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
					if(gameMode==controlMode)
						sendCmd(0);
				}
			}); 
		    Skillbutton2.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					// TODO 自动生成的方法存根
					if(gameMode==controlMode)
						sendCmd(1);
				}
			});
		    Skillbutton3.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					// TODO 自动生成的方法存根
					if(gameMode==controlMode)
						sendCmd(2);
				}
			});
		    Skillbutton4.setOnClickListener(new OnClickListener() {
	
		    	@Override
		    	public void onClick(View arg0) {
		    		// TODO 自动生成的方法存根
		    		if(gameMode==controlMode)
		    			sendCmd(3);
		    	}
		    });
		    Skillbutton5.setOnClickListener(new OnClickListener() {
	
		    	@Override
		    	public void onClick(View arg0) {
		    		// TODO 自动生成的方法存根
		    		if(gameMode==controlMode)
		    			sendCmd(4);
		    	}
		    });
		    Skillbutton6.setOnClickListener(new OnClickListener() {
	
		    	@Override
		    	public void onClick(View arg0) {
		    		// TODO 自动生成的方法存根
		    		if(gameMode==controlMode)
		    			sendCmd(5);
		    	}
		    });
		    
		}
		public void updateStateView(){//更新updateStateView
			StringBuffer tmp=new StringBuffer("");
			tmp.append("游戏状态栏："+'\n');
			if(gameMode==autoRandomMode)
			{
				tmp.append("游戏模式：autoRandomMode"+'\n');
			}
			else {
				tmp.append("游戏模式：controlMode"+'\n');
			}
			if(myService!=null&&myService.getState()==BluetoothService.STATE_CONNECTED)
			{
				if(myService.getServerState()==true)
				{
					tmp.append("服务器端"+'\n');
				}
				else {
					tmp.append("客户端"+'\n');
				}
			}
			if(activityState==serverMakeCmd||activityState==clientMakeCmd)
			{
				tmp.append("你的回合，请出招"+'\n');
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
				Toast.makeText(getApplicationContext(), "游戏模式已更改为controlMode", Toast.LENGTH_SHORT).show();
				
			}
			else {
				gameMode=autoRandomMode;
				Toast.makeText(getApplicationContext(), "游戏模式已更改为autoRandomMode", Toast.LENGTH_SHORT).show();
				
			}
			updateStateView();
			return true;
			}
			else {
				Toast.makeText(getApplicationContext(), "服务器正在运行中，不能更改游戏模式", Toast.LENGTH_SHORT).show();
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
	        builder.setTitle("请输入人物名称").setIcon(android.R.drawable.ic_dialog_info).setView(inputServer)
	                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int arg1) {
							// TODO 自动生成的方法存根
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
						Toast.makeText(getApplicationContext(), "输入不合法，请重新输入", Toast.LENGTH_SHORT).show();
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
	       // Toast.makeText(getApplicationContext(), "create"
            //        , Toast.LENGTH_SHORT).show();
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
	    		
	    	
	    	
	        
	        
	    	//Toast.makeText(getApplicationContext(), "start"
            //        , Toast.LENGTH_SHORT).show();
	    	
	    }
	    @Override
	    public synchronized void onResume() {
	    	super.onResume();
	    	if(myService != null) {
	    		if(myService.getState() == BluetoothService.STATE_NONE) {
	    			myService.start();
	    		}
	    		
	    	}
	    	//Toast.makeText(getApplicationContext(), "resume"
            //        , Toast.LENGTH_SHORT).show();
	    	
	    	
	    }
	    @Override
	    protected void onPause() {
	        super.onPause();
	        if(btAdapter.isEnabled()) {
	        	//sendMessage((byte)0x00);//.........151104
	        }
	        //Toast.makeText(getApplicationContext(), "pause"
              //      , Toast.LENGTH_SHORT).show();
	    }
	    @Override
	    protected void onStop()
	    {
	    	
	    	super.onStop();
	    	
	    	//Toast.makeText(getApplicationContext(), "stop"
             //       , Toast.LENGTH_SHORT).show();
	    }
	    @Override
	    public void onDestroy() {
	    	if(myService.getState()==BluetoothService.STATE_CONNECTED)
	    	{
	    		byte []tmp=new byte[]{-7};
		    	mySendMessage(tmp);
		    	
			}
	    	Toast.makeText(getApplicationContext(), "已退出蓝牙对战，连接已断开 "
                    , Toast.LENGTH_SHORT).show();
	        super.onDestroy();
	        if (myService != null) myService.stop();
	        //Toast.makeText(getApplicationContext(), "destroy"
             //       , Toast.LENGTH_SHORT).show();
	    }
	    public synchronized void mySendMessage(byte []b) {
	    	
			if (myService.getState() != BluetoothService.STATE_CONNECTED) {
				
				Toast.makeText(getApplicationContext(), "尚未连接到设备"
				        , Toast.LENGTH_SHORT).show();
	            return;
	        }
			int l=b.length;
			byte[] len=new byte[4];
			len=intToByteArray(l);
			myService.write(len);
	        myService.write(b);
	        
		}
	    
	  //交互界面事件处理返回结果
		public void onActivityResult(int requestCode, int resultCode, Intent data) {
			switch (requestCode) {//requestCode标识从哪个Activity跳转到该Activity 和startActivityForResult中的requestCode相对应 
			case 1://1代表连接设备  //resultCode表示返回值状态 由子Activity通过其setResult()方法返回       data包含了返回数据
				// 如果设备列表Activity返回一个连接的设备
				if (resultCode == Activity.RESULT_OK) {
					//判断是否已连接，若是，断开重连
					if(myService.getState()==BluetoothService.STATE_CONNECTED)
					{
						byte []tmp=new byte[]{-8};
				    	mySendMessage(tmp);
				    	stateTextView.setText("游戏状态栏:"+'\n');
	            		resultTextView.setText("战斗描述栏："+'\n');
				    	Toast.makeText(getApplicationContext(), "已断开原有连接，正在尝试新连接，请稍后", Toast.LENGTH_SHORT).show();
				    	myService.stop();
				    	myService.start();
				    	
					}
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
		
		 //处理其他线程传来的消息：
		@SuppressLint("HandlerLeak") private final Handler mHandler = new Handler() {
				@SuppressLint("HandlerLeak") @Override
				public void handleMessage(android.os.Message msg) {
					 switch (msg.what){
			            case Device_Name://device_name=4，表示连接到蓝牙设备
			                myDeviceName = msg.getData().getString(DeviceName);
			                Toast.makeText(getApplicationContext(), "已连接到 "
			                               + myDeviceName, Toast.LENGTH_SHORT).show();
			                if(myService.getServerState()==true)//初始化activityState
			    	        {
			    	        	setactivityState(serverDenyMessage);
			    	        }
			    	        else {
			    	        	setactivityState(clientHavenotStarted);
			    	        }
			                gameMode=autoRandomMode;//初始化gameMode
			                updateStateView();
			                break;
			            case Message:synchronized (this) {
			            	if(myService.getServerState()==true)//做服务器端，传过来的是名字//message=1,来自蓝牙的消息
	            			{
	            				if(activityState==serverWaitingName)
	            				{	/*byte[] a;
	            					String aString="";
	            					a=(byte[])msg.obj;
	            					try {
	            						aString=new String(a,0,msg.arg1,"UTF-8");
									
	            					} catch (UnsupportedEncodingException e) {
	            						// TODO 自动生成的 catch 块
	            						Toast.makeText(getApplicationContext(), "byte数据转换String错误", Toast.LENGTH_SHORT).show();
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
	            			else{//做客户端，接收到的是fightdescription
	            				
	            				Bundle data = msg.getData();
	            				String readMessage = data.getString("fightdsp");
	            				resultBuffer.append(readMessage);
	            				resultTextView.setText(resultBuffer);
	            				//Toast.makeText(getApplicationContext(), "resultbuffer长度:"+readMessage.length(), Toast.LENGTH_SHORT).show();//报告bug
			 				}
						}
			            	break;
			            case 2://表示来自fightThread类的战斗描述信息,仅服务器端会走这个分支
			            	byte[] aa=(byte[])msg.obj;
			            	mySendMessage(aa);//发送描述
			            	byte[] tmp1;
            				String tmpString="";
            				tmp1=(byte[])msg.obj;
            				try {
								tmpString=new String(tmp1,"UTF-8");
								
							} catch (UnsupportedEncodingException e) {
								// TODO 自动生成的 catch 块
								Toast.makeText(getApplicationContext(), "byte数据转换String错误", Toast.LENGTH_SHORT).show();
								e.printStackTrace();
							}
            				resultBuffer.append(tmpString);
            				resultTextView.setText(resultBuffer);
			            	//Toast.makeText(getApplicationContext(), "test000000000", Toast.LENGTH_SHORT).show();
			            	break;
			            case 10://从fightThread线程传回的本地战斗信息，仅autorandom模式用
			            	//已弃用，autorandom和control模式的服务器端战斗描述均已移植到case2中：
			            	//resultBuffer=(StringBuffer)msg.obj;
			            	//resultTextView.setText(resultBuffer);
			            	//战斗完成，向客户端发完成信号：
			            	//完成一次，等待再次开始
			            	if(msg.arg1==1){//战斗结束要做的一些状态变换
			            		//解除按键屏蔽
			            		setactivityState(serverDenyMessage);
			            		byte[] ss=new byte[]{-4};
				            	mySendMessage(ss);
				            	Toast.makeText(getApplicationContext(), "战斗已结束，可以点击开始游戏重新开始", Toast.LENGTH_SHORT).show();
			            	}
			            	//Toast.makeText(getApplicationContext(), "战斗已结束，可以点击开始游戏重新开始", Toast.LENGTH_SHORT).show();
			            	
			            	
			            	//mySendMessage(resultBuffer.toString().getBytes());
			            	break;
			            case 11://服务器端，接收到客户端的技能命令
			            	if(activityState==serverWaitingOptionCmd)
			            	{
			            		
			            		int cmd=msg.arg1;
			            		fightThread.fighterHandler.obtainMessage(2,2,cmd).sendToTarget();
			            		setactivityState(serverComputing);
			            	}
			            	else {
			            		//若信息传输和客户端按键控制不出现错误，不应该走入此分支
			            		Toast.makeText(getApplicationContext(), "wrong branch"+msg.arg1+ "  "+msg.arg2, Toast.LENGTH_SHORT).show();//报告bug
			            	}
			            	break;
			            case 12://客户端，接收到了服务器端的命令，可以发送指令了
			            	setactivityState(clientMakeCmd);//只有此状态下客户端才可以出招
			            	break;
			            case 13://服务器端，接收到了fightThread的出招命令，此时可以出招
			            	setactivityState(serverMakeCmd);
			            	break;
			            case 14://服务器端接到了fightThread的客户端可以出招的命令
			            	byte[] s2=new byte[]{-5};
        					mySendMessage(s2);
        					setactivityState(serverWaitingOptionCmd);//进入等待服务器出招状态
			            	break;
			            case 15://服务器端，control模式下，战斗结束处理
			            	synchronized (this) {
			            		
				            	if(msg.arg1==1){//战斗结束要做的一些状态变换
				            		//解除按键屏蔽
				            		setactivityState(serverDenyMessage);
				            		byte[] ss=new byte[]{-4};
					            	mySendMessage(ss);
					            	Toast.makeText(getApplicationContext(), "战斗已结束，可以点击开始游戏重新开始", Toast.LENGTH_SHORT).show();
				            	}
							}
			            	break;
			            case -1:synchronized (this) {
			            	Toast.makeText(getApplicationContext(), "服务器已接收信息", Toast.LENGTH_SHORT).show();
			            	setactivityState(clientWaitingFightDescription);
						}
			            	break;
			            case -2:Toast.makeText(getApplicationContext(), "服务器尚未开始", Toast.LENGTH_SHORT).show();
			            	setactivityState(clientHavenotStarted);
			            	break;
			            case -3:Toast.makeText(getApplicationContext(), "服务器正在运行中，请稍后", Toast.LENGTH_SHORT).show();
			            	break;
			            case -4:setactivityState(clientHavenotStarted);
			            Toast.makeText(getApplicationContext(), "战斗已结束，可以点击开始游戏重新开始", Toast.LENGTH_SHORT).show();
			            	break;
			            case -6://客户端修改游戏模式
			            	changeGameMode();
			            	break;
			            case 999://测试用：
			            	if(msg.arg1==0){
			            		if(msg.arg2==1)
			            		Toast.makeText(getApplicationContext(), "蓝牙连接失败，请重试", Toast.LENGTH_SHORT).show();
			            	}
			            	if(msg.arg1==999) {
			            		Toast.makeText(getApplicationContext(), "蓝牙失去连接，请重新连接", Toast.LENGTH_SHORT).show();
			            		myService.stop();
			            		myService.start();
			            		stateTextView.setText("游戏状态栏:"+'\n');
			            		resultTextView.setText("战斗描述栏："+'\n');
			            	}
			            	if(msg.arg1==99){
			            		
			            		myService.stop();
			            		myService.start();
			            		stateTextView.setText("游戏状态栏:"+'\n');
			            		resultTextView.setText("战斗描述栏："+'\n');
			            		Toast.makeText(getApplicationContext(), "已断开原有连接，正在尝试新连接，请稍后", Toast.LENGTH_SHORT).show();
			            	}
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
	          //由高位到低位
	          result[0] = (byte)((i >> 24) & 0xFF);
	          result[1] = (byte)((i >> 16) & 0xFF);
	          result[2] = (byte)((i >> 8) & 0xFF); 
	          result[3] = (byte)(i & 0xFF);
	          return result;
	        }
	 
	        /**
	         * byte[]转int
	         * @param bytes
	         * @return
	         */
	        public static int byteArrayToInt(byte[] bytes) {
	               int value= 0;
	               //由高位到低位
	               for (int i = 0; i < 4; i++) {
	                   int shift= (4 - 1 - i) * 8;
	                   value +=(bytes[i] & 0x000000FF) << shift;//往高位游
	               }
	               return value;
	         }

}//类结束
