package com.rmwang.namefight3;

public class State {
	public static final int nil=0;
	public static final int forever=-10;
	public static final int debuf=1;//减益状态
	public static final int buff=2;//增益状态
	public int id;//状态id
	public int type;//状态类型
	public int maxtime;//持续时间
	public String name;//
	public String description;//
	public int remaintime;
	State(){
		id=0;
		type=nil;
		maxtime=1;
		name="";
		description="";
		remaintime=0;
	}
	State(int tid,int ttpye,int tmaxtime,String tname,String tdes)
	{
		id=tid;
		type=ttpye;
		maxtime=tmaxtime;
		name=tname;
		description=tdes;
		remaintime=maxtime;
	}
}
