package com.chen.battle.skill;

import com.chen.battle.skill.structs.ESkillEffectType;
import com.chen.battle.skill.structs.SkillEffectBaseConfig;
import com.chen.battle.structs.BattleContext;
import com.chen.battle.structs.CVector2D;
import com.chen.battle.structs.SSGameUnit;

public abstract class SSSkillEffect 
{
	public ESkillEffectType skillEffectType;
	public SSSkill skill;
	public SkillEffectBaseConfig config;
	public int effectDelayTime;
	public SSGameUnit theOwner;
	public SSGameUnit startSkillGO;//效果从哪个点出发，比如A使用技能，打中B，从B身上出现一个效果，对C生效，那么这里的出发点就是B
	public long beginTime;
	public SSGameUnit target;
	public CVector2D dir;
	public CVector2D pos;
	public SSSkillEffect[] dependeEffect = new SSSkillEffect[16];
	public boolean IsForceStop;//效果是否被终止
	public int id;//唯一id
	public BattleContext battle;
	public boolean isExpired;
	
	/**
	 * 引导技能是否可以被中断
	 * @return
	 */
	public boolean IsCanStopUsing()
	{
		return true;
	}
	/**
	 * 是否有效
	 * @return
	 */
	public boolean IsInvalid()
	{
		return IsForceStop == true || skill == null || skill.skillConfig == null;
	}
	/**
	 * 是否需要等待完成
	 * @param now
	 * @return
	 */
	public boolean IsNeedWait(long now)
	{
		return now - beginTime < this.effectDelayTime;
	}
	/**
	 * 强制停止
	 */
	public void ForceStop()
	{
		this.End();
		IsForceStop = true;
		id = 0;
	}
	public void AddSelfToUsingList()
	{
		if (skill == null)
		{
			return;
		}
		if (theOwner == null)
		{
			return;
		}
		for (int i=0;i<32;i++)
		{
			if (skill.usingEffectArray[i] == 0)
			{
				skill.usingEffectArray[i] = id;
				return;
			}
		}
	}
	/**
	 * 停止依赖的特效
	 */
	public void StopDependedEffect()
	{
		for (int i=0;i<16;i++)
		{
			SSSkillEffect effect = this.dependeEffect[i];
			if (effect != null && effect.id != 0)
			{
				effect.ForceStop();
				this.dependeEffect[i] = null;
			}
		}
	}
	public void Clear()
	{
		skill = null;
		config = null;
		effectDelayTime = 0;
		theOwner = null;
		beginTime = 0;
		target = null;
		IsForceStop = false;
		id = 0;
		battle = null;
	}
	/**
	 * 更新技能
	 */
	public void CheckCooldown()
	{
		if (config == null || skill == null)
		{
			return;
		}
		if (skill.bIfCanCooldown && config.bIsCooldown)
		{
			skill.DoCooldown();
		}
	}
	public abstract boolean Begin();
	public abstract boolean Update(long now,long tick);
	public abstract void End();
}
