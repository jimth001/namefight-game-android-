package com.rmwang.namefight3;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button playWithComputerButton=(Button)findViewById(R.id.playwithcomputer);
        playWithComputerButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO 自动生成的方法存根
				Intent intent=new Intent(MainActivity.this, FightWithComputerActivity.class);
				startActivity(intent);
			}
		});
        Button playBasedBluetoothButton=(Button)findViewById(R.id.playwithothers);
        playBasedBluetoothButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO 自动生成的方法存根
				Intent intent=new Intent(MainActivity.this,FightBaseBluetoothActivity.class);
				startActivity(intent);
			}
		});
        Button quitGameButton=(Button)findViewById(R.id.quitgame);
        quitGameButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO 自动生成的方法存根
				System.exit(0);
			}
		});
    }


    //@Override
    /*public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;*/
   // }
    
}
