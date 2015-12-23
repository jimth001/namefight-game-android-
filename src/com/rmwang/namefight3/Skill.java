package com.rmwang.namefight3;

public class Skill {
	public static final int nil=0;
	public static final int waigong=1;//外功攻击
	public static final int neigong=2;//内功攻击
	public static final int huifu=3;//恢复技能
	public static final int xixue=4;//吸血特效
	public static final int xinei=5;//吸内特效
	public static final int pofang=6;//破防特效
	public int id;
	public int type;
	public int atk_xs;//伤害系数
	public int tx_jilv;//特效发动几率 0-100
	public float tx_cs;//特效参数
	public int texiao;
	public int debuff;
	public int buff;
	public int learnyaoqiu;//学习等级要求
	public String name;
	public int lv;
	public int maxlv;
	public int exp;
	public int expneeded[]={10,20,40,60,100,300,500,800,1000};//升级所需经验表
	public String description;
	public int spNeeded;
	Skill(){
		id=1;
		type=1;
		atk_xs=1;
		tx_jilv=0;
		tx_cs=0;
		texiao=nil;
		debuff=nil;
		buff=nil;
		learnyaoqiu=nil;
		name="普通攻击";
		lv=1;
		maxlv=10;
		description="最普通的攻击";
		exp=0;
		spNeeded=0;
	}
	Skill(int t_id,int t_type,int t_atk_xs,int t_txjilv,float t_tx_cs,int t_texiao,int t_debuff,int t_buff,int t_learnyaoqiu,String t_name,int t_lv,String t_dscp,int t_sp){
		id=t_id;
		type=t_type;
		atk_xs=t_atk_xs;
		tx_jilv=t_txjilv;
		tx_cs=t_tx_cs;
		texiao=t_texiao;
		debuff=t_debuff;
		buff=t_buff;
		learnyaoqiu=t_learnyaoqiu;
		name=t_name;
		lv=t_lv;
		description=t_dscp;
		maxlv=10;
		exp=0;
		spNeeded=t_sp;
	}
	Skill(Skill t){
		id=t.id;
		type=t.type;
		atk_xs=t.atk_xs;
		tx_jilv=t.tx_jilv;
		tx_cs=t.tx_cs;
		texiao=t.texiao;
		debuff=t.debuff;
		buff=t.buff;
		learnyaoqiu=t.learnyaoqiu;
		name=t.name;
		lv=t.lv;
		description=t.description;
		maxlv=t.maxlv;
		exp=t.exp;
		spNeeded=t.spNeeded;
	}
	public void lvup(){
		exp-=expneeded[lv-1];
		lv++;
	}
	public void expadd(int s){
		exp+=s;
		if(exp>expneeded[lv-1])
		{
			lvup();
		}
	}
}
