package com.rmwang.namefight3;

import java.util.ArrayList;

public class AllStateandSkillsCollection {
	public static final ArrayList<Skill> allSkills=new ArrayList<Skill>();
	private  static boolean firstini=true;
	public static final ArrayList<State> allStates=new ArrayList<State>();
	private void iniCommonCS()
	{
		//id,���ͣ��˺�ϵ������Ч���ʣ���Ч��������Ч���ͣ�debuff��buff��ѧϰ�ȼ�Ҫ�����ƣ��ȼ�������
		allSkills.add(new Skill());
		allSkills.add(new Skill(2, Skill.waigong, 2, 30, Skill.nil, Skill.pofang, 1, Skill.nil, 1, "���¾Ž�", 1, "",100));
		allSkills.add(new Skill(3, Skill.neigong, 3, 50, 50, Skill.xinei, 4, Skill.nil, 1, "��ڤ��", 1, "",100));
		allSkills.add(new Skill(4, Skill.neigong, 3, 30, Skill.nil, Skill.nil, Skill.nil, 2, 1, "������", 1, "",100));
		allSkills.add(new Skill(5, Skill.waigong, 4, 20, 50, Skill.xixue, Skill.nil, Skill.nil, 1, "Ѫ����", 1, "",100));
		allSkills.add(new Skill(6, Skill.neigong, 5, 30, Skill.nil, Skill.nil, Skill.nil, 3, 1, "̫����", 1, "",100));
		//id�����ͣ�����ʱ�䣬���ƣ�����
		allStates.add(new State(1,State.debuf,2,"����","������Ѫ��ÿ�غ�%2"));
		allStates.add(new State(2,State.buff,3,"��������","��߷���15%��ÿ�غ��ܵ������ԶԷ���ɷ����˺�"));
		allStates.add(new State(3,State.buff,3,"̫������","����˺�15%��ÿ�غϳ�����Ѫ����5%"));
		allStates.add(new State(4,State.debuf,1,"����","������"));
		allStates.add(new State(5,State.buff,State.forever,"���ǹ⻷","���ȫ����15%"));
	}
	AllStateandSkillsCollection(){
		if(firstini==true){
			firstini=false;
			iniCommonCS();
		}
	}
}
