package com.rmwang.namefight3;

import java.util.ArrayList;

public class AllStateandSkillsCollection {
	public static final ArrayList<Skill> allSkills=new ArrayList<Skill>();
	private  static boolean firstini=true;
	public static final ArrayList<State> allStates=new ArrayList<State>();
	private void iniCommonCS()
	{
		//id,类型，伤害系数，特效几率，特效参数，特效类型，debuff，buff，学习等级要求，名称，等级，描述
		allSkills.add(new Skill());
		allSkills.add(new Skill(2, Skill.waigong, 2, 30, Skill.nil, Skill.pofang, 1, Skill.nil, 1, "独孤九剑", 1, "",100));
		allSkills.add(new Skill(3, Skill.neigong, 3, 50, 50, Skill.xinei, 4, Skill.nil, 1, "北冥神功", 1, "",100));
		allSkills.add(new Skill(4, Skill.neigong, 3, 30, Skill.nil, Skill.nil, Skill.nil, 2, 1, "九阳神功", 1, "",100));
		allSkills.add(new Skill(5, Skill.waigong, 4, 20, 50, Skill.xixue, Skill.nil, Skill.nil, 1, "血刀大法", 1, "",100));
		allSkills.add(new Skill(6, Skill.neigong, 5, 30, Skill.nil, Skill.nil, Skill.nil, 3, 1, "太玄神功", 1, "",100));
		//id，类型，持续时间，名称，描述
		allStates.add(new State(1,State.debuf,2,"内伤","持续掉血，每回合%2"));
		allStates.add(new State(2,State.buff,3,"九阳真气","提高防御15%，每回合受到攻击对对方造成反震伤害"));
		allStates.add(new State(3,State.buff,3,"太玄真气","提高伤害15%，每回合持续回血回内5%"));
		allStates.add(new State(4,State.debuf,1,"缓速","身法减半"));
		allStates.add(new State(5,State.buff,State.forever,"主角光环","提高全属性15%"));
	}
	AllStateandSkillsCollection(){
		if(firstini==true){
			firstini=false;
			iniCommonCS();
		}
	}
}
