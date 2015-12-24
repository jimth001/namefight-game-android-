package com.rmwang.namefight3;

import java.util.ArrayList;
import java.util.Random;

import android.R.integer;
import android.os.Handler;
import android.os.Looper;


//git@github.com:jimth001/namefight-game-android-.git
public class Fighters{
	private float bili;//����
	private float wuxing;//����
	private float lidao;//����
	private float gengu;//����
	private int shenfa;//��
	private float maxhp;
	private float maxsp;
	private ArrayList<Skill> havingSkills=null;
	
	public float tbili;//ս����ʱ���Ա���
	public float twuxing;//����
	public float tlidao;//����
	public float tgengu;//����
	public int tshenfa;//��
	public float hp;
	public float sp;
	public ArrayList<State> tbuffs=null;
	private int exp;
	private int lv;//
	private int sxd;//���Ե�
	public String name;
	public final Random random=new Random();
	
	private static final AllStateandSkillsCollection allStateandSkillsCollection=new AllStateandSkillsCollection();
	
	Fighters(String a){
		name=a;
		havingSkills=new ArrayList<Skill>();
		tbuffs=new ArrayList<State>();
		int len=name.length();
		int i;
		int sum=0;
		for(i=0;i<len;i++)
		{
			sum+=Math.abs(name.indexOf(i));
		}
		bili=20+sum%30;
		wuxing=20+sum*2%30;
		lidao=20+sum*2%30;
		gengu=20+sum*2%30;
		shenfa=20+sum*2%30;
		maxhp=2000+sum*100%1000;
		maxsp=2000+sum*85%900;
		exp=0;
		lv=1;
		sxd=0;
		len=AllStateandSkillsCollection.allSkills.size();
		for(i=0;i<len;i++)//��ʼ�棬ȫ�����ܼ�����в���
		{
			havingSkills.add(new Skill(AllStateandSkillsCollection.allSkills.get(i)));
		}
	}
	public void LevelUp()
	{
		exp-=lv*100;
		lv++;
	}
	public void addExp(int a)
	{
		exp+=a;
		while(exp>lv*100)
		{
			LevelUp();
		}
	}
	public void iniForFight(StringBuffer dsp)//ս����ʼ������
	{
		this.tbili=this.bili;
		this.twuxing=this.wuxing;
		this.tlidao=this.lidao;
		this.tgengu=this.gengu;
		this.tshenfa=this.shenfa;
		this.hp=this.maxhp;
		this.sp=this.maxsp;
		this.tbuffs.clear();//���״̬��
		if(this.name.equals("����"))
		{
			dsp.append(this.name+"������ǹ⻷״̬��ս������"+'\n');
			this.tbili+=this.bili*0.15;
			this.tgengu+=this.gengu*0.15;
			this.tlidao+=this.lidao*0.15;
			this.tshenfa+=this.shenfa*0.15;
			this.twuxing+=this.wuxing*0.15;
		}
	}
	public void save(){}
	/*public StringBuffer ARfightBaseBT(Fighters p2){//autorandomfightbasebluetooth
		
		return nativeBuffer;
	}*/
	public StringBuffer autoRandomFight(Fighters p2){
		int counter=1;//�غϼ�����
		int timer1=0;//p1ʱ�������
		int timer2=0;//p2ʱ�������
		int maxspeed=0;//��¼��ʷ����ٶ�
		StringBuffer fightDescriptionBuffer=new StringBuffer("");//����ս������
		int result=0;
		this.iniForFight(fightDescriptionBuffer);
		p2.iniForFight(fightDescriptionBuffer);
		fightDescriptionBuffer.append(this.name+"��ʼ����:����"+this.hp+",����"+this.tlidao+",����"+this.tbili+",����"+this.twuxing+",��"+this.tshenfa+",����"+this.tgengu+'\n');
		fightDescriptionBuffer.append(p2.name+"��ʼ����:����"+p2.hp+",����"+p2.tlidao+",����"+p2.tbili+",����"+p2.twuxing+",��"+p2.tshenfa+",����"+p2.tgengu+'\n');
		while(result==0)//ս��δ����
		{
			maxspeed=Math.min(maxspeed, p2.tshenfa);
			maxspeed=Math.min(maxspeed, this.tshenfa);
			timer1+=this.tshenfa;
			timer2+=p2.tshenfa;
			if(timer1>=maxspeed) {
				timer1-=maxspeed;
				fightDescriptionBuffer.append(oneRound(this, p2,counter));
				this.tbuff_refresh(fightDescriptionBuffer,this);//ˢ��״̬
				p2.tbuff_refresh(fightDescriptionBuffer,p2);
				counter++;
			}
			result=judgeEnd(p2);
			if(result!=0) break;
			if(timer2>=maxspeed){
				timer2-=maxspeed;
				fightDescriptionBuffer.append(oneRound(p2, this,counter));
				p2.tbuff_refresh(fightDescriptionBuffer,p2);
				this.tbuff_refresh(fightDescriptionBuffer,this);
				counter++;
			}
			result=judgeEnd(p2);
		}
		if(result==1)//1lose
		{
			fightDescriptionBuffer.append(p2.name+"��ʤ");
		}
		else if(result==2)//2lose
		{
			fightDescriptionBuffer.append(this.name+"��ʤ");
		}
		else{
			//����
		}
		return fightDescriptionBuffer;
	}
	public StringBuffer oneRound(Fighters attacker,Fighters defender,int counter){
		StringBuffer oneRoundDescriptionbBuffer=new StringBuffer("");
		oneRoundDescriptionbBuffer.append("��"+counter+"�غϣ�"+'\n');
		for (int i = 0; i < attacker.tbuffs.size(); i++) {//�غϿ�ʼ����check states
			attacker.checkbuff(5, attacker.tbuffs.get(i).id,0,oneRoundDescriptionbBuffer,attacker,defender);
		}
		/*for (int i = 0; i < attacker.tbuffs.size(); i++) {//�غϿ�ʼ����check states
			attacker.checkbuff(5, attacker.tbuffs.get(i).id,0,oneRoundDescriptionbBuffer,attacker,defender);
		}*/
		int r=random.nextInt(6);//�������0-5
		float t_inj;
		t_inj=attack(attacker,defender,attacker.havingSkills.get(r),oneRoundDescriptionbBuffer);
		defend(defender, attacker,attacker.havingSkills.get(r), t_inj,oneRoundDescriptionbBuffer);
		return oneRoundDescriptionbBuffer;
	}
	public void oneRound(Fighters attacker,Fighters defender,int counter,Skill usingskill,StringBuffer dsp){
		
		dsp.append("��"+counter+"�غϣ�"+'\n');
		for (int i = 0; i < attacker.tbuffs.size(); i++) {//�غϿ�ʼ����check states
			attacker.checkbuff(5, attacker.tbuffs.get(i).id,0,dsp,attacker,defender);
		}
		/*for (int i = 0; i < attacker.tbuffs.size(); i++) {//�غϿ�ʼ����check states
			attacker.checkbuff(5, attacker.tbuffs.get(i).id,0,oneRoundDescriptionbBuffer,attacker,defender);
		}*/
		
		float t_inj;
		t_inj=attack(attacker,defender,usingskill,dsp);
		defend(defender, attacker,usingskill, t_inj,dsp);
	}
	public float attack(Fighters attacker,Fighters defender,Skill usingskill,StringBuffer dsp){//attackʹ��usingskill����
		float injure;
		injure=(attacker.bili*attacker.lidao)*usingskill.atk_xs;
		dsp.append(attacker.name+"ʹ����"+usingskill.name+'\n');
		if(usingskill.debuff!=Skill.nil) {
			defender.addtotbuff(usingskill.debuff, dsp,defender);
		}
		if(usingskill.buff!=Skill.nil){
			attacker.addtotbuff(usingskill.buff, dsp,attacker);
		}
		for (int i = 0; i < attacker.tbuffs.size(); i++) {
			injure=attacker.checkbuff(3, attacker.tbuffs.get(i).id,injure,dsp,attacker,null);
		}
		return injure;
	}
	public void defend(Fighters defender,Fighters attacker,Skill usingskill,float injure,StringBuffer dsp){//defender�������Է�ʹ�õ�skill��usingskill
		float realinj;
		realinj=injure-defender.gengu*defender.bili/30;
		for (int i = 0; i < defender.tbuffs.size(); i++) {
			injure=defender.checkbuff(4, defender.tbuffs.get(i).id,injure,dsp,attacker,defender);
		}
		if(usingskill.texiao!=Skill.nil){
			int k=random.nextInt(100);
			if(k<usingskill.tx_jilv)
			{
				//������Ч��
				switch(usingskill.texiao)
				{
				case Skill.xinei:int r=random.nextInt(100);
					if(r<usingskill.tx_jilv)//������Ч����
					{
						
					}
					break;
				case Skill.xixue:
					int r2=random.nextInt(100);
					if(r2<usingskill.tx_jilv)//��Ѫ��Ч����
					{
						float t = realinj*usingskill.tx_cs/100;
						dsp.append(attacker.name+"��������Ѫ��Ч����Ѫ"+t+'\n');
						attacker.hp+=t;
					}
					break;
				case Skill.pofang:
					int r3=random.nextInt(100);
					if(r3<usingskill.tx_jilv)//�Ʒ���Ч����
					{
						realinj=injure;
						dsp.append(attacker.name+"�������Ʒ���Ч�����ӷ���"+'\n');
					}
					break;
				case Skill.nil:
					break;
				}
			}
		}
		dsp.append(defender.name+"�ܵ�"+realinj+"���˺�"+'\n');
		defender.hp-=realinj;
		dsp.append(attacker.name+"����ʣ��"+attacker.hp+","+defender.name+"����ʣ��"+defender.hp+'\n');
	}
	public int judgeEnd(Fighters p2)//1,p1lose��2��p2lose��0��δ����
	{
		if(this.hp<0)
			return 1;
		else if(p2.hp<0)
			return 2;
		else return 0;
	}
	public void addtotbuff(int buffid,StringBuffer dsp,Fighters user){//���״̬
		int size=user.tbuffs.size();
		int i;
		for(i=0;i<size;i++)
		{
			if(user.tbuffs.get(i).id==buffid){
				user.tbuffs.get(i).remaintime=user.tbuffs.get(i).maxtime;
				checkbuff(1, buffid,0,dsp,null,null);
				dsp.append(",״̬ʣ��"+user.tbuffs.get(i).remaintime+"�غ�"+'\n');
				return;
			}
		}
		this.tbuffs.add(new State(AllStateandSkillsCollection.allStates.get(buffid-1)));
		checkbuff(1, buffid,0,dsp,null,null);
		dsp.append(",״̬ʣ��"+tbuffs.get(tbuffs.size()-1).remaintime+"�غ�"+'\n');
	}
	public void tbuff_refresh(StringBuffer dsp,Fighters user){//ˢ��״̬ʣ��ʱ��,�Ƴ���ʱ״̬
		//int size=user.tbuffs.size();
		int i;
		for(i=0;i<user.tbuffs.size();i++)
		{
			if(user.tbuffs.get(i).maxtime!=State.forever)//��������״̬
			{
				if(user.tbuffs.get(i).remaintime>0)
					user.tbuffs.get(i).remaintime--;
				else {
					checkbuff(2,user.tbuffs.get(i).id,0,dsp,null,null);
					user.tbuffs.remove(i);
					
				}
			}
		}
	}
	public float checkbuff(int when,int id,float inj,StringBuffer dsp,Fighters atk,Fighters def){//���������ʱ����1���ʱ��飬2.�Ƴ�ʱ��飬3.����ʱ��飬4����ʱ��飬5�غϿ�ʼʱ
		if(when==1)//add
		{
			switch (id) {
			case 1:dsp.append(this.name+"���������״̬");break;
			case 2:dsp.append(this.name+"����˾�������״̬");break;
			case 3:dsp.append(this.name+"�����̫������״̬");break;
			case 4:dsp.append(this.name+"����˼���״̬");tshenfa-=shenfa*0.5;break;
			case 5:dsp.append(this.name+"��������ǹ⻷״̬��ս������");
			this.tbili+=this.bili*0.15;
			this.tgengu+=this.gengu*0.15;
			this.tlidao+=this.lidao*0.15;
			this.tshenfa+=this.shenfa*0.15;
			this.twuxing+=this.wuxing*0.15;
				break;
			default:break;
			}
			return inj;
		}
		else if(when==2)//remove
		{
			if(id==1){
				dsp.append(this.name+"���������״̬"+'\n');
			}
			if(id==2){
				dsp.append(this.name+"����˾�������״̬"+'\n');
			}
			if(id==3){
				dsp.append(this.name+"�����̫������״̬"+'\n');
			}
			if(id==4){
				dsp.append(this.name+"�Ļ���״̬���"+'\n');tshenfa+=shenfa*0.5;
			}
			if(id==5) {dsp.append(this.name+"�����ǹ⻷״̬���"+'\n');
			this.tbili-=this.bili*0.15;
			this.tgengu-=this.gengu*0.15;
			this.tlidao-=this.lidao*0.15;
			this.tshenfa-=this.shenfa*0.15;
			this.twuxing-=this.wuxing*0.15;
				
			}
			
			
			return inj;
		}
		else if(when==3)//atk
		{
			if(id==3){
				inj*=1.15;dsp.append(atk.name+"������"+AllStateandSkillsCollection.allStates.get(id-1).name+"�����ã�����˺�����15%"+'\n');
			}
			return inj;
		}
		else if(when==4)//def
		{
			if(id==2){
				inj/=1.15;float t=(float) (inj*0.3+def.bili*def.lidao/300);atk.hp-=t;dsp.append(def.name+"������"+AllStateandSkillsCollection.allStates.get(id-1).name+"�����ã��ܵ��˺�����15%�����ԶԷ���ɷ���,"+atk.name+"�ܵ�"+t+"���˺�"+'\n');
			}
			return inj;
		}
		else if(when==5)//�غϿ�ʼ
		{
			
			if(id==1){
				float t=(float) (this.maxhp*0.02);this.hp-=t;dsp.append(this.name+"�ܵ�"+AllStateandSkillsCollection.allStates.get(id-1).name+"״̬��Ӱ�죬��ʧ"+t+"������"+'\n');
			}
			if(id==3){
				float r=(float) (1000+this.maxhp*0.02);this.hp+=r;dsp.append(this.name+"�ܵ�"+AllStateandSkillsCollection.allStates.get(id-1).name+"״̬Ӱ�죬�ָ�"+r+"������"+'\n');
			}
			
			return inj;
		}
		else{
			//����
			return inj;
		}
	}
    public byte[] attributeToByte()
    {
    	StringBuffer aBuffer=new StringBuffer("");
    	aBuffer.append(shenfa);
    	return aBuffer.toString().getBytes();
    }
    public Skill getSkill(int i)
    {
    		return havingSkills.get(i);
    }

}

