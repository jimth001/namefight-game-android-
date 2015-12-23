package com.rmwang.namefight3;

import android.app.Activity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.TextureView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class FightWithComputerActivity extends Activity{
	
	private EditText editText1;
	private EditText editText2;
	private TextView resultTextView;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_fightwithcomputer);
        Button startButton=(Button)findViewById(R.id.startwithcomputer);
        editText1=(EditText)findViewById(R.id.edit_text1);
		editText2=(EditText)findViewById(R.id.edit_text2);
		resultTextView=(TextView)findViewById(R.id.resulttextview);
        startButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO 自动生成的方法存根
				getStart();
			}
		});
    }
	public void getStart(){
		String name1;
		String name2;
		
		name1=editText1.getText().toString();
		name2=editText2.getText().toString();
		if(name1.length()==0||name2.length()==0||name1.equals(name2))
		{
			String tempString="输入不合法，请重新输入。输入不能为空，不能相同！"+'\n';
			resultTextView.setText(tempString);
		}
		else {
			resultTextView.setMovementMethod(new ScrollingMovementMethod());
			Fighters p1=new Fighters(name1);
			Fighters p2=new Fighters(name2);
			StringBuffer rBuffer=null;
			rBuffer=p1.autoRandomFight(p2);
			resultTextView.setText(rBuffer);
		}
	}
}
