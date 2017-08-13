package com.chen.battle.ai;

import com.chen.battle.structs.EGOActionState;
import com.chen.battle.structs.SSGameUnit;

public class SSAI
{
	public SSGameUnit theOwner;
	public boolean bIsMoving;
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
		if (theOwner.curActionInfo.eOAS == EGOActionState.PassiveState)
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
}