class FightThread extends Thread{
	private Fighters p1;
	private Fighters p2;
	private Handler mHandler=null;
	public final static int sleeptime=100;
	private StringBuffer oneRoundDescriptionBuffer;
	private StringBuffer nativeBuffer;
	private int gametimer1;
	private int gametimer2;
	private int roundcounter;
	private int gamemaxspeed;
	private int result;
	private void delay(int ms){  
    	try {  
    		Thread.currentThread();  
    		Thread.sleep(ms);  
    	} catch (InterruptedException e) {  
    		e.printStackTrace();  
    	}   
    }
	public void run() {
		// TODO �Զ����ɵķ������
		Looper.prepare();
		Looper.loop();
	}
	public FightThread(Handler m) {
		// TODO �Զ����ɵĹ��캯�����
		p1=null;
		p2=null;
		mHandler=m;
		oneRoundDescriptionBuffer=new StringBuffer("");
		nativeBuffer=new StringBuffer("");
		gametimer1=0;
		gametimer2=0;
		roundcounter=0;
		gamemaxspeed=0;
		result=0;
	}
	public void setP1(Fighters p){
		p1=p;
	}
	public void setP1(String a){
		p1=new Fighters(a);
	}
	public int judgeTurns()
	{
		gamemaxspeed=Math.min(gamemaxspeed, p2.tshenfa);
		gamemaxspeed=Math.min(gamemaxspeed, p1.tshenfa);
		int r=0;
		while(true){
			gametimer1+=p1.tshenfa;
			if(gametimer1>=gamemaxspeed+100)
			{
				gametimer1-=(gamemaxspeed+100);
				r=1;
				break;
			}
			gametimer2+=p2.tshenfa;
			if(gametimer2>=gamemaxspeed+100)
			{
				gametimer2-=(gamemaxspeed+100);
				r=2;
				break;
			}
		}
		return r;
	}
	public final Handler fighterHandler= new Handler(Looper.myLooper()) {
		@Override
		public void handleMessage(android.os.Message msg) {
			 switch (msg.what){
	            case 1:
	            	
	            	//����
	            	//�Զ����ս��ģʽ����ģʽ���漰sp
	            	p2=new Fighters((String)msg.obj);
	            	//ARfightBaseBT��ֲ���ˣ�201512231423
	            	int counter=1;//�غϼ�����
	        		int timer1=0;//p1ʱ�������
	        		int timer2=0;//p2ʱ�������
	        		int maxspeed=0;//��¼��ʷ����ٶ�
	        		StringBuffer fightDescriptionBuffer=new StringBuffer("");//ս���������ͱ���
	        		nativeBuffer.setLength(0);//����ս����������
	        		result=0;
	        		p1.iniForFight(fightDescriptionBuffer);
	        		p2.iniForFight(fightDescriptionBuffer);
	        		fightDescriptionBuffer.append(p1.name+"��ʼ����:����"+p1.hp+",����"+p1.tlidao+",����"+p1.tbili+'\n'+"����"+p1.twuxing+",��"+p1.tshenfa+",����"+p1.tgengu+'\n');
	        		fightDescriptionBuffer.append(p2.name+"��ʼ����:����"+p2.hp+",����"+p2.tlidao+",����"+p2.tbili+'\n'+"����"+p2.twuxing+",��"+p2.tshenfa+",����"+p2.tgengu+'\n');
	        		while(result==0)//ս��δ����
	        		{
	        			maxspeed=Math.min(maxspeed, p2.tshenfa);
	        			maxspeed=Math.min(maxspeed, p1.tshenfa);
	        			timer1+=p1.tshenfa;
	        			timer2+=p2.tshenfa;
	        			if(timer1>=maxspeed) {
	        				timer1-=maxspeed;
	        				fightDescriptionBuffer.append(p1.oneRound(p1, p2,counter));
	        				p1.tbuff_refresh(fightDescriptionBuffer,p1);//ˢ��״̬
	        				p2.tbuff_refresh(fightDescriptionBuffer,p2);
	        				counter++;
	        				delay(1000);
	        				mHandler.obtainMessage(2,0,0,fightDescriptionBuffer.toString().getBytes()).sendToTarget();
	        				delay(1000);
	        				nativeBuffer.append(fightDescriptionBuffer);
	        				fightDescriptionBuffer.setLength(0);//����ַ���
	        				
	        			}
	        			result=p1.judgeEnd(p2);
	        			if(result!=0) break;
	        			if(timer2>=maxspeed){
	        				timer2-=maxspeed;
	        				fightDescriptionBuffer.append(p1.oneRound(p2, p1,counter));
	        				p2.tbuff_refresh(fightDescriptionBuffer,p2);
	        				p1.tbuff_refresh(fightDescriptionBuffer,p1);
	        				counter++;
	        				delay(1000);
	        				mHandler.obtainMessage(2,0,0,fightDescriptionBuffer.toString().getBytes()).sendToTarget();
	        				delay(1000);
	        				nativeBuffer.append(fightDescriptionBuffer);
	        				fightDescriptionBuffer.setLength(0);//����ַ���
	        				delay(2000);
	        			}
	        			result=p1.judgeEnd(p2);
	        		}
	        		if(result==1)//1lose
	        		{
	        			fightDescriptionBuffer.append(p2.name+"��ʤ");
	        		}
	        		else if(result==2)//2lose
	        		{
	        			fightDescriptionBuffer.append(p1.name+"��ʤ");
	        		}
	        		else{
	        			//����
	        		}
	        		nativeBuffer.append(fightDescriptionBuffer);
	        		delay(1000);
	        		mHandler.obtainMessage(2,1,0,fightDescriptionBuffer.toString().getBytes()).sendToTarget();//arg1=1��־��ս������
	        		delay(1000);
	        		fightDescriptionBuffer.setLength(0);//����ַ���
	            	//ARfightBaseBT(pFighter);
	        		delay(1000);
	            	mHandler.obtainMessage(10,0,0,nativeBuffer).sendToTarget();
	            	delay(1000);
	                break;
	            case 2://�ɿ�ս��ģʽ����������������
	            	//arg1=1���򴫹������Ǳ������arg1=2�򴫹������ǿͻ������arg1=3���򴫹������ǿͻ����������
	            	if(msg.arg1==1){//arg2������
	            		delay(1000);
	            		p1.oneRound(p1, p2, roundcounter,p1.getSkill(msg.arg2),oneRoundDescriptionBuffer);
	            		roundcounter++;
	            		p1.tbuff_refresh(oneRoundDescriptionBuffer,p1);//ˢ��״̬
        				p2.tbuff_refresh(oneRoundDescriptionBuffer,p2);
        				
	            		mHandler.obtainMessage(2,0,0,oneRoundDescriptionBuffer.toString().getBytes()).sendToTarget();
	            		mHandler.obtainMessage(15,0,0,oneRoundDescriptionBuffer.toString().getBytes()).sendToTarget();
	            		
	            		oneRoundDescriptionBuffer.setLength(0);
	            		result=p1.judgeEnd(p2);
	            		if(result==1)//1lose
		        		{
		        			oneRoundDescriptionBuffer.append(p2.name+"��ʤ"+'\n');
		        			
		        			mHandler.obtainMessage(2,1,0,oneRoundDescriptionBuffer.toString().getBytes()).sendToTarget();//arg1=1��־��ս������
		        			mHandler.obtainMessage(15,1,0,oneRoundDescriptionBuffer.toString().getBytes()).sendToTarget();//arg1=1��־��ս������
			        		oneRoundDescriptionBuffer.setLength(0);//����ַ���
			            	
		        		}
		        		else if(result==2)//2lose
		        		{
		        			oneRoundDescriptionBuffer.append(p1.name+"��ʤ"+'\n');
		        			
		        			mHandler.obtainMessage(2,1,0,oneRoundDescriptionBuffer.toString().getBytes()).sendToTarget();//arg1=1��־��ս������
		        			mHandler.obtainMessage(15,1,0,oneRoundDescriptionBuffer.toString().getBytes()).sendToTarget();//arg1=1��־��ս������
			        		oneRoundDescriptionBuffer.setLength(0);//����ַ���
			            	
		        		}
		        		else{
		        			//δ����
		        			delay(2000);
		        			if(judgeTurns()==1)//p1,֪ͨ����������
		            		{
		            			mHandler.obtainMessage(13).sendToTarget();
		            		}
		            		else {//p2,֪ͨ�ͻ��˳���
								mHandler.obtainMessage(14).sendToTarget();
							}
		        		}
	            	}
	            	else if(msg.arg1==2){//arg2������
	            		delay(1000);
	            		p1.oneRound(p2, p1, roundcounter,p2.getSkill(msg.arg2),oneRoundDescriptionBuffer);
	            		roundcounter++;
	            		p1.tbuff_refresh(oneRoundDescriptionBuffer,p1);//ˢ��״̬
        				p2.tbuff_refresh(oneRoundDescriptionBuffer,p2);
        				
	            		mHandler.obtainMessage(2,0,0,oneRoundDescriptionBuffer.toString().getBytes()).sendToTarget();
	            		mHandler.obtainMessage(15,0,0,oneRoundDescriptionBuffer.toString().getBytes()).sendToTarget();
	            		
	            		oneRoundDescriptionBuffer.setLength(0);
	            		result=p1.judgeEnd(p2);
	            		if(result==1)//1lose
		        		{
		        			oneRoundDescriptionBuffer.append(p2.name+"��ʤ"+'\n');
		        			
		        			mHandler.obtainMessage(2,1,0,oneRoundDescriptionBuffer.toString().getBytes()).sendToTarget();//arg1=1��־��ս������
		        			mHandler.obtainMessage(15,1,0,oneRoundDescriptionBuffer.toString().getBytes()).sendToTarget();//arg1=1��־��ս������
		        			
			        		oneRoundDescriptionBuffer.setLength(0);//����ַ���
			            	
		        		}
		        		else if(result==2)//2lose
		        		{
		        			oneRoundDescriptionBuffer.append(p1.name+"��ʤ"+'\n');
		        			
		        			mHandler.obtainMessage(2,1,0,oneRoundDescriptionBuffer.toString().getBytes()).sendToTarget();//arg1=1��־��ս������
		        			mHandler.obtainMessage(15,1,0,oneRoundDescriptionBuffer.toString().getBytes()).sendToTarget();//arg1=1��־��ս������
			        		oneRoundDescriptionBuffer.setLength(0);//����ַ���
			            	
		        		}
		        		else{
		        			//δ����
		        			delay(2000);
		        			if(judgeTurns()==1)//p1,֪ͨ����������
		            		{
		            			mHandler.obtainMessage(13).sendToTarget();
		            		}
		            		else {//p2,֪ͨ�ͻ��˳���
								mHandler.obtainMessage(14).sendToTarget();
							}
		        		}
	            	}
	            	else if(msg.arg1==3){
	            		p2=new Fighters((String)msg.obj);
	            		gametimer1=0;
	            		gametimer2=0;
	            		roundcounter=1;
	            		gamemaxspeed=0;
	            		result=0;
	            		oneRoundDescriptionBuffer.setLength(0);
	            		p1.iniForFight(oneRoundDescriptionBuffer);
	            		p2.iniForFight(oneRoundDescriptionBuffer);
	            		if(judgeTurns()==1)//p1,֪ͨ����������
	            		{
	            			mHandler.obtainMessage(13).sendToTarget();
	            		}
	            		else {//p2,֪ͨ�ͻ��˳���
							mHandler.obtainMessage(14).sendToTarget();
						}
	            	}
	            	break;
	            case 3://ս��������־
	            	break;
	            case 4://
	            	break;
	            default:
	            		break;
	            }
		}
    };
}
