package com.rmwang.namefight3;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Random;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import android.widget.Toast;


//git@github.com:jimth001/namefight-game-android-.git
public class Fighters implements Runnable{
	private float bili;//臂力
	private float wuxing;//悟性
	private float lidao;//力道
	private float gengu;//根骨
	private int shenfa;//身法
	private float maxhp;
	private float maxsp;
	private ArrayList<Skill> havingSkills=null;
	private Handler mHandler=null;
	private float tbili;//战斗临时属性臂力
	private float twuxing;//悟性
	private float tlidao;//力道
	private float tgengu;//根骨
	private int tshenfa;//身法
	private float hp;
	private float sp;
	private ArrayList<State> tbuffs=null;
	private int exp;
	private int lv;//
	private int sxd;//属性点
	private String name;
	public final Random random=new Random();
	private static final AllStateandSkillsCollection allStateandSkillsCollection=new AllStateandSkillsCollection();
	
	Fighters(String a,Handler handler){
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
		mHandler=handler;
		len=allStateandSkillsCollection.allSkills.size();
		for(i=0;i<len;i++)//初始版，全部技能加入进行测试
		{
			havingSkills.add(allStateandSkillsCollection.allSkills.get(i));
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
	public void iniForFight(StringBuffer dsp)//战斗初始化函数
	{
		this.tbili=this.bili;
		this.twuxing=this.wuxing;
		this.tlidao=this.lidao;
		this.tgengu=this.gengu;
		this.tshenfa=this.shenfa;
		this.hp=this.maxhp;
		this.sp=this.maxsp;
		this.tbuffs.clear();//清空状态集
		if(this.name.equals("主角"))
		{
			dsp.append(this.name+"获得主角光环状态，战力大增"+'\n');
			this.tbili+=this.bili*0.15;
			this.tgengu+=this.gengu*0.15;
			this.tlidao+=this.lidao*0.15;
			this.tshenfa+=this.shenfa*0.15;
			this.twuxing+=this.wuxing*0.15;
		}
	}
	public void save(){}
	public StringBuffer ARfightBaseBT(Fighters p2){//autorandomfightbasebluetooth
		int counter=1;//回合计数器
		int timer1=0;//p1时间计数器
		int timer2=0;//p2时间计数器
		int maxspeed=0;//记录历史最高速度
		StringBuffer fightDescriptionBuffer=new StringBuffer("");//战斗描述传送变量
		StringBuffer nativeBuffer=new StringBuffer("");//本地战斗描述变量
		int result=0;
		this.iniForFight(fightDescriptionBuffer);
		p2.iniForFight(fightDescriptionBuffer);
		fightDescriptionBuffer.append(this.name+"初始属性:生命"+this.hp+",力道"+this.tlidao+",臂力"+this.tbili+",悟性"+this.twuxing+",身法"+this.tshenfa+",根骨"+this.tgengu+'\n');
		fightDescriptionBuffer.append(p2.name+"初始属性:生命"+p2.hp+",力道"+p2.tlidao+",臂力"+p2.tbili+",悟性"+p2.twuxing+",身法"+p2.tshenfa+",根骨"+p2.tgengu+'\n');
		while(result==0)//战斗未结束
		{
			maxspeed=Math.min(maxspeed, p2.tshenfa);
			maxspeed=Math.min(maxspeed, this.tshenfa);
			timer1+=this.tshenfa;
			timer2+=p2.tshenfa;
			if(timer1>=maxspeed) {
				timer1-=maxspeed;
				fightDescriptionBuffer.append(oneRound(this, p2,counter));
				this.tbuff_refresh(fightDescriptionBuffer,this);//刷新状态
				p2.tbuff_refresh(fightDescriptionBuffer,p2);
				counter++;
				mHandler.obtainMessage(2,0,0,fightDescriptionBuffer.toString().getBytes());
				nativeBuffer.append(fightDescriptionBuffer);
				fightDescriptionBuffer.setLength(0);//清空字符串
			}
			result=judgeEnd(p2);
			if(result!=0) break;
			if(timer2>=maxspeed){
				timer2-=maxspeed;
				fightDescriptionBuffer.append(oneRound(p2, this,counter));
				p2.tbuff_refresh(fightDescriptionBuffer,p2);
				this.tbuff_refresh(fightDescriptionBuffer,this);
				counter++;
				mHandler.obtainMessage(2,0,0,fightDescriptionBuffer.toString().getBytes());
				nativeBuffer.append(fightDescriptionBuffer);
				fightDescriptionBuffer.setLength(0);//清空字符串
			}
			result=judgeEnd(p2);
		}
		if(result==1)//1lose
		{
			fightDescriptionBuffer.append(p2.name+"获胜");
		}
		else if(result==2)//2lose
		{
			fightDescriptionBuffer.append(this.name+"获胜");
		}
		else{
			//报错
		}
		mHandler.obtainMessage(2,1,0,fightDescriptionBuffer.toString().getBytes());//arg1=1标志着战斗结束
		nativeBuffer.append(fightDescriptionBuffer);
		fightDescriptionBuffer.setLength(0);//清空字符串
		return nativeBuffer;
	}
	public StringBuffer autoRandomFight(Fighters p2){
		int counter=1;//回合计数器
		int timer1=0;//p1时间计数器
		int timer2=0;//p2时间计数器
		int maxspeed=0;//记录历史最高速度
		StringBuffer fightDescriptionBuffer=new StringBuffer("");//单场战斗描述
		int result=0;
		this.iniForFight(fightDescriptionBuffer);
		p2.iniForFight(fightDescriptionBuffer);
		fightDescriptionBuffer.append(this.name+"初始属性:生命"+this.hp+",力道"+this.tlidao+",臂力"+this.tbili+",悟性"+this.twuxing+",身法"+this.tshenfa+",根骨"+this.tgengu+'\n');
		fightDescriptionBuffer.append(p2.name+"初始属性:生命"+p2.hp+",力道"+p2.tlidao+",臂力"+p2.tbili+",悟性"+p2.twuxing+",身法"+p2.tshenfa+",根骨"+p2.tgengu+'\n');
		while(result==0)//战斗未结束
		{
			maxspeed=Math.min(maxspeed, p2.tshenfa);
			maxspeed=Math.min(maxspeed, this.tshenfa);
			timer1+=this.tshenfa;
			timer2+=p2.tshenfa;
			if(timer1>=maxspeed) {
				timer1-=maxspeed;
				fightDescriptionBuffer.append(oneRound(this, p2,counter));
				this.tbuff_refresh(fightDescriptionBuffer,this);//刷新状态
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
			fightDescriptionBuffer.append(p2.name+"获胜");
		}
		else if(result==2)//2lose
		{
			fightDescriptionBuffer.append(this.name+"获胜");
		}
		else{
			//报错
		}
		return fightDescriptionBuffer;
	}
	public StringBuffer oneRound(Fighters attacker,Fighters defender,int counter){
		StringBuffer oneRoundDescriptionbBuffer=new StringBuffer("");
		oneRoundDescriptionbBuffer.append("第"+counter+"回合："+'\n');
		for (int i = 0; i < attacker.tbuffs.size(); i++) {//回合开始，先check states
			attacker.checkbuff(5, attacker.tbuffs.get(i).id,0,oneRoundDescriptionbBuffer,attacker,defender);
		}
		/*for (int i = 0; i < attacker.tbuffs.size(); i++) {//回合开始，先check states
			attacker.checkbuff(5, attacker.tbuffs.get(i).id,0,oneRoundDescriptionbBuffer,attacker,defender);
		}*/
		int r=random.nextInt(6);//计算出招0-5
		float t_inj;
		t_inj=attack(attacker,defender,attacker.havingSkills.get(r),oneRoundDescriptionbBuffer);
		defend(defender, attacker,attacker.havingSkills.get(r), t_inj,oneRoundDescriptionbBuffer);
		return oneRoundDescriptionbBuffer;
	}
	public float attack(Fighters attacker,Fighters defender,Skill usingskill,StringBuffer dsp){//attack使用usingskill攻击
		float injure;
		injure=(attacker.bili*attacker.lidao)*usingskill.atk_xs;
		dsp.append(attacker.name+"使出了"+usingskill.name+'\n');
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
	public void defend(Fighters defender,Fighters attacker,Skill usingskill,float injure,StringBuffer dsp){//defender防御，对方使用的skill是usingskill
		float realinj;
		realinj=injure-defender.gengu*defender.bili/30;
		for (int i = 0; i < defender.tbuffs.size(); i++) {
			injure=defender.checkbuff(4, defender.tbuffs.get(i).id,injure,dsp,attacker,defender);
		}
		if(usingskill.texiao!=Skill.nil){
			int k=random.nextInt(100);
			if(k<usingskill.tx_jilv)
			{
				//发动特效：
				switch(usingskill.texiao)
				{
				case Skill.xinei:int r=random.nextInt(100);
					if(r<usingskill.tx_jilv)//吸内特效发动
					{
						
					}
					break;
				case Skill.xixue:
					int r2=random.nextInt(100);
					if(r2<usingskill.tx_jilv)//吸血特效发动
					{
						float t = realinj*usingskill.tx_cs/100;
						dsp.append(attacker.name+"发动了吸血特效，吸血"+t+'\n');
						attacker.hp+=t;
					}
					break;
				case Skill.pofang:
					int r3=random.nextInt(100);
					if(r3<usingskill.tx_jilv)//破防特效发动
					{
						realinj=injure;
						dsp.append(attacker.name+"发动了破防特效，无视防御"+'\n');
					}
					break;
				case Skill.nil:
					break;
				}
			}
		}
		dsp.append(defender.name+"受到"+realinj+"点伤害"+'\n');
		defender.hp-=realinj;
		dsp.append(attacker.name+"生命剩余"+attacker.hp+","+defender.name+"生命剩余"+defender.hp+'\n');
	}
	public int judgeEnd(Fighters p2)//1,p1lose，2，p2lose，0尚未结束
	{
		if(this.hp<0)
			return 1;
		else if(p2.hp<0)
			return 2;
		else return 0;
	}
	public void addtotbuff(int buffid,StringBuffer dsp,Fighters user){//添加状态
		int size=user.tbuffs.size();
		int i;
		for(i=0;i<size;i++)
		{
			if(user.tbuffs.get(i).id==buffid){
				user.tbuffs.get(i).remaintime=user.tbuffs.get(i).maxtime;
				return;
			}
		}
		this.tbuffs.add(AllStateandSkillsCollection.allStates.get(buffid-1));
		checkbuff(1, buffid,0,dsp,null,null);
	}
	public void tbuff_refresh(StringBuffer dsp,Fighters user){//刷新状态剩余时间,移除到时状态
		//int size=user.tbuffs.size();
		int i;
		for(i=0;i<user.tbuffs.size();i++)
		{
			if(user.tbuffs.get(i).maxtime!=State.forever)//不是永久状态
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
	public float checkbuff(int when,int id,float inj,StringBuffer dsp,Fighters atk,Fighters def){//参数：检查时机，1添加时检查，2.移除时检查，3.攻击时检查，4防御时检查，5回合开始时
		if(when==1)//add
		{
			switch (id) {
			case 1:dsp.append(this.name+"获得了内伤状态"+'\n');break;
			case 2:dsp.append(this.name+"获得了九阳真气状态"+'\n');break;
			case 3:dsp.append(this.name+"获得了太玄真气状态"+'\n');break;
			case 4:dsp.append(this.name+"获得了减速状态"+'\n');tshenfa-=shenfa*0.5;break;
			case 5:dsp.append(this.name+"获得了主角光环状态，战力大增"+'\n');
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
				dsp.append(this.name+"解除了内伤状态"+'\n');
			}
			if(id==2){
				dsp.append(this.name+"解除了九阳真气状态"+'\n');
			}
			if(id==3){
				dsp.append(this.name+"解除了太玄真气状态"+'\n');
			}
			if(id==4){
				dsp.append(this.name+"的缓速状态解除"+'\n');tshenfa+=shenfa*0.5;
			}
			if(id==5) {dsp.append(this.name+"的主角光环状态解除"+'\n');
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
				inj*=1.15;dsp.append(atk.name+"发动了"+AllStateandSkillsCollection.allStates.get(id-1).name+"的作用，造成伤害增加15%"+'\n');
			}
			return inj;
		}
		else if(when==4)//def
		{
			if(id==2){
				inj/=1.15;float t=(float) (inj*0.3+def.bili*def.lidao/300);atk.hp-=t;dsp.append(def.name+"发动了"+AllStateandSkillsCollection.allStates.get(id-1).name+"的作用，受到伤害减少15%，并对对方造成反击,"+atk.name+"受到"+t+"点伤害"+'\n');
			}
			return inj;
		}
		else if(when==5)//回合开始
		{
			
			if(id==1){
				float t=(float) (this.maxhp*0.02);this.hp-=t;dsp.append(this.name+"受到"+AllStateandSkillsCollection.allStates.get(id-1).name+"状态的影响，损失"+t+"点生命"+'\n');
			}
			if(id==3){
				float r=(float) (1000+this.maxhp*0.02);this.hp+=r;dsp.append(this.name+"受到"+AllStateandSkillsCollection.allStates.get(id-1).name+"状态影响，恢复"+r+"点生命"+'\n');
			}
			
			return inj;
		}
		else{
			//报错：
			return inj;
		}
	}
	@Override
	public void run() {
		// TODO 自动生成的方法存根
		Looper.prepare();
		Looper.loop();
	}
	public final Handler fighterHandler= new Handler(Looper.myLooper()) {
		@Override
		public void handleMessage(android.os.Message msg) {
			 switch (msg.what){
	            case 1://姓名
	            	Fighters pFighter=new Fighters((String)msg.obj, null);
	            	StringBuffer re=ARfightBaseBT(pFighter);
	            	mHandler.obtainMessage(10,0,0,re).sendToTarget();
	                break;
	            case 2://战斗描述
	            	break;
	            case 3://战斗结束标志
	            	break;
	            case 4://
	            	break;
	            default:
	            		break;
	            }
		}
    };
    public byte[] attributeToByte()
    {
    	StringBuffer aBuffer=new StringBuffer("");
    	aBuffer.append(shenfa);
    	return aBuffer.toString().getBytes();
    }

}
