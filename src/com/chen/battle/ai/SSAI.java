package com.chen.battle.ai;

import com.chen.battle.ai.structs.EAttackState;
import com.chen.battle.skill.SSSkill;
import com.chen.battle.structs.CVector2D;
import com.chen.battle.structs.EGOActionState;
import com.chen.battle.structs.SSGameUnit;

public class SSAI
{
	public SSGameUnit theOwner;
	public boolean bIsMoving;
	public long lastTryMoveTime;//上次尝试移动失败的时间
	public CVector2D moveTarPos;//当前前进的目标地址
	public long lastCheckMoveTarTime;//上一次检查目标坐标的时间
	public SSSkill attackSkill;
	public EAttackState eAttackState;
	public SSAI(SSGameUnit _theOwner)
	{
		this.theOwner = _theOwner;
	}
	public boolean IfPassitiveState()
	{
		if (theOwner == null)
		{
			return true;
		}
		if (theOwner.curActionInfo.eOAS.value > EGOActionState.PassiveState.value)
		{
			return true;
		}
		//如果是昏迷状态
//		if( m_pcMasterGU->GetFPData(eEffectCate_Dizziness) > 0){
//			return TRUE;
//		}
		return false;
	}
	public void OnMoveBlock()
	{
		bIsMoving = false;
	}
	public void CancleAttack()
	{
		this.moveTarPos.zero();
		if (eAttackState == EAttackState.Pursue)
		{
			//停止移动到目的地
		}
		this.attackSkill.TryCancle();
	}
	public void OnTeleport()
	{
		
	}
}
