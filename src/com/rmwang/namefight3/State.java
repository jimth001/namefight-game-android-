package com.rmwang.namefight3;

public class State {
	public static final int nil=0;
	public static final int forever=-10;
	public static final int debuf=1;//����״̬
	public static final int buff=2;//����״̬
	public int id;//״̬id
	public int type;//״̬����
	public int maxtime;//����ʱ��
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
