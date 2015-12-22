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
    public static final int starttimer=501;
	//游戏button：
    private ImageButton Skillbutton1;
    private ImageButton Skillbutton2;
    private ImageButton Skillbutton3;
    private ImageButton Skillbutton4;
    private ImageButton Skillbutton5;
    private ImageButton Skillbutton6;
    private Button startGameButton;
    private Thread fightThread;
	//打开蓝牙设备
	private static final int Enable_Bluetooth = 2;
	//战斗类对象：
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
	        builder.setTitle("请输入人物名称").setIcon(android.R.drawable.ic_dialog_info).setView(inputServer)
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
					// TODO 自动生成的方法存根
					if(myService.getState()==BluetoothService.STATE_CONNECTED)//已连接上
					{
						fightThread=new Thread(fighters,"战斗类线程");
						fightThread.start();
						if(myService.getServerState()==false)//不是服务器端
						{
							
							mySendMessage(nameString.getBytes());//
						}
						else{//做服务器端
							fighters=new Fighters(nameString, mHandler);
						}
					}
					else{
						Toast.makeText(getApplicationContext(), "尚未连接到设备", Toast.LENGTH_SHORT).show();
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
			            case Device_Name:
			                myDeviceName = msg.getData().getString(DeviceName);
			                Toast.makeText(getApplicationContext(), "已连接到 "
			                               + myDeviceName, Toast.LENGTH_SHORT).show();
			                break;
			            case Message:if(myService.getServerState()==true)//做服务器端，传过来的是名字
			            			{
			            				byte[] a;
			            				a=(byte[])msg.obj;
			            				try {
											String aString=new String(a,0,msg.arg1,"UTF-8");
											fighters.fighterHandler.obtainMessage(1,0,0,aString);
										} catch (UnsupportedEncodingException e) {
											// TODO 自动生成的 catch 块
											Toast.makeText(getApplicationContext(), "byte数据转换String错误", Toast.LENGTH_SHORT).show();
											e.printStackTrace();
										}
			            			}
			            			else{//做客户端，接收到的是fightdescription
			            			
					 				}
			            	break;
							default: /*Toast.makeText(getApplicationContext(), "自动控制指令错误"+msg.what+" "+msg.arg1+" "+msg.arg2
		                            , Toast.LENGTH_SHORT).show();*/
								break;
			            }
				}
		    	
		    };
		
}//类结束

